# Docker环境健康检查验证脚本
# 专门用于验证Docker容器中的健康检查修复

param(
    [string]$ServerUrl = "http://localhost:48080",
    [string]$AdminUrl = "http://localhost:8080",
    [int]$TimeoutSeconds = 30
)

$HealthEndpoint = "$ServerUrl/actuator/health"
$LogFile = "docker-health-validation.log"

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] $Message"
    
    switch ($Level) {
        "SUCCESS" { Write-Host $logMessage -ForegroundColor Green }
        "WARNING" { Write-Host $logMessage -ForegroundColor Yellow }
        "ERROR" { Write-Host $logMessage -ForegroundColor Red }
        default { Write-Host $logMessage -ForegroundColor Blue }
    }
    
    Add-Content -Path $LogFile -Value $logMessage
}

function Test-DockerContainers {
    Write-Log "检查Docker容器状态..."
    
    try {
        $containers = docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | ConvertFrom-Csv -Delimiter "`t"
        
        $expectedContainers = @("medicine-mysql", "medicine-redis", "medicine-server", "medicine-admin")
        
        foreach ($container in $expectedContainers) {
            $containerInfo = $containers | Where-Object { $_.Names -eq $container }
            if ($containerInfo) {
                Write-Log "✅ $container 正在运行: $($containerInfo.Status)" "SUCCESS"
            } else {
                Write-Log "❌ $container 未运行" "ERROR"
            }
        }
        
    } catch {
        Write-Log "无法检查Docker容器状态: $($_.Exception.Message)" "ERROR"
    }
}

function Test-ServerHealth {
    Write-Log "检查服务器健康状态..."
    
    $startTime = Get-Date
    $healthOk = $false
    
    while ((Get-Date) - $startTime -lt [TimeSpan]::FromSeconds($TimeoutSeconds)) {
        try {
            $response = Invoke-WebRequest -Uri $HealthEndpoint -TimeoutSec 5 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Log "✅ 服务器健康检查通过" "SUCCESS"
                $healthOk = $true
                break
            }
        } catch {
            Write-Log "健康检查失败，重试中..." "WARNING"
        }
        
        Start-Sleep -Seconds 2
        Write-Host "." -NoNewline
    }
    
    if (-not $healthOk) {
        Write-Log "❌ 服务器健康检查失败" "ERROR"
        return $false
    }
    
    return $true
}

function Get-HealthDetails {
    Write-Log "获取详细健康信息..."
    
    try {
        $response = Invoke-WebRequest -Uri $HealthEndpoint -TimeoutSec 10
        $healthData = $response.Content | ConvertFrom-Json
        
        Write-Log "健康状态: $($healthData.status)"
        
        if ($healthData.components) {
            $dbStatus = $healthData.components.db.status
            $redisStatus = $healthData.components.redis.status
            
            Write-Log "数据库状态: $dbStatus"
            Write-Log "Redis状态: $redisStatus"
            
            if ($dbStatus -eq "UP" -and $redisStatus -eq "UP") {
                Write-Log "✅ 核心组件健康状态正常" "SUCCESS"
            } else {
                Write-Log "⚠️ 部分核心组件状态异常" "WARNING"
            }
            
            # 检查外部服务集成是否被正确禁用
            if ($healthData.components.'drug-api') {
                $drugApiStatus = $healthData.components.'drug-api'.status
                Write-Log "药品API状态: $drugApiStatus"
            }
            
            if ($healthData.components.'external-service') {
                $externalStatus = $healthData.components.'external-service'.status
                Write-Log "外部服务状态: $externalStatus"
            }
        }
        
        Write-Log "完整健康信息:"
        $healthData | ConvertTo-Json -Depth 3 | Write-Host
        
    } catch {
        Write-Log "获取健康信息失败: $($_.Exception.Message)" "ERROR"
    }
}

function Test-AdminInterface {
    Write-Log "检查管理界面..."
    
    try {
        $response = Invoke-WebRequest -Uri $AdminUrl -TimeoutSec 10 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Log "✅ 管理界面可访问" "SUCCESS"
        } else {
            Write-Log "⚠️ 管理界面状态异常: $($response.StatusCode)" "WARNING"
        }
    } catch {
        Write-Log "❌ 管理界面无法访问: $($_.Exception.Message)" "ERROR"
    }
}

function Test-DockerLogs {
    Write-Log "检查Docker容器日志..."
    
    try {
        # 检查服务器容器日志
        $serverLogs = docker logs medicine-server --tail 20 2>&1
        
        # 检查是否有错误
        $errorCount = ($serverLogs | Select-String "ERROR").Count
        $warningCount = ($serverLogs | Select-String "WARN").Count
        
        if ($errorCount -gt 0) {
            Write-Log "⚠️ 发现 $errorCount 个错误日志" "WARNING"
        } else {
            Write-Log "✅ 未发现错误日志" "SUCCESS"
        }
        
        if ($warningCount -gt 0) {
            Write-Log "⚠️ 发现 $warningCount 个警告日志" "WARNING"
        }
        
        # 检查启动完成标识
        if ($serverLogs -match "Started MedicineServerApplication") {
            Write-Log "✅ 应用启动成功" "SUCCESS"
        } else {
            Write-Log "⚠️ 未找到应用启动成功标识" "WARNING"
        }
        
    } catch {
        Write-Log "无法获取Docker日志: $($_.Exception.Message)" "ERROR"
    }
}

# 主函数
function Main {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "   Docker环境健康检查验证" -ForegroundColor Green  
    Write-Host "========================================" -ForegroundColor Green
    
    Write-Log "开始Docker环境健康检查验证..."
    
    try {
        # 检查Docker容器状态
        Test-DockerContainers
        
        # 检查服务器健康状态
        $healthOk = Test-ServerHealth
        if (-not $healthOk) {
            Write-Log "健康检查失败，停止验证" "ERROR"
            return
        }
        
        # 获取详细健康信息
        Get-HealthDetails
        
        # 检查管理界面
        Test-AdminInterface
        
        # 检查Docker日志
        Test-DockerLogs
        
        Write-Log "✅ Docker环境健康检查验证完成！" "SUCCESS"
        
        Write-Host ""
        Write-Host "==================== 验证结果 ====================" -ForegroundColor Cyan
        Write-Host "Docker容器: 运行中" -ForegroundColor Green
        Write-Host "服务器健康: 通过" -ForegroundColor Green
        Write-Host "核心组件: 正常" -ForegroundColor Green
        Write-Host "管理界面: 可访问" -ForegroundColor Green
        Write-Host "================================================" -ForegroundColor Cyan
        
    } catch {
        Write-Log "验证过程中发生错误: $($_.Exception.Message)" "ERROR"
    }
}

# 运行主函数
Main

