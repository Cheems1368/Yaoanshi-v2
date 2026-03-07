-- 用药计划管理相关表结构

-- 用药计划表
CREATE TABLE med_plans (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    plan_name VARCHAR(128) NOT NULL COMMENT '计划名称',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-活跃,2-暂停,3-终止)',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    description TEXT COMMENT '计划描述',
    instructions TEXT COMMENT '用药说明',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (id),
    KEY idx_med_plan_patient_id (patient_id),
    KEY idx_med_plan_status (status),
    KEY idx_med_plan_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用药计划表';

-- 用药计划项表
CREATE TABLE med_plan_items (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划项ID',
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    drug_name VARCHAR(128) NOT NULL COMMENT '药品名称',
    dosage VARCHAR(64) NOT NULL COMMENT '单次剂量',
    frequency VARCHAR(64) NOT NULL COMMENT '频次',
    time_points JSON NOT NULL COMMENT '时间点数组',
    meal_relation TINYINT NOT NULL DEFAULT 0 COMMENT '餐前后关系(0-独立,1-餐前,2-餐后,3-餐中)',
    instructions TEXT COMMENT '用法说明',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (id),
    KEY idx_med_plan_item_plan_id (plan_id),
    KEY idx_med_plan_item_drug_name (drug_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用药计划项表';

-- 今日待办表
CREATE TABLE med_today_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    plan_item_id BIGINT NOT NULL COMMENT '计划项ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    task_date DATE NOT NULL COMMENT '任务日期',
    scheduled_time TIME NOT NULL COMMENT '计划时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '任务状态(0-待完成,1-已完成,2-已跳过)',
    completed_time DATETIME COMMENT '完成时间',
    notes TEXT COMMENT '备注',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (id),
    KEY idx_today_task_patient_date (patient_id, task_date),
    KEY idx_today_task_plan_item (plan_item_id),
    KEY idx_today_task_status (status),
    UNIQUE KEY uk_today_task_unique (plan_item_id, task_date, scheduled_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='今日待办表';

-- 插入测试数据
INSERT INTO med_plans (patient_id, plan_name, start_date, end_date, status, version, description, instructions) VALUES
(1, '高血压用药计划', '2025-01-06', '2025-04-06', 1, 1, '高血压患者用药计划', '按时服药，注意监测血压'),
(2, '糖尿病用药计划', '2025-01-06', '2025-07-06', 1, 1, '糖尿病患者用药计划', '按时服药，注意监测血糖');

INSERT INTO med_plan_items (plan_id, drug_name, dosage, frequency, time_points, meal_relation, instructions, sort_order) VALUES
(1, '氨氯地平', '5mg', '每日一次', '["08:00"]', 1, '餐前服用', 1),
(1, '美托洛尔', '25mg', '每日两次', '["08:00", "20:00"]', 1, '餐前服用', 2),
(2, '二甲双胍', '500mg', '每日两次', '["08:00", "18:00"]', 2, '餐后服用', 1),
(2, '格列齐特', '80mg', '每日一次', '["08:00"]', 1, '餐前服用', 2);

-- 生成今日待办任务
INSERT INTO med_today_tasks (plan_item_id, patient_id, task_date, scheduled_time, status) VALUES
(1, 1, '2025-01-06', '08:00:00', 0),
(2, 1, '2025-01-06', '08:00:00', 0),
(2, 1, '2025-01-06', '20:00:00', 0),
(3, 2, '2025-01-06', '08:00:00', 0),
(3, 2, '2025-01-06', '18:00:00', 0),
(4, 2, '2025-01-06', '08:00:00', 0);
