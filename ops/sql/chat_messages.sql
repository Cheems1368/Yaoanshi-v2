-- 聊天消息表
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    order_id BIGINT NOT NULL COMMENT '工单ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    sender_type VARCHAR(20) NOT NULL COMMENT '发送者类型（patient/pharmacist/doctor/system）',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    receiver_type VARCHAR(20) NOT NULL COMMENT '接收者类型（patient/pharmacist/doctor/system）',
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型（text/voice/image/file/system）',
    content TEXT COMMENT '消息内容',
    voice_url VARCHAR(500) COMMENT '语音文件URL',
    voice_duration INT COMMENT '语音时长（秒）',
    voice_to_text TEXT COMMENT '语音转文字结果',
    attachments JSON COMMENT '附件URL列表',
    status VARCHAR(20) DEFAULT 'sent' COMMENT '消息状态（sent/delivered/read/failed）',
    send_time DATETIME NOT NULL COMMENT '发送时间',
    read_time DATETIME COMMENT '阅读时间',
    is_recalled TINYINT DEFAULT 0 COMMENT '是否撤回（0-未撤回，1-已撤回）',
    recall_time DATETIME COMMENT '撤回时间',
    recall_reason VARCHAR(200) COMMENT '撤回原因',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    INDEX idx_order_id (order_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_send_time (send_time),
    INDEX idx_status (status),
    INDEX idx_is_recalled (is_recalled),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_order_send_time (order_id, send_time)
) COMMENT '聊天消息表';

-- 插入测试数据
INSERT INTO chat_messages (order_id, sender_id, sender_type, receiver_id, receiver_type, message_type, content, voice_url, voice_to_text, status, send_time) VALUES
(1, 1, 'patient', 2, 'pharmacist', 'voice', '我高血压药快吃完了，想续开', '/uploads/voice/voice_001.mp3', '我高血压药快吃完了，想续开', 'sent', '2025-01-10 10:00:00'),
(1, 2, 'pharmacist', 1, 'patient', 'text', '阿姨，最近有没有头晕胸闷？血压在家里量得还可以吗？', NULL, NULL, 'sent', '2025-01-10 10:02:00'),
(1, 1, 'patient', 2, 'pharmacist', 'voice', '这两天没什么不舒服，偶尔有点头晕，血压基本稳定', '/uploads/voice/voice_002.mp3', '这两天没什么不舒服，偶尔有点头晕，血压基本稳定', 'sent', '2025-01-10 10:04:00'),
(1, 2, 'pharmacist', 1, 'patient', 'text', '好的，根据您的血压记录，建议继续服用氨氯地平片5mg，每日一次，晚饭后服用', NULL, NULL, 'sent', '2025-01-10 10:06:00');
