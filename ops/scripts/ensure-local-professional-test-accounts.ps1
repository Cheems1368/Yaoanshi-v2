param(
    [string]$DbPassword = $env:MEDICINE_DB_PASSWORD,
    [string]$MysqlContainer = "medicine-mysql",
    [string]$Database = "medicine"
)

$ErrorActionPreference = "Stop"

function Get-DevPasswordFromDockerEnv {
    param([string]$RootPath)

    $dockerEnvPath = Join-Path $RootPath "ops\docker.env"
    if (-not (Test-Path $dockerEnvPath)) {
        return $null
    }

    $line = Get-Content $dockerEnvPath | Where-Object { $_ -like "MYSQL_ROOT_PASSWORD=*" } | Select-Object -First 1
    if (-not $line) {
        return $null
    }

    return ($line -split "=", 2)[1].Trim()
}

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not $DbPassword) {
    $DbPassword = Get-DevPasswordFromDockerEnv -RootPath $root
}

if (-not $DbPassword) {
    throw "MEDICINE_DB_PASSWORD is required, or ops/docker.env must define MYSQL_ROOT_PASSWORD."
}

$passwordHash = '$2a$10$So3U.Krla/NYewEq/MhN1uFOJsbb47OxXhGzHX2uiuFD.KSqwZGZW'

$sql = @"
USE $Database;

CREATE TABLE IF NOT EXISTS pharmacist_profile (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    system_user_id BIGINT NOT NULL,
    license_no VARCHAR(64) NULL,
    real_name VARCHAR(64) NOT NULL,
    mobile VARCHAR(20) NULL,
    service_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    work_hours_json JSON NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_pharmacist_system_user (system_user_id),
    KEY idx_pharmacist_service_status (tenant_id, service_status)
);

CREATE TABLE IF NOT EXISTS doctor_profile (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    system_user_id BIGINT NOT NULL,
    specialty_code VARCHAR(32) NULL,
    title_name VARCHAR(64) NULL,
    real_name VARCHAR(64) NOT NULL,
    mobile VARCHAR(20) NULL,
    service_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    work_hours_json JSON NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_doctor_system_user (system_user_id),
    KEY idx_doctor_service_status (tenant_id, service_status)
);

START TRANSACTION;

SET @tenant_id = 1;
SET @password_hash = '$passwordHash';

INSERT INTO system_role (
    name, code, sort, data_scope, data_scope_dept_ids, status, type, remark,
    creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT 'Local Doctor', 'doctor', 10, 1, '', 0, 2, 'Local doctor test role',
       'local-test', NOW(), 'local-test', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
    SELECT 1 FROM system_role WHERE tenant_id = @tenant_id AND code = 'doctor' AND deleted = 0
);

INSERT INTO system_role (
    name, code, sort, data_scope, data_scope_dept_ids, status, type, remark,
    creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT 'Local Pharmacist', 'pharmacist', 11, 1, '', 0, 2, 'Local pharmacist test role',
       'local-test', NOW(), 'local-test', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
    SELECT 1 FROM system_role WHERE tenant_id = @tenant_id AND code = 'pharmacist' AND deleted = 0
);

UPDATE system_role
SET status = 0, deleted = b'0', updater = 'local-test', update_time = NOW()
WHERE tenant_id = @tenant_id AND code IN ('doctor', 'pharmacist') AND deleted = 0;

SELECT @doctor_role_id := id
FROM system_role
WHERE tenant_id = @tenant_id AND code = 'doctor' AND deleted = 0
ORDER BY id ASC
LIMIT 1;

SELECT @pharmacist_role_id := id
FROM system_role
WHERE tenant_id = @tenant_id AND code = 'pharmacist' AND deleted = 0
ORDER BY id ASC
LIMIT 1;

INSERT INTO system_users (
    username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar,
    status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT 'doctor', @password_hash, 'doctor', 'Local doctor test account', NULL, NULL,
       'doctor@local.test', '15600000001', 0, '', 0, '', NULL,
       'local-test', NOW(), 'local-test', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
    SELECT 1 FROM system_users WHERE tenant_id = @tenant_id AND username = 'doctor' AND deleted = 0
);

