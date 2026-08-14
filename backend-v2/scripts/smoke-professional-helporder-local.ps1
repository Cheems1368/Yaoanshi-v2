param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [string]$Password = "123456",
    [switch]$SkipAccountEnsure
)

$ErrorActionPreference = "Stop"

function Invoke-JsonRequest {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        $Body = $null
    )

    $params = @{
        Method = $Method
        Uri = $Url
        Headers = $Headers
        TimeoutSec = 30
    }

    if ($null -ne $Body) {
        $params.ContentType = "application/json; charset=utf-8"
        $params.Body = $Body | ConvertTo-Json -Depth 10
    }

    try {
        Invoke-RestMethod @params
    } catch {
        $response = $_.Exception.Response
        if ($response -and $response.GetResponseStream()) {
            $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
            $content = $reader.ReadToEnd()
            $reader.Close()
            throw "HTTP request failed: $Method $Url $content"
        }
        throw
    }
}

function Assert-Ok {
    param(
        $Response,
        [string]$Label
    )

    if ($null -eq $Response -or $Response.code -ne 200) {
        $code = if ($null -eq $Response) { "<null>" } else { $Response.code }
        $msg = if ($null -eq $Response) { "" } else { $Response.msg }
        throw "$Label failed: code=$code msg=$msg"
    }
}

function Login-Professional {
    param(
        [string]$Username,
        [string]$Role
    )

    $response = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/system/auth/login" -Headers @{ "tenant-id" = "1" } -Body @{
        username = $Username
        password = $Password
        role = $Role
    }
    Assert-Ok -Response $response -Label "$Username login"
    if (-not $response.data.accessToken) {
        throw "$Username login did not return accessToken"
    }
    return $response.data.accessToken
}

function Assert-ProfessionalRole {
    param(
        [string]$Token,
        [string]$ExpectedRole,
        [string]$UnexpectedRole
    )

    $response = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/system/auth/get-permission-info" -Headers @{
        Authorization = "Bearer $Token"
        "tenant-id" = "1"
    }
    Assert-Ok -Response $response -Label "$ExpectedRole permission info"
    $roles = @($response.data.roles)
    if ($roles -notcontains $ExpectedRole) {
        throw "Expected role '$ExpectedRole', actual roles: $($roles -join ',')"
    }
    if ($roles -contains $UnexpectedRole) {
        throw "Unexpected role '$UnexpectedRole' on $ExpectedRole account: $($roles -join ',')"
    }
}

$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not $SkipAccountEnsure) {
    $ensureScript = Join-Path $root "ops\scripts\ensure-local-professional-test-accounts.ps1"
    powershell -ExecutionPolicy Bypass -File $ensureScript
    if ($LASTEXITCODE -ne 0) {
        throw "Account ensure script failed."
    }
}

$tenantHeaders = @{ "tenant-id" = "1" }
$random = Get-Random -Minimum 10000000 -Maximum 99999999
$patientMobile = "139$random"
$deviceId = "smoke-professional-$random"
$hospitalPatientId = "SMOKE-$random"

$sendOtp = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/auth/send-otp" -Headers $tenantHeaders -Body @{
    mobile = $patientMobile
    scene = "APP_LOGIN"
}
Assert-Ok -Response $sendOtp -Label "send patient OTP"

$patientLogin = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/auth/login" -Headers $tenantHeaders -Body @{
    mobile = $patientMobile
    otpCode = "123456"
    deviceId = $deviceId
    deviceName = "professional smoke"
    platform = "WEB"
}
Assert-Ok -Response $patientLogin -Label "patient login"
$patientToken = $patientLogin.data.accessToken
if (-not $patientToken) {
    throw "Patient login did not return accessToken"
}

$patientHeaders = @{
    Authorization = "Bearer $patientToken"
    "tenant-id" = "1"
}

$bind = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/patients/bind" -Headers $patientHeaders -Body @{
    mobile = $patientMobile
    hospitalPatientId = $hospitalPatientId
    name = "Smoke Patient $random"
}
Assert-Ok -Response $bind -Label "bind patient"

$createdOrder = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/help-orders" -Headers $patientHeaders -Body @{
    category = "OTHER"
    title = "Professional smoke help order $random"
    description = "Created by smoke-professional-helporder-local.ps1"
    priority = "NORMAL"
    requiresOfflineEmergency = $false
}
Assert-Ok -Response $createdOrder -Label "create help order"
$orderId = $createdOrder.data.id
if (-not $orderId -or $createdOrder.data.status -ne "SUBMITTED") {
    throw "Created order did not return expected SUBMITTED status"
}

$pharmacistToken = Login-Professional -Username "pharmacist" -Role "pharmacist"
Assert-ProfessionalRole -Token $pharmacistToken -ExpectedRole "pharmacist" -UnexpectedRole "doctor"
$pharmacistHeaders = @{
    Authorization = "Bearer $pharmacistToken"
    "tenant-id" = "1"
}

$claimed = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/help-orders/$orderId/claim" -Headers $pharmacistHeaders
Assert-Ok -Response $claimed -Label "pharmacist claim"
if ($claimed.data.status -ne "PHARMACIST_IN_PROGRESS") {
    throw "Expected PHARMACIST_IN_PROGRESS after claim, got $($claimed.data.status)"
}

$escalated = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/help-orders/$orderId/escalate" -Headers $pharmacistHeaders -Body @{
    note = "Escalated by professional smoke"
}
Assert-Ok -Response $escalated -Label "pharmacist escalate"
if ($escalated.data.status -ne "WAITING_DOCTOR" -or $escalated.data.currentHandlerType -ne "DOCTOR") {
    throw "Expected WAITING_DOCTOR/DOCTOR after escalate, got $($escalated.data.status)/$($escalated.data.currentHandlerType)"
}

$doctorToken = Login-Professional -Username "doctor" -Role "doctor"
Assert-ProfessionalRole -Token $doctorToken -ExpectedRole "doctor" -UnexpectedRole "pharmacist"
$doctorHeaders = @{
    Authorization = "Bearer $doctorToken"
    "tenant-id" = "1"
}

$doctorReply = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/help-orders/$orderId/reply" -Headers $doctorHeaders -Body @{
    content = "Doctor smoke reply"
}
Assert-Ok -Response $doctorReply -Label "doctor reply"
if ($doctorReply.data.status -ne "DOCTOR_IN_PROGRESS") {
    throw "Expected DOCTOR_IN_PROGRESS after doctor reply, got $($doctorReply.data.status)"
}

$eventTypes = @($doctorReply.data.events | ForEach-Object { $_.eventType })
if ($eventTypes -notcontains "DOCTOR_REPLY") {
    throw "Doctor reply event not found. Events: $($eventTypes -join ',')"
}

[pscustomobject]@{
    patientMobile = $patientMobile
    hospitalPatientId = $hospitalPatientId
    orderId = $orderId
    finalStatus = $doctorReply.data.status
    events = $eventTypes
} | ConvertTo-Json -Depth 6
