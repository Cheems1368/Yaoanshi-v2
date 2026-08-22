CREATE TABLE IF NOT EXISTS ai_consent_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    policy_version VARCHAR(64) NOT NULL,
    provider_code VARCHAR(64) NOT NULL,
    provider_display_name VARCHAR(128) NOT NULL,
    statement_hash CHAR(64) NOT NULL,
    granted_at DATETIME NOT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_ai_consent_policy (
        tenant_id, account_id, policy_version, provider_code, statement_hash, deleted
    ),
    KEY idx_ai_consent_account (tenant_id, account_id, granted_at)
);

CREATE TABLE IF NOT EXISTS ai_request_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    provider_code VARCHAR(64) NOT NULL,
    provider_host VARCHAR(255) NOT NULL,
    model VARCHAR(128) NOT NULL,
    request_class VARCHAR(64) NOT NULL,
    original_message_count INT NOT NULL,
    transmitted_message_count INT NOT NULL,
    original_char_count INT NOT NULL,
    transmitted_char_count INT NOT NULL,
    redaction_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    error_code VARCHAR(64) NULL,
    latency_ms BIGINT NULL,
    expires_at DATETIME NOT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_ai_audit_request (tenant_id, request_id, deleted),
    KEY idx_ai_audit_account (tenant_id, account_id, create_time),
    KEY idx_ai_audit_expiry (expires_at, id)
);