INSERT INTO system_users (
    username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar,
    status, login_ip, login_date, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT 'pharmacist', @password_hash, 'pharmacist', 'Local pharmacist test account', NULL, NULL,
       'pharmacist@local.test', '15600000003', 0, '', 0, '', NULL,
       'local-test', NOW(), 'local-test', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
    SELECT 1 FROM system_users WHERE tenant_id = @tenant_id AND username = 'pharmacist' AND deleted = 0
);

UPDATE system_users
SET password = @password_hash,
    nickname = username,
    status = 0,
    deleted = b'0',
    updater = 'local-test',
    update_time = NOW()
WHERE tenant_id = @tenant_id AND username IN ('doctor', 'pharmacist') AND deleted = 0;

SELECT @doctor_user_id := id
FROM system_users
WHERE tenant_id = @tenant_id AND username = 'doctor' AND deleted = 0
ORDER BY id ASC
LIMIT 1;

SELECT @pharmacist_user_id := id
FROM system_users
WHERE tenant_id = @tenant_id AND username = 'pharmacist' AND deleted = 0
ORDER BY id ASC
LIMIT 1;

UPDATE system_user_role
SET deleted = b'1', updater = 'local-test', update_time = NOW()
WHERE tenant_id = @tenant_id
  AND user_id IN (@doctor_user_id, @pharmacist_user_id)
  AND deleted = 0;

INSERT INTO system_user_role (
    user_id, role_id, creator, create_time, updater, update_time, deleted, tenant_id
) VALUES
    (@doctor_user_id, @doctor_role_id, 'local-test', NOW(), 'local-test', NOW(), b'0', @tenant_id),
    (@pharmacist_user_id, @pharmacist_role_id, 'local-test', NOW(), 'local-test', NOW(), b'0', @tenant_id);

INSERT INTO doctor_profile (
    system_user_id, specialty_code, title_name, real_name, mobile, service_status, work_hours_json,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    @doctor_user_id, 'GENERAL', 'Doctor', 'doctor', '15600000001', 'ACTIVE', NULL,
    'local-test', NOW(), 'local-test', NOW(), b'0', @tenant_id
)
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name),
    mobile = VALUES(mobile),
    service_status = 'ACTIVE',
    deleted = b'0',
    updater = 'local-test',
    update_time = NOW(),
    tenant_id = VALUES(tenant_id);

INSERT INTO pharmacist_profile (
    system_user_id, license_no, real_name, mobile, service_status, work_hours_json,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    @pharmacist_user_id, 'LOCAL-PHARMACIST', 'pharmacist', '15600000003', 'ACTIVE', NULL,
    'local-test', NOW(), 'local-test', NOW(), b'0', @tenant_id
)
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name),
    mobile = VALUES(mobile),
    service_status = 'ACTIVE',
    deleted = b'0',
    updater = 'local-test',
    update_time = NOW(),
    tenant_id = VALUES(tenant_id);

COMMIT;

SELECT u.username, GROUP_CONCAT(r.code ORDER BY r.code SEPARATOR ',') AS roles
FROM system_users u
JOIN system_user_role ur
  ON ur.user_id = u.id
 AND ur.tenant_id = u.tenant_id
 AND ur.deleted = 0
JOIN system_role r
  ON r.id = ur.role_id
 AND r.tenant_id = u.tenant_id
 AND r.deleted = 0
WHERE u.tenant_id = @tenant_id
  AND u.username IN ('doctor', 'pharmacist')
  AND u.deleted = 0
GROUP BY u.username
ORDER BY u.username;
"@

Write-Host "Ensuring local professional test accounts in $MysqlContainer/$Database..."
$sql | docker exec -i $MysqlContainer mysql -uroot "-p$DbPassword" $Database
if ($LASTEXITCODE -ne 0) {
    throw "Failed to ensure local professional test accounts."
}

Write-Host "Local professional test accounts are ready: doctor / 123456, pharmacist / 123456."
