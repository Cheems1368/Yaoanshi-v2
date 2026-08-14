-- V10: Allow multiple image attachments per help-order event.

CREATE TABLE IF NOT EXISTS help_order_event_attachment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    help_order_event_id BIGINT NOT NULL,
    file_asset_id BIGINT NOT NULL,
    attachment_type VARCHAR(20) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    KEY idx_help_order_event_attachment_event (tenant_id, help_order_event_id),
    KEY idx_help_order_event_attachment_file (tenant_id, file_asset_id)
);
