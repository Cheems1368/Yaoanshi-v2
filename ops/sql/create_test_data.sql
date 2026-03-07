-- =============================================
-- 创建测试患者数据
-- 用于快速测试患者端功能
-- =============================================

USE medicine;

-- 1. 创建测试患者信息
INSERT INTO patients (
    user_id,
    name,
    phone,
    age,
    gender,
    id_card,
    id_type,
    ethnicity,
    height,
    weight,
    blood_type,
    allergies,
    avatar,
    emergency_contact_name,
    emergency_contact_phone,
    emergency_contact_relationship,
    is_real_name_verified,
    real_name_verified_time,
    address,
    insurance_card_number,
    allergy_history,
    medical_history,
    family_history,
    personal_history,
    patient_type,
    insurance,
    birthday,
    creator,
    create_time,
    updater,
    update_time,
    deleted,
    tenant_id
) VALUES (
    135, -- patient用户的ID
    '张阿姨',
    '15600000002',
    65,
    '女',
    '330106195801010001',
    '居民身份证',
    '汉族',
    160,
    55,
    'A',
    '青霉素过敏',
    '',
    '张小明',
    '15600000003',
    '儿子',
    TRUE,
    NOW(),
    '浙江省舟山市定海区人民路123号',
    'YB123456789',
    '青霉素过敏',
    '高血压病史10年',
    '父亲有糖尿病史',
    '无特殊病史',
    1, -- 门诊患者
    1, -- 医保
    '1958-01-01',
    'admin',
    NOW(),
    'admin',
    NOW(),
    b'0',
    1
) ON DUPLICATE KEY UPDATE
    name = '张阿姨',
    phone = '15600000002',
    age = 65,
    gender = '女',
    id_card = '330106195801010001',
    id_type = '居民身份证',
    ethnicity = '汉族',
    height = 160,
    weight = 55,
    blood_type = 'A',
    allergies = '青霉素过敏',
    emergency_contact_name = '张小明',
    emergency_contact_phone = '15600000003',
    emergency_contact_relationship = '儿子',
    is_real_name_verified = TRUE,
    real_name_verified_time = NOW(),
    address = '浙江省舟山市定海区人民路123号',
    insurance_card_number = 'YB123456789',
    allergy_history = '青霉素过敏',
    medical_history = '高血压病史10年',
    family_history = '父亲有糖尿病史',
    personal_history = '无特殊病史',
    patient_type = 1,
    insurance = 1,
    birthday = '1958-01-01',
    updater = 'admin',
    update_time = NOW();

-- 2. 创建测试工单
INSERT INTO help_orders (
    patient_id,
    assigned_pharmacist_id,
    assigned_doctor_id,
    title,
    description,
    intended_drugs,
    attachments,
    to_doctor,
    status,
    assigned_at,
    escalated_at,
    prescription_ready_at,
    done_at,
    rejected_at,
    creator,
    create_time,
    updater,
    update_time,
    deleted,
    tenant_id
) VALUES (
    LAST_INSERT_ID(), -- 使用刚创建的患者ID
    136, -- pharmacist用户的ID
    NULL,
    '高血压用药咨询',
    '张阿姨需要咨询高血压用药问题，目前服用降压药效果不佳，希望药师提供用药建议。',
    '["硝苯地平", "氨氯地平", "缬沙坦"]',
    '[]',
    FALSE,
    'SUBMITTED',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    'patient',
    NOW(),
    'patient',
    NOW(),
    b'0',
    1
);

-- 3. 验证测试数据
SELECT '=== 测试患者数据 ===' as info;
SELECT 
    p.id,
    p.name,
    p.phone,
    p.age,
    p.gender,
    p.patient_type,
    p.insurance,
    p.is_real_name_verified,
    p.create_time
FROM patients p
WHERE p.name = '张阿姨';

SELECT '=== 测试工单数据 ===' as info;
SELECT 
    ho.id,
    ho.title,
    ho.description,
    ho.status,
    ho.patient_id,
    ho.assigned_pharmacist_id,
    ho.create_time
FROM help_orders ho
WHERE ho.title = '高血压用药咨询';

-- 4. 显示登录测试信息
SELECT '=== 登录测试信息 ===' as info;
SELECT 
    username as '用户名',
    nickname as '昵称', 
    '123456' as '密码',
    CASE 
        WHEN username = 'admin' THEN 'admin'
        WHEN username = 'doctor' THEN 'doctor'
        WHEN username = 'patient' THEN 'patient'
        WHEN username = 'pharmacist' THEN 'pharmacist'
    END as '角色'
FROM system_users 
WHERE username IN ('admin', 'doctor', 'patient', 'pharmacist')
ORDER BY username;
