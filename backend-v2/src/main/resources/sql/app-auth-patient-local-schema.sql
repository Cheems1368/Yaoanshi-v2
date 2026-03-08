CREATE TABLE IF NOT EXISTS app_account (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    mobile VARCHAR(20) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    display_name VARCHAR(64) NULL,
    last_login_ip VARCHAR(64) NULL,
    last_login_time DATETIME NULL,
    password_hash VARCHAR(255) NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_app_account_mobile_tenant (tenant_id, mobile)
);

CREATE TABLE IF NOT EXISTS app_account_device_session (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    app_account_id BIGINT NOT NULL,
    device_id VARCHAR(64) NOT NULL,
    device_name VARCHAR(128) NULL,
    platform VARCHAR(20) NOT NULL,
    access_token_jti VARCHAR(64) NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    refresh_token_expires_at DATETIME NOT NULL,
    last_seen_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    revoked_reason VARCHAR(64) NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_session_account_device (app_account_id, device_id),
    UNIQUE KEY uk_session_refresh_hash (refresh_token_hash),
    KEY idx_session_account (tenant_id, app_account_id),
    KEY idx_session_refresh_expire (refresh_token_expires_at)
);

CREATE TABLE IF NOT EXISTS otp_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    mobile VARCHAR(20) NOT NULL,
    scene VARCHAR(20) NOT NULL,
    provider VARCHAR(32) NULL,
    request_id VARCHAR(64) NULL,
    send_status VARCHAR(20) NOT NULL,
    verify_status VARCHAR(20) NULL,
    client_ip VARCHAR(64) NULL,
    error_code VARCHAR(64) NULL,
    error_message VARCHAR(255) NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_otp_mobile (tenant_id, mobile),
    KEY idx_otp_request_id (request_id)
);

CREATE TABLE IF NOT EXISTS patient (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    app_account_id BIGINT NULL,
    hospital_patient_id VARCHAR(64) NOT NULL,
    mobile VARCHAR(20) NOT NULL,
    name VARCHAR(64) NOT NULL,
    gender VARCHAR(16) NULL,
    birth_date DATE NULL,
    age INT NULL,
    id_card_masked VARCHAR(32) NULL,
    primary_disease_code VARCHAR(32) NULL,
    primary_disease_name VARCHAR(64) NULL,
    drug_allergy TEXT NULL,
    food_allergy TEXT NULL,
    medical_history_json JSON NULL,
    binding_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    bound_by_pharmacist_id BIGINT NULL,
    bound_at DATETIME NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_patient_app_account (app_account_id),
    UNIQUE KEY uk_patient_mobile_hospital (tenant_id, mobile, hospital_patient_id),
    KEY idx_patient_hospital (tenant_id, hospital_patient_id)
);

SET @patient_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient'
      AND column_name = 'birth_date'
);
SET @patient_column_sql = IF(@patient_column_exists = 0, 'ALTER TABLE patient ADD COLUMN birth_date DATE NULL', 'SELECT 1');
PREPARE patient_stmt FROM @patient_column_sql;
EXECUTE patient_stmt;
DEALLOCATE PREPARE patient_stmt;

SET @patient_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient'
      AND column_name = 'age'
);
SET @patient_column_sql = IF(@patient_column_exists = 0, 'ALTER TABLE patient ADD COLUMN age INT NULL', 'SELECT 1');
PREPARE patient_stmt FROM @patient_column_sql;
EXECUTE patient_stmt;
DEALLOCATE PREPARE patient_stmt;

SET @patient_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient'
      AND column_name = 'id_card_masked'
);
SET @patient_column_sql = IF(@patient_column_exists = 0, 'ALTER TABLE patient ADD COLUMN id_card_masked VARCHAR(32) NULL', 'SELECT 1');
PREPARE patient_stmt FROM @patient_column_sql;
EXECUTE patient_stmt;
DEALLOCATE PREPARE patient_stmt;

SET @patient_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient'
      AND column_name = 'primary_disease_code'
);
SET @patient_column_sql = IF(@patient_column_exists = 0, 'ALTER TABLE patient ADD COLUMN primary_disease_code VARCHAR(32) NULL', 'SELECT 1');
PREPARE patient_stmt FROM @patient_column_sql;
EXECUTE patient_stmt;
DEALLOCATE PREPARE patient_stmt;

