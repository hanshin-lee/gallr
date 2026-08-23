begin;

create extension if not exists pgtap with schema extensions;
set local search_path = extensions, public;

select plan(30);

select has_function(
  'public',
  'admin_delete_exhibition_draft',
  array['text', 'uuid', 'integer', 'uuid'],
  'the public permanent-draft deletion command exists'
);
select ok(
  has_function_privilege(
    'authenticated',
    'public.admin_delete_exhibition_draft(text, uuid, integer, uuid)',
    'EXECUTE'
  )
  and not has_function_privilege(
    'anon',
    'public.admin_delete_exhibition_draft(text, uuid, integer, uuid)',
    'EXECUTE'
  )
  and not has_function_privilege(
    'service_role',
    'public.admin_delete_exhibition_draft(text, uuid, integer, uuid)',
    'EXECUTE'
  ),
  'only authenticated can execute the public deletion command'
);
select ok(
  not (
    select procedure.prosecdef
    from pg_proc as procedure
    join pg_namespace as namespace on namespace.oid = procedure.pronamespace
    where namespace.nspname = 'public'
      and procedure.proname = 'admin_delete_exhibition_draft'
  ),
  'the public deletion command is SECURITY INVOKER'
);
select ok(
  (
    select procedure.proconfig @> array['search_path=""']::text[]
    from pg_proc as procedure
    join pg_namespace as namespace on namespace.oid = procedure.pronamespace
    where namespace.nspname = 'public'
      and procedure.proname = 'admin_delete_exhibition_draft'
  ),
  'the public deletion command pins an empty search_path'
);

insert into auth.users (id, email, raw_user_meta_data)
values
  (
    '00000000-0000-0000-0000-000000000601'::uuid,
    'delete-publisher@example.invalid',
    '{}'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000602'::uuid,
    'delete-admin@example.invalid',
    '{}'::jsonb
  );

insert into content.staff_members (user_id, role, active)
values
  (
    '00000000-0000-0000-0000-000000000601'::uuid,
    'publisher'::content.staff_role,
    true
  ),
  (
    '00000000-0000-0000-0000-000000000602'::uuid,
    'admin'::content.staff_role,
    true
  );

create temporary table delete_test_state (
  key text primary key,
  payload jsonb
) on commit drop;
grant select, insert, update, delete on delete_test_state to authenticated;

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000601","role":"authenticated"}',
  true
);
insert into pg_temp.delete_test_state (key, payload)
values ('publisher_draft', public.admin_create_exhibition_draft());

select throws_ok(
  format(
    'select public.admin_delete_exhibition_draft(%L, %L::uuid, 1, %L::uuid)',
    (select payload ->> 'id' from pg_temp.delete_test_state where key = 'publisher_draft'),
    (select payload ->> 'working_version_id' from pg_temp.delete_test_state where key = 'publisher_draft'),
    '60000000-0000-0000-0000-000000000001'
  ),
  '42501',
  'insufficient_staff_role',
  'a publisher cannot permanently delete a draft'
);

select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000602","role":"authenticated"}',
  true
);
insert into pg_temp.delete_test_state (key, payload)
values ('admin_draft', public.admin_create_exhibition_draft());

select throws_ok(
  format(
    'select public.admin_delete_exhibition_draft(%L, %L::uuid, 1, %L::uuid)',
    (select payload ->> 'id' from pg_temp.delete_test_state where key = 'admin_draft'),
    '60000000-0000-0000-0000-000000000099',
    '60000000-0000-0000-0000-000000000006'
  ),
  'P0002',
  'working_version_not_found',
  'the command rejects a non-current working-version ID'
);

select throws_ok(
  format(
    'select public.admin_delete_exhibition_draft(%L, %L::uuid, 0, %L::uuid)',
    (select payload ->> 'id' from pg_temp.delete_test_state where key = 'admin_draft'),
    (select payload ->> 'working_version_id' from pg_temp.delete_test_state where key = 'admin_draft'),
    '60000000-0000-0000-0000-000000000002'
  ),
  '40001',
  'revision_conflict',
  'the command rejects a stale revision'
);

