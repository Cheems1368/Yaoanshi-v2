-- =============================================
-- 为患者角色添加菜单权限
-- =============================================

USE medicine;

-- 获取患者角色ID
SET @patient_role_id = (SELECT id FROM system_role WHERE code = 'patient' AND tenant_id = 1);

-- 为患者角色添加基础权限（查询、获取等）
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 
    @patient_role_id,
    id,
    'admin',
    NOW(),
    'admin', 
    NOW(),
    b'0',
    1
FROM system_menu 
WHERE permission IN (
    -- 基础查询权限
    'system:user:query',
    'system:user:get',
    'system:dept:query',
    'system:dict:query',
    'system:config:query',
    'infra:file:query',
    'infra:file:get',
    'infra:file:create',
    'infra:file:update',
    'infra:file:delete',
    -- 工单相关权限（如果存在）
    'help:order:query',
    'help:order:get',
    'help:order:create',
    'help:order:update',
    'help:order:delete',
    'help-order:query',
    'help-order:get',
    'help-order:create',
    'help-order:update',
    'help-order:delete',
    -- 处方相关权限（如果存在）
    'prescription:query',
    'prescription:get',
    'prescription:create',
    'prescription:update',
    'prescription:delete',
    -- 患者相关权限（如果存在）
    'patient:query',
    'patient:get',
    'patient:create',
    'patient:update',
    'patient:delete',
    'patient:list:query',
    'patient:list:get',
    'patient:list:create',
    'patient:list:update',
    'patient:list:delete',
    -- 用药相关权限（如果存在）
    'medication:query',
    'medication:get',
    'medication:create',
    'medication:update',
    'medication:delete',
    'medication:help-order:query',
    'medication:help-order:get',
    'medication:help-order:create',
    'medication:help-order:update',
    'medication:help-order:delete'
) AND deleted = b'0'
ON DUPLICATE KEY UPDATE
    updater = 'admin',
    update_time = NOW();

-- 验证结果
SELECT '=== Patient Role Permissions Added ===' as info;
SELECT 
    r.name as role_name,
    m.name as menu_name,
    m.permission
FROM system_role r
JOIN system_role_menu rm ON r.id = rm.role_id
JOIN system_menu m ON rm.menu_id = m.id
WHERE r.code = 'patient' 
  AND r.tenant_id = 1
  AND rm.deleted = b'0'
  AND m.deleted = b'0'
ORDER BY m.sort;