SET @patient_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient'
      AND column_name = 'primary_disease_name'
);
SET @patient_column_sql = IF(@patient_column_exists = 0, 'ALTER TABLE patient ADD COLUMN primary_disease_name VARCHAR(64) NULL', 'SELECT 1');
PREPARE patient_stmt FROM @patient_column_sql;
EXECUTE patient_stmt;
DEALLOCATE PREPARE patient_stmt;

SET @patient_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient'
      AND column_name = 'drug_allergy'
);
SET @patient_column_sql = IF(@patient_column_exists = 0, 'ALTER TABLE patient ADD COLUMN drug_allergy TEXT NULL', 'SELECT 1');
PREPARE patient_stmt FROM @patient_column_sql;
EXECUTE patient_stmt;
DEALLOCATE PREPARE patient_stmt;

SET @patient_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient'
      AND column_name = 'food_allergy'
);
SET @patient_column_sql = IF(@patient_column_exists = 0, 'ALTER TABLE patient ADD COLUMN food_allergy TEXT NULL', 'SELECT 1');
PREPARE patient_stmt FROM @patient_column_sql;
EXECUTE patient_stmt;
DEALLOCATE PREPARE patient_stmt;

SET @patient_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient'
      AND column_name = 'medical_history_json'
);
SET @patient_column_sql = IF(@patient_column_exists = 0, 'ALTER TABLE patient ADD COLUMN medical_history_json JSON NULL', 'SELECT 1');
PREPARE patient_stmt FROM @patient_column_sql;
EXECUTE patient_stmt;
DEALLOCATE PREPARE patient_stmt;

SET @patient_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient'
      AND column_name = 'bound_by_pharmacist_id'
);
SET @patient_column_sql = IF(@patient_column_exists = 0, 'ALTER TABLE patient ADD COLUMN bound_by_pharmacist_id BIGINT NULL', 'SELECT 1');
PREPARE patient_stmt FROM @patient_column_sql;
EXECUTE patient_stmt;
DEALLOCATE PREPARE patient_stmt;

CREATE TABLE IF NOT EXISTS patient_family_binding (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    family_account_id BIGINT NOT NULL,
    relationship_type VARCHAR(20) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    authorized_at DATETIME NOT NULL,
    unbound_at DATETIME NULL,
    unbound_reason VARCHAR(255) NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_binding_patient (tenant_id, patient_id, status),
    KEY idx_binding_family (tenant_id, family_account_id)
);

CREATE TABLE IF NOT EXISTS patient_profile (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    contact_mobile VARCHAR(20) NULL,
    id_type VARCHAR(32) NULL,
    ethnicity VARCHAR(32) NULL,
    height_cm INT NULL,
    weight_kg INT NULL,
    blood_type VARCHAR(16) NULL,
    allergies TEXT NULL,
    avatar_url VARCHAR(512) NULL,
    address VARCHAR(255) NULL,
    patient_type INT NULL,
    insurance INT NULL,
    emergency_contact_name VARCHAR(64) NULL,
    emergency_contact_phone VARCHAR(20) NULL,
    emergency_contact_relationship VARCHAR(32) NULL,
    identity_material_hash CHAR(64) NULL,
    real_name_verified BIT(1) NOT NULL DEFAULT b'0',
    real_name_verified_time DATETIME NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_patient_profile_patient (tenant_id, patient_id)
);

SET @patient_profile_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'patient_profile'
      AND column_name = 'identity_material_hash'
);
SET @patient_profile_column_sql = IF(
    @patient_profile_column_exists = 0,
    'ALTER TABLE patient_profile ADD COLUMN identity_material_hash CHAR(64) NULL AFTER emergency_contact_relationship',
    'SELECT 1'
);
PREPARE patient_profile_stmt FROM @patient_profile_column_sql;
EXECUTE patient_profile_stmt;
DEALLOCATE PREPARE patient_profile_stmt;

