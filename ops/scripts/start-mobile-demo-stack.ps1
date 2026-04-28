param(
    [string]$Root = (Split-Path -Parent (Split-Path -Parent $PSScriptRoot)),
    [switch]$SkipDocker,
    [switch]$SkipAccountEnsure
)

$ErrorActionPreference = "Stop"

function Test-PortListening {
    param([int]$Port)
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Wait-HttpReady {
    param(
        [string]$Url,
        [int]$TimeoutSeconds = 120
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

function Start-DevServerIfNeeded {
    param(
        [int]$Port,
        [string]$WorkingDirectory
    )

    if (Test-PortListening -Port $Port) {
        Write-Host "Port $Port already listening; reusing existing dev server."
        return
    }

    Start-Process `
        -FilePath "npm.cmd" `
        -ArgumentList "run dev -- --host 127.0.0.1 --port $Port" `
        -WorkingDirectory $WorkingDirectory `
        -WindowStyle Hidden | Out-Null
}

if (-not $SkipDocker) {
    docker compose -f (Join-Path $Root "ops\docker-compose.yml") up -d --build mysql redis minio backend-v2 | Out-Null
    Wait-HttpReady -Url "http://127.0.0.1:8080/health"
}

if (-not $SkipAccountEnsure) {
    powershell -ExecutionPolicy Bypass -File (Join-Path $Root "ops\scripts\ensure-local-professional-test-accounts.ps1") `
        -MysqlContainer "yaoshian-mysql" `
        -Database "yaoshian_mvp" `
        -DbPassword "123456"
}

Start-DevServerIfNeeded -Port 3001 -WorkingDirectory (Join-Path $Root "frontend\huanzhe")
Start-DevServerIfNeeded -Port 3002 -WorkingDirectory (Join-Path $Root "frontend\yisheng\web-admin")
Start-DevServerIfNeeded -Port 3003 -WorkingDirectory (Join-Path $Root "frontend\pharmacist")

Wait-HttpReady -Url "http://127.0.0.1:3001"
Wait-HttpReady -Url "http://127.0.0.1:3002"
Wait-HttpReady -Url "http://127.0.0.1:3003"

Write-Host "Demo stack ready:"
Write-Host "  Backend:    http://127.0.0.1:8080"
Write-Host "  Patient:    http://127.0.0.1:3001"
Write-Host "  Doctor:     http://127.0.0.1:3002"
Write-Host "  Pharmacist: http://127.0.0.1:3003"
