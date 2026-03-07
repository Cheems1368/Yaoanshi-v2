-- 血压记录表
CREATE TABLE blood_pressure_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    systolic_pressure INT NOT NULL COMMENT '收缩压（mmHg）',
    diastolic_pressure INT NOT NULL COMMENT '舒张压（mmHg）',
    heart_rate INT COMMENT '心率（次/分钟）',
    measure_time DATETIME NOT NULL COMMENT '测量时间',
    device_type VARCHAR(50) COMMENT '测量设备',
    notes TEXT COMMENT '备注',
    data_source VARCHAR(50) DEFAULT '手动录入' COMMENT '数据来源（手动录入/设备同步/医生录入）',
    is_abnormal TINYINT DEFAULT 0 COMMENT '是否异常（0-正常，1-异常）',
    abnormal_type VARCHAR(50) COMMENT '异常类型（高血压/低血压/心率异常）',
    measure_status TINYINT DEFAULT 0 COMMENT '测量状态（0-正常，1-餐前，2-餐后，3-运动后）',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    INDEX idx_patient_id (patient_id),
    INDEX idx_measure_time (measure_time),
    INDEX idx_patient_measure_time (patient_id, measure_time),
    INDEX idx_is_abnormal (is_abnormal),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_create_time (create_time)
) COMMENT '血压记录表';

-- 插入测试数据
INSERT INTO blood_pressure_records (patient_id, systolic_pressure, diastolic_pressure, heart_rate, measure_time, device_type, notes, data_source, is_abnormal, abnormal_type, measure_status) VALUES
(1, 132, 85, 72, '2025-01-08 08:30:00', '电子血压计', '晨起测量', '手动录入', 0, '正常', 0),
(1, 139, 87, 75, '2025-01-09 08:25:00', '电子血压计', '晨起测量', '手动录入', 0, '正常', 0),
(1, 136, 86, 73, '2025-01-10 08:35:00', '电子血压计', '晨起测量', '手动录入', 0, '正常', 0),
(1, 145, 92, 78, '2025-01-11 08:20:00', '电子血压计', '晨起测量', '手动录入', 1, '高血压1级', 0),
(1, 142, 89, 76, '2025-01-12 08:40:00', '电子血压计', '晨起测量', '手动录入', 0, '正常', 0),
(1, 138, 88, 74, '2025-01-13 08:30:00', '电子血压计', '晨起测量', '手动录入', 0, '正常', 0),
(1, 141, 90, 77, '2025-01-14 08:25:00', '电子血压计', '晨起测量', '手动录入', 0, '正常', 0);