CREATE TABLE IF NOT EXISTS patient_health_profile (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    layer1_allergy TEXT NULL,
    layer1_diagnosis VARCHAR(255) NULL,
    layer2_medical_history TEXT NULL,
    layer2_smoking_drinking TEXT NULL,
    layer2_liver_kidney TEXT NULL,
    layer2_current_medication TEXT NULL,
    layer3_detailed_symptoms TEXT NULL,
    layer3_family_history TEXT NULL,
    layer3_surgery_history TEXT NULL,
    layer3_social_history TEXT NULL,
    submitted_at DATETIME NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_patient_health_profile_patient (tenant_id, patient_id)
);

CREATE TABLE IF NOT EXISTS pharmacist_profile (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    system_user_id BIGINT NOT NULL,
    license_no VARCHAR(64) NULL,
    real_name VARCHAR(64) NOT NULL,
    mobile VARCHAR(20) NULL,
    service_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    work_hours_json JSON NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_pharmacist_system_user (system_user_id),
    KEY idx_pharmacist_service_status (tenant_id, service_status)
);

CREATE TABLE IF NOT EXISTS doctor_profile (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    system_user_id BIGINT NOT NULL,
    specialty_code VARCHAR(32) NULL,
    title_name VARCHAR(64) NULL,
    real_name VARCHAR(64) NOT NULL,
    mobile VARCHAR(20) NULL,
    service_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    work_hours_json JSON NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_doctor_system_user (system_user_id),
    KEY idx_doctor_service_status (tenant_id, service_status)
);

CREATE TABLE IF NOT EXISTS abcd2_assessment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    source_ref_id BIGINT NULL,
    raw_input_json JSON NOT NULL,
    score INT NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    calculated_at DATETIME NOT NULL,
    calculated_by_id BIGINT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_abcd2_patient (tenant_id, patient_id, calculated_at)
);

CREATE TABLE IF NOT EXISTS file_asset (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bucket VARCHAR(64) NOT NULL,
    object_key VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    sha256 CHAR(64) NULL,
    storage_provider VARCHAR(32) NOT NULL,
    storage_region VARCHAR(64) NULL,
    public_url VARCHAR(512) NULL,
    uploaded_by_type VARCHAR(20) NOT NULL,
    uploaded_by_id BIGINT NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    scan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_file_object_key (object_key),
    KEY idx_file_business_type (tenant_id, business_type),
    KEY idx_file_uploader (tenant_id, uploaded_by_type, uploaded_by_id)
);

CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    ocr_confidence DECIMAL(5,2) NULL,
    uploaded_file_asset_id BIGINT NOT NULL,
    submitted_by_type VARCHAR(20) NOT NULL,
    submitted_by_id BIGINT NOT NULL,
    reviewed_by_pharmacist_id BIGINT NULL,
    reviewed_at DATETIME NULL,
    reject_reason VARCHAR(255) NULL,
    generated_medication_plan_id BIGINT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_prescription_patient (tenant_id, patient_id),
    KEY idx_prescription_status (tenant_id, status)
);

CREATE TABLE IF NOT EXISTS prescription_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    drug_name VARCHAR(128) NOT NULL,
    drug_spec VARCHAR(128) NULL,
    dosage VARCHAR(64) NULL,
    frequency VARCHAR(64) NULL,
    route VARCHAR(64) NULL,
    duration_days INT NULL,
    normalized_json JSON NULL,
    source VARCHAR(20) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_prescription_item_prescription (tenant_id, prescription_id)
);

CREATE TABLE IF NOT EXISTS prescription_attachment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    file_asset_id BIGINT NOT NULL,
    attachment_type VARCHAR(20) NOT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_prescription_attachment_prescription (tenant_id, prescription_id)
);

CREATE TABLE IF NOT EXISTS prescription_ocr_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    provider VARCHAR(64) NOT NULL,
    request_payload_json JSON NULL,
    response_payload_json JSON NULL,
    confidence DECIMAL(5,2) NULL,
    status VARCHAR(20) NOT NULL,
    manual_fallback_used BIT(1) NOT NULL DEFAULT b'0',
    executed_at DATETIME NOT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_ocr_prescription (tenant_id, prescription_id, executed_at)
);

CREATE TABLE IF NOT EXISTS ddi_rule (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL,
    drug_a_name VARCHAR(128) NOT NULL,
    drug_b_name VARCHAR(128) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    message VARCHAR(255) NOT NULL,
    recommended_action VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    maintained_by_pharmacist_id BIGINT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_ddi_rule_code (rule_code),
    KEY idx_ddi_severity (tenant_id, severity)
);

