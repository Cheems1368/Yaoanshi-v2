param(
    [string]$ImageName = "medicine-backend-v2",
    [string]$Tag = "latest",
    [string]$Environment = "prod",
    [switch]$ScanSecurity = $true
)

$ErrorActionPreference = "Stop"

Write-Host "[1] Build image" -ForegroundColor Cyan
$buildArgs = @(
    "build",
    "-f", "../../backend-v2/Dockerfile",
    "-t", "$ImageName`:$Tag",
    "--build-arg", "BUILDKIT_INLINE_CACHE=1",
    "../.."
)
if ($Environment -eq "prod") {
    $buildArgs += "--no-cache"
}
& docker @buildArgs

if (-not $ScanSecurity) {
    exit 0
}

Write-Host "[2] Optional Trivy scan" -ForegroundColor Cyan
try {
    & trivy image --severity HIGH,CRITICAL "$ImageName`:$Tag"
}
catch {
    Write-Warning "Trivy unavailable, skipped security scan."
}
