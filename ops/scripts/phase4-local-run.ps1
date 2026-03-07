param(
    [string]$BackendBaseUrl = "http://127.0.0.1:8080",
    [string]$LegacyBaseUrl = "http://127.0.0.1:48080",
    [string]$PatientBaseUrl = "http://127.0.0.1:3001",
    [string]$DoctorBaseUrl = "http://127.0.0.1:3002",
    [string]$PharmacistBaseUrl = "http://127.0.0.1:3003",
    [string]$JavaHome = "D:\Java\Java 17",
    [string]$DbPassword = $env:MEDICINE_DB_PASSWORD,
    [string]$LegacyDbPassword = $env:MEDICINE_LEGACY_DB_PASSWORD,
    [string]$RedisPort = "16379",
    [int]$JwtAccessTokenSeconds = 7200,
    [switch]$SkipBuild,
    [switch]$SkipExpiryE2E
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Test-HttpReady {
    param(
        [string]$Url,
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-WebRequest -Uri $Url -TimeoutSec 5 | Out-Null
            return
        } catch {
            Start-Sleep -Seconds 2
        }
    }

    throw "Timed out waiting for $Url"
}

function Test-PortListening {
    param([int]$Port)
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Start-ProcessIfNeeded {
    param(
        [int]$Port,
        [string]$FilePath,
        [string]$Arguments,
        [string]$WorkingDirectory
    )

    if (Test-PortListening -Port $Port) {
        Write-Host "Port $Port already listening, reuse existing process." -ForegroundColor Yellow
        return
    }

    Start-Process -FilePath $FilePath -ArgumentList $Arguments -WorkingDirectory $WorkingDirectory | Out-Null
}

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

$workspace = Split-Path -Parent $PSScriptRoot
$root = Split-Path -Parent $workspace

if (-not $DbPassword) {
    $DbPassword = Get-DevPasswordFromDockerEnv -RootPath $root
}

if (-not $DbPassword) {
    throw "MEDICINE_DB_PASSWORD is required. Set it in the environment before running this script."
}

if (-not $LegacyDbPassword) {
    $LegacyDbPassword = $DbPassword
}

$backendJar = Join-Path $root "backend-v2\target\backend-v2-0.0.1-SNAPSHOT.jar"
$javaExe = Join-Path $JavaHome "bin\java.exe"

Write-Step "Start legacy Docker services"
docker compose -f (Join-Path $root "backend\medicinal-his-delivery\docker-compose.yml") up -d mysql redis server | Out-Null
Test-HttpReady -Url "$LegacyBaseUrl/actuator/health" -TimeoutSeconds 120

if (-not $SkipBuild) {
    Write-Step "Build backend-v2"
    mvn -q -DskipTests compile -f (Join-Path $root "backend-v2\pom.xml")

    Write-Step "Build frontends"
    npm run build --prefix (Join-Path $root "frontend\huanzhe")
    npm run build --prefix (Join-Path $root "frontend\pharmacist")
    npm run build --prefix (Join-Path $root "frontend\yisheng\web-admin")
}

Write-Step "Start backend-v2"
$env:MEDICINE_DB_PASSWORD = $DbPassword
$env:MEDICINE_LEGACY_DB_PASSWORD = $LegacyDbPassword
$env:MEDICINE_REDIS_PORT = $RedisPort
$env:MEDICINE_JWT_ACCESS_TOKEN_SECONDS = "$JwtAccessTokenSeconds"
Start-ProcessIfNeeded -Port 8080 -FilePath $javaExe -Arguments "-jar `"$backendJar`" --spring.profiles.active=local" -WorkingDirectory (Join-Path $root "backend-v2")
Test-HttpReady -Url "$BackendBaseUrl/health" -TimeoutSeconds 120

Write-Step "Start frontend dev servers"
Start-ProcessIfNeeded -Port 3001 -FilePath "npm.cmd" -Arguments "run dev -- --host 127.0.0.1 --port 3001" -WorkingDirectory (Join-Path $root "frontend\huanzhe")
Start-ProcessIfNeeded -Port 3002 -FilePath "npm.cmd" -Arguments "run dev -- --host 127.0.0.1 --port 3002" -WorkingDirectory (Join-Path $root "frontend\yisheng\web-admin")
Start-ProcessIfNeeded -Port 3003 -FilePath "npm.cmd" -Arguments "run dev -- --host 127.0.0.1 --port 3003" -WorkingDirectory (Join-Path $root "frontend\pharmacist")
if (-not (Test-PortListening -Port 3001)) {
    throw "Patient frontend on port 3001 is not listening."
}
if (-not (Test-PortListening -Port 3002)) {
    throw "Doctor frontend on port 3002 is not listening."
}
if (-not (Test-PortListening -Port 3003)) {
    throw "Pharmacist frontend on port 3003 is not listening."
}

Write-Step "Run backend smoke"
powershell -ExecutionPolicy Bypass -File (Join-Path $root "backend-v2\scripts\smoke-core-flows-local.ps1") -BaseUrl $BackendBaseUrl -LegacyBaseUrl $LegacyBaseUrl
if ($LASTEXITCODE -ne 0) {
    throw "Backend smoke failed."
}

Write-Step "Run browser E2E"
node (Join-Path $root "frontend\huanzhe\scripts\e2e-auth-refresh.mjs")
if ($LASTEXITCODE -ne 0) {
    throw "e2e-auth-refresh failed."
}
node (Join-Path $root "frontend\huanzhe\scripts\e2e-pharmacist-patient.mjs")
if ($LASTEXITCODE -ne 0) {
    throw "e2e-pharmacist-patient failed."
}
if ($JwtAccessTokenSeconds -ge 10) {
    node (Join-Path $root "frontend\huanzhe\scripts\e2e-admin-notifications.mjs")
    if ($LASTEXITCODE -ne 0) {
        throw "e2e-admin-notifications failed."
    }
} else {
    Write-Host "Skip realtime-notification E2E under short JWT TTL mode." -ForegroundColor Yellow
}

if (-not $SkipExpiryE2E) {
    if ($JwtAccessTokenSeconds -ge 10) {
        throw "Natural expiry E2E requires JwtAccessTokenSeconds < 10."
    }
    $env:E2E_ACCESS_EXPIRY_WAIT_MS = [Math]::Max($JwtAccessTokenSeconds * 1000 + 1500, 4000)
    node (Join-Path $root "frontend\huanzhe\scripts\e2e-auth-expiry.mjs")
    if ($LASTEXITCODE -ne 0) {
        throw "e2e-auth-expiry failed."
    }
}

Write-Step "Phase 4 local run completed"
Write-Host "Backend: $BackendBaseUrl"
Write-Host "Patient: $PatientBaseUrl"
Write-Host "Doctor: $DoctorBaseUrl"
Write-Host "Pharmacist: $PharmacistBaseUrl"
