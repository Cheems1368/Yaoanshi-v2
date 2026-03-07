CREATE DATABASE IF NOT EXISTS medicine CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE medicine;

CREATE TABLE IF NOT EXISTS reminder_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    today_task_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    scheduled_time DATETIME NOT NULL,
    type TINYINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    retry_count INT NOT NULL DEFAULT 0,
    last_retry_time DATETIME,
    notification_content TEXT,
    failure_reason TEXT,
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL DEFAULT '0',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reminder_configs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    type TINYINT NOT NULL,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    advance_minutes INT NOT NULL DEFAULT 30,
    notification_method VARCHAR(32) NOT NULL DEFAULT 'SYSTEM',
    template_id VARCHAR(64),
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL DEFAULT '0',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO reminder_configs (patient_id, type, enabled, advance_minutes, notification_method) VALUES
(1, 1, 1, 30, 'SYSTEM'),
(2, 1, 1, 30, 'SYSTEM');

INSERT IGNORE INTO reminder_tasks (today_task_id, patient_id, scheduled_time, type, status, notification_content) VALUES
(1, 1, '2025-01-27 07:30:00', 1, 0, 'Test reminder 1'),
(2, 1, '2025-01-27 07:30:00', 1, 0, 'Test reminder 2'),
(3, 2, '2025-01-27 07:30:00', 1, 0, 'Test reminder 3');
