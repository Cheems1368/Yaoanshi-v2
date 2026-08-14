-- V8: AI consent gate — add ai_consent_granted_at to app_account

ALTER TABLE app_account
    ADD COLUMN ai_consent_granted_at DATETIME NULL COMMENT 'AI助手首次同意时间，NULL表示未同意';
