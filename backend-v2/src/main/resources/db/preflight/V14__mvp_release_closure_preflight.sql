-- Run against the target MySQL 8 database before allowing Flyway to apply V14.
-- It creates only a session-scoped temporary guard table and never changes business data.

CREATE TEMPORARY TABLE v14_family_binding_preflight_guard (
    violation TINYINT NOT NULL,
    CONSTRAINT v14_preflight_blocked_duplicate_active_family_bindings CHECK (violation = 0)
);

INSERT INTO v14_family_binding_preflight_guard (violation)
SELECT 1
FROM (
    SELECT tenant_id, family_account_id
    FROM patient_family_binding
    WHERE status = 'ACTIVE' AND deleted = b'0'
    GROUP BY tenant_id, family_account_id
    HAVING COUNT(*) > 1
) duplicate_bindings
LIMIT 1;

DROP TEMPORARY TABLE v14_family_binding_preflight_guard;
