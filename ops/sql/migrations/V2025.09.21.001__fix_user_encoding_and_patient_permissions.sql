-- =============================================
-- 数据库迁移脚本 V2025.09.21.001
-- 修复用户名称乱码和患者权限问题
-- =============================================

-- 版本信息
-- 版本号: V2025.09.21.001
-- 描述: 修复用户名称乱码和患者权限问题
-- 创建时间: 2025-09-21
-- 作者: AI Assistant

USE medicine;

-- 1. 修复用户名称乱码问题
-- 更新系统用户昵称，确保正确显示中文
UPDATE system_users 
SET nickname = '医生'
WHERE username = 'doctor' AND tenant_id = 1;

UPDATE system_users 
SET nickname = '患者'
WHERE username = 'patient' AND tenant_id = 1;

UPDATE system_users 
SET nickname = '药师'
WHERE username = 'pharmacist' AND tenant_id = 1;

UPDATE system_users 
SET remark = '医生账号'
WHERE username = 'doctor' AND tenant_id = 1;

UPDATE system_users 
SET remark = '患者账号'
WHERE username = 'patient' AND tenant_id = 1;

UPDATE system_users 
SET remark = '药师账号'
WHERE username = 'pharmacist' AND tenant_id = 1;

-- 2. 确保患者角色存在
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
    '患者', 
    'patient', 
    50, 
    1, 
    '', 
    0, 
    1, 
    '患者移动端角色', 
    'migration', 
    NOW(), 
    'migration', 
    NOW(), 
    b'0',
    1
) ON DUPLICATE KEY UPDATE 
    name = '患者',
    remark = '患者移动端角色',
    updater = 'migration',
    update_time = NOW(),
    tenant_id = 1;

-- 3. 确保患者菜单权限存在
INSERT INTO system_menu (
    name, 
    permission, 
    type, 
    sort, 
    parent_id, 
    status, 
    visible, 
    keep_alive, 
    always_show, 
    creator, 
    create_time, 
    updater, 
    update_time, 
    deleted,
    tenant_id
) VALUES 
('患者信息-查询权限', 'patient:list:query', 3, 1, 0, 0, TRUE, TRUE, TRUE, 'migration', NOW(), 'migration', NOW(), FALSE, 1),
('患者信息-创建权限', 'patient:list:create', 3, 2, 0, 0, TRUE, TRUE, TRUE, 'migration', NOW(), 'migration', NOW(), FALSE, 1),
('患者信息-获取权限', 'patient:list:get', 3, 3, 0, 0, TRUE, TRUE, TRUE, 'migration', NOW(), 'migration', NOW(), FALSE, 1),
('患者信息-更新权限', 'patient:list:update', 3, 4, 0, 0, TRUE, TRUE, TRUE, 'migration', NOW(), 'migration', NOW(), FALSE, 1),
('工单-查询权限', 'help-order:query', 3, 5, 0, 0, TRUE, TRUE, TRUE, 'migration', NOW(), 'migration', NOW(), FALSE, 1),
('工单-创建权限', 'help-order:create', 3, 6, 0, 0, TRUE, TRUE, TRUE, 'migration', NOW(), 'migration', NOW(), FALSE, 1),
('工单-获取权限', 'help-order:get', 3, 7, 0, 0, TRUE, TRUE, TRUE, 'migration', NOW(), 'migration', NOW(), FALSE, 1),
('处方-查询权限', 'prescription:query', 3, 8, 0, 0, TRUE, TRUE, TRUE, 'migration', NOW(), 'migration', NOW(), FALSE, 1),
('处方-获取权限', 'prescription:get', 3, 9, 0, 0, TRUE, TRUE, TRUE, 'migration', NOW(), 'migration', NOW(), FALSE, 1)
ON DUPLICATE KEY UPDATE 
    updater = 'migration',
    update_time = NOW(),
    tenant_id = 1;

-- 4. 为患者角色分配权限
SET @patient_role_id = (SELECT id FROM system_role WHERE code = 'patient' AND tenant_id = 1 LIMIT 1);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 
    @patient_role_id,
    id,
    'migration',
    NOW(),
    'migration', 
    NOW(),
    FALSE,
    tenant_id
FROM system_menu 
WHERE tenant_id = 1 AND deleted = FALSE AND permission IN (
    'patient:list:query',
    'patient:list:create', 
    'patient:list:get',
    'patient:list:update',
    'help-order:query',
    'help-order:create',
    'help-order:get',
    'prescription:query',
    'prescription:get',
    'system:user:query',
    'system:user:get',
    'infra:file:query',
    'infra:file:get',
    'infra:file:create',
    'infra:file:update'
)
ON DUPLICATE KEY UPDATE
    updater = 'migration',
    update_time = NOW(),
    tenant_id = VALUES(tenant_id);

-- 5. 确保患者用户绑定到患者角色
INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 
    u.id,
    @patient_role_id,
    'migration',
    NOW(),
    'migration',
    NOW(),
    FALSE,
    1
FROM system_users u
WHERE u.username = 'patient' AND u.tenant_id = 1
ON DUPLICATE KEY UPDATE
    updater = 'migration',
    update_time = NOW(),
    tenant_id = VALUES(tenant_id);

-- 6. 创建迁移记录表（如果不存在）
CREATE TABLE IF NOT EXISTS schema_migrations (
    version VARCHAR(50) PRIMARY KEY,
    description TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time_ms INT,
    success BOOLEAN DEFAULT TRUE,
    error_message TEXT
);

-- 7. 记录迁移执行
INSERT INTO schema_migrations (version, description, executed_at, success) 
VALUES ('V2025.09.21.001', '修复用户名称乱码和患者权限问题', NOW(), TRUE)
ON DUPLICATE KEY UPDATE 
    executed_at = NOW(),
    success = TRUE;

-- 8. 验证迁移结果
SELECT '=== 迁移验证结果 ===' as info;

-- 验证用户名称修复
SELECT '用户名称修复验证' as check_type;
SELECT 
    id,
    username,
    nickname,
    remark
FROM system_users 
WHERE username IN ('doctor', 'patient', 'pharmacist') AND tenant_id = 1
ORDER BY username;

-- 验证患者权限配置
SELECT '患者权限配置验证' as check_type;
SELECT 
    r.name as role_name,
    m.name as menu_name,
    m.permission
FROM system_role r
JOIN system_role_menu rm ON r.id = rm.role_id
JOIN system_menu m ON rm.menu_id = m.id
WHERE r.code = 'patient' AND r.deleted = FALSE AND m.deleted = FALSE AND r.tenant_id = 1 AND m.tenant_id = 1
ORDER BY m.permission;

-- 验证用户角色绑定
SELECT '用户角色绑定验证' as check_type;
SELECT 
    u.username,
    u.nickname,
    r.name as role_name,
    r.code as role_code
FROM system_users u 
LEFT JOIN system_user_role ur ON u.id = ur.user_id AND ur.deleted = FALSE AND ur.tenant_id = u.tenant_id
LEFT JOIN system_role r ON ur.role_id = r.id AND r.deleted = FALSE
WHERE u.username IN ('doctor', 'patient', 'pharmacist') AND u.tenant_id = 1
ORDER BY u.username;

SELECT '=== 迁移完成 ===' as info;
