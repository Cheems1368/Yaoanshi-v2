param(
    [string]$ConfigPath = (Join-Path $PSScriptRoot 'nginx-prod.conf')
)

$config = Get-Content -LiteralPath $ConfigPath -Raw
$loginPaths = @(
    '/admin-api/system/auth/login',
    '/admin-api/system/auth/session/login'
)

foreach ($loginPath in $loginPaths) {
    $escapedPath = [regex]::Escape($loginPath)
    $location = [regex]::Match(
        $config,
        "location\s+$escapedPath\s*\{(?<body>.*?)\}",
        [System.Text.RegularExpressions.RegexOptions]::Singleline
    )

    if (-not $location.Success) {
        throw "Missing production Nginx location for $loginPath"
    }

    if ($location.Groups['body'].Value -notmatch 'limit_req\s+zone=login\s+burst=5\s+nodelay;') {
        throw "Production Nginx login limiter is not applied to $loginPath"
    }
}

Write-Output 'Production authentication rate-limit configuration is valid.'
