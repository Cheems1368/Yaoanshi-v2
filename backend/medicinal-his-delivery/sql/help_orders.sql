-- 工单表
CREATE TABLE help_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单编号',
    patient_id BIGINT NOT NULL COMMENT '患者编号',
    assigned_pharmacist_id BIGINT COMMENT '分配药师编号',
    assigned_doctor_id BIGINT COMMENT '分配医生编号',
    title VARCHAR(128) NOT NULL COMMENT '工单标题',
    description TEXT NOT NULL COMMENT '工单描述',
    intended_drugs JSON COMMENT '意向药品',
    attachments JSON COMMENT '附件列表',
    to_doctor BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否需要医生',
    status ENUM(
        'SUBMITTED','ASSIGNED_PHARMACIST','PHARMACIST_REVIEW',
        'ADVICE_READY','TO_DOCTOR','DOCTOR_REVIEW',
        'PRESCRIPTION_READY','DONE','REJECTED'
    ) NOT NULL DEFAULT 'SUBMITTED' COMMENT '工单状态',
    assigned_at DATETIME COMMENT '分配时间',
    escalated_at DATETIME COMMENT '升级时间',
    prescription_ready_at DATETIME COMMENT '处方就绪时间',
    done_at DATETIME COMMENT '完成时间',
    rejected_at DATETIME COMMENT '驳回时间',
    rejected_by BIGINT COMMENT '驳回人',
    reject_reason TEXT COMMENT '驳回原因',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    INDEX idx_patient_id (patient_id),
    INDEX idx_assigned_pharmacist_id (assigned_pharmacist_id),
    INDEX idx_assigned_doctor_id (assigned_doctor_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_patient_status (patient_id, status),
    INDEX idx_pharmacist_status (assigned_pharmacist_id, status),
    INDEX idx_doctor_status (assigned_doctor_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单表';

-- 插入测试数据
INSERT INTO help_orders (patient_id, title, description, status, creator, tenant_id) VALUES
(1, '用药咨询', '我想咨询一下阿司匹林的用法', 'SUBMITTED', 'admin', 1),
(2, '药物相互作用', '同时服用布洛芬和感冒药是否有问题', 'PHARMACIST_REVIEW', 'admin', 1),
(3, '处方审核', '医生开的处方需要审核', 'TO_DOCTOR', 'admin', 1);

