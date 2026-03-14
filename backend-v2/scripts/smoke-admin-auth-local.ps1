param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [string]$Username = "pharmacist",
    [string]$Password = "123456",
    [string]$Role = "pharmacist"
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
        $params.Body = $Body | ConvertTo-Json -Depth 10
    }

    Invoke-RestMethod @params
}

function Invoke-JsonRequestAllowFailure {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        $Body = $null
    )

    try {
        $payload = Invoke-JsonRequest -Method $Method -Url $Url -Headers $Headers -Body $Body
        return [pscustomobject]@{
            ok = $true
            payload = $payload
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

$tenantHeaders = @{ "tenant-id" = "1" }
$login = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/system/auth/login" -Headers $tenantHeaders -Body @{
    username = $Username
    password = $Password
    role = $Role
}

$accessToken = $login.data.accessToken
$refreshToken = $login.data.refreshToken
if (-not $accessToken -or -not $refreshToken) {
    throw "Login did not return accessToken and refreshToken"
}

$authHeaders = @{
    Authorization = "Bearer $accessToken"
    "tenant-id" = "1"
}

$permissionInfo = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/system/auth/get-permission-info" -Headers $authHeaders
if (-not $permissionInfo.data.user.id -or -not $permissionInfo.data.user.username) {
    throw "Permission info did not return expected user fields"
}

$patients = Invoke-JsonRequest -Method Get -Url "$BaseUrl/admin-api/patients?pageNo=1&pageSize=1" -Headers $authHeaders

$refresh = Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/system/auth/refresh-token?refreshToken=$([uri]::EscapeDataString($refreshToken))" -Headers $tenantHeaders
$newAccessToken = $refresh.data.accessToken
$newRefreshToken = $refresh.data.refreshToken
if (-not $newAccessToken -or -not $newRefreshToken) {
    throw "Refresh did not return a new token pair"
}

$newAuthHeaders = @{
    Authorization = "Bearer $newAccessToken"
    "tenant-id" = "1"
}

Invoke-JsonRequest -Method Post -Url "$BaseUrl/admin-api/system/auth/logout" -Headers $newAuthHeaders | Out-Null
$refreshAfterLogout = Invoke-JsonRequestAllowFailure -Method Post -Url "$BaseUrl/admin-api/system/auth/refresh-token?refreshToken=$([uri]::EscapeDataString($newRefreshToken))" -Headers $tenantHeaders

[pscustomobject]@{
    loginCode = $login.code
    permissionInfoCode = $permissionInfo.code
    userId = $permissionInfo.data.user.id
    username = $permissionInfo.data.user.username
    roles = $permissionInfo.data.roles
    permissions = $permissionInfo.data.permissions
    protectedApiCode = $patients.code
    refreshCode = $refresh.code
    refreshAfterLogoutOk = $refreshAfterLogout.ok
    refreshAfterLogoutCode = $refreshAfterLogout.payload.code
    refreshAfterLogoutMsg = $refreshAfterLogout.payload.msg
} | ConvertTo-Json -Depth 8
