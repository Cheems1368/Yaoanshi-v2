param([string]$InstallRoot = 'D:\RapidOCR')
$ErrorActionPreference = 'Stop'
$InstallRoot = [IO.Path]::GetFullPath($InstallRoot)
if ($InstallRoot -ne 'D:\RapidOCR') { throw 'This installation is pinned to D:\RapidOCR' }
foreach ($directory in @('', 'downloads', 'downloads\wheels', 'cache', 'tmp', 'models', 'results', 'logs')) {
    New-Item -ItemType Directory -Force -Path (Join-Path $InstallRoot $directory) | Out-Null
}
$env:TEMP = Join-Path $InstallRoot 'tmp'
$env:TMP = $env:TEMP
$env:PIP_CACHE_DIR = Join-Path $InstallRoot 'cache'
$installer = Join-Path $InstallRoot 'downloads\python-3.13.16-amd64.exe'
if (-not (Test-Path -LiteralPath $installer)) {
    Invoke-WebRequest -UseBasicParsing -Uri 'https://www.python.org/ftp/python/3.13.16/python-3.13.16-amd64.exe' -OutFile $installer
}
if ((Get-AuthenticodeSignature -LiteralPath $installer).Status -ne 'Valid') { throw 'Invalid Python installer signature' }
$python = Join-Path $InstallRoot 'python\python.exe'
if (-not (Test-Path -LiteralPath $python)) {
    $process = Start-Process -FilePath $installer -WindowStyle Hidden -PassThru -Wait -ArgumentList @(
        '/quiet', 'InstallAllUsers=0', "TargetDir=$InstallRoot\python", 'Include_launcher=0',
        'InstallLauncherAllUsers=0', 'PrependPath=0', 'Include_test=0', 'Include_doc=0',
        'Include_tcltk=0', 'AssociateFiles=0', 'Shortcuts=0', '/log', "$InstallRoot\logs\python-install.log")
    if ($process.ExitCode -notin 0,3010) { throw 'Python installation failed' }
}
if ((& $python --version) -ne 'Python 3.13.16') { throw 'Python version mismatch' }
$venvPython = Join-Path $InstallRoot 'venv\Scripts\python.exe'
if (-not (Test-Path -LiteralPath $venvPython)) {
    & $python -m venv (Join-Path $InstallRoot 'venv')
    if ($LASTEXITCODE -ne 0) { throw 'venv creation failed' }
}
$wheels = Join-Path $InstallRoot 'downloads\wheels'
& $venvPython -m pip download --only-binary=:all: --dest $wheels -r (Join-Path $PSScriptRoot 'requirements.lock.txt')
if ($LASTEXITCODE -ne 0) { throw 'Dependency download failed' }
# Compare every downloaded artifact against the publishing registry before installation.
$verifiedWheels = foreach ($wheel in Get-ChildItem -LiteralPath $wheels -Filter '*.whl') {
    $parts = $wheel.Name -split '-'
    $metadata = Invoke-RestMethod -Uri "https://pypi.org/pypi/$($parts[0])/$($parts[1])/json"
    $published = $metadata.urls | Where-Object { $_.filename -eq $wheel.Name } | Select-Object -First 1
    $sha256 = (Get-FileHash -LiteralPath $wheel.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    if (-not $published -or $sha256 -ne $published.digests.sha256) { throw "Wheel verification failed: $($wheel.Name)" }
    [pscustomobject]@{ name=$wheel.Name; sha256=$sha256; source=$published.url }
}
$verifiedWheels | ConvertTo-Json | Set-Content -LiteralPath "$InstallRoot\results\wheels.json" -Encoding utf8
& $venvPython -m pip install --no-index --find-links $wheels -r (Join-Path $PSScriptRoot 'requirements.lock.txt')
if ($LASTEXITCODE -ne 0) { throw 'Dependency installation failed' }
& $venvPython -m pip check
if ($LASTEXITCODE -ne 0) { throw 'Dependency compatibility failed' }
& $venvPython (Join-Path $PSScriptRoot 'prepare_models.py') --root $InstallRoot
if ($LASTEXITCODE -ne 0) { throw 'Model preparation failed' }
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'requirements.lock.txt') -Destination "$InstallRoot\requirements.lock.txt"
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'models.lock.json') -Destination "$InstallRoot\results\models.json"
Get-FileHash -LiteralPath $installer -Algorithm SHA256 | ConvertTo-Json |
    Set-Content -LiteralPath "$InstallRoot\results\python-installer.json" -Encoding utf8
Write-Host 'RapidOCR installation verified at D:\RapidOCR'