update pg_temp.delete_test_state
set payload = public.admin_delete_exhibition_draft(
  payload ->> 'id',
  (payload ->> 'working_version_id')::uuid,
  (payload ->> 'revision')::integer,
  '60000000-0000-0000-0000-000000000003'::uuid
)
where key = 'admin_draft';

select is(
  (select payload ->> 'status' from pg_temp.delete_test_state where key = 'admin_draft'),
  'deleted',
  'the command reports permanent deletion'
);
select is(
  (
    select count(*)
    from content.exhibitions
    where id = (
      select payload ->> 'id'
      from pg_temp.delete_test_state
      where key = 'admin_draft'
    )
  ),
  0::bigint,
  'the draft identity is deleted'
);
select is(
  (
    select count(*)
    from content.exhibition_versions
    where exhibition_id = (
      select payload ->> 'id'
      from pg_temp.delete_test_state
      where key = 'admin_draft'
    )
  ),
  0::bigint,
  'the draft version is deleted'
);
select is(
  (
    select count(*)
    from content.audit_log
    where entity_id = (
      select payload ->> 'id'
      from pg_temp.delete_test_state
      where key = 'admin_draft'
    )
      and action = 'exhibition.draft_deleted'
  ),
  1::bigint,
  'deletion appends an audit event'
);

select is(
  public.admin_delete_exhibition_draft(
    (select payload ->> 'id' from pg_temp.delete_test_state where key = 'admin_draft'),
    (select (payload ->> 'working_version_id')::uuid from pg_temp.delete_test_state where key = 'admin_draft'),
    (select (payload ->> 'revision')::integer from pg_temp.delete_test_state where key = 'admin_draft'),
    '60000000-0000-0000-0000-000000000003'::uuid
  ),
  (select payload from pg_temp.delete_test_state where key = 'admin_draft'),
  'replaying the request returns the stored response'
);
select is(
  (
    select count(*)
    from content.audit_log
    where entity_id = (
      select payload ->> 'id'
      from pg_temp.delete_test_state
      where key = 'admin_draft'
    )
      and action = 'exhibition.draft_deleted'
  ),
  1::bigint,
  'replay does not duplicate the audit event'
);

insert into pg_temp.delete_test_state (key, payload)
values ('media_draft', public.admin_create_exhibition_draft());
reset role;

insert into content.media_assets (
  id,
  bucket_id,
  object_path,
  uploaded_by
)
values (
  '60000000-0000-0000-0000-000000000101'::uuid,
  'exhibition-media',
  'tests/delete-draft.jpg',
  '00000000-0000-0000-0000-000000000602'::uuid
);
insert into content.exhibition_version_media (
  version_id,
  media_id,
  role,
  sort_order,
  created_by
)
select
  (payload ->> 'working_version_id')::uuid,
  '60000000-0000-0000-0000-000000000101'::uuid,
  'cover'::content.media_role,
  0,
  '00000000-0000-0000-0000-000000000602'::uuid
from pg_temp.delete_test_state
where key = 'media_draft';

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000602","role":"authenticated"}',
  true
);
select throws_ok(
  format(
    'select public.admin_delete_exhibition_draft(%L, %L::uuid, 1, %L::uuid)',
    (select payload ->> 'id' from pg_temp.delete_test_state where key = 'media_draft'),
    (select payload ->> 'working_version_id' from pg_temp.delete_test_state where key = 'media_draft'),
    '60000000-0000-0000-0000-000000000004'
  ),
  '23503',
  'draft_delete_requires_media_detach',
  'a draft with attached media cannot be deleted'
);
select is(
  (
    select count(*)
    from content.exhibitions
    where id = (
      select payload ->> 'id'
      from pg_temp.delete_test_state
      where key = 'media_draft'
    )
  ),
  1::bigint,
  'a rejected media deletion leaves the identity intact'
);

