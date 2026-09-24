param(
    [string]$JavaHome = "",
    [switch]$NoSync
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

    throw "JDK 21 is required for the Capacitor Android debug build. Set -JavaHome, JAVA21_HOME, or JDK21_HOME."
}

$root = Split-Path -Parent $PSScriptRoot
$jdk21 = Resolve-Jdk21
$env:JAVA_HOME = $jdk21
$env:Path = "$jdk21\bin;$env:Path"
$env:CAPACITOR_ANDROID_LOCAL_HTTP = "true"

Push-Location $root
try {
    if (-not $NoSync) {
        & "npx.cmd" cap sync android
        if ($LASTEXITCODE -ne 0) {
            throw "Capacitor sync failed."
        }
    }

    Push-Location "android"
    try {
        & ".\gradlew.bat" ":app:assembleDebug" "--no-daemon"
        if ($LASTEXITCODE -ne 0) {
            throw "Android debug build failed."
        }
    } finally {
        Pop-Location
    }
} finally {
    Pop-Location
}
