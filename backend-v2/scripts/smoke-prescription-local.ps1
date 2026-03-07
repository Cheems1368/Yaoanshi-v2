param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [string]$LegacyBaseUrl = "http://127.0.0.1:48080",
    [string]$PatientMobile = "13800138111",
    [string]$HospitalPatientId = "HZY-SMOKE-RX-1001",
    [string]$PatientName = "Prescription Smoke",
    [string]$DeviceId = "patient-smoke-prescription",
    [string]$PharmacistUsername = "pharmacist",
    [string]$PharmacistPassword = "123456"
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
    }

    if ($null -ne $Body) {
        $params.ContentType = "application/json"
        $params.Body = $Body | ConvertTo-Json -Depth 8
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
    Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/auth/send-otp" -Body @{
        mobile = $PatientMobile
        scene = "APP_LOGIN"
    } | Out-Null

    $login = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/auth/login" -Body @{
        mobile = $PatientMobile
        otpCode = "123456"
        deviceId = $DeviceId
        deviceName = "Prescription Smoke"
        platform = "WEB"
    }

    $headers = @{
        Authorization = "Bearer $($login.data.accessToken)"
        "tenant-id" = "1"
    }

    Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/patients/bind" -Headers $headers -Body @{
        mobile = $PatientMobile
        hospitalPatientId = $HospitalPatientId
        name = $PatientName
    } | Out-Null

    return [pscustomobject]@{
        accessToken = $login.data.accessToken
        refreshToken = $login.data.refreshToken
        deviceId = $login.data.deviceId
        headers = $headers
    }
}

function New-AdminHeaders {
    $login = Invoke-JsonRequest -Method Post -Url "$LegacyBaseUrl/admin-api/system/auth/login" -Headers @{ "tenant-id" = "1" } -Body @{
        username = $PharmacistUsername
        password = $PharmacistPassword
        role = "pharmacist"
    }

    return @{
        Authorization = "Bearer $($login.data.accessToken)"
        "tenant-id" = "1"
    }
}

$patient = New-AppSession
$pharmacistHeaders = New-AdminHeaders

$presign = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/files/presign" -Headers $patient.headers -Body @{
    filename = "smoke-prescription.png"
    contentType = "image/png"
    sizeBytes = 68
    businessType = "PRESCRIPTION"
}

$uploaded = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/prescriptions/uploads" -Headers $patient.headers -Body @{
    objectKey = $presign.data.objectKey
    originalFilename = "smoke-prescription.png"
    contentType = "image/png"
    sizeBytes = 68
}

$firstConfirm = Invoke-JsonRequestAllowFailure -Method Post -Url "$BaseUrl/admin-api/prescriptions/$($uploaded.data.id)/confirm" -Headers $pharmacistHeaders -Body @{
    items = @(
        @{
            drugName = "Aspirin"
            dosage = "100mg"
            frequency = "QD"
            route = "PO"
            durationDays = 7
        },
        @{
            drugName = "Warfarin"
            dosage = "2.5mg"
            frequency = "QD"
            route = "PO"
            durationDays = 7
        }
    )
}

$detailBeforeConfirm = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/prescriptions/$($uploaded.data.id)" -Headers $pharmacistHeaders
$screeningId = $detailBeforeConfirm.data.pendingDdiScreenings[0].id

$screeningConfirm = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/ddi/screenings/$screeningId/confirm" -Headers $pharmacistHeaders -Body @{
    note = "Smoke confirmed continue"
}

$finalConfirm = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/prescriptions/$($uploaded.data.id)/confirm" -Headers $pharmacistHeaders -Body @{
    items = @(
        @{
            drugName = "Aspirin"
            dosage = "100mg"
            frequency = "QD"
            route = "PO"
            durationDays = 7
        },
        @{
            drugName = "Warfarin"
            dosage = "2.5mg"
            frequency = "QD"
            route = "PO"
            durationDays = 7
        }
    )
}

$rejectPresign = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/files/presign" -Headers $patient.headers -Body @{
    filename = "reject-prescription.png"
    contentType = "image/png"
    sizeBytes = 68
    businessType = "PRESCRIPTION"
}

$rejectUploaded = Invoke-JsonRequest -Method Post -Url "$BaseUrl/app-api/prescriptions/uploads" -Headers $patient.headers -Body @{
    objectKey = $rejectPresign.data.objectKey
    originalFilename = "reject-prescription.png"
    contentType = "image/png"
    sizeBytes = 68
}

$rejectResult = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/prescriptions/$($rejectUploaded.data.id)/reject" -Headers $pharmacistHeaders -Body @{
    reason = "Image needs re-upload"
}

$rejectDetail = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/prescriptions/$($rejectUploaded.data.id)" -Headers $pharmacistHeaders
$list = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/prescriptions?pageNo=1&pageSize=20" -Headers $pharmacistHeaders

[pscustomobject]@{
    uploadedPrescriptionId = $uploaded.data.id
    firstConfirmOk = $firstConfirm.ok
    firstConfirmCode = $firstConfirm.payload.code
    firstConfirmMsg = $firstConfirm.payload.msg
    screeningId = $screeningId
    screeningConfirmStatus = $screeningConfirm.data.confirmStatus
    finalConfirmStatus = $finalConfirm.data.status
    rejectPrescriptionId = $rejectUploaded.data.id
    rejectStatus = $rejectResult.data.status
    rejectReason = $rejectDetail.data.rejectReason
    listTotal = $list.data.total
} | ConvertTo-Json -Depth 6
