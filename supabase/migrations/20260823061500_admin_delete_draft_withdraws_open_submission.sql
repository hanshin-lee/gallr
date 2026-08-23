-- Permanent draft deletion previously refused any identity that had ever
-- emitted an outbox event. Delivered events are never purged, so a single
-- owner submission — or one archive/restore round trip — stranded an
-- accidental draft forever behind a guard that reads as a transient
-- concurrency check. Only work the outbox worker will still attempt blocks
-- deletion now.
--
-- Deletion also has to answer for the owner-workspace submission that points
-- at the draft. `exhibition_submissions.owner_exhibition_id` is ON DELETE
-- RESTRICT and was never checked, so the delete failed with a raw foreign key
-- violation. An open review round is withdrawn with the draft; a round that
-- already reached a decision keeps that decision and only loses the pointer to
-- the deleted draft. `content.submission_status` already carries 'withdrawn';
-- this is its first writer.
--
-- `launch_kits` and `local_promotions` are the remaining ON DELETE RESTRICT
-- references and get explicit named guards rather than raw 23503s.

-- An owner_workspace round must reference a live owner draft while the round is
-- open. Once the draft is permanently deleted the reference cannot exist, so a
-- round that has reached a terminal decision may hold a null pointer.
alter table content.exhibition_submissions
  drop constraint if exists exhibition_submissions_owner_source_pair;
alter table content.exhibition_submissions
  add constraint exhibition_submissions_owner_source_pair
  check (
    (
      source = 'owner_workspace'
      and (
        owner_exhibition_id is not null
        or status in (
          'rejected'::content.submission_status,
          'withdrawn'::content.submission_status
        )
      )
    )
    or (
      source in ('public_form', 'editor_workspace')
      and owner_exhibition_id is null
    )
  );

comment on constraint exhibition_submissions_owner_source_pair
  on content.exhibition_submissions is
  'Open owner_workspace rounds reference a live owner draft; rejected and withdrawn rounds may outlive a permanently deleted draft.';

create or replace function content_private.admin_delete_exhibition_draft_impl(
  p_exhibition_id text,
  p_expected_version_id uuid,
  p_expected_revision integer
)
returns jsonb
language plpgsql
volatile
security definer
set search_path = ''
as $$
declare
  v_user_id uuid;
  v_exhibition content.exhibitions%rowtype;
  v_version content.exhibition_versions%rowtype;
  v_withdrawn_submission_ids uuid[];
  v_detached_submission_ids uuid[];
  v_response jsonb;
