# =============================================
# 医疗HIS系统冒烟测试脚本 (PowerShell版本)
# 测试患者表单提交和药师登录流程
# =============================================

param(
    [string]$BaseUrl = "http://localhost:48080",
    [string]$AdminUrl = "http://localhost:8080",
    [string]$PatientUrl = "http://localhost:5173",
    [string]$PharmacistUrl = "http://localhost:3000",
    [switch]$Verbose
)

# 设置错误处理
$ErrorActionPreference = "Stop"

# 测试结果统计
$script:TotalTests = 0
$script:PassedTests = 0
$script:FailedTests = 0

# 日志函数
function Write-LogInfo {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Yellow
}

function Write-LogSuccess {
    param([string]$Message)
    Write-Host "[PASS] $Message" -ForegroundColor Green
    $script:PassedTests++
}

function Write-LogError {
    param([string]$Message)
    Write-Host "[FAIL] $Message" -ForegroundColor Red
    $script:FailedTests++
}

# 测试函数
function Invoke-Test {
    param(
        [string]$TestName,
        [scriptblock]$TestCommand
    )
    
    $script:TotalTests++
    Write-LogInfo "Running test: $TestName"
    
    try {
        $result = & $TestCommand
        if ($result) {
            Write-LogSuccess $TestName
            return $true
        } else {
            Write-LogError $TestName
            return $false
        }
    } catch {
        Write-LogError "$TestName - Error: $($_.Exception.Message)"
        return $false
    }
}

# 检查服务是否运行
function Test-Service {
    param(
        [string]$ServiceName,
        [string]$Url
    )
    
    try {
        $response = Invoke-WebRequest -Uri $Url -Method Get -TimeoutSec 5 -UseBasicParsing
        return $response.StatusCode -eq 200
    } catch {
        Write-LogError "Service $ServiceName is not running at $Url"
        return $false
    }
}

