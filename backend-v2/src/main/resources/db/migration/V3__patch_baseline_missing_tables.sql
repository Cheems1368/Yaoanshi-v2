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
