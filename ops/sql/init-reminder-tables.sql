-- 提醒任务调度相关表结构初始化脚本
-- 用于解决健康检查问题

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS medicine CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE medicine;

-- 提醒任务表
CREATE TABLE IF NOT EXISTS reminder_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '提醒任务ID',
    today_task_id BIGINT NOT NULL COMMENT '今日待办任务ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    scheduled_time DATETIME NOT NULL COMMENT '计划提醒时间',
    type TINYINT NOT NULL COMMENT '提醒类型(1-用药提醒,2-复诊提醒,3-检查提醒)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '提醒状态(0-待执行,1-执行中,2-成功,3-失败,4-已放弃)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    last_retry_time DATETIME COMMENT '最后重试时间',
    notification_content TEXT COMMENT '通知内容',
    failure_reason TEXT COMMENT '失败原因',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (id),
    KEY idx_reminder_task_patient_id (patient_id),
    KEY idx_reminder_task_scheduled_time (scheduled_time),
    KEY idx_reminder_task_status (status),
    KEY idx_reminder_task_today_task (today_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提醒任务表';

-- 提醒配置表
CREATE TABLE IF NOT EXISTS reminder_configs (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    type TINYINT NOT NULL COMMENT '提醒类型(1-用药提醒,2-复诊提醒,3-检查提醒)',
    enabled BIT(1) NOT NULL DEFAULT b'1' COMMENT '是否启用',
    advance_minutes INT NOT NULL DEFAULT 30 COMMENT '提前分钟数',
    notification_method VARCHAR(32) NOT NULL DEFAULT 'SYSTEM' COMMENT '通知方式(SMS,EMAIL,SYSTEM,ALL)',
    template_id VARCHAR(64) COMMENT '模板ID',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (id),
    KEY idx_reminder_config_patient_id (patient_id),
    KEY idx_reminder_config_type (type),
    UNIQUE KEY uk_reminder_config_patient_type (patient_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提醒配置表';

-- 插入默认提醒配置
INSERT IGNORE INTO reminder_configs (patient_id, type, enabled, advance_minutes, notification_method) VALUES
(1, 1, 1, 30, 'SYSTEM'),
(2, 1, 1, 30, 'SYSTEM');

-- 插入测试提醒任务
INSERT IGNORE INTO reminder_tasks (today_task_id, patient_id, scheduled_time, type, status, notification_content) VALUES
(1, 1, '2025-01-27 07:30:00', 1, 0, '用药提醒：氨氯地平 5mg，请按时服药'),
(2, 1, '2025-01-27 07:30:00', 1, 0, '用药提醒：美托洛尔 25mg，请按时服药'),
(3, 2, '2025-01-27 07:30:00', 1, 0, '用药提醒：二甲双胍 500mg，请按时服药');

-- 验证表创建成功
SELECT 'reminder_tasks table created successfully' as status;
SELECT 'reminder_configs table created successfully' as status;
SELECT COUNT(*) as reminder_tasks_count FROM reminder_tasks;
SELECT COUNT(*) as reminder_configs_count FROM reminder_configs;