begin
  v_user_id := content_private.admin_assert_staff(
    'admin'::content.staff_role
  );

  select exhibition.*
  into v_exhibition
  from content.exhibitions as exhibition
  where exhibition.id = p_exhibition_id
  for update;

  if not found then
    raise exception using errcode = 'P0002', message = 'exhibition_not_found';
  end if;

  -- Any version that is not a draft means the identity entered the publication
  -- lifecycle. Checking for 'published' alone left a 'superseded' row able to
  -- restrict the exhibitions delete with a raw foreign key violation.
  if v_exhibition.archived_at is not null
     or v_exhibition.published_version_id is not null
     or exists (
       select 1
       from content.exhibition_versions as historical_version
       where historical_version.exhibition_id = p_exhibition_id
         and historical_version.status
             <> 'draft'::content.exhibition_version_status
     ) then
    raise exception using
      errcode = '22023',
      message = 'only_never_published_drafts_can_be_deleted';
  end if;

  select version.*
  into v_version
  from content.exhibition_versions as version
  where version.exhibition_id = p_exhibition_id
    and version.status = 'draft'::content.exhibition_version_status
  order by version.version_number desc
  limit 1
  for update;

  if not found or v_version.id is distinct from p_expected_version_id then
    raise exception using errcode = 'P0002', message = 'working_version_not_found';
  end if;
  if p_expected_revision is null or v_version.revision <> p_expected_revision then
    raise exception using
      errcode = '40001',
      message = 'revision_conflict',
      detail = v_version.revision::text;
  end if;

  -- Every draft version of the identity is removed below, so media attached to
  -- any of them — not just the working version — must be detached first.
  if exists (
    select 1
    from content.exhibition_version_media as attachment
    join content.exhibition_versions as owning_version
      on owning_version.id = attachment.version_id
    where owning_version.exhibition_id = p_exhibition_id
  ) then
    raise exception using
      errcode = '23503',
      message = 'draft_delete_requires_media_detach';
  end if;

  if exists (
    select 1
    from content.legacy_import_links as import_link
    where import_link.exhibition_id = p_exhibition_id
  ) then
    raise exception using
      errcode = '23503',
      message = 'imported_exhibitions_cannot_be_deleted';
  end if;

  if exists (
    select 1
    from content.exhibition_submissions as submission
    where submission.accepted_exhibition_id = p_exhibition_id
  ) then
    raise exception using
      errcode = '23503',
      message = 'draft_delete_has_submission_reference';
  end if;

  if exists (
    select 1
    from content.curation_placements as placement
    where placement.exhibition_id = p_exhibition_id
  ) then
    raise exception using
      errcode = '23503',
      message = 'draft_delete_has_curation_reference';
  end if;

  if exists (
    select 1
    from content.launch_kits as kit
    where kit.exhibition_id = p_exhibition_id
  ) then
    raise exception using
      errcode = '23503',
      message = 'draft_delete_has_launch_kit_reference';
  end if;

  if exists (
    select 1
    from content.local_promotions as promotion
    where promotion.exhibition_id = p_exhibition_id
  ) then
    raise exception using
      errcode = '23503',
      message = 'draft_delete_has_promotion_reference';
  end if;

  -- Delivered and dead-lettered events are terminal history. Blocking on their
  -- existence made deletion unreachable for any identity that was ever
  -- submitted, archived, or restored, because nothing purges the outbox.
  if exists (
    select 1
    from content.outbox_events as event
    where event.aggregate_type = 'exhibition'
      and event.aggregate_id = p_exhibition_id
      and event.delivered_at is null
      and event.dead_lettered_at is null
  ) then
    raise exception using
      errcode = '23503',
      message = 'draft_delete_has_pending_outbox_event';
  end if;

  -- An owner round still open in the staff queue is withdrawn with the draft so
  -- no reviewer is left holding a submission for a record that no longer
  -- exists. 'pending_upload' is deliberately excluded: it has no submitted_at
  -- and would violate exhibition_submissions_submitted_at.
  with withdrawn_round as (
    update content.exhibition_submissions as submission
    set status = 'withdrawn'::content.submission_status,
        owner_exhibition_id = null
    where submission.owner_exhibition_id = p_exhibition_id
      and submission.source = 'owner_workspace'
      and submission.status in (
        'submitted'::content.submission_status,
        'in_review'::content.submission_status
      )
    returning submission.id
  )
  select coalesce(array_agg(withdrawn_round.id), array[]::uuid[])
  into v_withdrawn_submission_ids
  from withdrawn_round;

  -- Rounds that already reached a decision keep it and only lose the pointer.
  with detached_round as (
    update content.exhibition_submissions as submission
    set owner_exhibition_id = null
    where submission.owner_exhibition_id = p_exhibition_id
      and submission.source = 'owner_workspace'
    returning submission.id
  )
  select coalesce(array_agg(detached_round.id), array[]::uuid[])
  into v_detached_submission_ids
  from detached_round;

  v_response := jsonb_build_object(
    'id', p_exhibition_id,
    'working_version_id', v_version.id,
    'revision', v_version.revision,
    'status', 'deleted',
    'withdrawn_submission_ids', to_jsonb(v_withdrawn_submission_ids)
  );

  insert into content.audit_log (
    actor_user_id,
    action,
    entity_type,
    entity_id,
    metadata
  )
  values (
    v_user_id,
    'exhibition.draft_deleted',
    'exhibition',
    p_exhibition_id,
    jsonb_build_object(
      'working_version_id', v_version.id,
      'version_number', v_version.version_number,
      'revision', v_version.revision,
      'name_ko', v_version.name_ko,
      'withdrawn_submission_ids', to_jsonb(v_withdrawn_submission_ids),
      'detached_submission_ids', to_jsonb(v_detached_submission_ids)
    )
  );

  delete from content.exhibition_versions
  where exhibition_id = p_exhibition_id;

  delete from content.exhibitions
  where id = p_exhibition_id;

  return v_response;
end;
$$;

revoke all on function content_private.admin_delete_exhibition_draft_impl(text, uuid, integer)
  from public, anon, authenticated, service_role;

comment on function content_private.admin_delete_exhibition_draft_impl(text, uuid, integer) is
  'Permanently deletes a never-published draft, withdrawing an open owner submission round and detaching decided rounds.';

