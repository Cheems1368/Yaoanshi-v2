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
