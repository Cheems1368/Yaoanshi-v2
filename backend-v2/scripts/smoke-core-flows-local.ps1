param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [string]$LegacyBaseUrl = "http://127.0.0.1:48080",
    [string]$PatientMobile = "",
    [string]$FamilyMobile = "",
    [string]$HospitalPatientId = "",
    [string]$PatientName = "",
    [string]$PatientDeviceId = "patient-smoke-core",
    [string]$FamilyDeviceId = "family-smoke-core",
    [string]$PharmacistUsername = "pharmacist",
    [string]$PharmacistPassword = "123456",
    [string]$DoctorUsername = "doctor",
    [string]$DoctorPassword = "123456"
)

$ErrorActionPreference = "Stop"
$runId = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds().ToString()

if (-not $PatientMobile) {
    $PatientMobile = "138" + $runId.Substring([Math]::Max(0, $runId.Length - 8)).PadLeft(8, '0')
}

if (-not $FamilyMobile) {
    $FamilyMobile = "139" + $runId.Substring([Math]::Max(0, $runId.Length - 8)).PadLeft(8, '0')
}

if (-not $HospitalPatientId) {
    $HospitalPatientId = "HZY-SMOKE-CORE-$runId"
}

if (-not $PatientName) {
    $PatientName = "Core Flow Smoke $runId"
}

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
    }

    if ($null -ne $Body) {
        $params.ContentType = "application/json"
        $params.Body = $Body | ConvertTo-Json -Depth 10
    }

    return Invoke-RestMethod @params
}

function Invoke-JsonRequestAllowFailure {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        $Body = $null
    )

    try {
        $response = Invoke-JsonRequest -Method $Method -Url $Url -Headers $Headers -Body $Body
        return [pscustomobject]@{
            ok = $true
            payload = $response
        }
    } catch {
        $httpResponse = $_.Exception.Response
        if (-not $httpResponse) {
            throw
        }

        $reader = New-Object System.IO.StreamReader($httpResponse.GetResponseStream())
        $content = $reader.ReadToEnd()
        $reader.Close()

        return [pscustomobject]@{
            ok = $false
            payload = $content | ConvertFrom-Json
        }
    }
}

function New-AppSession {
    param(
        [string]$Mobile,
        [string]$DeviceId,
        [bool]$BindPatient = $false
    )

    Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/auth/send-otp" -Body @{
        mobile = $Mobile
        scene = "APP_LOGIN"
    } | Out-Null

    $login = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/auth/login" -Body @{
        mobile = $Mobile
        otpCode = "123456"
        deviceId = $DeviceId
        deviceName = "Core Flow Smoke"
        platform = "WEB"
    }

    $headers = @{
        Authorization = "Bearer $($login.data.accessToken)"
        "tenant-id" = "1"
    }

    if ($BindPatient) {
        Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/patients/bind" -Headers $headers -Body @{
            mobile = $Mobile
            hospitalPatientId = $HospitalPatientId
            name = $PatientName
        } | Out-Null
    }

    return [pscustomobject]@{
        accessToken = $login.data.accessToken
        refreshToken = $login.data.refreshToken
        deviceId = $login.data.deviceId
        headers = $headers
    }
}

function New-AdminHeaders {
    param(
        [string]$Username,
        [string]$Password,
        [string]$Role
    )

    $login = Invoke-JsonRequest -Method Post -Url "$LegacyBaseUrl/admin-api/system/auth/login" -Headers @{ "tenant-id" = "1" } -Body @{
        username = $Username
        password = $Password
        role = $Role
    }

    return @{
        Authorization = "Bearer $($login.data.accessToken)"
        "tenant-id" = "1"
    }
}

$patient = New-AppSession -Mobile $PatientMobile -DeviceId $PatientDeviceId -BindPatient $true
$patientMe = Invoke-JsonRequest -Method Get -Url "$BaseUrl/app-api/patients/me" -Headers $patient.headers
$patientId = $patientMe.data.id
$pharmacistHeaders = New-AdminHeaders -Username $PharmacistUsername -Password $PharmacistPassword -Role "pharmacist"
$doctorHeaders = New-AdminHeaders -Username $DoctorUsername -Password $DoctorPassword -Role "doctor"
$today = [DateTime]::UtcNow.ToString("yyyy-MM-dd")

$plan = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/medication-plans" -Headers $pharmacistHeaders -Body @{
    patientId = $patientId
    planName = "Core Flow Plan"
    status = "ACTIVE"
    sourceType = "MANUAL"
    effectiveFrom = [DateTime]::UtcNow.ToString("o")
    notes = "Core flow smoke"
    items = @(
        @{
            drugName = "Amlodipine"
            dosage = "5mg"
            frequencyCode = "BID"
            timePoints = @("08:00", "20:00")
            mealRelation = "AFTER_MEAL"
            route = "PO"
            instructions = "Take with water"
        }
    )
}

$planTasks = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/medication-plans/$($plan.data.id)/tasks?dateFrom=$today&dateTo=$today" -Headers $pharmacistHeaders
$firstTaskId = $planTasks.data[0].id
$secondTaskId = $planTasks.data[1].id

$taken = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/checkins" -Headers $patient.headers -Body @{
    doseTaskId = $firstTaskId
    result = "TAKEN"
    actualTakenAt = [DateTime]::UtcNow.ToString("o")
    note = "patient taken"
}

