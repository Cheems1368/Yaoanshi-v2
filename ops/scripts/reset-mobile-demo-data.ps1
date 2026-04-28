param(
    [Parameter(Mandatory = $true)]
    [string]$Mobile,
    [Parameter(Mandatory = $true)]
    [string]$HospitalPatientId,
    [Parameter(Mandatory = $true)]
    [string]$Name,
    [string]$Database = "yaoshian_mvp",
    [string]$MysqlContainer = "yaoshian-mysql",
    [string]$MysqlRootPassword = "123456",
    [long]$TenantId = 1
)

$ErrorActionPreference = "Stop"

if ($Mobile -notmatch "^1\d{10}$") {
    throw "Mobile must be an 11-digit mainland China mobile number."
}

if (-not $HospitalPatientId.Trim()) {
    throw "HospitalPatientId is required."
}

if (-not $Name.Trim()) {
    throw "Name is required."
}

$escapedMobile = $Mobile.Replace("'", "''")
$escapedHospitalPatientId = $HospitalPatientId.Replace("'", "''")
$escapedName = $Name.Replace("'", "''")

$sql = @"
SET @tenant_id = $TenantId;
SET @mobile = '$escapedMobile';
SET @hospital_patient_id = '$escapedHospitalPatientId';
SET @name = '$escapedName';

SET @patient_id = (
  SELECT id
  FROM patient
  WHERE tenant_id = @tenant_id
    AND mobile = @mobile
    AND hospital_patient_id = @hospital_patient_id
    AND name = @name
  LIMIT 1
);

SET @account_id = (
  SELECT id
  FROM app_account
  WHERE tenant_id = @tenant_id
    AND mobile = @mobile
    AND deleted = b'0'
  LIMIT 1
);

UPDATE app_account_device_session
SET deleted = b'1', update_time = UTC_TIMESTAMP(), updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @account_id IS NOT NULL
  AND app_account_id = @account_id;

UPDATE patient_family_binding
SET status = 'UNBOUND', unbound_at = UTC_TIMESTAMP(), unbound_reason = 'mobile_demo_reset',
    deleted = b'1', update_time = UTC_TIMESTAMP(), updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND patient_id = @patient_id;

UPDATE help_order_event e
JOIN help_order h ON h.id = e.help_order_id AND h.tenant_id = e.tenant_id
SET e.deleted = b'1', e.update_time = UTC_TIMESTAMP(), e.updater = 'mobile-demo-reset'
WHERE e.tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND h.patient_id = @patient_id;

UPDATE help_order
SET deleted = b'1', update_time = UTC_TIMESTAMP(), updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND patient_id = @patient_id;

UPDATE self_reported_medication
SET deleted = b'1', update_time = UTC_TIMESTAMP(), updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND patient_id = @patient_id;

UPDATE ddi_screening_log
SET deleted = b'1', update_time = UTC_TIMESTAMP(), updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND patient_id = @patient_id;

UPDATE prescription_ocr_record o
JOIN prescription p ON p.id = o.prescription_id AND p.tenant_id = o.tenant_id
SET o.deleted = b'1', o.update_time = UTC_TIMESTAMP(), o.updater = 'mobile-demo-reset'
WHERE o.tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND p.patient_id = @patient_id;

UPDATE prescription_item i
JOIN prescription p ON p.id = i.prescription_id AND p.tenant_id = i.tenant_id
SET i.deleted = b'1', i.update_time = UTC_TIMESTAMP(), i.updater = 'mobile-demo-reset'
WHERE i.tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND p.patient_id = @patient_id;

UPDATE prescription_attachment a
JOIN prescription p ON p.id = a.prescription_id AND p.tenant_id = a.tenant_id
SET a.deleted = b'1', a.update_time = UTC_TIMESTAMP(), a.updater = 'mobile-demo-reset'
WHERE a.tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND p.patient_id = @patient_id;

UPDATE prescription
SET deleted = b'1', update_time = UTC_TIMESTAMP(), updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND patient_id = @patient_id;

UPDATE medication_checkin_record
SET deleted = b'1', update_time = UTC_TIMESTAMP(), updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND patient_id = @patient_id;

UPDATE medication_dose_task
SET deleted = b'1', update_time = UTC_TIMESTAMP(), updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND patient_id = @patient_id;

UPDATE medication_plan_event e
JOIN medication_plan p ON p.id = e.plan_id AND p.tenant_id = e.tenant_id
SET e.deleted = b'1', e.update_time = UTC_TIMESTAMP(), e.updater = 'mobile-demo-reset'
WHERE e.tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND p.patient_id = @patient_id;

UPDATE medication_plan_item i
JOIN medication_plan p ON p.id = i.plan_id AND p.tenant_id = i.tenant_id
SET i.deleted = b'1', i.update_time = UTC_TIMESTAMP(), i.updater = 'mobile-demo-reset'
WHERE i.tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND p.patient_id = @patient_id;

UPDATE medication_plan
SET deleted = b'1', update_time = UTC_TIMESTAMP(), updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND patient_id = @patient_id;

UPDATE patient
SET app_account_id = NULL,
    binding_status = 'PENDING',
    bound_at = NULL,
    deleted = b'0',
    update_time = UTC_TIMESTAMP(),
    updater = 'mobile-demo-reset'
WHERE tenant_id = @tenant_id
  AND @patient_id IS NOT NULL
  AND id = @patient_id;

SELECT @patient_id AS patient_id, @account_id AS account_id;
"@

$tempSql = New-TemporaryFile
try {
    Set-Content -Path $tempSql -Value $sql -Encoding UTF8
    Get-Content $tempSql | docker exec -i $MysqlContainer mysql -uroot "-p$MysqlRootPassword" $Database
} finally {
    Remove-Item -LiteralPath $tempSql -Force -ErrorAction SilentlyContinue
}
