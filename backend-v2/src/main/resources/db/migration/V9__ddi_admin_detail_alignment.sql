-- V9: Align admin DDI/contraindication detail fields for inline pharmacist review

ALTER TABLE drug_contraindication
    ADD COLUMN mechanism_desc TEXT NULL COMMENT 'Mechanism description for pharmacist review',
    ADD COLUMN alternative_recommendation TEXT NULL COMMENT 'Alternative recommendation for pharmacist review',
    ADD COLUMN reference_source VARCHAR(255) NULL COMMENT 'Reference source for pharmacist review';

ALTER TABLE ddi_screening_log
    ADD COLUMN rule_code VARCHAR(128) NULL COMMENT 'Rule code snapshot at screening time',
    ADD COLUMN rule_status_at_screening VARCHAR(20) NULL COMMENT 'Rule status snapshot at screening time',
    ADD COLUMN drug_a_name VARCHAR(128) NULL COMMENT 'Configured drug A snapshot at screening time',
    ADD COLUMN drug_b_name VARCHAR(128) NULL COMMENT 'Configured drug B snapshot at screening time',
    ADD COLUMN message VARCHAR(255) NULL COMMENT 'Risk message snapshot at screening time',
    ADD COLUMN recommended_action VARCHAR(255) NULL COMMENT 'Recommended action snapshot at screening time',
    ADD COLUMN mechanism_desc TEXT NULL COMMENT 'Mechanism description snapshot at screening time',
    ADD COLUMN alternative_recommendation TEXT NULL COMMENT 'Alternative recommendation snapshot at screening time',
    ADD COLUMN reference_source VARCHAR(255) NULL COMMENT 'Reference source snapshot at screening time',
    ADD COLUMN patient_summary VARCHAR(255) NULL COMMENT 'Patient summary snapshot at screening time';
