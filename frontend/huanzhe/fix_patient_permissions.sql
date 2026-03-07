-- =============================================
-- 患者端权限修复脚本
-- 为患者账号添加角色和权限
-- =============================================

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

-- 3. 为患者角色添加菜单权限
-- 添加患者管理相关菜单权限
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
    'patient:list:query',
    'patient:list:create', 
    'patient:list:update',
    'patient:list:get',
    'help-orders:query',
    'help-orders:create',
    'prescription:query',
    'prescription:get'
) AND deleted = b'0'
ON DUPLICATE KEY UPDATE
    updater = 'admin',
    update_time = NOW();

-- 4. 为患者账号分配角色
-- 为patient用户分配患者角色
INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 
    u.id,
    @patient_role_id,
    'admin',
    NOW(),
    'admin',
    NOW(),
    b'0',
    1
FROM system_users u
WHERE u.username = 'patient' AND u.tenant_id = 1
ON DUPLICATE KEY UPDATE
    updater = 'admin',
    update_time = NOW();

-- 为zhangsan用户分配患者角色
INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 
    u.id,
    @patient_role_id,
    'admin',
    NOW(),
    'admin',
    NOW(),
    b'0',
    1
FROM system_users u
WHERE u.username = 'zhangsan' AND u.tenant_id = 1
ON DUPLICATE KEY UPDATE
    updater = 'admin',
    update_time = NOW();

-- 5. 验证结果
SELECT '=== 患者角色创建结果 ===' as info;
SELECT 
    id,
    name,
    code,
    status,
    remark
FROM system_role 
WHERE code = 'patient' AND tenant_id = 1;

SELECT '=== 患者用户角色分配结果 ===' as info;
SELECT 
    u.username,
    u.nickname,
    r.name as role_name,
    r.code as role_code
FROM system_users u
JOIN system_user_role ur ON u.id = ur.user_id
JOIN system_role r ON ur.role_id = r.id
WHERE u.username IN ('patient', 'zhangsan') 
  AND u.tenant_id = 1
  AND ur.deleted = b'0'
  AND r.deleted = b'0';

SELECT '=== 患者角色权限分配结果 ===' as info;
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

-- 6. 显示登录测试信息
SELECT '=== 患者端登录测试信息 ===' as info;
SELECT 
    username as '用户名',
    nickname as '昵称', 
    '123456' as '密码',
    CASE 
        WHEN EXISTS(SELECT 1 FROM system_user_role ur JOIN system_role r ON ur.role_id = r.id WHERE ur.user_id = u.id AND r.code = 'patient' AND ur.deleted = b'0') 
        THEN '有患者角色' 
        ELSE '无患者角色' 
    END as '角色状态'
FROM system_users u
WHERE username IN ('patient', 'zhangsan') 
  AND tenant_id = 1
ORDER BY username;
