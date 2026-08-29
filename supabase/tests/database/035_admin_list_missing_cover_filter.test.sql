begin;

-- Covers the Admin "missing cover image" list filter and the restored
-- owner-visibility guard on the extended staff list overload.

create extension if not exists pgtap with schema extensions;
set local search_path = extensions, public;

select plan(19);

select ok(
  has_function_privilege(
    'authenticated',
    'public.admin_list_exhibitions(text,text,text,boolean,boolean,text)',
    'EXECUTE'
  ),
  'authenticated users can call the cover-aware list RPC'
);

select ok(
  not has_function_privilege(
    'anon',
    'public.admin_list_exhibitions(text,text,text,boolean,boolean,text)',
    'EXECUTE'
  ),
  'anonymous users cannot call the cover-aware list RPC'
);

select ok(
  not has_function_privilege(
    'service_role',
    'public.admin_list_exhibitions(text,text,text,boolean,boolean,text)',
    'EXECUTE'
  ),
  'service_role receives no implicit grant on the cover-aware list RPC'
);

insert into auth.users (id, email, raw_user_meta_data)
values (
  '00000000-0000-0000-0000-000000003501'::uuid,
  'admin-cover-filter@example.invalid',
  '{"full_name":"Cover Filter Editor"}'::jsonb
);

insert into content.staff_members (user_id, role, active)
values (
  '00000000-0000-0000-0000-000000003501'::uuid,
  'contributor'::content.staff_role,
  true
);

create temporary table cover_filter_state (
  label text primary key,
  payload jsonb not null
) on commit drop;
grant select, insert, update, delete on cover_filter_state to authenticated;

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000003501","role":"authenticated"}',
  true
);

insert into pg_temp.cover_filter_state (label, payload)
values ('without_cover', public.admin_create_exhibition_draft());
insert into pg_temp.cover_filter_state (label, payload)
values ('with_cover', public.admin_create_exhibition_draft());

update pg_temp.cover_filter_state
set payload = public.admin_save_exhibition_draft(
  payload ->> 'id',
  (payload ->> 'working_version_id')::uuid,
  (payload ->> 'revision')::integer,
  jsonb_build_object(
    'name_ko', '커버 필터 ' || label,
    'venue_name_ko', '테스트 전시장',
    'opening_date', to_char(
      (current_timestamp at time zone 'Asia/Seoul')::date - 1,
      'YYYY-MM-DD'
    ),
    'closing_date', to_char(
      (current_timestamp at time zone 'Asia/Seoul')::date + 1,
      'YYYY-MM-DD'
    )
  )
);

reset role;

update content.exhibition_versions
set legacy_cover_image_url = 'https://images.example.invalid/cover.webp'
where id = (
  select (payload ->> 'working_version_id')::uuid
  from pg_temp.cover_filter_state
  where label = 'with_cover'
);

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000003501","role":"authenticated"}',
  true
);

select is(
  (
    select payload ->> 'cover_image_url'
    from public.admin_list_exhibitions('', null, null, false, false, 'updated_desc')
      as listed(payload)
    where payload ->> 'id' = (
      select payload ->> 'id' from pg_temp.cover_filter_state where label = 'with_cover'
    )
  ),
  'https://images.example.invalid/cover.webp',
  'the cover-aware list RPC keeps returning the resolved cover URL'
);

select is(
  (
    select count(*)::integer
    from public.admin_list_exhibitions('', null, null, false, false, 'updated_desc')
      as listed(payload)
    where payload ->> 'id' in (
      select payload ->> 'id' from pg_temp.cover_filter_state
    )
  ),
  2,
  'the missing-cover filter is off by default'
);

select is(
  (
    select array_agg(listed.payload ->> 'id' order by listed.payload ->> 'id')
    from public.admin_list_exhibitions('', null, null, false, true, 'updated_desc')
      as listed(payload)
    where listed.payload ->> 'id' in (
      select payload ->> 'id' from pg_temp.cover_filter_state
    )
  ),
  (
    select array_agg(payload ->> 'id')
    from pg_temp.cover_filter_state
    where label = 'without_cover'
  ),
  'the missing-cover filter keeps only exhibitions without a cover URL'
);

select is(
  (
    select count(*)::integer
    from public.admin_list_exhibitions(
      '',
      'draft',
      'running',
      true,
      true,
      'opening_asc'
    ) as listed(payload)
    where listed.payload ->> 'id' = (
      select payload ->> 'id' from pg_temp.cover_filter_state where label = 'without_cover'
    )
  ),
  1,
  'the missing-cover filter combines with publish state, date state, placement, and sort'
);

reset role;

update content.exhibition_versions
set legacy_cover_image_url = '   '
where id = (
  select (payload ->> 'working_version_id')::uuid
  from pg_temp.cover_filter_state
  where label = 'with_cover'
);

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000003501","role":"authenticated"}',
  true
);

select is(
  (
    select count(*)::integer
    from public.admin_list_exhibitions('', null, null, false, true, 'updated_desc')
      as listed(payload)
    where listed.payload ->> 'id' in (
      select payload ->> 'id' from pg_temp.cover_filter_state
    )
  ),
  2,
  'a blank legacy cover URL counts as a missing cover'
);

select throws_ok(
  $$select * from public.admin_list_exhibitions('', null, null, false, false, 'unknown')$$,
  '22023',
  'invalid_exhibition_sort',
  'the cover-aware list RPC rejects unsupported sort values'
);

select throws_ok(
  $$select * from public.admin_list_exhibitions('', null, null, false, false, null)$$,
  '22023',
  'invalid_exhibition_sort',
  'the cover-aware list RPC rejects a null sort instead of silently reordering'
);

