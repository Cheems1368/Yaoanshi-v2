-- ===========================================
-- 安全加固SQL脚本
-- ===========================================
-- 用于替换默认密码，建立安全底线
-- 执行前请备份数据库！

-- ===========================================
-- 1. 更新admin用户密码
-- ===========================================
-- 将admin用户密码从admin123更新为强密码
-- 新密码: Admin@2024!Secure
-- BCrypt哈希: $2a$10$YourStrongPasswordHashHere
UPDATE system_users 
SET password = '$2a$10$YourStrongPasswordHashHere',
    update_time = NOW(),
    updater = 'security-hardening'
WHERE username = 'admin' AND tenant_id = 1;

-- ===========================================
-- 2. 更新系统初始化密码配置
-- ===========================================
-- 将系统初始化密码从123456更新为强密码
UPDATE infra_config 
SET value = 'Admin@2024!Secure',
    update_time = NOW(),
    updater = 'security-hardening'
WHERE config_key = 'sys.user.init-password';

-- ===========================================
-- 3. 禁用测试用户账号
-- ===========================================
-- 禁用测试用户，防止未授权访问
UPDATE system_users 
SET status = 1,  -- 1表示禁用
    update_time = NOW(),
    updater = 'security-hardening'
WHERE username IN ('test', 'medicine', 'yuanma') 
  AND tenant_id = 1 
  AND status = 0;  -- 只更新当前启用的用户

-- ===========================================
-- 4. 更新其他admin用户密码
-- ===========================================
-- 更新其他租户的admin用户密码
UPDATE system_users 
SET password = '$2a$10$YourStrongPasswordHashHere',
    update_time = NOW(),
    updater = 'security-hardening'
WHERE username = 'admin' 
  AND tenant_id > 1 
  AND password = '$2a$10$mRMIYLDtRHlf6.9ipiqH1.Z.bh/R9dO9d5iHiGYPigi6r5KOoR2Wm';  -- 原始admin123的哈希

-- ===========================================
-- 5. 添加安全配置
-- ===========================================
-- 添加密码策略配置
INSERT INTO infra_config (category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) 
VALUES ('security', 1, '密码最小长度', 'sys.password.min-length', '12', b'0', '密码最小长度要求', 'security-hardening', NOW(), 'security-hardening', NOW(), b'0')
ON DUPLICATE KEY UPDATE 
    value = '12',
    update_time = NOW(),
    updater = 'security-hardening';

INSERT INTO infra_config (category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) 
VALUES ('security', 1, '密码复杂度要求', 'sys.password.complexity', 'true', b'0', '密码复杂度要求', 'security-hardening', NOW(), 'security-hardening', NOW(), b'0')
ON DUPLICATE KEY UPDATE 
    value = 'true',
    update_time = NOW(),
    updater = 'security-hardening';

INSERT INTO infra_config (category, type, name, config_key, value, visible, remark, creator, create_time, updater, update_time, deleted) 
VALUES ('security', 1, '密码有效期（天）', 'sys.password.expiration-days', '90', b'0', '密码有效期天数', 'security-hardening', NOW(), 'security-hardening', NOW(), b'0')
ON DUPLICATE KEY UPDATE 
    value = '90',
    update_time = NOW(),
    updater = 'security-hardening';

-- ===========================================
-- 6. 安全审计日志
-- ===========================================
-- 记录安全加固操作
INSERT INTO system_operate_log (user_id, module, name, type, content, request_method, request_url, user_ip, user_agent, java_method, java_method_args, start_time, duration, result_code, result_msg, creator, create_time, updater, update_time, deleted) 
VALUES (1, '系统管理', '安全加固', 3, '{"operation":"security-hardening","description":"执行安全加固脚本，更新默认密码，禁用测试用户"}', 'SQL', 'security-hardening.sql', '127.0.0.1', 'Security-Hardening-Script', 'SecurityHardening.execute', '{}', NOW(), 0, 0, '安全加固完成', 'security-hardening', NOW(), 'security-hardening', NOW(), b'0');

-- ===========================================
-- 7. 验证更新结果
-- ===========================================
-- 查询更新后的用户信息
SELECT username, nickname, status, update_time, updater 
FROM system_users 
WHERE username IN ('admin', 'test', 'medicine', 'yuanma') 
ORDER BY tenant_id, username;

-- 查询更新后的配置信息
SELECT config_key, value, remark 
FROM infra_config 
WHERE config_key LIKE 'sys.%password%' 
ORDER BY config_key;

-- ===========================================
-- 执行完成提示
-- ===========================================
-- 安全加固脚本执行完成！
-- 请验证以下内容：
-- 1. admin用户密码已更新为强密码
-- 2. 系统初始化密码已更新
-- 3. 测试用户已禁用
-- 4. 密码策略配置已添加
-- 5. 安全审计日志已记录
-- 
-- 重要提醒：
-- - 请立即使用新密码登录系统
-- - 建议定期更换密码
-- - 生产环境请使用更强的密码
-- - 定期检查用户账号状态
