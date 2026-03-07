USE medicine;

-- 创建角色（如果不存在）
INSERT INTO system_role (name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT '医生', 'doctor', 30, 1, '', 0, 1, '医生工作台角色', 'script', NOW(), 'script', NOW(), b'0', 1
WHERE NOT EXISTS (SELECT 1 FROM system_role WHERE code = 'doctor' AND deleted = b'0');

INSERT INTO system_role (name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT '药师', 'pharmacist', 40, 1, '', 0, 1, '药师工作台角色', 'script', NOW(), 'script', NOW(), b'0', 1
WHERE NOT EXISTS (SELECT 1 FROM system_role WHERE code = 'pharmacist' AND deleted = b'0');

INSERT INTO system_role (name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT '患者', 'patient', 50, 1, '', 0, 1, '患者移动端角色', 'script', NOW(), 'script', NOW(), b'0', 1
WHERE NOT EXISTS (SELECT 1 FROM system_role WHERE code = 'patient' AND deleted = b'0');

SET @doctor_role_id = (SELECT id FROM system_role WHERE code = 'doctor' AND deleted = b'0' ORDER BY id LIMIT 1);
SET @pharmacist_role_id = (SELECT id FROM system_role WHERE code = 'pharmacist' AND deleted = b'0' ORDER BY id LIMIT 1);
SET @patient_role_id = (SELECT id FROM system_role WHERE code = 'patient' AND deleted = b'0' ORDER BY id LIMIT 1);

-- 清理多余的 patient 角色副本（只保留最早的一条）
DELETE FROM system_role WHERE code = 'patient' AND deleted = b'0' AND id <> @patient_role_id;

-- 重新绑定用户角色
DELETE FROM system_user_role WHERE user_id IN (
  SELECT id FROM system_users WHERE username IN ('doctor', 'pharmacist', 'patient')
);

INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT u.id, @doctor_role_id, 'script', NOW(), 'script', NOW(), b'0', 1
FROM system_users u WHERE u.username = 'doctor';

INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT u.id, @pharmacist_role_id, 'script', NOW(), 'script', NOW(), b'0', 1
FROM system_users u WHERE u.username = 'pharmacist';

INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT u.id, @patient_role_id, 'script', NOW(), 'script', NOW(), b'0', 1
FROM system_users u WHERE u.username = 'patient';

-- 返回角色以及绑定结果
SELECT '=== Role Definitions ===' AS info;
SELECT id, name, code, sort, status FROM system_role WHERE code IN ('doctor', 'pharmacist', 'patient') ORDER BY id;

SELECT '=== User Role Bindings ===' AS info;
SELECT u.username, r.code AS role_code
FROM system_users u
JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0'
JOIN system_role r ON r.id = ur.role_id AND r.deleted = b'0'
WHERE u.username IN ('doctor','pharmacist','patient')
ORDER BY u.username;
