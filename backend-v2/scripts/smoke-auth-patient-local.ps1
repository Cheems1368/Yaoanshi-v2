param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [string]$Mobile = "13800138000",
    [string]$DeviceId = "patient-web-local",
    [string]$HospitalPatientId = "HZY-1001",
    [string]$PatientName = "Test Patient"
)

$ErrorActionPreference = "Stop"

function Invoke-JsonPost {
    param(
        [string]$Url,
        [hashtable]$Body,
        [hashtable]$Headers = @{}
    )

    return Invoke-RestMethod -Method Post -Uri $Url -ContentType "application/json" -Headers $Headers -Body ($Body | ConvertTo-Json)
}

function Invoke-JsonPostAllowFailure {
    param(
        [string]$Url,
        [hashtable]$Body,
        [hashtable]$Headers = @{}
    )

    try {
        $response = Invoke-WebRequest -Method Post -Uri $Url -ContentType "application/json" -Headers $Headers -Body ($Body | ConvertTo-Json)
        return [pscustomobject]@{
            statusCode = [int]$response.StatusCode
            payload = $response.Content | ConvertFrom-Json
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
            statusCode = [int]$httpResponse.StatusCode
            payload = $content | ConvertFrom-Json
        }
    }
}

$sendOtp = Invoke-JsonPost -Url "$BaseUrl/app-api/auth/send-otp" -Body @{
    mobile = $Mobile
    scene = "APP_LOGIN"
}

$login = Invoke-JsonPost -Url "$BaseUrl/app-api/auth/login" -Body @{
    mobile = $Mobile
    otpCode = "123456"
    deviceId = $DeviceId
    deviceName = "Local Smoke"
    platform = "WEB"
}

$accessToken = $login.data.accessToken
$headers = @{
    Authorization = "Bearer $accessToken"
}

$meBeforeBind = Invoke-RestMethod -Method Get -Uri "$BaseUrl/app-api/auth/me" -Headers $headers

$bind = Invoke-JsonPost -Url "$BaseUrl/app-api/patients/bind" -Headers $headers -Body @{
    mobile = $Mobile
    hospitalPatientId = $HospitalPatientId
    name = $PatientName
}

$refresh = Invoke-JsonPost -Url "$BaseUrl/app-api/auth/refresh-token" -Body @{
    refreshToken = $login.data.refreshToken
    deviceId = $DeviceId
}

$refreshedHeaders = @{
    Authorization = "Bearer $($refresh.data.accessToken)"
}

$logout = Invoke-JsonPost -Url "$BaseUrl/app-api/auth/logout" -Headers $refreshedHeaders -Body @{
    deviceId = $DeviceId
}

$refreshAfterLogout = Invoke-JsonPostAllowFailure -Url "$BaseUrl/app-api/auth/refresh-token" -Body @{
    refreshToken = $refresh.data.refreshToken
    deviceId = $DeviceId
}

$meAfterBind = Invoke-RestMethod -Method Get -Uri "$BaseUrl/app-api/auth/me" -Headers $headers
$patientMe = Invoke-RestMethod -Method Get -Uri "$BaseUrl/app-api/patients/me" -Headers $headers

[pscustomobject]@{
    sendOtpCode = $sendOtp.code
    loginCode = $login.code
    authMeBoundBeforeBind = $meBeforeBind.data.bound
    bindCode = $bind.code
    refreshCode = $refresh.code
    logoutCode = $logout.code
    refreshAfterLogoutHttpStatus = $refreshAfterLogout.statusCode
    refreshAfterLogoutCode = $refreshAfterLogout.payload.code
    refreshAfterLogoutMsg = $refreshAfterLogout.payload.msg
    authMeBoundAfterBind = $meAfterBind.data.bound
    patientMeCode = $patientMe.code
    patientId = $patientMe.data.id
    bound = $patientMe.data.bound
    deviceId = $login.data.deviceId
    refreshedDeviceId = $refresh.data.deviceId
} | ConvertTo-Json