reset role;
insert into content.exhibitions (
  id,
  created_by,
  updated_by
)
values (
  'delete-published-test',
  '00000000-0000-0000-0000-000000000602'::uuid,
  '00000000-0000-0000-0000-000000000602'::uuid
);
insert into content.exhibition_versions (
  id,
  exhibition_id,
  version_number,
  revision,
  status,
  name_ko,
  venue_name_ko,
  city_ko,
  region_ko,
  address_ko,
  opening_date,
  closing_date,
  latitude,
  longitude,
  published_at,
  published_by,
  created_by,
  updated_by
)
values (
  '60000000-0000-0000-0000-000000000201'::uuid,
  'delete-published-test',
  1,
  1,
  'published'::content.exhibition_version_status,
  '삭제 불가 공개 전시',
  '테스트 전시장',
  '서울',
  '용산구',
  '서울 용산구 한남대로 28',
  '2026-07-28'::date,
  '2026-08-28'::date,
  37.5344,
  127.0005,
  now(),
  '00000000-0000-0000-0000-000000000602'::uuid,
  '00000000-0000-0000-0000-000000000602'::uuid,
  '00000000-0000-0000-0000-000000000602'::uuid
);
update content.exhibitions
set published_version_id = '60000000-0000-0000-0000-000000000201'::uuid
where id = 'delete-published-test';

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000602","role":"authenticated"}',
  true
);
select throws_ok(
  $$
    select public.admin_delete_exhibition_draft(
      'delete-published-test',
      '60000000-0000-0000-0000-000000000201'::uuid,
      1,
      '60000000-0000-0000-0000-000000000005'::uuid
    )
  $$,
  '22023',
  'only_never_published_drafts_can_be_deleted',
  'a published identity cannot be permanently deleted'
);
select is(
  (
    select count(*)
    from content.exhibitions
    where id = 'delete-published-test'
  ),
  1::bigint,
  'a rejected published deletion leaves the identity intact'
);

-- Delivered and dead-lettered outbox history is terminal and must not strand a
-- never-published draft. An existence check made every submitted, archived, or
-- restored identity permanently undeletable because nothing purges the outbox.
set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000602","role":"authenticated"}',
  true
);
insert into pg_temp.delete_test_state (key, payload)
values ('settled_outbox_draft', public.admin_create_exhibition_draft());

reset role;
insert into content.outbox_events (
  aggregate_type,
  aggregate_id,
  event_type,
  payload,
  deduplication_key,
  status,
  delivered_at
)
select
  'exhibition',
  payload ->> 'id',
  'owner_exhibition.submitted',
  '{}'::jsonb,
  'delete-test:settled:delivered',
  'delivered'::content.outbox_status,
  now()
from pg_temp.delete_test_state
where key = 'settled_outbox_draft';
insert into content.outbox_events (
  aggregate_type,
  aggregate_id,
  event_type,
  payload,
  deduplication_key,
  status,
  dead_lettered_at
)
select
  'exhibition',
  payload ->> 'id',
  'exhibition.archived',
  '{}'::jsonb,
  'delete-test:settled:dead-lettered',
  'failed'::content.outbox_status,
  now()
from pg_temp.delete_test_state
where key = 'settled_outbox_draft';

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000602","role":"authenticated"}',
  true
);
select is(
  (
    select public.admin_delete_exhibition_draft(
      payload ->> 'id',
      (payload ->> 'working_version_id')::uuid,
      1,
      '60000000-0000-0000-0000-000000000006'::uuid
    ) ->> 'status'
    from pg_temp.delete_test_state
    where key = 'settled_outbox_draft'
  ),
  'deleted',
  'delivered and dead-lettered outbox history does not block deletion'
);
select is(
  (
    select count(*)
    from content.exhibitions
    where id = (
      select payload ->> 'id'
      from pg_temp.delete_test_state
      where key = 'settled_outbox_draft'
    )
  ),
  0::bigint,
  'the identity with settled outbox history is removed'
);

-- Work the outbox worker will still attempt is the only outbox state that
-- blocks deletion.
insert into pg_temp.delete_test_state (key, payload)
values ('pending_outbox_draft', public.admin_create_exhibition_draft());

reset role;
insert into content.outbox_events (
  aggregate_type,
  aggregate_id,
  event_type,
  payload,
  deduplication_key
)
select
  'exhibition',
  payload ->> 'id',
  'owner_exhibition.submitted',
  '{}'::jsonb,
  'delete-test:pending:queued'
