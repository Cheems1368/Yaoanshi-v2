# Docker环境启动脚本
# 用于重新构建和启动Docker服务

param(
    [switch]$Rebuild,
    [switch]$Force,
    [string]$Profile = "minimal-test"
)

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
}

function Stop-Containers {
    Write-Log "停止现有容器..."
    
    try {
        docker-compose down
        Write-Log "✅ 容器已停止" "SUCCESS"
    } catch {
        Write-Log "停止容器时发生错误: $($_.Exception.Message)" "ERROR"
    }
}

function Build-Images {
    Write-Log "构建Docker镜像..."
    
    try {
        if ($Rebuild) {
            docker-compose build --no-cache
        } else {
            docker-compose build
        }
        Write-Log "✅ 镜像构建完成" "SUCCESS"
    } catch {
        Write-Log "镜像构建失败: $($_.Exception.Message)" "ERROR"
        exit 1
    }
}

function Start-Services {
    Write-Log "启动Docker服务..."
    
    try {
        # 设置环境变量
        $env:SPRING_PROFILES_ACTIVE = $Profile
        
        # 启动服务
        docker-compose up -d
        
        Write-Log "✅ 服务已启动" "SUCCESS"
        
        # 等待服务启动
        Write-Log "等待服务启动..."
        Start-Sleep -Seconds 10
        
    } catch {
        Write-Log "启动服务失败: $($_.Exception.Message)" "ERROR"
        exit 1
    }
}

function Test-Services {
    Write-Log "测试服务状态..."
    
    try {
        # 检查容器状态
        $containers = docker ps --format "table {{.Names}}\t{{.Status}}"
        Write-Log "容器状态:"
        Write-Host $containers
        
        # 等待健康检查
        Write-Log "等待健康检查..."
        Start-Sleep -Seconds 30
        
        # 检查健康状态
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:48080/actuator/health" -TimeoutSec 10
            if ($response.StatusCode -eq 200) {
                Write-Log "✅ 健康检查通过" "SUCCESS"
            } else {
                Write-Log "⚠️ 健康检查状态异常: $($response.StatusCode)" "WARNING"
            }
        } catch {
            Write-Log "⚠️ 健康检查失败: $($_.Exception.Message)" "WARNING"
        }
        
    } catch {
        Write-Log "测试服务时发生错误: $($_.Exception.Message)" "ERROR"
    }
}

function Show-Logs {
    Write-Log "显示服务日志..."
    
    try {
        Write-Host "`n=== 服务器日志 (最后20行) ===" -ForegroundColor Cyan
        docker logs medicine-server --tail 20
        
        Write-Host "`n=== MySQL日志 (最后10行) ===" -ForegroundColor Cyan
        docker logs medicine-mysql --tail 10
        
        Write-Host "`n=== Redis日志 (最后10行) ===" -ForegroundColor Cyan
        docker logs medicine-redis --tail 10
        
    } catch {
        Write-Log "获取日志时发生错误: $($_.Exception.Message)" "ERROR"
    }
}

# 主函数
function Main {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "   Docker环境启动脚本" -ForegroundColor Green  
    Write-Host "========================================" -ForegroundColor Green
    
    Write-Log "配置: Profile=$Profile, Rebuild=$Rebuild, Force=$Force"
    
    try {
        # 停止现有容器
        Stop-Containers
        
        # 构建镜像
        if ($Rebuild -or $Force) {
            Build-Images
        }
        
        # 启动服务
        Start-Services
        
        # 测试服务
        Test-Services
        
        # 显示日志
        Show-Logs
        
        Write-Log "✅ Docker环境启动完成！" "SUCCESS"
        
        Write-Host ""
        Write-Host "==================== 服务信息 ====================" -ForegroundColor Cyan
        Write-Host "服务器: http://localhost:48080" -ForegroundColor Green
        Write-Host "管理界面: http://localhost:8080" -ForegroundColor Green
        Write-Host "健康检查: http://localhost:48080/actuator/health" -ForegroundColor Green
        Write-Host "MySQL: localhost:13306" -ForegroundColor Green
        Write-Host "Redis: localhost:16379" -ForegroundColor Green
        Write-Host "================================================" -ForegroundColor Cyan
        
        Write-Host "`n💡 提示:" -ForegroundColor Yellow
        Write-Host "• 运行健康检查验证: .\scripts\docker-health-validation.ps1" -ForegroundColor White
        Write-Host "• 查看实时日志: docker-compose logs -f" -ForegroundColor White
        Write-Host "• 停止服务: docker-compose down" -ForegroundColor White
        
    } catch {
        Write-Log "启动过程中发生错误: $($_.Exception.Message)" "ERROR"
        exit 1
    }
}

# 运行主函数
Main
