ALTER TABLE medication_dose_task
    ADD COLUMN delayed_generated BIT(1) NOT NULL DEFAULT b'0',
    ADD COLUMN active_plan_item_id BIGINT GENERATED ALWAYS AS
        (CASE WHEN deleted = b'0' THEN plan_item_id ELSE NULL END) STORED,
    ADD UNIQUE KEY uk_dose_task_active_schedule (tenant_id, active_plan_item_id, scheduled_at);

ALTER TABLE medication_plan_item
    ADD COLUMN first_execution_at DATETIME NULL;

CREATE TABLE assessment_reminder (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    phase VARCHAR(20) NOT NULL,
    notification_log_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_assessment_reminder_phase (tenant_id, patient_id, phase)
);

CREATE TABLE notification_commit_gate (
    id TINYINT NOT NULL PRIMARY KEY
);
INSERT INTO notification_commit_gate (id) VALUES (1);
