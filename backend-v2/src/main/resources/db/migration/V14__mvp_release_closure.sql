-- MVP v0.1.0 reliability closure. This migration is additive only.

CREATE TEMPORARY TABLE v14_family_binding_keep AS
SELECT tenant_id, family_account_id, id AS keep_id
FROM (
    SELECT id,
           tenant_id,
           family_account_id,
           ROW_NUMBER() OVER (
               PARTITION BY tenant_id, family_account_id
               ORDER BY authorized_at ASC, id ASC
           ) AS binding_rank
    FROM patient_family_binding
    WHERE status = 'ACTIVE' AND deleted = b'0'
) ranked_bindings
WHERE binding_rank = 1;

UPDATE patient_family_binding duplicate_binding
INNER JOIN v14_family_binding_keep canonical
    ON canonical.tenant_id = duplicate_binding.tenant_id
    AND canonical.family_account_id = duplicate_binding.family_account_id
SET duplicate_binding.status = 'UNBOUND',
    duplicate_binding.unbound_at = COALESCE(duplicate_binding.unbound_at, UTC_TIMESTAMP()),
    duplicate_binding.unbound_reason = COALESCE(duplicate_binding.unbound_reason, 'V14 single-patient family binding normalization'),
    duplicate_binding.updater = 'v14-family-binding-normalize',
    duplicate_binding.update_time = UTC_TIMESTAMP()
WHERE duplicate_binding.id <> canonical.keep_id
  AND duplicate_binding.status = 'ACTIVE'
  AND duplicate_binding.deleted = b'0';

DROP TEMPORARY TABLE v14_family_binding_keep;

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
    ADD COLUMN uniqueness_guard TINYINT NULL DEFAULT 1 AFTER question_set_version;

UPDATE knowledge_assessment
SET uniqueness_guard = NULL
WHERE deleted = b'1';

CREATE TEMPORARY TABLE v14_knowledge_assessment_keep AS
SELECT tenant_id, patient_id, phase, question_set_version, MIN(id) AS keep_id
FROM knowledge_assessment
WHERE deleted = b'0'
GROUP BY tenant_id, patient_id, phase, question_set_version
HAVING COUNT(*) > 1;

UPDATE knowledge_assessment duplicate_row
INNER JOIN v14_knowledge_assessment_keep canonical
    ON canonical.tenant_id = duplicate_row.tenant_id
    AND canonical.patient_id = duplicate_row.patient_id
    AND canonical.phase = duplicate_row.phase
    AND canonical.question_set_version = duplicate_row.question_set_version
SET duplicate_row.uniqueness_guard = NULL,
    duplicate_row.updater = 'v14-assessment-deduplicate',
    duplicate_row.update_time = UTC_TIMESTAMP()
WHERE duplicate_row.id <> canonical.keep_id
  AND duplicate_row.deleted = b'0';

DROP TEMPORARY TABLE v14_knowledge_assessment_keep;

ALTER TABLE knowledge_assessment
    ADD UNIQUE KEY uk_knowledge_patient_phase_version (
        tenant_id, patient_id, phase, question_set_version, uniqueness_guard
    );

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
