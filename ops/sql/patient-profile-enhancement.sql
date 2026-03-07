-- ===========================================
-- 患者资料管理增强SQL脚本
-- ===========================================
-- 用于实现红层必填的患者档案管理

-- ===========================================
-- 1. 扩展患者表字段
-- ===========================================

-- 添加患者基础信息字段
ALTER TABLE patients ADD COLUMN name VARCHAR(100) COMMENT '患者姓名';
ALTER TABLE patients ADD COLUMN gender TINYINT COMMENT '患者性别';
ALTER TABLE patients ADD COLUMN mobile VARCHAR(20) COMMENT '患者手机号';
ALTER TABLE patients ADD COLUMN id_card VARCHAR(18) COMMENT '患者身份证号';

-- 添加患者档案管理字段
ALTER TABLE patients ADD COLUMN audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态(0-待审核,1-已通过,2-已拒绝,3-需要修改)';
ALTER TABLE patients ADD COLUMN completeness_score DECIMAL(5,2) COMMENT '完整性得分(0-100)';
ALTER TABLE patients ADD COLUMN completeness_level VARCHAR(20) COMMENT '完整性等级(EXCELLENT,GOOD,FAIR,POOR)';

-- 添加紧急联系人字段
ALTER TABLE patients ADD COLUMN emergency_contact VARCHAR(100) COMMENT '紧急联系人';
ALTER TABLE patients ADD COLUMN emergency_phone VARCHAR(20) COMMENT '紧急联系电话';

-- ===========================================
-- 2. 创建患者档案审核记录表
-- ===========================================

CREATE TABLE IF NOT EXISTS patient_audit_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    auditor_id BIGINT NOT NULL COMMENT '审核人ID',
    audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态(0-待审核,1-已通过,2-已拒绝,3-需要修改)',
    completeness_score DECIMAL(5,2) COMMENT '完整性得分(0-100)',
    completeness_level VARCHAR(20) COMMENT '完整性等级(EXCELLENT,GOOD,FAIR,POOR)',
    audit_comment TEXT COMMENT '审核意见',
    submit_time DATETIME NOT NULL COMMENT '提交时间',
    audit_time DATETIME COMMENT '审核时间',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (id),
    KEY idx_patient_audit_patient_id (patient_id),
    KEY idx_patient_audit_auditor_id (auditor_id),
    KEY idx_patient_audit_status (audit_status),
    KEY idx_patient_audit_submit_time (submit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者档案审核记录表';

-- ===========================================
-- 3. 创建患者档案模板表
-- ===========================================

CREATE TABLE IF NOT EXISTS patient_profile_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    description TEXT COMMENT '模板描述',
    template_type TINYINT NOT NULL COMMENT '模板类型(0-住院患者,1-门诊患者,2-居家患者)',
    field_configs JSON COMMENT '字段配置(JSON格式)',
    is_default BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否默认模板',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (id),
    KEY idx_template_type (template_type),
    KEY idx_template_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者档案模板表';

-- ===========================================
-- 4. 插入默认患者档案模板
-- ===========================================

-- 住院患者模板
INSERT INTO patient_profile_template (template_name, description, template_type, field_configs, is_default, creator, create_time, updater, update_time, deleted, tenant_id) 
VALUES (
    '住院患者档案模板',
    '住院患者档案模板，包含完整的医疗信息',
    0,
    '{
        "fields": [
            {"fieldName": "name", "fieldLabel": "患者姓名", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "gender", "fieldLabel": "患者性别", "fieldType": "SELECT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "birthday", "fieldLabel": "患者生日", "fieldType": "DATE", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "mobile", "fieldLabel": "患者手机号", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "idCard", "fieldLabel": "患者身份证号", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "patientType", "fieldLabel": "患者类型", "fieldType": "SELECT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "address", "fieldLabel": "家庭住址", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "allergyHistory", "fieldLabel": "过敏史", "fieldType": "TEXTAREA", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "medicalHistory", "fieldLabel": "患病史", "fieldType": "TEXTAREA", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "familyHistory", "fieldLabel": "家族史", "fieldType": "TEXTAREA", "required": false, "fieldLevel": "IMPORTANT"},
            {"fieldName": "personalHistory", "fieldLabel": "个人史", "fieldType": "TEXTAREA", "required": false, "fieldLevel": "IMPORTANT"},
            {"fieldName": "insuranceCardNumber", "fieldLabel": "医保卡号", "fieldType": "TEXT", "required": false, "fieldLevel": "IMPORTANT"},
            {"fieldName": "insurance", "fieldLabel": "报销类型", "fieldType": "SELECT", "required": false, "fieldLevel": "IMPORTANT"},
            {"fieldName": "emergencyContact", "fieldLabel": "紧急联系人", "fieldType": "TEXT", "required": false, "fieldLevel": "IMPORTANT"},
            {"fieldName": "emergencyPhone", "fieldLabel": "紧急联系电话", "fieldType": "TEXT", "required": false, "fieldLevel": "IMPORTANT"}
        ]
    }',
    b'1',
    'system',
    NOW(),
    'system',
    NOW(),
    b'0',
    1
);