from pg_temp.delete_test_state
where key = 'pending_outbox_draft';

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000602","role":"authenticated"}',
  true
);
select throws_ok(
  format(
    'select public.admin_delete_exhibition_draft(%L, %L::uuid, 1, %L::uuid)',
    (select payload ->> 'id' from pg_temp.delete_test_state where key = 'pending_outbox_draft'),
    (select payload ->> 'working_version_id' from pg_temp.delete_test_state where key = 'pending_outbox_draft'),
    '60000000-0000-0000-0000-000000000007'
  ),
  '23503',
  'draft_delete_has_pending_outbox_event',
  'an undelivered outbox event blocks deletion'
);
select is(
  (
    select count(*)
    from content.exhibitions
    where id = (
      select payload ->> 'id'
      from pg_temp.delete_test_state
      where key = 'pending_outbox_draft'
    )
  ),
  1::bigint,
  'a rejected pending-outbox deletion leaves the identity intact'
);

-- An owner round still open in the staff queue is withdrawn with the draft.
insert into pg_temp.delete_test_state (key, payload)
values ('open_round_draft', public.admin_create_exhibition_draft());

reset role;
insert into content.galleries (id, name_ko)
values ('60000000-0000-0000-0000-000000000301'::uuid, '삭제 테스트 갤러리');
insert into content.media_assets (id, bucket_id, object_path, uploaded_by)
values (
  '60000000-0000-0000-0000-000000000102'::uuid,
  'exhibition-media',
  'tests/delete-draft-open-round.jpg',
  '00000000-0000-0000-0000-000000000602'::uuid
);
insert into content.exhibition_version_media (
  version_id,
  media_id,
  role,
  sort_order,
  created_by
)
select
  (payload ->> 'working_version_id')::uuid,
  '60000000-0000-0000-0000-000000000102'::uuid,
  'cover'::content.media_role,
  0,
  '00000000-0000-0000-0000-000000000602'::uuid
from pg_temp.delete_test_state
where key = 'open_round_draft';
insert into content.exhibition_submissions (
  id,
  status,
  submitter_email,
  payload,
  source,
  owner_exhibition_id,
  submitted_at
)
select
  '60000000-0000-0000-0000-000000000401'::uuid,
  'in_review'::content.submission_status,
  'owner@example.invalid',
  jsonb_build_object('version_id', payload ->> 'working_version_id'),
  'owner_workspace',
  payload ->> 'id',
  now()
from pg_temp.delete_test_state
where key = 'open_round_draft';
-- The round keeps its own media snapshot. Detaching the draft's attachments is
-- the production shape of this case: deletion still requires a clean version.
delete from content.exhibition_version_media
where media_id = '60000000-0000-0000-0000-000000000102'::uuid;

select is(
  (
    select content_private.admin_exhibition_json(
      payload ->> 'id',
      (payload ->> 'working_version_id')::uuid
    ) -> 'has_open_owner_submission'
    from pg_temp.delete_test_state
    where key = 'open_round_draft'
  ),
  'true'::jsonb,
  'the exhibition projection warns that an open owner round exists'
);

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000602","role":"authenticated"}',
  true
);
select is(
  (
    select public.admin_delete_exhibition_draft(
      payload ->> 'id',
      (payload ->> 'working_version_id')::uuid,
      1,
      '60000000-0000-0000-0000-000000000008'::uuid
    ) -> 'withdrawn_submission_ids'
    from pg_temp.delete_test_state
    where key = 'open_round_draft'
  ),
  '["60000000-0000-0000-0000-000000000401"]'::jsonb,
  'the response reports the withdrawn open owner round'
);
select is(
  (
    select submission.status::text || ':' ||
           coalesce(submission.owner_exhibition_id, 'null')
    from content.exhibition_submissions as submission
    where submission.id = '60000000-0000-0000-0000-000000000401'::uuid
  ),
  'withdrawn:null',
  'an open owner round is withdrawn and detached from the deleted draft'
);
select is(
  (
    select count(*)
    from content.exhibitions
    where id = (
      select payload ->> 'id'
      from pg_temp.delete_test_state
      where key = 'open_round_draft'
    )
  ),
  0::bigint,
  'the draft behind an open owner round is removed'
);