-- Deletion withdraws an open owner round, which removes a live item from the
-- staff review queue. Staff have to see that consequence in the confirmation
-- dialog, so the shared exhibition projection reports whether such a round
-- exists. The lookup is an index probe against the existing partial unique
-- index exhibition_submissions_one_open_owner_round_idx, which covers exactly
-- the owner_workspace rows in 'submitted' or 'in_review'.
create or replace function content_private.admin_exhibition_json(
  p_exhibition_id text,
  p_version_id uuid
)
returns jsonb
language sql
stable
security definer
set search_path = ''
as $function$
  select jsonb_build_object(
    'id', exhibition.id,
    'working_version_id', version.id,
    'version_number', version.version_number,
    'published_version_id', exhibition.published_version_id,
    'has_unpublished_changes', version.status = 'draft'::content.exhibition_version_status,
    'name_ko', version.name_ko,
    'name_en', version.name_en,
    'venue_name_ko', version.venue_name_ko,
    'venue_name_en', version.venue_name_en,
    'city_ko', version.city_ko,
    'city_en', version.city_en,
    'region_ko', version.region_ko,
    'region_en', version.region_en,
    'address_ko', version.address_ko,
    'address_en', version.address_en,
    'latitude', coalesce(version.latitude::text, ''),
    'longitude', coalesce(version.longitude::text, ''),
    'event_id', coalesce(version.event_id, ''),
    'editor_id', coalesce(version.editor_id, ''),
    'opening_date', coalesce(to_char(version.opening_date, 'YYYY-MM-DD'), ''),
    'closing_date', coalesce(to_char(version.closing_date, 'YYYY-MM-DD'), ''),
    'description_ko', version.description_ko,
    'description_en', version.description_en,
    'credits_ko', version.credits_ko,
    'credits_en', version.credits_en,
    'hours', coalesce(version.hours, ''),
    'contact', coalesce(version.contact, ''),
    'ticket_url', coalesce(version.ticket_url, ''),
    'reception_date', coalesce(
      to_char(version.reception_date at time zone 'Asia/Seoul', 'YYYY-MM-DD'),
      ''
    ),
    'reception_start_time', coalesce(version.opening_time, ''),
    'reception_end_time', coalesce(version.reception_end_time, ''),
    'cover_image_url', coalesce(cover.public_url, version.legacy_cover_image_url),
    'cover_alt_ko', coalesce(cover.alt_ko, ''),
    'cover_alt_en', coalesce(cover.alt_en, ''),
    'image_credit', coalesce(cover.credit, ''),
    'is_featured', version.is_featured,
    'is_homepage_featured', version.is_homepage_featured,
    'has_open_owner_submission', exists (
      select 1
      from content.exhibition_submissions as owner_round
      where owner_round.owner_exhibition_id = exhibition.id
        and owner_round.source = 'owner_workspace'
        and owner_round.status in (
          'submitted'::content.submission_status,
          'in_review'::content.submission_status
        )
    ),
    'status', case
      when exhibition.archived_at is not null then 'archived'
      when version.status = 'draft'::content.exhibition_version_status then 'draft'
      else 'published'
    end,
    'revision', version.revision,
    'created_at', exhibition.created_at,
    'published_at', published.published_at,
    'updated_at', greatest(exhibition.updated_at, version.updated_at),
    'updated_by', coalesce(
      nullif(updater_profile.display_name, ''),
      nullif(updater.email, ''),
      'Unknown staff member'
    )
  )
  from content.exhibitions as exhibition
  join content.exhibition_versions as version
    on version.exhibition_id = exhibition.id
   and version.id = p_version_id
  left join content.exhibition_versions as published
    on published.id = exhibition.published_version_id
  left join lateral (
    select
      asset.public_url,
      attachment.alt_ko,
      attachment.alt_en,
      attachment.credit
    from content.exhibition_version_media as attachment
    join content.media_assets as asset on asset.id = attachment.media_id
    where attachment.version_id = version.id
      and attachment.role = 'cover'::content.media_role
    order by attachment.sort_order, attachment.created_at
    limit 1
  ) as cover on true
  left join auth.users as updater
    on updater.id = case
      when exhibition.updated_at > version.updated_at then exhibition.updated_by
      else version.updated_by
    end
  left join public.profiles as updater_profile on updater_profile.id = updater.id
  where exhibition.id = p_exhibition_id;
$function$;

revoke all on function content_private.admin_exhibition_json(text, uuid)
  from public, anon, authenticated, service_role;