$corrected = Invoke-JsonRequest -Method Put -Url "$BaseUrl/app-api/checkins/$($taken.data.id)/correct" -Headers $patient.headers -Body @{
    result = "MISSED"
    actualTakenAt = [DateTime]::UtcNow.ToString("o")
    skipReason = "PATIENT_REPORTED"
    note = "patient corrected"
}

$familyBinding = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/patients/me/family-bindings" -Headers $patient.headers -Body @{
    familyMobile = $FamilyMobile
    familyDisplayName = "Core Family"
    relationshipType = "SPOUSE"
}

$family = New-AppSession -Mobile $FamilyMobile -DeviceId $FamilyDeviceId
$familyCheckin = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/checkins" -Headers $family.headers -Body @{
    doseTaskId = $secondTaskId
    result = "TAKEN"
    actualTakenAt = [DateTime]::UtcNow.ToString("o")
    note = "family taken"
}

$helpOrder = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/help-orders" -Headers $family.headers -Body @{
    category = "MEDICATION_CONSULTATION"
    title = "Need help"
    description = "family submitted help-order"
    priority = "NORMAL"
    requiresOfflineEmergency = $false
}

$claim = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/help-orders/$($helpOrder.data.id)/claim" -Headers $pharmacistHeaders
$reply = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/help-orders/$($helpOrder.data.id)/reply" -Headers $pharmacistHeaders -Body @{
    content = "pharmacist replied"
}
$escalate = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/help-orders/$($helpOrder.data.id)/escalate" -Headers $pharmacistHeaders -Body @{
    note = "escalated to doctor"
}
$doctorReply = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/help-orders/$($helpOrder.data.id)/reply" -Headers $doctorHeaders -Body @{
    content = "doctor replied"
}
$doctorClose = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/help-orders/$($helpOrder.data.id)/close" -Headers $doctorHeaders -Body @{
    note = "closed by doctor"
}

$knowledge = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/assessments/knowledge" -Headers $family.headers -Body @{
    phase = "FOLLOW_UP"
    questionSetVersion = "v1"
    answers = @(
        @{
            questionCode = "Q1"
            questionText = "Question 1"
            selectedOption = "A"
            correctOption = "A"
            correct = $true
        }
    )
}

$satisfaction = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/assessments/satisfaction" -Headers $family.headers -Body @{
    helpOrderId = $helpOrder.data.id
    rating = 5
    comment = "resolved"
}

$report = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/medication-plans/$($plan.data.id)/adherence-report?dateFrom=$today&dateTo=$today" -Headers $pharmacistHeaders
$submittedNotifications = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/notifications/logs?businessType=HELP_ORDER_SUBMITTED&pageNo=1&pageSize=20" -Headers $pharmacistHeaders
$escalatedNotifications = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/notifications/logs?businessType=HELP_ORDER_ESCALATED&pageNo=1&pageSize=20" -Headers $pharmacistHeaders
$closedNotifications = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/notifications/logs?businessType=HELP_ORDER_CLOSED&pageNo=1&pageSize=20" -Headers $pharmacistHeaders

if ($submittedNotifications.data.total -lt 1 -or $submittedNotifications.data.list[0].deliveryStatus -ne "FAILED") {
    throw "Expected HELP_ORDER_SUBMITTED notification deliveryStatus=FAILED when no professional WebSocket session is connected"
}

if ($escalatedNotifications.data.total -lt 1 -or $escalatedNotifications.data.list[0].deliveryStatus -ne "FAILED") {
    throw "Expected HELP_ORDER_ESCALATED notification deliveryStatus=FAILED when no professional WebSocket session is connected"
}

if ($closedNotifications.data.total -lt 1 -or $closedNotifications.data.list[0].deliveryStatus -ne "SENT") {
    throw "Expected HELP_ORDER_CLOSED notification deliveryStatus=SENT for in-app patient delivery"
}

Invoke-JsonRequest -Method Delete -Url "$BaseUrl/app-api/patients/me/family-bindings/$($familyBinding.data.id)" -Headers $patient.headers | Out-Null
$familyAfterUnbind = Invoke-JsonRequestAllowFailure -Method Get -Url "$BaseUrl/app-api/dose-tasks/today" -Headers $family.headers

[pscustomobject]@{
    patientId = $patientId
    planId = $plan.data.id
    taskCount = $planTasks.data.Count
    firstCheckinResult = $taken.data.result
    correctedCheckinResult = $corrected.data.result
    correctedIsCorrection = $corrected.data.correction
    familyBindingId = $familyBinding.data.id
    familyOperatorType = $familyCheckin.data.operatorType
    helpOrderId = $helpOrder.data.id
    claimStatus = $claim.data.status
    pharmacistReplyStatus = $reply.data.status
    escalatedStatus = $escalate.data.status
    doctorReplyStatus = $doctorReply.data.status
    doctorCloseStatus = $doctorClose.data.status
    knowledgeAssessmentId = $knowledge.data.id
    satisfactionSurveyId = $satisfaction.data.id
    adherenceReport = $report.data
    submittedNotificationStatus = $submittedNotifications.data.list[0].deliveryStatus
    escalatedNotificationStatus = $escalatedNotifications.data.list[0].deliveryStatus
    closedNotificationStatus = $closedNotifications.data.list[0].deliveryStatus
    closedNotificationTotal = $closedNotifications.data.total
    familyAfterUnbindOk = $familyAfterUnbind.ok
    familyAfterUnbindCode = $familyAfterUnbind.payload.code
    familyAfterUnbindMsg = $familyAfterUnbind.payload.msg
} | ConvertTo-Json -Depth 8