-- 门诊患者模板
INSERT INTO patient_profile_template (template_name, description, template_type, field_configs, is_default, creator, create_time, updater, update_time, deleted, tenant_id) 
VALUES (
    '门诊患者档案模板',
    '门诊患者档案模板，包含基础医疗信息',
    1,
    '{
        "fields": [
            {"fieldName": "name", "fieldLabel": "患者姓名", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "gender", "fieldLabel": "患者性别", "fieldType": "SELECT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "birthday", "fieldLabel": "患者生日", "fieldType": "DATE", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "mobile", "fieldLabel": "患者手机号", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "idCard", "fieldLabel": "患者身份证号", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "patientType", "fieldLabel": "患者类型", "fieldType": "SELECT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "address", "fieldLabel": "家庭住址", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "allergyHistory", "fieldLabel": "过敏史", "fieldType": "TEXTAREA", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "medicalHistory", "fieldLabel": "患病史", "fieldType": "TEXTAREA", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "familyHistory", "fieldLabel": "家族史", "fieldType": "TEXTAREA", "required": false, "fieldLevel": "OPTIONAL"},
            {"fieldName": "personalHistory", "fieldLabel": "个人史", "fieldType": "TEXTAREA", "required": false, "fieldLevel": "OPTIONAL"},
            {"fieldName": "insuranceCardNumber", "fieldLabel": "医保卡号", "fieldType": "TEXT", "required": false, "fieldLevel": "IMPORTANT"},
            {"fieldName": "insurance", "fieldLabel": "报销类型", "fieldType": "SELECT", "required": false, "fieldLevel": "IMPORTANT"}
        ]
    }',
    b'1',
    'system',
    NOW(),
    'system',
    NOW(),
    b'0',
    1
);

-- 居家患者模板
INSERT INTO patient_profile_template (template_name, description, template_type, field_configs, is_default, creator, create_time, updater, update_time, deleted, tenant_id) 
VALUES (
    '居家患者档案模板',
    '居家患者档案模板，包含基础信息和紧急联系人',
    2,
    '{
        "fields": [
            {"fieldName": "name", "fieldLabel": "患者姓名", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "gender", "fieldLabel": "患者性别", "fieldType": "SELECT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "birthday", "fieldLabel": "患者生日", "fieldType": "DATE", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "mobile", "fieldLabel": "患者手机号", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "idCard", "fieldLabel": "患者身份证号", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "patientType", "fieldLabel": "患者类型", "fieldType": "SELECT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "address", "fieldLabel": "家庭住址", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "allergyHistory", "fieldLabel": "过敏史", "fieldType": "TEXTAREA", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "medicalHistory", "fieldLabel": "患病史", "fieldType": "TEXTAREA", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "emergencyContact", "fieldLabel": "紧急联系人", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "emergencyPhone", "fieldLabel": "紧急联系电话", "fieldType": "TEXT", "required": true, "fieldLevel": "REQUIRED"},
            {"fieldName": "familyHistory", "fieldLabel": "家族史", "fieldType": "TEXTAREA", "required": false, "fieldLevel": "IMPORTANT"},
            {"fieldName": "personalHistory", "fieldLabel": "个人史", "fieldType": "TEXTAREA", "required": false, "fieldLevel": "IMPORTANT"}
        ]
    }',
    b'1',
    'system',
    NOW(),
    'system',
    NOW(),
    b'0',
    1
);

-- ===========================================
-- 5. 创建患者档案完整性检查视图
-- ===========================================

CREATE VIEW patient_completeness_view AS
SELECT 
    p.id,
    p.name,
    p.gender,
    p.birthday,
    p.mobile,
    p.id_card,
    p.patient_type,
    p.address,
    p.allergy_history,
    p.medical_history,
    p.family_history,
    p.personal_history,
    p.insurance_card_number,
    p.insurance,
    p.emergency_contact,
    p.emergency_phone,
    p.audit_status,
    p.completeness_score,
    p.completeness_level,
    CASE 
        WHEN p.name IS NULL OR p.name = '' THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.gender IS NULL THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.birthday IS NULL THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.mobile IS NULL OR p.mobile = '' THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.id_card IS NULL OR p.id_card = '' THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.patient_type IS NULL THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.address IS NULL OR p.address = '' THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.allergy_history IS NULL OR p.allergy_history = '' THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.medical_history IS NULL OR p.medical_history = '' THEN 1 ELSE 0 
    END AS missing_required_fields,
    CASE 
        WHEN p.family_history IS NULL OR p.family_history = '' THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.personal_history IS NULL OR p.personal_history = '' THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.insurance_card_number IS NULL OR p.insurance_card_number = '' THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.insurance IS NULL THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.emergency_contact IS NULL OR p.emergency_contact = '' THEN 1 ELSE 0 
    END + 
    CASE 
        WHEN p.emergency_phone IS NULL OR p.emergency_phone = '' THEN 1 ELSE 0 
    END AS missing_important_fields,
    p.create_time,
    p.update_time
