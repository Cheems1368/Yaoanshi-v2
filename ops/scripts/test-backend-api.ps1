# =============================================
# 后端API测试脚本
# 测试管理端和用户端API
# =============================================

param(
    [string]$AdminUrl = "http://localhost:48080",
    [string]$AppUrl = "http://localhost:48081",
    [int]$MaxWaitSeconds = 120
)

$ErrorActionPreference = "Stop"

# 测试结果
$script:TestResults = @{
    Total = 0
    Passed = 0
    Failed = 0
    Details = @()
}

function Write-TestResult {
    param(
        [string]$TestName,
        [bool]$Passed,
        [string]$Message = ""
    )
    
    $script:TestResults.Total++
    if ($Passed) {
        $script:TestResults.Passed++
        Write-Host "✅ PASS: $TestName" -ForegroundColor Green
        if ($Message) { Write-Host "   $Message" -ForegroundColor Gray }
    } else {
        $script:TestResults.Failed++
        Write-Host "❌ FAIL: $TestName" -ForegroundColor Red
        if ($Message) { Write-Host "   $Message" -ForegroundColor Yellow }
    }
    
    $script:TestResults.Details += @{
        Test = $TestName
        Passed = $Passed
        Message = $Message
    }
}

function Wait-ForService {
    param(
        [string]$Url,
        [string]$ServiceName,
        [int]$MaxWait = 120
    )
    
    Write-Host "`n等待 $ServiceName 服务启动..." -ForegroundColor Cyan
    $startTime = Get-Date
    $attempts = 0
    
    while ((Get-Date) - $startTime -lt [TimeSpan]::FromSeconds($MaxWait)) {
        $attempts++
        try {
            $response = Invoke-WebRequest -Uri "$Url/actuator/health" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                $health = $response.Content | ConvertFrom-Json
                if ($health.status -eq "UP") {
                    Write-Host "✅ $ServiceName 服务已就绪 (尝试 $attempts 次)" -ForegroundColor Green
                    return $true
                }
            }
        } catch {
            # 继续等待
        }
        
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 3
    }
    
    Write-Host "`n❌ $ServiceName 服务在 $MaxWait 秒内未就绪" -ForegroundColor Red
    return $false
}

function Test-HealthCheck {
    param([string]$Url, [string]$ServiceName)
    
    try {
        $response = Invoke-WebRequest -Uri "$Url/actuator/health" -UseBasicParsing -TimeoutSec 10
        $health = $response.Content | ConvertFrom-Json
        
        $passed = $health.status -eq "UP"
        $message = "状态: $($health.status)"
        
        if ($health.components) {
            $dbStatus = $health.components.db.status
            $redisStatus = $health.components.redis.status
            $message += ", 数据库: $dbStatus, Redis: $redisStatus"
            $passed = $passed -and ($dbStatus -eq "UP") -and ($redisStatus -eq "UP")
        }
        
        Write-TestResult -TestName "$ServiceName 健康检查" -Passed $passed -Message $message
        return $passed
    } catch {
        Write-TestResult -TestName "$ServiceName 健康检查" -Passed $false -Message $_.Exception.Message
        return $false
    }
}

