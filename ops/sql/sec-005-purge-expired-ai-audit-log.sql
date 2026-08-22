-- Preview expired, content-free AI audit metadata before any deletion.
SELECT COUNT(*) AS expired_ai_audit_rows
FROM ai_request_audit_log
WHERE expires_at <= CURRENT_TIMESTAMP;

-- Run the bounded DELETE only through an authorized operations window.
-- Repeat in batches until the preview count reaches zero.
DELETE FROM ai_request_audit_log
WHERE expires_at <= CURRENT_TIMESTAMP
ORDER BY expires_at, id
LIMIT 1000;
