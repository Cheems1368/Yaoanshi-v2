-- =============================================
-- 鐢ㄦ埛绠＄悊鍒濆鍖栬剼鏈?
-- 鑸熷北鍒嗗矝鍖荤枟绠＄悊绯荤粺
-- DEPRECATED: use D:/yaoanshi/ops/scripts/ensure-local-professional-test-accounts.ps1
-- for local professional test accounts. This old script only creates users and can
-- leave doctor/pharmacist role bindings inconsistent.

-- =============================================

USE medicine;

-- 娓呯悊鐜版湁鐨勬祴璇曡处鍙凤紙淇濈暀admin锛?
DELETE FROM system_users 
WHERE username IN ('doctor', 'patient', 'pharmacist', 'test', 'yuanma', 'admin107', 'admin108', 'admin109', 'admin110', 'newobject', 'aoteman');

-- 鎻掑叆鏍囧噯鐢ㄦ埛璐﹀彿
-- 娉ㄦ剰锛氭墍鏈夊瘑鐮侀兘鏄?'123456' 鐨凚Crypt鍔犲瘑
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
    creator, 
    create_time, 
    updater, 
    update_time, 
    deleted, 
    tenant_id
) VALUES 
-- 鍖荤敓璐﹀彿
('doctor', '$2a$10$So3U.Krla/NYewEq/MhN1uFOJsbb47OxXhGzHX2uiuFD.KSqwZGZW', '鍖荤敓', '鍖荤敓璐﹀彿', 100, '[1]', 'doctor@medicine.com', '15600000001', 1, '', 0, 'admin', NOW(), 'admin', NOW(), b'0', 1),

-- 鐥呬汉璐﹀彿  
('patient', '$2a$10$So3U.Krla/NYewEq/MhN1uFOJsbb47OxXhGzHX2uiuFD.KSqwZGZW', '鐥呬汉', '鐥呬汉璐﹀彿', 100, '[1]', 'patient@medicine.com', '15600000002', 1, '', 0, 'admin', NOW(), 'admin', NOW(), b'0', 1),

-- 鑽笀璐﹀彿
('pharmacist', '$2a$10$So3U.Krla/NYewEq/MhN1uFOJsbb47OxXhGzHX2uiuFD.KSqwZGZW', '鑽笀', '鑽笀璐﹀彿', 100, '[1]', 'pharmacist@medicine.com', '15600000003', 1, '', 0, 'admin', NOW(), 'admin', NOW(), b'0', 1);

-- 楠岃瘉鐢ㄦ埛鍒涘缓缁撴灉
SELECT 
    username,
    nickname,
    remark,
    status,
    LENGTH(password) as password_length,
    LEFT(password, 20) as password_start,
    create_time
FROM system_users 
WHERE username IN ('admin', 'doctor', 'patient', 'pharmacist')
ORDER BY username;

-- 鏄剧ず鐧诲綍娴嬭瘯淇℃伅
SELECT '=== User Login Test Info ===' as info;
SELECT 
    username as 'username',
    nickname as 'nickname', 
    '123456' as 'password',
    CASE 
        WHEN username = 'admin' THEN 'admin'
        WHEN username = 'doctor' THEN 'doctor'
        WHEN username = 'patient' THEN 'patient'
        WHEN username = 'pharmacist' THEN 'pharmacist'
    END as 'role'
FROM system_users 
WHERE username IN ('admin', 'doctor', 'patient', 'pharmacist')
ORDER BY username;
