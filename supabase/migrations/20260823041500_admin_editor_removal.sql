-- Admin editor deactivation for account-less editors, plus permanent removal.
--
-- Two gaps are closed here.
--
-- First, 20260813023533 computed 'has_access' from content.editor_memberships
-- and admin_set_editor_access_impl raised 'editor_membership_not_found' when no
-- membership existed. Editors mirrored from the legacy catalogue never receive a
-- membership row, so the Admin directory rendered no access control for them at
-- all and the command would have failed if it had. Deactivation now degrades to
-- profile visibility when there is no linked workspace account.
--
-- Second, 20260813023533 deliberately offered no delete path. Removal is now
-- explicit and permanent: dependent editor rows are cleared, exhibition
-- attribution detaches through the existing ON DELETE SET NULL foreign keys, and
-- the audit entry retains the detached counts. The hardcoded 'gallr-editors'
-- seed identity stays undeletable because shipped mobile clients resolve it.

begin;

create or replace function content_private.admin_set_editor_access_impl(
  p_editor_id text,
  p_expected_revision integer,
  p_active boolean
)
returns jsonb
language plpgsql
volatile
security definer
set search_path = ''
as $function$
declare
  v_actor_user_id uuid := content_private.admin_assert_staff('admin'::content.staff_role);
  v_editor public.editors%rowtype;
  v_membership content.editor_memberships%rowtype;
  v_has_membership boolean := false;
begin
  if p_expected_revision is null or p_expected_revision < 1 then
    raise exception using
      errcode = '22023', message = 'expected_revision_must_be_positive';
  end if;
  if p_active is null then
    raise exception using errcode = '22023', message = 'active_state_required';
  end if;

  select editor.*
  into v_editor
  from public.editors as editor
  where editor.id = p_editor_id
  for update;

  if not found then
    raise exception using errcode = 'P0002', message = 'editor_not_found';
  end if;
  if v_editor.revision <> p_expected_revision then
    raise exception using
      errcode = '40001',
      message = 'revision_conflict',
      detail = v_editor.revision::text;
  end if;

  select membership.*
  into v_membership
  from content.editor_memberships as membership
  where membership.editor_id = v_editor.id
  for update;

  v_has_membership := found;

  -- An editor without a linked workspace account has no membership to toggle.
  -- Deactivation still has to mean something for that editor, so it falls back
  -- to withdrawing the public profile instead of failing the command. Restoring
  -- has no equivalent fallback: there is no account to hand back, so the command
  -- fails closed rather than consuming a revision and recording a restore that
  -- never happened.
  if p_active and not v_has_membership then
    raise exception using
      errcode = 'P0002', message = 'editor_membership_not_found';
  end if;

  if v_has_membership then
    update content.editor_memberships
    set
      active = p_active,
      updated_by = v_actor_user_id
    where user_id = v_membership.user_id;
  end if;

  update public.editors
  set
    is_active = case when p_active then is_active else false end,
    revision = revision + 1,
    updated_at = now()
  where id = v_editor.id;

  insert into content.audit_log (
    actor_user_id, action, entity_type, entity_id, metadata
  )
  values (
    v_actor_user_id,
    case
      when p_active then 'editor.access_restored'
      else 'editor.access_deactivated'
    end,
    'editor',
    v_editor.id,
    jsonb_build_object(
      'previous_revision', v_editor.revision,
      'revision', v_editor.revision + 1,
      'access_active', p_active,
      'profile_active', case when p_active then v_editor.is_active else false end,
      'has_workspace_account', v_has_membership
    )
  );

  return content_private.admin_editor_json(v_editor.id);
end;
$function$;

-- Permanent removal. Exhibition attribution detaches rather than blocking:
-- public.exhibitions.editor_id and content.exhibition_versions.editor_id are
-- both ON DELETE SET NULL, so affected exhibitions keep publishing without an
-- editor credit. The linked auth.users account is intentionally left in place;
-- removing editor authorization is not account deletion.
create or replace function content_private.admin_delete_editor_impl(
  p_editor_id text,
  p_expected_revision integer
)
returns jsonb
language plpgsql
volatile
security definer
set search_path = ''
as $function$
declare
  v_actor_user_id uuid := content_private.admin_assert_staff('admin'::content.staff_role);
  v_editor public.editors%rowtype;
  v_membership_user_id uuid;
  v_detached_exhibitions integer := 0;
  v_detached_versions integer := 0;
  v_removed_requests integer := 0;
begin
  if p_expected_revision is null or p_expected_revision < 1 then
    raise exception using
      errcode = '22023', message = 'expected_revision_must_be_positive';
  end if;

  select editor.*
  into v_editor
  from public.editors as editor
  where editor.id = p_editor_id
  for update;

  if not found then
    raise exception using errcode = 'P0002', message = 'editor_not_found';
  end if;
  if v_editor.revision <> p_expected_revision then
    raise exception using
      errcode = '40001',
      message = 'revision_conflict',
      detail = v_editor.revision::text;
  end if;
  if v_editor.id = 'gallr-editors' then
    raise exception using
      errcode = '42501', message = 'editor_identity_is_protected';
  end if;

  select count(*)
  into v_detached_exhibitions
  from public.exhibitions as exhibition
  where exhibition.editor_id = v_editor.id;

  select count(*)
  into v_detached_versions
  from content.exhibition_versions as version
  where version.editor_id = v_editor.id;

  select membership.user_id
  into v_membership_user_id
  from content.editor_memberships as membership
  where membership.editor_id = v_editor.id
  for update;

  -- content.editor_requests and content.editor_memberships both reference the
  -- editor ON DELETE RESTRICT, so they are cleared before the identity row.
  with removed as (
    delete from content.editor_requests as request
    where request.editor_id = v_editor.id
    returning 1
  )
  select count(*) into v_removed_requests from removed;

  delete from content.editor_memberships as membership
  where membership.editor_id = v_editor.id;

  delete from public.editors as editor
  where editor.id = v_editor.id;

  insert into content.audit_log (
    actor_user_id, action, entity_type, entity_id, metadata
  )
  values (
    v_actor_user_id,
    'editor.removed',
    'editor',
    v_editor.id,
    jsonb_build_object(
      'previous_revision', v_editor.revision,
      'name_ko', v_editor.name_ko,
      'name_en', v_editor.name_en,
      'had_workspace_account', v_membership_user_id is not null,
      'detached_exhibitions', v_detached_exhibitions,
      'detached_exhibition_versions', v_detached_versions,
      'removed_requests', v_removed_requests
    )
  );

  return jsonb_build_object(
    'editor_id', v_editor.id,
    'detached_exhibitions', v_detached_exhibitions,
    'detached_exhibition_versions', v_detached_versions,
    'removed_requests', v_removed_requests,
    'had_workspace_account', v_membership_user_id is not null
  );
end;
$function$;

revoke all on function
  content_private.admin_delete_editor_impl(text, integer)
  from public, anon, authenticated, service_role;

grant execute on function
  content_private.admin_delete_editor_impl(text, integer)
  to authenticated;

create or replace function public.admin_delete_editor(
  p_editor_id text,
  p_expected_revision integer
)
returns jsonb
language sql
volatile
security invoker
set search_path = ''
as $function$
  select content_private.admin_delete_editor_impl(
    p_editor_id, p_expected_revision
  );
$function$;

revoke all on function public.admin_delete_editor(text, integer)
  from public, anon, authenticated, service_role;

grant execute on function public.admin_delete_editor(text, integer)
  to authenticated;

commit;
