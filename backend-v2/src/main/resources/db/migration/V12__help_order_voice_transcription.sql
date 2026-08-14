ALTER TABLE file_asset
    MODIFY business_type VARCHAR(64) NOT NULL;

CREATE TABLE IF NOT EXISTS help_order_voice_transcript (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    help_order_id BIGINT NOT NULL,
    help_order_event_attachment_id BIGINT NOT NULL,
    playback_file_asset_id BIGINT NOT NULL,
    transcript_source_file_asset_id BIGINT NOT NULL,
    duration_seconds INT NOT NULL,
    provider VARCHAR(32) NOT NULL DEFAULT 'XFYUN',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transcript_text TEXT NULL,
    error_code VARCHAR(64) NULL,
    transcribed_by_type VARCHAR(20) NULL,
    transcribed_by_id BIGINT NULL,
    transcribed_at DATETIME NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_help_order_voice_attachment (tenant_id, help_order_event_attachment_id, deleted),
    KEY idx_help_order_voice_order (tenant_id, help_order_id),
    KEY idx_help_order_voice_playback_file (tenant_id, playback_file_asset_id),
    KEY idx_help_order_voice_source_file (tenant_id, transcript_source_file_asset_id)
);