CREATE TABLE IF NOT EXISTS drug_contraindication (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL,
    drug_name VARCHAR(128) NOT NULL,
    condition_type VARCHAR(32) NOT NULL,
    condition_value VARCHAR(128) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    message VARCHAR(255) NOT NULL,
    recommended_action VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    maintained_by_pharmacist_id BIGINT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_contraindication_rule_code (rule_code),
    KEY idx_contraindication_drug (tenant_id, drug_name),
    KEY idx_contraindication_condition (tenant_id, condition_type, condition_value),
    KEY idx_contraindication_severity (tenant_id, severity)
);

CREATE TABLE IF NOT EXISTS ddi_screening_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    prescription_id BIGINT NULL,
    medication_plan_id BIGINT NULL,
    matched_rule_id BIGINT NOT NULL,
    severity VARCHAR(16) NOT NULL,
    confirm_status VARCHAR(20) NOT NULL,
    confirmed_by_pharmacist_id BIGINT NULL,
    confirmed_at DATETIME NULL,
    confirmation_note VARCHAR(255) NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_ddi_log_prescription (tenant_id, prescription_id),
    KEY idx_ddi_log_rule (tenant_id, matched_rule_id)
);

CREATE TABLE IF NOT EXISTS medication_plan (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    plan_name VARCHAR(128) NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    source_type VARCHAR(20) NOT NULL,
    source_prescription_id BIGINT NULL,
    effective_from DATETIME NOT NULL,
    effective_to DATETIME NULL,
    notes TEXT NULL,
    created_by_pharmacist_id BIGINT NOT NULL,
    confirmed_at DATETIME NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_plan_patient (tenant_id, patient_id),
    KEY idx_plan_status (tenant_id, status)
);

CREATE TABLE IF NOT EXISTS medication_plan_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    drug_name VARCHAR(128) NOT NULL,
    drug_spec VARCHAR(128) NULL,
    dosage VARCHAR(64) NOT NULL,
    frequency_code VARCHAR(32) NOT NULL,
    time_points_json JSON NOT NULL,
    meal_relation VARCHAR(20) NULL,
    route VARCHAR(64) NULL,
    instructions TEXT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_plan_item_plan (tenant_id, plan_id)
);

CREATE TABLE IF NOT EXISTS medication_dose_task (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    plan_item_id BIGINT NOT NULL,
    task_date DATE NOT NULL,
    scheduled_at DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_effective_checkin_id BIGINT NULL,
    locked_after DATETIME NOT NULL,
    drug_name VARCHAR(128) NOT NULL,
    dosage VARCHAR(64) NOT NULL,
    frequency_code VARCHAR(32) NOT NULL,
    route VARCHAR(64) NULL,
    meal_relation VARCHAR(20) NULL,
    instructions TEXT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_task_patient_date (tenant_id, patient_id, task_date),
    KEY idx_task_plan (tenant_id, plan_id)
);

CREATE TABLE IF NOT EXISTS medication_checkin_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dose_task_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    operator_type VARCHAR(20) NOT NULL,
    operator_id BIGINT NOT NULL,
    result VARCHAR(20) NOT NULL,
    actual_taken_at DATETIME NULL,
    note VARCHAR(255) NULL,
    skip_reason VARCHAR(255) NULL,
    is_correction BIT(1) NOT NULL DEFAULT b'0',
    supersedes_record_id BIGINT NULL,
    is_effective BIT(1) NOT NULL DEFAULT b'1',
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_checkin_task (tenant_id, dose_task_id),
    KEY idx_checkin_patient (tenant_id, patient_id)
);

CREATE TABLE IF NOT EXISTS medication_plan_event (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    content TEXT NULL,
    operator_type VARCHAR(20) NOT NULL,
    operator_id BIGINT NOT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_plan_event_plan (tenant_id, plan_id)
);

