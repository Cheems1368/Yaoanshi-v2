Param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('image','container','fs')]
    [string]$TargetType,

    [Parameter(Mandatory = $true)]
    [string]$TargetName,

    [string]$Severity = 'CRITICAL,HIGH',

    [ValidateSet('table','json','sarif','cyclonedx')]
    [string]$Format = 'table',

    [string]$OutputDir = './trivy-reports',

    [switch]$IgnoreUnfixed,

    [int]$TimeoutSeconds = 600
)

$ErrorActionPreference = 'Stop'

function Write-Section([string]$text) {
    Write-Host "`n=== $text ===" -ForegroundColor Cyan
}

function Ensure-Tool {
    if (-not (Get-Command trivy -ErrorAction SilentlyContinue)) {
        Write-Host "未检测到 Trivy，请先安装后再运行本脚本。" -ForegroundColor Yellow
        Write-Host "安装参考: https://aquasecurity.github.io/trivy/latest/installation/" -ForegroundColor Yellow
        throw "Trivy 未安装"
    }
}

function New-ReportPaths {
    param(
        [string]$baseDir,
        [string]$targetType,
        [string]$targetName,
        [string]$format
    )
    $timestamp = (Get-Date).ToString('yyyyMMdd_HHmmss')
    $safeName = ($targetName -replace '[^a-zA-Z0-9_.-]','_')
    $dir = Join-Path $baseDir $timestamp
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
    $ext = if ($format -eq 'table') { 'txt' } else { $format }
    $rawPath = Join-Path $dir ("trivy_${targetType}_${safeName}." + $ext)
    $mdPath = Join-Path $dir ("SUMMARY_${targetType}_${safeName}.md")
    return @{ Raw = $rawPath; Md = $mdPath; Dir = $dir }
}

function Build-TrivyArgs {
    param(
        [string]$mode,
        [string]$target,
        [string]$severity,
        [string]$format,
        [switch]$ignoreUnfixed
    )
    $cliArgs = @()
    $cliArgs += $mode
    $cliArgs += '--severity'
    $cliArgs += $severity
    $cliArgs += '--timeout'
    $cliArgs += ("$TimeoutSeconds" + 's')
    if ($ignoreUnfixed) { $cliArgs += '--ignore-unfixed' }
    if ($format -ne 'table') {
        $cliArgs += '--format'
        $cliArgs += $format
    }
    $cliArgs += $target
    return $cliArgs
}

try {
    Write-Section '环境检查'
    Ensure-Tool

    if (-not (Test-Path $OutputDir)) {
        New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
    }

    $paths = New-ReportPaths -baseDir $OutputDir -targetType $TargetType -targetName $TargetName -format $Format

    Write-Section "开始扫描 ($TargetType): $TargetName"

    switch ($TargetType) {
        'image'     { $mode = 'image' }
        'container' { $mode = 'image'; $TargetName = "$(docker inspect --format='{{.Image}}' $TargetName)" }
        'fs'        { $mode = 'fs' }
    }

    if (-not $TargetName) { throw '无法解析扫描目标名称' }

    $trivyArgs = Build-TrivyArgs -mode $mode -target $TargetName -severity $Severity -format $Format -ignoreUnfixed:$IgnoreUnfixed

    Write-Host ("trivy " + ($trivyArgs -join ' ')) -ForegroundColor DarkGray

    $rawOutput = & trivy @trivyArgs 2>&1
    $rawOutput | Out-File -FilePath $paths.Raw -Encoding UTF8

    Write-Section '生成摘要报告'
    $summary = @()
    $summary += '# Trivy 扫描摘要'
    $summary += ''
    $summary += ('- 扫描类型: ' + $TargetType)
    $summary += ('- 目标: `' + $TargetName + '`')
    $summary += ('- 严重级别: ' + $Severity)
    $summary += ('- 忽略未修复: ' + ($IgnoreUnfixed.IsPresent.ToString()))
    $summary += ('- 输出格式: ' + $Format)
    $summary += ('- 原始结果: `' + $paths.Raw + '`')
    $summary += ('- 时间: ' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'))
    $summary += ""
    $summary += "## 结果概览"
    if ($Format -eq 'table') {
        $matchResults = ($rawOutput | Select-String -Pattern 'Total: (\d+)' -AllMatches)
        $total = if ($matchResults.Matches.Count -gt 0) { $matchResults.Matches[0].Groups[1].Value } else { '未知' }
        $summary += "- 漏洞总数: $total"
    } else {
        try {
            $json = $rawOutput | ConvertFrom-Json -ErrorAction Stop
            $vulnCount = 0
            if ($null -ne $json.Results) {
                foreach ($r in $json.Results) { if ($r.Vulnerabilities) { $vulnCount += $r.Vulnerabilities.Count } }
            }
            $summary += "- 漏洞总数: $vulnCount"
        } catch {
            $summary += "- 漏洞总数: 未解析 (非JSON输出)"
        }
    }
    $summary += ""
    $summary += "> 详细原始输出请查看上述原始结果文件。"
    $summary -join "`n" | Out-File -FilePath $paths.Md -Encoding UTF8

    Write-Section '完成'
    Write-Host ("摘要: " + $paths.Md) -ForegroundColor Green
    Write-Host ("原始: " + $paths.Raw) -ForegroundColor Green
}
catch {
    Write-Error $_
    exit 1
}

