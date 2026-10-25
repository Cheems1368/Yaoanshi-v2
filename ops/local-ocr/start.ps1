param([switch]$Validation, [switch]$OcrOnly)
$ErrorActionPreference = 'Stop'
$OutputEncoding = New-Object System.Text.UTF8Encoding($false)
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$runtimeRoot = 'D:\RapidOCR'
$statePath = Join-Path $runtimeRoot 'processes.json'
foreach ($directory in @('logs', 'tmp', 'results')) {
    New-Item -ItemType Directory -Force -Path (Join-Path $runtimeRoot $directory) | Out-Null
}
$env:TEMP = Join-Path $runtimeRoot 'tmp'
$env:TMP = $env:TEMP
$env:PYTHONPYCACHEPREFIX = Join-Path $runtimeRoot 'cache\pycache'
$state = @{}
if (Test-Path -LiteralPath $statePath) {
    $saved = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
    foreach ($property in $saved.PSObject.Properties) { $state[$property.Name] = $property.Value }
}

function Wait-Ready([string]$Url) {
    $deadline = (Get-Date).AddSeconds(120)
    while ((Get-Date) -lt $deadline) {
        try { Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 3 | Out-Null; return }
        catch { Start-Sleep -Seconds 1 }
    }
    throw "Service did not become ready: $Url. Check D:\RapidOCR\logs."
}

function Start-Owned([string]$Name, [string]$Executable, [string[]]$Arguments, [string]$Directory, [int]$Port) {
    $record = $state[$Name]
    if ($record) {
        $existing = Get-Process -Id $record.id -ErrorAction SilentlyContinue
        if ($existing -and $existing.StartTime.ToUniversalTime().Ticks.ToString() -eq $record.startedTicks -and $existing.Path -eq $record.executable) { return }
    }
    if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) {
        throw "Port $Port belongs to a process not started by this script."
    }
    $process = Start-Process -FilePath $Executable -ArgumentList $Arguments -WorkingDirectory $Directory `
        -WindowStyle Hidden -PassThru -RedirectStandardOutput "$runtimeRoot\logs\$Name.out.log" `
        -RedirectStandardError "$runtimeRoot\logs\$Name.err.log"
    $state[$Name] = @{ id=$process.Id; startedTicks=$process.StartTime.ToUniversalTime().Ticks.ToString(); executable=[IO.Path]::GetFullPath($Executable) }
    $state | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $statePath -Encoding utf8
}

$python = Join-Path $runtimeRoot 'venv\Scripts\python.exe'
if (-not (Test-Path -LiteralPath $python)) { throw 'Run ops/local-ocr/install.ps1 first.' }
Start-Owned 'ocr' $python @('-u', ('"' + (Join-Path $PSScriptRoot 'server.py') + '"')) $repoRoot 19090
Wait-Ready 'http://127.0.0.1:19090/health'
if ($OcrOnly) { Write-Host 'Local OCR ready at 127.0.0.1:19090'; return }

# Load existing secrets without echoing any environment values.
. (Join-Path $repoRoot 'backend-v2\scripts\run-local-with-env.ps1') -NoStart *> $null
docker compose -f (Join-Path $repoRoot 'ops\docker-compose.yml') up -d redis minio
if ($LASTEXITCODE -ne 0) { throw 'Redis/MinIO startup failed' }
$mysqlContainer = 'yaoshian-mysql'
if ($Validation) {
    $env:MYSQL_ROOT_PASSWORD = $env:MEDICINE_DB_PASSWORD
    docker compose -p yaoanshi-ocr -f (Join-Path $PSScriptRoot 'compose.validation.yml') up -d mysql
    if ($LASTEXITCODE -ne 0) { throw 'Validation MySQL startup failed' }
    $mysqlContainer = 'yaoanshi-ocr-mysql'
    $deadline = (Get-Date).AddSeconds(120)
    do {
        try {
            'SELECT 1;' | docker exec -i $mysqlContainer sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql -uroot' *> $null
            $mysqlReady = $LASTEXITCODE -eq 0
        } catch { $mysqlReady = $false }
        if ($mysqlReady) { break }
        if ((Get-Date) -gt $deadline) { throw 'Validation MySQL did not become ready' }
        Start-Sleep -Seconds 2
    } while ($true)
    $sqlSource = Get-Content -LiteralPath (Join-Path $repoRoot 'ops\sql\mysql\ruoyi-vue-pro.sql') -Raw -Encoding UTF8
    $tables = [regex]::Matches($sqlSource, '(?ms)^CREATE TABLE\s+`(system_[^`]+)`\s+.*?;')
    if ($tables.Count -eq 0) { throw 'Legacy authentication table baseline not found' }
    $ddl = ($tables | ForEach-Object { $_.Value -replace '^CREATE TABLE', 'CREATE TABLE IF NOT EXISTS' }) -join "`n"
    $ddl | docker exec -i $mysqlContainer sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql -uroot yaoanshi_ocr_validation'
    if ($LASTEXITCODE -ne 0) { throw 'Validation authentication schema initialization failed' }
    $env:MEDICINE_DB_URL = 'jdbc:mysql://127.0.0.1:18306/yaoanshi_ocr_validation?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true'
    $env:MEDICINE_LEGACY_DB_URL = $env:MEDICINE_DB_URL
    $env:MEDICINE_REDIS_DATABASE = '15'
    $env:MEDICINE_STORAGE_BUCKET = 'yaoanshi-ocr-validation'
} else {
    docker compose -f (Join-Path $repoRoot 'ops\docker-compose.yml') up -d mysql
    if ($LASTEXITCODE -ne 0) { throw 'MySQL startup failed. Use -Validation for the isolated 18306 environment.' }
}
$env:MEDICINE_LOCAL_OCR_BASE_URL = 'http://127.0.0.1:19090/ocr'
& mvn -q '-DskipTests' package -f (Join-Path $repoRoot 'backend-v2\pom.xml')
if ($LASTEXITCODE -ne 0) { throw 'Backend packaging failed' }
$jar = Get-ChildItem -LiteralPath (Join-Path $repoRoot 'backend-v2\target') -Filter 'backend-v2-*.jar' | Select-Object -First 1
Start-Owned 'backend' (Get-Command java.exe).Source @('-jar', ('"' + $jar.FullName + '"')) (Join-Path $repoRoot 'backend-v2') 8080
Wait-Ready 'http://127.0.0.1:8080/health'
if ($Validation) {
    & (Join-Path $repoRoot 'ops\scripts\ensure-local-professional-test-accounts.ps1') `
        -MysqlContainer $mysqlContainer -Database 'yaoanshi_ocr_validation' *> $null
    if ($LASTEXITCODE -ne 0) { throw 'Validation account preparation failed' }
}
$node = (Get-Command node.exe).Source
foreach ($frontend in @(@{name='patient';path='frontend\huanzhe';port=3001}, @{name='pharmacist';path='frontend\pharmacist';port=3003})) {
    $directory = Join-Path $repoRoot $frontend.path
    $vite = Join-Path $directory 'node_modules\vite\bin\vite.js'
    Start-Owned $frontend.name $node @(('"' + $vite + '"'), '--host', '127.0.0.1', '--port', $frontend.port.ToString(), '--strictPort') $directory $frontend.port
    Wait-Ready "http://127.0.0.1:$($frontend.port)"
}
Write-Host 'Ready: OCR 19090, backend 8080, patient Web 3001, pharmacist Web 3003.'
