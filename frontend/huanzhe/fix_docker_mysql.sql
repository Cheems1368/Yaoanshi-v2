-- 修复Docker MySQL中的患者权限
USE medicine;

-- 1. 创建患者角色
INSERT INTO system_role (
    name, 
    code, 
    sort, 
    data_scope, 
    data_scope_dept_ids, 
    status, 
    type, 
    remark, 
    creator, 
    create_time, 
    updater, 
    update_time, 
    deleted, 
    tenant_id
) VALUES (
    'Patient', 
    'patient', 
    100, 
    1, 
    '', 
    0, 
    1, 
    'Patient role for patient application', 
    'admin', 
    NOW(), 
    'admin', 
    NOW(), 
    b'0', 
    1
) ON DUPLICATE KEY UPDATE 
    name = 'Patient',
    remark = 'Patient role for patient application',
    updater = 'admin',
    update_time = NOW();

-- 2. 获取患者角色ID
SET @patient_role_id = (SELECT id FROM system_role WHERE code = 'patient' AND tenant_id = 1);

-- 3. 为patient用户分配患者角色
INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 
    139,
    @patient_role_id,
    'admin',
    NOW(),
    'admin',
    NOW(),
    b'0',
    1
ON DUPLICATE KEY UPDATE
    updater = 'admin',
    update_time = NOW();

-- 4. 为患者角色添加基础权限
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
    'system:user:query',
    'system:user:get',
    'system:dept:query',
    'system:dict:query',
    'infra:file:query',
    'infra:file:get',
    'infra:file:create',
    'infra:file:update',
    'infra:file:delete'
) AND deleted = b'0'
ON DUPLICATE KEY UPDATE
    updater = 'admin',
    update_time = NOW();

-- 5. 验证结果
SELECT '=== Docker MySQL Patient Role Created ===' as info;
SELECT 
    id,
    name,
    code,
    status,
    remark
FROM system_role 
WHERE code = 'patient' AND tenant_id = 1;

SELECT '=== Docker MySQL Patient User Role Assignment ===' as info;
SELECT 
    u.username,
    u.nickname,
    r.name as role_name,
    r.code as role_code
FROM system_users u
JOIN system_user_role ur ON u.id = ur.user_id
JOIN system_role r ON ur.role_id = r.id
WHERE u.username = 'patient' 
  AND u.tenant_id = 1
  AND ur.deleted = b'0'
  AND r.deleted = b'0';