function Test-AdminLogin {
    param([string]$BaseUrl)
    
    try {
        $body = @{
            username = "admin"
            password = "admin123"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$BaseUrl/admin-api/system/auth/login" `
            -Method Post `
            -Body $body `
            -ContentType "application/json" `
            -ErrorAction Stop
        
        if ($response.code -eq 0 -and $response.data.accessToken) {
            $token = $response.data.accessToken
            $userInfo = $response.data.userInfo
            Write-TestResult -TestName "管理端登录" -Passed $true -Message "用户: $($userInfo.username), 角色: $($userInfo.nickname)"
            return $token
        } else {
            Write-TestResult -TestName "管理端登录" -Passed $false -Message "响应码: $($response.code)"
            return $null
        }
    } catch {
        Write-TestResult -TestName "管理端登录" -Passed $false -Message $_.Exception.Message
        return $null
    }
}

function Test-HelpOrderAPI {
    param([string]$BaseUrl, [string]$Token)
    
    try {
        $headers = @{
            Authorization = "Bearer $Token"
        }
        
        # 测试工单分页查询
        $response = Invoke-RestMethod -Uri "$BaseUrl/admin-api/system/help-order/page?pageNo=1&pageSize=10" `
            -Method Get `
            -Headers $headers `
            -ErrorAction Stop
        
        if ($response.code -eq 0) {
            Write-TestResult -TestName "工单分页查询" -Passed $true -Message "总数: $($response.data.total)"
            return $true
        } else {
            Write-TestResult -TestName "工单分页查询" -Passed $false -Message "响应码: $($response.code)"
            return $false
        }
    } catch {
        Write-TestResult -TestName "工单分页查询" -Passed $false -Message $_.Exception.Message
        return $false
    }
}

function Test-PatientAPI {
    param([string]$BaseUrl, [string]$Token)
    
    try {
        $headers = @{
            Authorization = "Bearer $Token"
        }
        
        # 测试患者分页查询
        $response = Invoke-RestMethod -Uri "$BaseUrl/admin-api/patient/list/page?pageNo=1&pageSize=10" `
            -Method Get `
            -Headers $headers `
            -ErrorAction Stop
        
        if ($response.code -eq 0) {
            Write-TestResult -TestName "患者分页查询" -Passed $true -Message "总数: $($response.data.total)"
            return $true
        } else {
            Write-TestResult -TestName "患者分页查询" -Passed $false -Message "响应码: $($response.code)"
            return $false
        }
    } catch {
        Write-TestResult -TestName "患者分页查询" -Passed $false -Message $_.Exception.Message
        return $false
    }
}

function Test-AppAPI {
    param([string]$BaseUrl)
    
    try {
        # 用户端API无需认证，直接测试
        $response = Invoke-RestMethod -Uri "$BaseUrl/app-api/help-order/list" `
            -Method Get `
            -ErrorAction Stop
        
        Write-TestResult -TestName "用户端工单列表" -Passed $true -Message "API可访问"
        return $true
    } catch {
        # 如果服务不存在，这是正常的（用户端可能未启动）
        if ($_.Exception.Message -like "*无法连接*" -or $_.Exception.Message -like "*Connection*") {
            Write-TestResult -TestName "用户端工单列表" -Passed $false -Message "用户端服务未启动（可选）"
        } else {
            Write-TestResult -TestName "用户端工单列表" -Passed $false -Message $_.Exception.Message
        }
        return $false
    }
}

# 主测试流程
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   后端API测试" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. 等待管理端服务就绪
if (-not (Wait-ForService -Url $AdminUrl -ServiceName "管理端" -MaxWait $MaxWaitSeconds)) {
    Write-Host "`n❌ 管理端服务未就绪，终止测试" -ForegroundColor Red
    exit 1
}

# 2. 健康检查
Test-HealthCheck -Url $AdminUrl -ServiceName "管理端"

# 3. 登录测试
$token = Test-AdminLogin -BaseUrl $AdminUrl

# 4. API功能测试（需要token）
if ($token) {
    Test-HelpOrderAPI -BaseUrl $AdminUrl -Token $token
    Test-PatientAPI -BaseUrl $AdminUrl -Token $token
}

# 5. 用户端测试（可选）
if ($AppUrl) {
    Write-Host "`n测试用户端服务..." -ForegroundColor Cyan
    Test-AppAPI -BaseUrl $AppUrl
}

# 输出测试结果
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   测试结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "总测试数: $($script:TestResults.Total)" -ForegroundColor White
Write-Host "通过: $($script:TestResults.Passed)" -ForegroundColor Green
Write-Host "失败: $($script:TestResults.Failed)" -ForegroundColor $(if ($script:TestResults.Failed -gt 0) { "Red" } else { "Green" })

if ($script:TestResults.Failed -gt 0) {
    Write-Host "`n失败的测试:" -ForegroundColor Yellow
    $script:TestResults.Details | Where-Object { -not $_.Passed } | ForEach-Object {
        Write-Host "  - $($_.Test): $($_.Message)" -ForegroundColor Red
    }
    exit 1
} else {
    Write-Host "`n✅ 所有测试通过！" -ForegroundColor Green
    exit 0
}


