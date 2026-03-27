param(
    [string]$EnvFile = (Join-Path (Split-Path -Parent $PSScriptRoot) ".env.local"),
    [string]$SpringProfile = "local",
    [switch]$NoStart
)

$ErrorActionPreference = "Stop"

function Normalize-EnvValue {
    param([string]$Value)

    if ($null -eq $Value) {
        return ""
    }

    $trimmed = $Value.Trim()
    if ($trimmed.Length -ge 2) {
        $first = $trimmed[0]
        $last = $trimmed[$trimmed.Length - 1]
        if (($first -eq '"' -and $last -eq '"') -or ($first -eq "'" -and $last -eq "'")) {
            return $trimmed.Substring(1, $trimmed.Length - 2)
        }
    }

    return $trimmed
}

function Get-EnvValue {
    param([string]$Name)

    return [System.Environment]::GetEnvironmentVariable($Name, 'Process')
}

function Set-EnvIfMissing {
    param(
        [string]$Name,
        [string]$Value
    )

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return
    }

    if ([string]::IsNullOrWhiteSpace((Get-EnvValue $Name))) {
        [System.Environment]::SetEnvironmentVariable($Name, $Value, 'Process')
        if ($loaded -notcontains $Name) {
            $script:loaded += $Name
        }
    }
}

function Get-DevPasswordFromDockerEnv {
    $workspaceRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
    $dockerEnvPath = Join-Path $workspaceRoot "ops\docker.env"
    if (-not (Test-Path $dockerEnvPath)) {
        return $null
    }

    $line = Get-Content $dockerEnvPath | Where-Object { $_ -like "MYSQL_ROOT_PASSWORD=*" } | Select-Object -First 1
    if (-not $line) {
        return $null
    }

    return ($line -split "=", 2)[1].Trim()
}

$envFilePath = [System.IO.Path]::GetFullPath($EnvFile)
if (-not (Test-Path $envFilePath)) {
    throw "Missing env file: $envFilePath. Copy backend-v2/.env.local.example to backend-v2/.env.local first."
}

$loaded = @()
Get-Content $envFilePath | ForEach-Object {
    $line = $_
    if ([string]::IsNullOrWhiteSpace($line)) {
        return
    }

    $trimmed = $line.Trim()
    if ($trimmed.StartsWith('#')) {
        return
    }

    $parts = $line -split '=', 2
    if ($parts.Count -ne 2) {
        throw "Invalid .env line: $line"
    }

    $name = $parts[0].Trim()
    if (-not $name) {
        throw "Invalid env variable name in line: $line"
    }

    $value = Normalize-EnvValue $parts[1]
    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
    $loaded += $name
}

if ($loaded.Count -eq 0) {
    throw "No env variables were loaded from $envFilePath"
}

$dbPassword = Get-EnvValue "MEDICINE_DB_PASSWORD"
if ([string]::IsNullOrWhiteSpace($dbPassword)) {
    $dbPassword = Get-DevPasswordFromDockerEnv
}
Set-EnvIfMissing -Name "MEDICINE_DB_PASSWORD" -Value $dbPassword
Set-EnvIfMissing -Name "MEDICINE_LEGACY_DB_PASSWORD" -Value (Get-EnvValue "MEDICINE_DB_PASSWORD")
Set-EnvIfMissing -Name "MEDICINE_REDIS_PORT" -Value "16379"
Set-EnvIfMissing -Name "MEDICINE_JWT_SECRET" -Value "local-dev-jwt-secret-change-me"
Set-EnvIfMissing -Name "MEDICINE_STORAGE_ENDPOINT" -Value "http://127.0.0.1:9000"
Set-EnvIfMissing -Name "MEDICINE_STORAGE_ACCESS_KEY" -Value "minioadmin"
Set-EnvIfMissing -Name "MEDICINE_STORAGE_SECRET_KEY" -Value "minioadmin"
Set-EnvIfMissing -Name "MEDICINE_STORAGE_BUCKET" -Value "yaoanshi-mvp"
Set-EnvIfMissing -Name "MEDICINE_STORAGE_REGION" -Value "us-east-1"
Set-EnvIfMissing -Name "MEDICINE_OCR_BASE_URL" -Value "https://api.siliconflow.cn/v1/chat/completions"
Set-EnvIfMissing -Name "MEDICINE_OCR_MODEL" -Value "deepseek-ai/deepseek-vl2"
Set-EnvIfMissing -Name "MEDICINE_OCR_TIMEOUT_MS" -Value "30000"

if (-not [string]::IsNullOrWhiteSpace($SpringProfile)) {
    [System.Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", $SpringProfile, 'Process')
    if ($loaded -notcontains "SPRING_PROFILES_ACTIVE") {
        $loaded += "SPRING_PROFILES_ACTIVE"
    }
}

Write-Host "Loaded env file: $envFilePath"
foreach ($name in $loaded) {
    $displayValue = if ($name -like '*API_KEY*' -or $name -like '*SECRET*') { '<set>' } else { [System.Environment]::GetEnvironmentVariable($name, 'Process') }
    Write-Host ("  {0}={1}" -f $name, $displayValue)
}

if ($NoStart) {
    Write-Host "NoStart specified, skipping backend startup."
    return
}

$pomPath = [System.IO.Path]::GetFullPath((Join-Path (Split-Path -Parent $PSScriptRoot) "pom.xml"))
$mavenGlobalSettingsPath = [System.IO.Path]::GetFullPath((Join-Path (Split-Path -Parent $PSScriptRoot) ".mvn\global-settings.xml"))
Write-Host ("Starting backend-v2 with Maven (profile={0})..." -f [System.Environment]::GetEnvironmentVariable("SPRING_PROFILES_ACTIVE", 'Process'))
$mavenArgs = @()
if (Test-Path $mavenGlobalSettingsPath) {
    # Bypass broken machine-wide mirrors and use repo-pinned Maven settings instead.
    Write-Host ("Using repo Maven global settings: {0}" -f $mavenGlobalSettingsPath)
    $mavenArgs += @("-gs", $mavenGlobalSettingsPath)
}
$mavenArgs += @("spring-boot:run", "-f", $pomPath)
& mvn @mavenArgs
exit $LASTEXITCODE
