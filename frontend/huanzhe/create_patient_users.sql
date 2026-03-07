-- =============================================
-- 创建患者用户和分配角色脚本
-- =============================================

USE medicine;

-- 1. 创建patient用户
INSERT INTO system_users (
    username,
    password,
    nickname,
    remark,
    dept_id,
    post_ids,
    email,
    mobile,
    sex,
    avatar,
    status,
    login_ip,
    login_date,
    creator,
    create_time,
    updater,
    update_time,
    deleted,
    tenant_id
) VALUES (
    'patient',
    0x24326124313024734e2e497a59614f586e78795767394b326e644c41656165467639414751444661746e6f667135354f454f566f5077417873585043, -- 123456
    'Patient User',
    'Patient user for patient application',
    1,
    '',
    'patient@example.com',
    '13800138000',
    1,
    '',
    0,
    '',
    NULL,
    'admin',
    NOW(),
    'admin',
    NOW(),
    b'0',
    1
) ON DUPLICATE KEY UPDATE
    nickname = 'Patient User',
    remark = 'Patient user for patient application',
    updater = 'admin',
    update_time = NOW();

-- 2. 创建zhangsan用户
INSERT INTO system_users (
    username,
    password,
    nickname,
    remark,
    dept_id,
    post_ids,
    email,
    mobile,
    sex,
    avatar,
    status,
    login_ip,
    login_date,
    creator,
    create_time,
    updater,
    update_time,
    deleted,
    tenant_id
) VALUES (
    'zhangsan',
    0x24326124313024734e2e497a59614f586e78795767394b326e644c41656165467639414751444661746e6f667135354f454f566f5077417873585043, -- 123456
    'Zhang San',
    'Test patient user',
    1,
    '',
    'zhangsan@example.com',
    '13800138001',
    1,
    '',
    0,
    '',
    NULL,
    'admin',
    NOW(),
    'admin',
    NOW(),
    b'0',
    1
) ON DUPLICATE KEY UPDATE
    nickname = 'Zhang San',
    remark = 'Test patient user',
    updater = 'admin',
    update_time = NOW();

-- 3. 获取患者角色ID
SET @patient_role_id = (SELECT id FROM system_role WHERE code = 'patient' AND tenant_id = 1);

-- 4. 为patient用户分配患者角�?
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

-- 5. 为zhangsan用户分配患者角�?
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

-- 6. 验证结果
SELECT '=== Patient Users Created ===' as info;
SELECT 
    id,
    username,
    nickname,
    status,
    remark
FROM system_users 
WHERE username IN ('patient', 'zhangsan') 
  AND tenant_id = 1;

SELECT '=== Patient User Role Assignment ===' as info;
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

SELECT '=== Login Test Info ===' as info;
SELECT 
    username as 'Username',
    nickname as 'Nickname', 
    '123456' as 'Password',
    CASE 
        WHEN EXISTS(SELECT 1 FROM system_user_role ur JOIN system_role r ON ur.role_id = r.id WHERE ur.user_id = u.id AND r.code = 'patient' AND ur.deleted = b'0') 
        THEN 'Has Patient Role' 
        ELSE 'No Patient Role' 
    END as 'Role Status'
FROM system_users u
WHERE username IN ('patient', 'zhangsan') 
  AND tenant_id = 1
ORDER BY username;