# 获取访问令牌
function Get-AccessToken {
    param(
        [string]$Username,
        [string]$Password
    )
    
    try {
        $body = @{
            username = $Username
            password = $Password
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$BaseUrl/admin-api/system/auth/login" `
            -Method Post `
            -Body $body `
            -ContentType "application/json"
        
        return $response.data.accessToken
    } catch {
        Write-LogError "Failed to get token for $Username : $($_.Exception.Message)"
        return $null
    }
}

# 测试患者权限
function Test-PatientPermissions {
    $token = Get-AccessToken -Username "patient" -Password "123456"
    
    if (-not $token) {
        return $false
    }
    
    try {
        $body = @{
            userId = 135
            address = "测试地址"
            patientType = 1
            insurance = 1
            birthday = "1990-01-01T00:00:00"
            name = "测试患者"
            phone = "13800000000"
        } | ConvertTo-Json
        
        $headers = @{
            Authorization = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$BaseUrl/admin-api/patient/list/create" `
            -Method Post `
            -Body $body `
            -Headers $headers
        
        return $response.code -eq 0
    } catch {
        Write-LogError "Patient permission test failed: $($_.Exception.Message)"
        return $false
    }
}

# 测试药师登录和仪表盘访问
function Test-PharmacistDashboard {
    $token = Get-AccessToken -Username "pharmacist" -Password "123456"
    
    if (-not $token) {
        return $false
    }
    
    try {
        $headers = @{
            Authorization = "Bearer $token"
        }
        
        $response = Invoke-RestMethod -Uri "$BaseUrl/admin-api/system/auth/get-permission-info" `
            -Method Get `
            -Headers $headers
        
        if ($response.code -eq 0) {
            # 检查用户角色
            $userInfo = $response.data | ConvertTo-Json -Depth 10
            return $userInfo -match "pharmacist"
        } else {
            Write-LogError "Failed to get pharmacist user info"
            return $false
        }
    } catch {
        Write-LogError "Pharmacist dashboard test failed: $($_.Exception.Message)"
        return $false
    }
}

# 测试工单创建
function Test-HelpOrderCreation {
    $token = Get-AccessToken -Username "patient" -Password "123456"
    
    if (-not $token) {
        return $false
    }
    
    try {
        $body = @{
            patientId = 135
            title = "测试工单"
            description = "这是一个测试工单"
            intendedDrugs = @("测试药品")
            attachments = @()
            toDoctor = $false
        } | ConvertTo-Json
        
        $headers = @{
            Authorization = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$BaseUrl/admin-api/help-orders/create" `
            -Method Post `
            -Body $body `
            -Headers $headers
        
        return $response.code -eq 0
    } catch {
        Write-LogError "Help order creation test failed: $($_.Exception.Message)"
        return $false
    }
}

# 测试数据库连接
function Test-DatabaseConnection {
    try {
        $connectionString = "Server=localhost;Database=medicine;Uid=root;Pwd=123456;"
        $connection = New-Object System.Data.SqlClient.SqlConnection($connectionString)
        $connection.Open()
        $connection.Close()
        return $true
    } catch {
        # 尝试MySQL连接
        try {
            $result = & mysql -uroot -p123456 -D medicine -e "SELECT 1" 2>$null
            return $LASTEXITCODE -eq 0
        } catch {
            Write-LogError "Database connection failed: $($_.Exception.Message)"
            return $false
        }
    }
}

# 测试用户名称显示
function Test-UserNameDisplay {
    try {
        $result = & mysql -uroot -p123456 -D medicine -e "SELECT nickname FROM system_users WHERE username = 'pharmacist'" 2>$null
        $nickname = ($result | Select-String "药师" | ForEach-Object { $_.Line.Split()[1] })
        
        if ($nickname -eq "药师") {
            return $true
        } else {
            Write-LogError "User name display test failed. Expected '药师', got '$nickname'"
            return $false
        }
    } catch {
        Write-LogError "User name display test failed: $($_.Exception.Message)"
        return $false
    }
}

# 主测试函数
function Start-SmokeTests {
    Write-LogInfo "Starting Medical HIS System Smoke Tests..."
    Write-LogInfo "=========================================="
    
    # 检查服务状态
    Invoke-Test -TestName "Backend Service Check" -TestCommand { Test-Service -ServiceName "Backend" -Url $BaseUrl }
    Invoke-Test -TestName "Database Connection Check" -TestCommand { Test-DatabaseConnection }
    
    # 测试用户名称修复
    Invoke-Test -TestName "User Name Display Test" -TestCommand { Test-UserNameDisplay }
    
    # 测试患者权限
    Invoke-Test -TestName "Patient Permission Test" -TestCommand { Test-PatientPermissions }
    
    # 测试药师登录和仪表盘
    Invoke-Test -TestName "Pharmacist Dashboard Test" -TestCommand { Test-PharmacistDashboard }
    
    # 测试工单创建
    Invoke-Test -TestName "Help Order Creation Test" -TestCommand { Test-HelpOrderCreation }
    
    # 输出测试结果
    Write-LogInfo "=========================================="
    Write-LogInfo "Test Results Summary:"
    Write-LogInfo "Total Tests: $script:TotalTests"
    Write-LogSuccess "Passed: $script:PassedTests"
    
    if ($script:FailedTests -gt 0) {
        Write-LogError "Failed: $script:FailedTests"
        exit 1
    } else {
        Write-LogSuccess "All tests passed!"
        exit 0
    }
}

# 检查依赖
function Test-Dependencies {
    $missingDeps = @()
    
    if (-not (Get-Command curl -ErrorAction SilentlyContinue)) {
        $missingDeps += "curl"
    }
    
    if (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
        $missingDeps += "mysql"
    }
    
    if ($missingDeps.Count -gt 0) {
        Write-LogError "Missing dependencies: $($missingDeps -join ', ')"
        Write-LogError "Please install the missing dependencies and try again."
        exit 1
    }
}

# 脚本入口
if ($MyInvocation.InvocationName -ne '.') {
    Test-Dependencies
    Start-SmokeTests
}