FROM patients p
WHERE p.deleted = 0;

-- ===========================================
-- 6. 创建患者档案完整性检查存储过程
-- ===========================================

DELIMITER //

CREATE PROCEDURE UpdatePatientCompleteness(IN patient_id BIGINT)
BEGIN
    DECLARE missing_required INT DEFAULT 0;
    DECLARE missing_important INT DEFAULT 0;
    DECLARE total_fields INT DEFAULT 15;
    DECLARE completeness_score DECIMAL(5,2) DEFAULT 0;
    DECLARE completeness_level VARCHAR(20) DEFAULT 'POOR';
    
    -- 计算缺失的必填字段
    SELECT 
        CASE WHEN name IS NULL OR name = '' THEN 1 ELSE 0 END +
        CASE WHEN gender IS NULL THEN 1 ELSE 0 END +
        CASE WHEN birthday IS NULL THEN 1 ELSE 0 END +
        CASE WHEN mobile IS NULL OR mobile = '' THEN 1 ELSE 0 END +
        CASE WHEN id_card IS NULL OR id_card = '' THEN 1 ELSE 0 END +
        CASE WHEN patient_type IS NULL THEN 1 ELSE 0 END +
        CASE WHEN address IS NULL OR address = '' THEN 1 ELSE 0 END +
        CASE WHEN allergy_history IS NULL OR allergy_history = '' THEN 1 ELSE 0 END +
        CASE WHEN medical_history IS NULL OR medical_history = '' THEN 1 ELSE 0 END
    INTO missing_required
    FROM patients 
    WHERE id = patient_id;
    
    -- 计算缺失的重要字段
    SELECT 
        CASE WHEN family_history IS NULL OR family_history = '' THEN 1 ELSE 0 END +
        CASE WHEN personal_history IS NULL OR personal_history = '' THEN 1 ELSE 0 END +
        CASE WHEN insurance_card_number IS NULL OR insurance_card_number = '' THEN 1 ELSE 0 END +
        CASE WHEN insurance IS NULL THEN 1 ELSE 0 END +
        CASE WHEN emergency_contact IS NULL OR emergency_contact = '' THEN 1 ELSE 0 END +
        CASE WHEN emergency_phone IS NULL OR emergency_phone = '' THEN 1 ELSE 0 END
    INTO missing_important
    FROM patients 
    WHERE id = patient_id;
    
    -- 计算完整性得分
    SET completeness_score = (total_fields - missing_required - missing_important) / total_fields * 100;
    
    -- 确定完整性等级
    IF completeness_score >= 90 THEN
        SET completeness_level = 'EXCELLENT';
    ELSEIF completeness_score >= 80 THEN
        SET completeness_level = 'GOOD';
    ELSEIF completeness_score >= 70 THEN
        SET completeness_level = 'FAIR';
    ELSE
        SET completeness_level = 'POOR';
    END IF;
    
    -- 更新患者档案完整性信息
    UPDATE patients 
    SET 
        completeness_score = completeness_score,
        completeness_level = completeness_level,
        update_time = NOW()
    WHERE id = patient_id;
    
END //

DELIMITER ;

-- ===========================================
-- 7. 创建患者档案完整性检查触发器
-- ===========================================

DELIMITER //

CREATE TRIGGER tr_patients_completeness_after_update
AFTER UPDATE ON patients
FOR EACH ROW
BEGIN
    -- 调用存储过程更新完整性信息
    CALL UpdatePatientCompleteness(NEW.id);
END //

DELIMITER ;

-- ===========================================
-- 8. 验证数据完整性
-- ===========================================

-- 检查表结构
DESCRIBE patients;
DESCRIBE patient_audit_record;
DESCRIBE patient_profile_template;

-- 检查模板数据
SELECT template_name, template_type, is_default FROM patient_profile_template WHERE deleted = 0;

-- 检查视图
SELECT * FROM patient_completeness_view LIMIT 5;

-- ===========================================
-- 执行完成提示
-- ===========================================
-- 患者资料管理增强脚本执行完成！
-- 请验证以下内容：
-- 1. 患者表字段扩展完成
-- 2. 患者档案审核记录表创建完成
-- 3. 患者档案模板表创建完成
-- 4. 默认模板数据插入完成
-- 5. 患者档案完整性检查视图创建完成
-- 6. 患者档案完整性检查存储过程创建完成
-- 7. 患者档案完整性检查触发器创建完成
-- 
-- 重要提醒：
-- - 请验证所有表结构正确
-- - 请验证模板数据完整
-- - 请验证存储过程和触发器正常工作
-- - 请定期检查患者档案完整性
