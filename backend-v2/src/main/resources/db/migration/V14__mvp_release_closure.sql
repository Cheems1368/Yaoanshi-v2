-- MVP v0.1.0 reliability closure. This migration is additive only.

DELIMITER $$
CREATE PROCEDURE assert_no_duplicate_active_family_bindings()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM patient_family_binding
        WHERE status = 'ACTIVE' AND deleted = b'0'
        GROUP BY tenant_id, family_account_id
        HAVING COUNT(*) > 1
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'V14 blocked: family account has multiple active patient bindings; resolve duplicates first';
    END IF;
END$$
DELIMITER ;

CALL assert_no_duplicate_active_family_bindings();
DROP PROCEDURE assert_no_duplicate_active_family_bindings;

ALTER TABLE medication_checkin_record
    ADD COLUMN client_request_id VARCHAR(64) NULL AFTER patient_id,
    ADD UNIQUE KEY uk_checkin_client_request (tenant_id, patient_id, client_request_id);

ALTER TABLE patient_family_binding
    ADD COLUMN active_family_account_id BIGINT
        GENERATED ALWAYS AS (
            CASE WHEN status = 'ACTIVE' AND deleted = b'0' THEN family_account_id ELSE NULL END
        ) STORED,
    ADD UNIQUE KEY uk_family_single_active_patient (tenant_id, active_family_account_id);

ALTER TABLE knowledge_assessment
    ADD UNIQUE KEY uk_knowledge_patient_phase_version (tenant_id, patient_id, phase, question_set_version);

ALTER TABLE notification_log
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0 AFTER delivery_status,
    ADD COLUMN next_retry_at DATETIME NULL AFTER attempt_count,
    ADD COLUMN last_attempt_at DATETIME NULL AFTER next_retry_at,
    ADD COLUMN claim_token VARCHAR(64) NULL AFTER last_attempt_at,
    ADD COLUMN claimed_at DATETIME NULL AFTER claim_token,
    ADD KEY idx_notification_retry (tenant_id, delivery_status, next_retry_at);

ALTER TABLE file_asset
    ADD COLUMN cleanup_attempt_count INT NOT NULL DEFAULT 0 AFTER scan_status,
    ADD COLUMN next_cleanup_at DATETIME NULL AFTER cleanup_attempt_count,
    ADD COLUMN cleanup_failure_reason VARCHAR(255) NULL AFTER next_cleanup_at,
    ADD COLUMN cleanup_claim_token VARCHAR(64) NULL AFTER cleanup_failure_reason,
    ADD COLUMN cleanup_claimed_at DATETIME NULL AFTER cleanup_claim_token,
    ADD KEY idx_file_asset_cleanup (tenant_id, scan_status, next_cleanup_at, create_time);
