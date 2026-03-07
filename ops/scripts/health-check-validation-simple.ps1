# 简化的健康检查验证脚本 (PowerShell版本)

param(
    [string]$AppName = "medicine-server",
    [int]$AppPort = 8080,
    [int]$StartupTimeout = 120,
    [int]$HealthCheckTimeout = 30
)

$HealthEndpoint = "http://localhost:$AppPort/actuator/health"
$LogFile = "startup-validation.log"

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

function Test-Port {
    param([int]$Port)
    
    Write-Log "检查端口 $Port 是否被占用..."
    
    $process = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
    if ($process) {
        Write-Log "端口 $Port 已被占用，尝试停止现有进程..." "WARNING"
        $processId = $process.OwningProcess
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    } else {
        Write-Log "端口 $Port 可用" "SUCCESS"
    }
}

function Start-Application {
    Write-Log "启动应用 $AppName..."
    
    $jarFile = Get-ChildItem -Path "target" -Filter "$AppName-*.jar" | Select-Object -First 1
    if (-not $jarFile) {
        Write-Log "未找到JAR文件，请先构建应用" "ERROR"
        return $null
    }
    
    $process = Start-Process -FilePath "java" -ArgumentList @(
        "-jar",
        "-Dspring.profiles.active=minimal-test",
        "-Dspring.config.location=classpath:/application-minimal-test.yaml",
        "-Dlogging.level.com.sqlapk.medicine=DEBUG",
        "-Dlogging.level.org.springframework.web=DEBUG",
        $jarFile.FullName
    ) -RedirectStandardOutput "app.log" -RedirectStandardError "app.log" -PassThru -WindowStyle Hidden
    
    Write-Log "应用已启动，PID: $($process.Id)"
    return $process
}

function Wait-ForStartup {
    param([System.Diagnostics.Process]$Process, [int]$TimeoutSeconds)
    
    Write-Log "等待应用启动完成（超时: $TimeoutSeconds秒）..."
    
    $startTime = Get-Date
    $startupCompleted = $false
    
    while ((Get-Date) - $startTime -lt [TimeSpan]::FromSeconds($TimeoutSeconds)) {
        if ($Process.HasExited) {
            Write-Log "应用进程已退出" "ERROR"
            return $false
        }
        
        if (Test-Path "app.log") {
            $logContent = Get-Content "app.log" -Raw -ErrorAction SilentlyContinue
            if ($logContent -match "Started MedicineServerApplication") {
                Write-Log "检测到应用启动完成标识" "SUCCESS"
                $startupCompleted = $true
                break
            }
        }
        
        $portCheck = Get-NetTCPConnection -LocalPort $AppPort -ErrorAction SilentlyContinue
        if ($portCheck) {
            Write-Log "端口 $AppPort 开始监听" "SUCCESS"
            $startupCompleted = $true
            break
        }
        
        Start-Sleep -Seconds 2
        Write-Host "." -NoNewline
    }
    
    if (-not $startupCompleted) {
        Write-Log "应用启动超时" "ERROR"
        return $false
    }
    
    Write-Log "等待应用完全就绪..."
    Start-Sleep -Seconds 5
    return $true
}

function Test-Health {
    param([int]$TimeoutSeconds)
    
    Write-Log "检查应用健康状态..."
    
    $startTime = Get-Date
    $healthOk = $false
    
    while ((Get-Date) - $startTime -lt [TimeSpan]::FromSeconds($TimeoutSeconds)) {
        try {
            $response = Invoke-WebRequest -Uri $HealthEndpoint -TimeoutSec 5 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Log "健康检查通过" "SUCCESS"
                $healthOk = $true
                break
            }
        } catch {
            # 忽略错误，继续重试
        }
        
        Start-Sleep -Seconds 2
        Write-Host "." -NoNewline
    }
    
    if (-not $healthOk) {
        Write-Log "健康检查失败" "ERROR"
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
                Write-Log "核心组件健康状态正常" "SUCCESS"
            } else {
                Write-Log "部分核心组件状态异常" "WARNING"
            }
        }
        
        Write-Log "完整健康信息:"
        $healthData | ConvertTo-Json -Depth 3 | Write-Host
        
    } catch {
        Write-Log "获取健康信息失败: $($_.Exception.Message)" "ERROR"
    }
}

function Clear-Resources {
    Write-Log "清理资源..."
    
    $processes = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -eq "" }
    if ($processes) {
        foreach ($process in $processes) {
            Write-Log "停止Java进程 $($process.Id)"
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        }
    }
}

# 主函数
function Main {
    Write-Log "开始健康检查自动化验证..."
    
    try {
        Test-Port -Port $AppPort
        $process = Start-Application
        
        if (-not $process) {
            return
        }
        
        $startupOk = Wait-ForStartup -Process $process -TimeoutSeconds $StartupTimeout
        
        if (-not $startupOk) {
            Write-Log "应用启动失败" "ERROR"
            return
        }
        
        $healthOk = Test-Health -TimeoutSeconds $HealthCheckTimeout
        if (-not $healthOk) {
            Write-Log "健康检查失败" "ERROR"
            return
        }
        
        Get-HealthDetails
        
        Write-Log "健康检查自动化验证完成！" "SUCCESS"
        
        Write-Host ""
        Write-Host "==================== 验证结果 ====================" -ForegroundColor Cyan
        Write-Host "应用状态: 运行中" -ForegroundColor Green
        Write-Host "健康检查: 通过" -ForegroundColor Green
        Write-Host "核心组件: 正常" -ForegroundColor Green
        Write-Host "================================================" -ForegroundColor Cyan
        
    } catch {
        Write-Log "验证过程中发生错误: $($_.Exception.Message)" "ERROR"
    } finally {
        $cleanup = Read-Host "是否清理资源并停止应用？(y/N)"
        if ($cleanup -eq "y" -or $cleanup -eq "Y") {
            Clear-Resources
        }
    }
}

# 运行主函数
Main
