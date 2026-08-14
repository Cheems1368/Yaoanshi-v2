param(
    [string]$DeviceId = "",
    [int]$PatientPort = 3001,
    [int]$BackendPort = 8080,
    [int]$MinioPort = 9000,
    [string]$AndroidSdkRoot = $env:ANDROID_HOME,
    [string]$JavaHome = ""
)

$ErrorActionPreference = "Stop"

function Test-Jdk21 {
    param([string]$Candidate)

    if (-not $Candidate -or -not (Test-Path (Join-Path $Candidate "bin\java.exe"))) {
        return $false
    }

    $releaseFile = Join-Path $Candidate "release"
    if (-not (Test-Path $releaseFile)) {
        return $false
    }

    return [bool](Select-String -Path $releaseFile -Pattern 'JAVA_VERSION="21' -Quiet)
}

function Resolve-Jdk21 {
    $candidates = @(
        $JavaHome,
        $env:JAVA21_HOME,
        $env:JDK21_HOME,
        $env:JAVA_HOME,
        "D:\Java\java21",
        "C:\Program Files\Java\jdk-21"
    )

    foreach ($candidate in $candidates | Where-Object { $_ } | Select-Object -Unique) {
        if (Test-Jdk21 -Candidate $candidate) {
            return $candidate
        }
    }

    throw "JDK 21 is required for Capacitor Android live reload. Set -JavaHome, JAVA21_HOME, or JDK21_HOME."
}

function Resolve-Adb {
    $candidates = @()
    if ($AndroidSdkRoot) {
        $candidates += (Join-Path $AndroidSdkRoot "platform-tools\adb.exe")
    }
    if ($env:ANDROID_SDK_ROOT) {
        $candidates += (Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe")
    }
    $pathAdb = Get-Command adb -ErrorAction SilentlyContinue
    if ($pathAdb) {
        $candidates += $pathAdb.Source
    }

    foreach ($candidate in $candidates | Where-Object { $_ } | Select-Object -Unique) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "adb.exe not found. Set ANDROID_HOME or ANDROID_SDK_ROOT to your Android SDK path."
}

function Get-ConnectedDeviceId {
    param([string]$AdbPath)

    if ($DeviceId) {
        return $DeviceId
    }

    $devices = @(& $AdbPath devices |
        Select-Object -Skip 1 |
        Where-Object { $_ -match "\tdevice$" } |
        ForEach-Object { ($_ -split "\s+")[0] })

    if ($devices.Count -eq 0) {
        throw "No Android device is connected for adb debugging."
    }
    if ($devices.Count -gt 1) {
        throw "Multiple Android devices found. Re-run with -DeviceId <id>."
    }

    return $devices[0]
}

function Test-PortListening {
    param([int]$Port)
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Wait-HttpReady {
    param(
        [string]$Url,
        [int]$TimeoutSeconds = 60
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-WebRequest -Uri $Url -TimeoutSec 3 | Out-Null
            return
        } catch {
            Start-Sleep -Seconds 2
        }
    }

    throw "Timed out waiting for $Url"
}

function Set-LiveReloadConfig {
    param(
        [string]$ConfigPath,
        [string]$Url
    )

    $originalContent = Get-Content -Path $ConfigPath -Raw -Encoding UTF8
    $config = $originalContent | ConvertFrom-Json
    $config | Add-Member -MemberType NoteProperty -Name server -Value ([pscustomobject]@{ url = $Url }) -Force
    $config | ConvertTo-Json -Depth 10 | Set-Content -Path $ConfigPath -Encoding UTF8

    return $originalContent
}

$root = Split-Path -Parent $PSScriptRoot
$jdk21 = Resolve-Jdk21
$env:JAVA_HOME = $jdk21
$env:Path = "$jdk21\bin;$env:Path"
$adb = Resolve-Adb
$targetDevice = Get-ConnectedDeviceId -AdbPath $adb

if (-not (Test-PortListening -Port $PatientPort)) {
    Start-Process `
        -FilePath "npm.cmd" `
        -ArgumentList "run dev -- --host 127.0.0.1 --port $PatientPort" `
        -WorkingDirectory $root `
        -WindowStyle Hidden | Out-Null
}

Wait-HttpReady -Url "http://127.0.0.1:$PatientPort"

& $adb -s $targetDevice reverse "tcp:$PatientPort" "tcp:$PatientPort" | Out-Null
& $adb -s $targetDevice reverse "tcp:$BackendPort" "tcp:$BackendPort" | Out-Null
& $adb -s $targetDevice reverse "tcp:$MinioPort" "tcp:$MinioPort" | Out-Null

Push-Location $root
try {
    & "npx.cmd" cap sync android
    if ($LASTEXITCODE -ne 0) {
        throw "Capacitor sync failed."
    }

    $configPath = Join-Path $root "android\app\src\main\assets\capacitor.config.json"
    $originalConfig = Set-LiveReloadConfig -ConfigPath $configPath -Url "http://127.0.0.1:$PatientPort"

    try {
        Push-Location "android"
        try {
            & ".\gradlew.bat" ":app:assembleDebug" "--no-daemon"
            if ($LASTEXITCODE -ne 0) {
                throw "Android debug build failed."
            }
        } finally {
            Pop-Location
        }

        $apkPath = Join-Path $root "android\app\build\outputs\apk\debug\app-debug.apk"
        & $adb -s $targetDevice install -r $apkPath
        if ($LASTEXITCODE -ne 0) {
            throw "Android APK install failed."
        }

        & $adb -s $targetDevice shell am start -n "com.yaoanshi.patient/.MainActivity" | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Android app launch failed."
        }
    } finally {
        Set-Content -Path $configPath -Value $originalConfig -Encoding UTF8
    }
} finally {
    Pop-Location
}