CREATE TABLE IF NOT EXISTS help_order (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    patient_id BIGINT NOT NULL,
    submitted_by_type VARCHAR(20) NOT NULL,
    submitted_by_id BIGINT NOT NULL,
    category VARCHAR(32) NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    requires_offline_emergency BIT(1) NOT NULL DEFAULT b'0',
    current_handler_type VARCHAR(20) NULL,
    current_handler_id BIGINT NULL,
    responsible_pharmacist_id BIGINT NULL,
    assigned_doctor_id BIGINT NULL,
    first_responded_at DATETIME NULL,
    closed_at DATETIME NULL,
    reject_reason VARCHAR(255) NULL,
    service_notice VARCHAR(255) NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_help_order_no (order_no),
    KEY idx_help_order_patient (tenant_id, patient_id),
    KEY idx_help_order_status (tenant_id, status),
    KEY idx_help_order_responsible_pharmacist (tenant_id, responsible_pharmacist_id),
    KEY idx_help_order_assigned_doctor (tenant_id, assigned_doctor_id)
);

CREATE TABLE IF NOT EXISTS help_order_event (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    help_order_id BIGINT NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    from_status VARCHAR(32) NULL,
    to_status VARCHAR(32) NULL,
    operator_type VARCHAR(20) NOT NULL,
    operator_id BIGINT NOT NULL,
    content TEXT NULL,
    attachment_file_asset_id BIGINT NULL,
    metadata_json JSON NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_help_order_event_order (tenant_id, help_order_id)
);

CREATE TABLE IF NOT EXISTS notification_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    business_id BIGINT NULL,
    scheduled_at DATETIME NULL,
    sent_at DATETIME NULL,
    delivery_status VARCHAR(20) NOT NULL,
    failure_code VARCHAR(64) NULL,
    failure_reason VARCHAR(255) NULL,
    payload_json JSON NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_notification_target (tenant_id, target_type, target_id),
    KEY idx_notification_business_type (tenant_id, business_type)
);

CREATE TABLE IF NOT EXISTS knowledge_assessment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    phase VARCHAR(20) NOT NULL,
    question_set_version VARCHAR(32) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    max_score DECIMAL(5,2) NOT NULL,
    submitted_by_type VARCHAR(20) NOT NULL,
    submitted_by_id BIGINT NOT NULL,
    submitted_at DATETIME NOT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_knowledge_patient (tenant_id, patient_id)
);

CREATE TABLE IF NOT EXISTS knowledge_assessment_answer (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    question_code VARCHAR(64) NOT NULL,
    question_text_snapshot TEXT NOT NULL,
    selected_option VARCHAR(32) NOT NULL,
    correct_option VARCHAR(32) NULL,
    is_correct BIT(1) NOT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    KEY idx_knowledge_answer_assessment (tenant_id, assessment_id)
);

CREATE TABLE IF NOT EXISTS satisfaction_survey (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    help_order_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    comment VARCHAR(255) NULL,
    submitted_by_type VARCHAR(20) NOT NULL,
    submitted_by_id BIGINT NOT NULL,
    submitted_at DATETIME NOT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_satisfaction_help_order (tenant_id, help_order_id),
    KEY idx_satisfaction_patient (tenant_id, patient_id)
);

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status,
    maintained_by_pharmacist_id, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT 'DDI-HIGH-ASPIRIN-WARFARIN', 'Aspirin', 'Warfarin', 'HIGH',
       'Aspirin and Warfarin may increase bleeding risk.',
       'Review anticoagulation strategy and confirm before continuing.',
       'ACTIVE', NULL, 'schema-bootstrap', UTC_TIMESTAMP(), 'schema-bootstrap', UTC_TIMESTAMP(), b'0', 1
WHERE NOT EXISTS (
    SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-ASPIRIN-WARFARIN'
);

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status,
    maintained_by_pharmacist_id, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT 'DDI-HIGH-CLOPIDOGREL-OMEPRAZOLE', 'Clopidogrel', 'Omeprazole', 'HIGH',
       'Clopidogrel and Omeprazole may reduce antiplatelet effect.',
       'Confirm indication or consider alternative gastric protection.',
       'ACTIVE', NULL, 'schema-bootstrap', UTC_TIMESTAMP(), 'schema-bootstrap', UTC_TIMESTAMP(), b'0', 1
WHERE NOT EXISTS (
    SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-CLOPIDOGREL-OMEPRAZOLE'
);
