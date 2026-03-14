param(
    [string]$ImageName = "medicine-backend-v2",
    [string]$Tag = "latest"
)

$ErrorActionPreference = "Stop"

$containerName = "smoke-test-container"
try {
    docker rm -f $containerName | Out-Null 2>$null
}
catch {
}

Write-Host "[1] Start smoke container" -ForegroundColor Cyan
docker run -d --name $containerName -p 18081:8080 "$ImageName`:$Tag" | Out-Null

try {
    Start-Sleep -Seconds 25
    Write-Host "[2] Check /health" -ForegroundColor Cyan
    $response = Invoke-WebRequest -Uri "http://localhost:18081/health" -TimeoutSec 10
    if ($response.StatusCode -ne 200) {
        throw "Health endpoint returned $($response.StatusCode)"
    }

    Write-Host "[3] Check runtime user" -ForegroundColor Cyan
    $user = docker exec $containerName whoami
    if ($user.Trim() -ne "appuser") {
        throw "Unexpected runtime user: $user"
    }
}
finally {
    docker rm -f $containerName | Out-Null 2>$null
}
