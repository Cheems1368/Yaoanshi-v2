ALTER TABLE prescription_ocr_record
    ADD COLUMN extracted_text TEXT NULL AFTER response_payload_json,
    ADD COLUMN failure_reason VARCHAR(255) NULL AFTER extracted_text,
    ADD COLUMN structured_items_json JSON NULL AFTER failure_reason;

CREATE TABLE IF NOT EXISTS education_content_card (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    content_code VARCHAR(64) NOT NULL,
    title VARCHAR(160) NOT NULL,
    summary VARCHAR(500) NULL,
    body TEXT NOT NULL,
    category VARCHAR(64) NULL,
    category_name VARCHAR(120) NULL,
    diagnosis_tags VARCHAR(500) NULL,
    drug_tags VARCHAR(500) NULL,
    population_tags VARCHAR(500) NULL,
    source VARCHAR(255) NULL,
    review_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    reviewed_by VARCHAR(128) NULL,
    reviewed_at DATETIME NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_education_content_code_tenant (tenant_id, content_code),
    KEY idx_education_review_status (tenant_id, review_status, deleted),
    KEY idx_education_category (tenant_id, category, deleted)
);