select throws_ok(
  $$select * from public.admin_list_exhibitions('', 'bogus', null, false, false, 'updated_desc')$$,
  '22023',
  'invalid_exhibition_status_filter',
  'the cover-aware list RPC rejects unsupported publish-state filters'
);

select throws_ok(
  $$select * from public.admin_list_exhibitions('', null, 'bogus', false, false, 'updated_desc')$$,
  '22023',
  'invalid_exhibition_temporal_filter',
  'the cover-aware list RPC rejects unsupported date-state filters'
);

reset role;

-- A cover attached through the media workflow resolves before the legacy URL.
insert into content.media_assets (
  id,
  status,
  bucket_id,
  object_path,
  public_url,
  delivery_bucket_id,
  delivery_object_path,
  mime_type,
  byte_size,
  width,
  height,
  checksum_sha256,
  alt_ko,
  alt_en,
  credit,
  uploaded_by,
  published_at
)
values (
  '30000000-0000-0000-0000-000000003501'::uuid,
  'published'::content.media_asset_status,
  'exhibition-media',
  'tests/cover-filter.jpg',
  'https://cdn.example.invalid/cover-filter.jpg',
  'exhibition-images',
  'cms/30000000-0000-0000-0000-000000003501/original.jpg',
  'image/jpeg',
  1024,
  1200,
  800,
  repeat('c', 64),
  '표지',
  'Cover',
  'Test Artist',
  '00000000-0000-0000-0000-000000003501'::uuid,
  now()
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
  '30000000-0000-0000-0000-000000003501'::uuid,
  'cover'::content.media_role,
  0,
  '00000000-0000-0000-0000-000000003501'::uuid
from pg_temp.cover_filter_state
where label = 'with_cover';

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000003501","role":"authenticated"}',
  true
);

select is(
  (
    select payload ->> 'cover_image_url'
    from public.admin_list_exhibitions('', null, null, false, false, 'updated_desc')
      as listed(payload)
    where payload ->> 'id' = (
      select payload ->> 'id' from pg_temp.cover_filter_state where label = 'with_cover'
    )
  ),
  'https://cdn.example.invalid/cover-filter.jpg',
  'an attached media cover is the resolved cover URL'
);

select is(
  (
    select count(*)::integer
    from public.admin_list_exhibitions('', null, null, false, true, 'updated_desc')
      as listed(payload)
    where listed.payload ->> 'id' = (
      select payload ->> 'id' from pg_temp.cover_filter_state where label = 'with_cover'
    )
  ),
  0,
  'an attached media cover removes the exhibition from the missing-cover list'
);

reset role;

-- A cover that is still processing has no delivery URL yet and stays "missing".
update content.media_assets
set public_url = null,
    status = 'ready'::content.media_asset_status,
    published_at = null
where id = '30000000-0000-0000-0000-000000003501'::uuid;

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000003501","role":"authenticated"}',
  true
);

select is(
  (
    select count(*)::integer
    from public.admin_list_exhibitions('', null, null, false, true, 'updated_desc')
      as listed(payload)
    where listed.payload ->> 'id' = (
      select payload ->> 'id' from pg_temp.cover_filter_state where label = 'with_cover'
    )
  ),
  1,
  'a cover without a delivery URL still counts as missing'
);

reset role;

insert into auth.users (id, email, raw_user_meta_data)
values (
  '00000000-0000-0000-0000-000000003502'::uuid,
  'non-staff-cover-filter@example.invalid',
  '{}'::jsonb
);

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000003502","role":"authenticated"}',
  true
);

select throws_ok(
  $$select * from public.admin_list_exhibitions('', null, null, false, false, 'updated_desc')$$,
  '42501',
  'active_staff_membership_required',
  'a signed-in non-staff user cannot call the cover-aware list RPC'
);

reset role;

update content.exhibitions
set owner_status = 'draft'::content.owner_exhibition_status
where id = (
  select payload ->> 'id' from pg_temp.cover_filter_state where label = 'without_cover'
);

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000003501","role":"authenticated"}',
  true
);

select is(
  (
    select count(*)::integer
    from public.admin_list_exhibitions('', null, null, false, false, 'updated_desc')
      as listed(payload)
    where listed.payload ->> 'id' = (
      select payload ->> 'id' from pg_temp.cover_filter_state where label = 'without_cover'
    )
  ),
  0,
  'the cover-aware list RPC hides private owner drafts from staff'
);

select is(
  (
    select count(*)::integer
    from public.admin_list_exhibitions('', null, null, false, 'updated_desc')
      as listed(payload)
    where listed.payload ->> 'id' = (
      select payload ->> 'id' from pg_temp.cover_filter_state where label = 'without_cover'
    )
  ),
  0,
  'the five-argument list overload hides private owner drafts from staff'
);

reset role;

update content.exhibitions
set owner_status = 'published'::content.owner_exhibition_status
where id = (
  select payload ->> 'id' from pg_temp.cover_filter_state where label = 'without_cover'
);

set local role authenticated;
select set_config(
  'request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-000000003501","role":"authenticated"}',
  true
);

select is(
  (
    select count(*)::integer
    from public.admin_list_exhibitions('', null, null, false, false, 'updated_desc')
      as listed(payload)
    where listed.payload ->> 'id' = (
      select payload ->> 'id' from pg_temp.cover_filter_state where label = 'without_cover'
    )
  ),
  1,
  'published owner exhibitions stay visible in the staff list'
);

reset role;

select * from finish();
rollback;
