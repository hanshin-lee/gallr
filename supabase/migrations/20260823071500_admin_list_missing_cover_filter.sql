-- Add the Admin "missing cover image" list filter as a new
-- admin_list_exhibitions overload. The existing two- and five-argument public
-- overloads keep serving already-deployed clients; their private
-- implementations now delegate here so there is one list query body.
--
-- The shared body also restores the admin_owner_exhibition_visible guard that
-- the five-argument overload lost when it merged from a parallel branch:
-- private gallery-owner drafts must stay out of the staff catalogue, matching
-- the original two-argument body and admin_get_exhibition.

create or replace function content_private.admin_list_exhibitions_impl(
  p_search text,
  p_status text,
  p_temporal_status text,
  p_featured_only boolean,
  p_missing_cover_only boolean,
  p_sort text
)
returns setof jsonb
language plpgsql
stable
security definer
set search_path = ''
as $function$
declare
  v_search text := lower(btrim(coalesce(p_search, '')));
  v_today date := (current_timestamp at time zone 'Asia/Seoul')::date;
begin
  perform content_private.admin_assert_staff('contributor'::content.staff_role);

  if p_status is not null and p_status not in ('draft', 'published', 'archived') then
    raise exception using errcode = '22023', message = 'invalid_exhibition_status_filter';
  end if;
  if p_temporal_status is not null
     and p_temporal_status not in ('running', 'upcoming', 'ended') then
    raise exception using errcode = '22023', message = 'invalid_exhibition_temporal_filter';
  end if;
  if p_sort is null or p_sort not in (
    'updated_desc', 'published_desc', 'opening_asc', 'closing_asc', 'created_desc'
  ) then
    raise exception using errcode = '22023', message = 'invalid_exhibition_sort';
  end if;

  return query
  select content_private.admin_exhibition_json(exhibition.id, chosen.id)
  from content.exhibitions as exhibition
  join lateral (
    select
      version.id,
      version.status,
      version.name_ko,
      version.name_en,
      version.venue_name_ko,
      version.venue_name_en,
      version.opening_date,
      version.closing_date,
      version.is_homepage_featured,
      version.legacy_cover_image_url,
      version.updated_at
    from content.exhibition_versions as version
    where version.exhibition_id = exhibition.id
      and (
        version.status = 'draft'::content.exhibition_version_status
        or version.id = exhibition.published_version_id
      )
    order by
      (version.status = 'draft'::content.exhibition_version_status) desc,
      version.version_number desc
    limit 1
  ) as chosen on true
  left join content.exhibition_versions as published
    on published.id = exhibition.published_version_id
  cross join lateral (
    select case
      when exhibition.archived_at is not null then 'archived'
      when chosen.status = 'draft'::content.exhibition_version_status then 'draft'
      else 'published'
    end as value
  ) as resolved_status
  where content_private.admin_owner_exhibition_visible(exhibition.id)
    and (
      v_search = ''
      or position(
        v_search in lower(concat_ws(
          ' ', exhibition.id, chosen.name_ko, chosen.name_en,
          chosen.venue_name_ko, chosen.venue_name_en
        ))
      ) > 0
    )
    and (p_status is null or resolved_status.value = p_status)
    and (not coalesce(p_featured_only, false) or chosen.is_homepage_featured)
    -- The cover lookup mirrors admin_exhibition_json's cover_image_url
    -- resolution and only runs when the filter is on.
    and (
      not coalesce(p_missing_cover_only, false)
      or nullif(
        btrim(coalesce(
          (
            select asset.public_url
            from content.exhibition_version_media as attachment
            join content.media_assets as asset on asset.id = attachment.media_id
            where attachment.version_id = chosen.id
              and attachment.role = 'cover'::content.media_role
            order by attachment.sort_order, attachment.created_at
            limit 1
          ),
          chosen.legacy_cover_image_url,
          ''
        )),
        ''
      ) is null
    )
    and (
      p_temporal_status is null
      or (p_temporal_status = 'running'
        and chosen.opening_date <= v_today
        and chosen.closing_date >= v_today)
      or (p_temporal_status = 'upcoming'
        and chosen.opening_date <= chosen.closing_date
        and chosen.opening_date > v_today)
      or (p_temporal_status = 'ended'
        and (
          chosen.opening_date > chosen.closing_date
          or chosen.closing_date < v_today
        ))
    )
  order by
    case when p_sort = 'opening_asc' then chosen.opening_date end asc nulls last,
    case when p_sort = 'closing_asc' then chosen.closing_date end asc nulls last,
    case when p_sort = 'published_desc' then published.published_at end desc nulls last,
    case when p_sort = 'created_desc' then exhibition.created_at end desc nulls last,
    case when p_sort = 'updated_desc'
      then greatest(exhibition.updated_at, chosen.updated_at) end desc nulls last,
    exhibition.id;
end;
$function$;

revoke all on function content_private.admin_list_exhibitions_impl(
  text, text, text, boolean, boolean, text
) from public, anon, authenticated, service_role;
grant execute on function content_private.admin_list_exhibitions_impl(
  text, text, text, boolean, boolean, text
) to authenticated;

create or replace function content_private.admin_list_exhibitions_impl(
  p_search text,
  p_status text,
  p_temporal_status text,
  p_featured_only boolean,
  p_sort text
)
returns setof jsonb
language sql
stable
security definer
set search_path = ''
as $function$
  select *
  from content_private.admin_list_exhibitions_impl(
    p_search,
    p_status,
    p_temporal_status,
    p_featured_only,
    false,
    p_sort
  );
$function$;

revoke all on function content_private.admin_list_exhibitions_impl(
  text, text, text, boolean, text
) from public, anon, authenticated, service_role;
grant execute on function content_private.admin_list_exhibitions_impl(
  text, text, text, boolean, text
) to authenticated;

create or replace function content_private.admin_list_exhibitions_impl(
  p_search text default '',
  p_status text default null
)
returns setof jsonb
language sql
stable
security definer
set search_path = ''
as $function$
  select *
  from content_private.admin_list_exhibitions_impl(
    p_search,
    p_status,
    null,
    false,
    false,
    'updated_desc'
  );
$function$;

revoke all on function content_private.admin_list_exhibitions_impl(text, text)
  from public, anon, authenticated, service_role;
grant execute on function content_private.admin_list_exhibitions_impl(text, text)
  to authenticated;

create or replace function public.admin_list_exhibitions(
  p_search text,
  p_status text,
  p_temporal_status text,
  p_featured_only boolean,
  p_missing_cover_only boolean,
  p_sort text
)
returns setof jsonb
language sql
stable
security invoker
set search_path = ''
as $function$
  select *
  from content_private.admin_list_exhibitions_impl(
    p_search,
    p_status,
    p_temporal_status,
    p_featured_only,
    p_missing_cover_only,
    p_sort
  );
$function$;

revoke all on function public.admin_list_exhibitions(
  text, text, text, boolean, boolean, text
) from public, anon, authenticated, service_role;
grant execute on function public.admin_list_exhibitions(
  text, text, text, boolean, boolean, text
) to authenticated;
