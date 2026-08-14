-- V6: Self-reported medication domain + DDI rule field extension

-- 1. New table: self_reported_medication
CREATE TABLE IF NOT EXISTS self_reported_medication (
    id                        BIGINT       NOT NULL AUTO_INCREMENT,
    patient_id                BIGINT       NOT NULL COMMENT '患者账号ID',
    drug_name                 VARCHAR(128) NOT NULL COMMENT '药物名称',
    dosage                    VARCHAR(64)           COMMENT '剂量（可选）',
    status                    VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                           COMMENT 'PENDING / ACTIVE / STOPPED / TRANSFERRED',
    notes                     TEXT                  COMMENT '患者补充说明',
    operator_type             VARCHAR(20)  NOT NULL COMMENT 'PATIENT / FAMILY',
    operator_account_id       BIGINT       NOT NULL COMMENT '实际操作者账号ID',
    work_order_id             BIGINT                COMMENT '关联工单ID',
    transferred_plan_item_id  BIGINT                COMMENT '已转入正式计划项ID',
    creator                   VARCHAR(64)           COMMENT '创建人',
    create_time               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater                   VARCHAR(64)           COMMENT '更新人',
    update_time               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                   TINYINT(1)   NOT NULL DEFAULT 0,
    tenant_id                 BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_srm_patient   (tenant_id, patient_id, deleted),
    INDEX idx_srm_status    (tenant_id, patient_id, status, deleted),
    INDEX idx_srm_workorder (work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者自报用药记录';

-- 2. Extend ddi_rule: add mechanism, alternatives, source and patient summary
ALTER TABLE ddi_rule
    ADD COLUMN mechanism_desc               TEXT          COMMENT '相互作用机制说明（药师端展示）',
    ADD COLUMN alternative_recommendation   TEXT          COMMENT '替代用药建议（药师端展示）',
    ADD COLUMN reference_source             VARCHAR(255)  COMMENT '参考来源（标题·章节·版本）',
    ADD COLUMN patient_summary              VARCHAR(255)  COMMENT '患者侧简短摘要（空则沿用 message）';

-- 3. Extend drug_contraindication: patient summary
ALTER TABLE drug_contraindication
    ADD COLUMN patient_summary              VARCHAR(255)  COMMENT '患者侧简短摘要';

-- 4. Extend ddi_screening_log: link to self-reported medication
ALTER TABLE ddi_screening_log
    ADD COLUMN self_reported_medication_id  BIGINT        COMMENT '来源自报用药ID（与 prescription_id 二选一）';
