param([switch]$OcrOnly)
$ErrorActionPreference = 'Stop'
$statePath = 'D:\RapidOCR\processes.json'
if (-not (Test-Path -LiteralPath $statePath)) { Write-Host 'No owned processes recorded.'; return }
$saved = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
$remaining = @{}
function Stop-OwnedChildren([int]$ParentId, [long]$ParentStartedTicks) {
    foreach ($child in Get-CimInstance Win32_Process -Filter "ParentProcessId=$ParentId") {
        $childProcess = Get-Process -Id $child.ProcessId -ErrorAction SilentlyContinue
        if ($childProcess -and $childProcess.Path -eq $child.ExecutablePath -and $childProcess.StartTime.ToUniversalTime().Ticks -ge $ParentStartedTicks) {
            Stop-OwnedChildren $childProcess.Id $childProcess.StartTime.ToUniversalTime().Ticks
            Stop-Process -InputObject $childProcess
        }
    }
}
foreach ($property in $saved.PSObject.Properties) {
    if ($OcrOnly -and $property.Name -ne 'ocr') { $remaining[$property.Name] = $property.Value; continue }
    $record = $property.Value
    $process = Get-Process -Id $record.id -ErrorAction SilentlyContinue
    if (-not $process) { continue }
    if ($process.StartTime.ToUniversalTime().Ticks.ToString() -ne $record.startedTicks -or $process.Path -ne $record.executable) {
        throw "Process identity changed for $($property.Name); refusing to stop it."
    }
    Stop-OwnedChildren $process.Id $process.StartTime.ToUniversalTime().Ticks
    Stop-Process -InputObject $process
    Write-Host "Stopped $($property.Name)."
}
$remaining | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $statePath -Encoding utf8
Write-Host 'Docker services and verification data are retained.'