-- A round that already reached a decision keeps it and only loses the pointer.
insert into pg_temp.delete_test_state (key, payload)
values ('decided_round_draft', public.admin_create_exhibition_draft());

reset role;
insert into content.media_assets (id, bucket_id, object_path, uploaded_by)
values (
  '60000000-0000-0000-0000-000000000103'::uuid,
  'exhibition-media',
  'tests/delete-draft-decided-round.jpg',
  '00000000-0000-0000-0000-000000000602'::uuid
);
insert into content.exhibition_version_media (
  version_id,
  media_id,
  role,
  sort_order,
  created_by
)
select
  (payload ->> 'working_version_id')::uuid,
  '60000000-0000-0000-0000-000000000103'::uuid,
  'cover'::content.media_role,
  0,
  '00000000-0000-0000-0000-000000000602'::uuid
from pg_temp.delete_test_state
where key = 'decided_round_draft';
insert into content.exhibition_submissions (
  id,
  status,
  submitter_email,
  payload,
  source,
  owner_exhibition_id,
  submitted_at,
  reviewed_at
)
select
  '60000000-0000-0000-0000-000000000402'::uuid,
  'rejected'::content.submission_status,
  'owner@example.invalid',
  jsonb_build_object('version_id', payload ->> 'working_version_id'),
  'owner_workspace',
  payload ->> 'id',
  now(),
  now()
from pg_temp.delete_test_state
where key = 'decided_round_draft';
delete from content.exhibition_version_media
where media_id = '60000000-0000-0000-0000-000000000103'::uuid;

select is(
  (
    select content_private.admin_exhibition_json(
      payload ->> 'id',
      (payload ->> 'working_version_id')::uuid
    ) -> 'has_open_owner_submission'
    from pg_temp.delete_test_state
    where key = 'decided_round_draft'
  ),
  'false'::jsonb,
  'a decided owner round raises no deletion warning'
);

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000602","role":"authenticated"}',
  true
);
select is(
  (
    select public.admin_delete_exhibition_draft(
      payload ->> 'id',
      (payload ->> 'working_version_id')::uuid,
      1,
      '60000000-0000-0000-0000-000000000009'::uuid
    ) -> 'withdrawn_submission_ids'
    from pg_temp.delete_test_state
    where key = 'decided_round_draft'
  ),
  '[]'::jsonb,
  'a decided owner round is not reported as withdrawn'
);
select is(
  (
    select submission.status::text || ':' ||
           coalesce(submission.owner_exhibition_id, 'null')
    from content.exhibition_submissions as submission
    where submission.id = '60000000-0000-0000-0000-000000000402'::uuid
  ),
  'rejected:null',
  'a decided owner round keeps its decision and loses the deleted pointer'
);

-- launch_kits is ON DELETE RESTRICT and now refuses with a named reason
-- instead of a raw foreign key violation.
insert into pg_temp.delete_test_state (key, payload)
values ('launch_kit_draft', public.admin_create_exhibition_draft());

reset role;
insert into content.launch_kits (exhibition_id, gallery_id)
select
  payload ->> 'id',
  '60000000-0000-0000-0000-000000000301'::uuid
from pg_temp.delete_test_state
where key = 'launch_kit_draft';

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000000602","role":"authenticated"}',
  true
);
select throws_ok(
  format(
    'select public.admin_delete_exhibition_draft(%L, %L::uuid, 1, %L::uuid)',
    (select payload ->> 'id' from pg_temp.delete_test_state where key = 'launch_kit_draft'),
    (select payload ->> 'working_version_id' from pg_temp.delete_test_state where key = 'launch_kit_draft'),
    '60000000-0000-0000-0000-00000000000a'
  ),
  '23503',
  'draft_delete_has_launch_kit_reference',
  'a draft with a launch kit cannot be deleted'
);
select is(
  (
    select count(*)
    from content.exhibitions
    where id = (
      select payload ->> 'id'
      from pg_temp.delete_test_state
      where key = 'launch_kit_draft'
    )
  ),
  1::bigint,
  'a rejected launch-kit deletion leaves the identity intact'
);

select * from finish();
rollback;
