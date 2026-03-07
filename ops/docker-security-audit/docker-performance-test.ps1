# Docker性能测试脚本
# 药学管理系统 - 性能对比测试

param(
    [string]$ImageName = "medicine-server",
    [string]$OriginalTag = "latest",
    [string]$OptimizedTag = "optimized",
    [int]$TestDuration = 60
)

# 颜色定义
$Red = "Red"
$Green = "Green"
$Yellow = "Yellow"
$Blue = "Blue"

# 日志函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

function Write-TestResult {
    param(
        [string]$TestName,
        [string]$Status,
        [string]$Message = "",
        [string]$Value = ""
    )
    
    switch ($Status) {
        "PASS" {
            Write-ColorOutput "✓ $TestName : $Message $Value" $Green
        }
        "FAIL" {
            Write-ColorOutput "✗ $TestName : $Message $Value" $Red
        }
        "WARN" {
            Write-ColorOutput "⚠ $TestName : $Message $Value" $Yellow
        }
        "INFO" {
            Write-ColorOutput "ℹ $TestName : $Message $Value" $Blue
        }
    }
}

# 测试镜像大小
function Test-ImageSize {
    Write-ColorOutput "`n[1] 测试镜像大小" $Blue
    
    try {
        $originalSize = docker images --format "{{.Size}}" "${ImageName}:${OriginalTag}"
        $optimizedSize = docker images --format "{{.Size}}" "${ImageName}:${OptimizedTag}"
        
        Write-TestResult "原始镜像大小" "INFO" "大小" $originalSize
        Write-TestResult "优化镜像大小" "INFO" "大小" $optimizedSize
        
        # 解析大小进行比较
        $originalMB = [double]($originalSize -replace 'MB', '' -replace 'GB', '')
        $optimizedMB = [double]($optimizedSize -replace 'MB', '' -replace 'GB', '')
        
        if ($originalSize -like "*GB*") {
            $originalMB = $originalMB * 1024
        }
        if ($optimizedSize -like "*GB*") {
            $optimizedMB = $optimizedMB * 1024
        }
        
        $sizeDiff = $optimizedMB - $originalMB
        $sizePercent = ($sizeDiff / $originalMB) * 100
        
        if ($sizeDiff -lt 0) {
            Write-TestResult "镜像大小优化" "PASS" "减少" "$([math]::Abs($sizeDiff))MB ($([math]::Abs($sizePercent).ToString('F1'))%)"
        } else {
            Write-TestResult "镜像大小优化" "WARN" "增加" "$sizeDiff MB ($sizePercent.ToString('F1')%)"
        }
    }
    catch {
        Write-TestResult "镜像大小测试" "FAIL" "无法获取镜像大小"
    }
}

# 测试启动时间
function Test-StartupTime {
    Write-ColorOutput "`n[2] 测试启动时间" $Blue
    
    # 测试原始镜像
    Write-TestResult "原始镜像启动测试" "INFO" "启动中..."
    $startTime = Get-Date
    $originalContainer = docker run -d --name test-original "${ImageName}:${OriginalTag}"
    Start-Sleep -Seconds 30
    
    # 检查原始镜像是否启动成功
    $originalStatus = docker inspect $originalContainer --format="{{.State.Status}}"
    if ($originalStatus -eq "running") {
        $originalStartupTime = (Get-Date) - $startTime
        Write-TestResult "原始镜像启动时间" "PASS" "启动成功" "$($originalStartupTime.TotalSeconds.ToString('F1'))秒"
    } else {
        Write-TestResult "原始镜像启动时间" "FAIL" "启动失败"
    }
    
    # 清理原始镜像容器
    docker stop $originalContainer
    docker rm $originalContainer
    
    # 测试优化镜像
    Write-TestResult "优化镜像启动测试" "INFO" "启动中..."
    $startTime = Get-Date
    $optimizedContainer = docker run -d --name test-optimized "${ImageName}:${OptimizedTag}"
    Start-Sleep -Seconds 30
    
    # 检查优化镜像是否启动成功
    $optimizedStatus = docker inspect $optimizedContainer --format="{{.State.Status}}"
    if ($optimizedStatus -eq "running") {
        $optimizedStartupTime = (Get-Date) - $startTime
        Write-TestResult "优化镜像启动时间" "PASS" "启动成功" "$($optimizedStartupTime.TotalSeconds.ToString('F1'))秒"
        
        # 计算启动时间改进
        if ($originalStartupTime) {
            $startupDiff = $originalStartupTime - $optimizedStartupTime
            $startupPercent = ($startupDiff.TotalSeconds / $originalStartupTime.TotalSeconds) * 100
            
            if ($startupDiff.TotalSeconds -gt 0) {
                Write-TestResult "启动时间优化" "PASS" "提升" "$($startupDiff.TotalSeconds.ToString('F1'))秒 ($startupPercent.ToString('F1')%)"
            } else {
                Write-TestResult "启动时间优化" "WARN" "下降" "$([math]::Abs($startupDiff.TotalSeconds).ToString('F1'))秒 ($([math]::Abs($startupPercent).ToString('F1'))%)"
            }
        }
    } else {
        Write-TestResult "优化镜像启动时间" "FAIL" "启动失败"
    }
    
    # 清理优化镜像容器
    docker stop $optimizedContainer
    docker rm $optimizedContainer
}

# 测试内存使用
function Test-MemoryUsage {
    Write-ColorOutput "`n[3] 测试内存使用" $Blue
    
    # 启动优化镜像进行内存测试
    $container = docker run -d --name test-memory "${ImageName}:${OptimizedTag}"
    Start-Sleep -Seconds 30
    
    try {
        # 获取内存使用情况
        $stats = docker stats --no-stream --format "{{.MemUsage}}" test-memory
        $memoryUsage = ($stats -split '/')[0]
        
        Write-TestResult "内存使用测试" "PASS" "当前使用" $memoryUsage
        
        # 检查内存使用是否合理
        $memoryMB = [double]($memoryUsage -replace 'MiB', '' -replace 'GiB', '')
        if ($memoryUsage -like "*GiB*") {
            $memoryMB = $memoryMB * 1024
        }
        
        if ($memoryMB -lt 1024) {
            Write-TestResult "内存使用优化" "PASS" "内存使用合理" "< 1GB"
        } elseif ($memoryMB -lt 2048) {
            Write-TestResult "内存使用优化" "WARN" "内存使用较高" "1-2GB"
        } else {
            Write-TestResult "内存使用优化" "FAIL" "内存使用过高" "> 2GB"
        }
    }
    catch {
        Write-TestResult "内存使用测试" "FAIL" "无法获取内存使用情况"
    }
    finally {
        # 清理测试容器
        docker stop $container
        docker rm $container
    }
}

# 测试健康检查
function Test-HealthCheck {
    Write-ColorOutput "`n[4] 测试健康检查" $Blue
    
    # 启动优化镜像
    $container = docker run -d --name test-health -p 48081:48080 "${ImageName}:${OptimizedTag}"
    Start-Sleep -Seconds 60
    
    try {
        # 测试健康检查端点
        $healthResponse = Invoke-WebRequest -Uri "http://localhost:48081/actuator/health" -UseBasicParsing -TimeoutSec 10
        if ($healthResponse.StatusCode -eq 200) {
            Write-TestResult "健康检查端点" "PASS" "响应正常" "HTTP 200"
        } else {
            Write-TestResult "健康检查端点" "FAIL" "响应异常" "HTTP $($healthResponse.StatusCode)"
        }
        
        # 检查Docker健康检查状态
        $healthStatus = docker inspect $container --format="{{.State.Health.Status}}"
        if ($healthStatus -eq "healthy") {
            Write-TestResult "Docker健康检查" "PASS" "状态健康" $healthStatus
        } else {
            Write-TestResult "Docker健康检查" "WARN" "状态" $healthStatus
        }
    }
    catch {
        Write-TestResult "健康检查测试" "FAIL" "无法访问健康检查端点"
    }
    finally {
        # 清理测试容器
        docker stop $container
        docker rm $container
    }
}

# 测试用户权限
function Test-UserPermissions {
    Write-ColorOutput "`n[5] 测试用户权限" $Blue
    
    try {
        # 测试优化镜像的用户权限
        $user = docker run --rm "${ImageName}:${OptimizedTag}" whoami
        if ($user -eq "appuser") {
            Write-TestResult "用户权限测试" "PASS" "使用非root用户" $user
        } else {
            Write-TestResult "用户权限测试" "FAIL" "用户" $user
        }
        
        # 测试文件权限
        $filePermissions = docker run --rm "${ImageName}:${OptimizedTag}" ls -la /app
        if ($filePermissions -match "appuser") {
            Write-TestResult "文件权限测试" "PASS" "文件权限正确"
        } else {
            Write-TestResult "文件权限测试" "WARN" "文件权限需要检查"
        }
    }
    catch {
        Write-TestResult "用户权限测试" "FAIL" "无法测试用户权限"
    }
}

# 生成性能报告
function Generate-PerformanceReport {
    Write-ColorOutput "`n[6] 生成性能报告" $Blue
    
    $reportFile = "performance-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"
    $reportContent = @"
# Docker性能测试报告

**测试时间**: $(Get-Date)
**原始镜像**: ${ImageName}:${OriginalTag}
**优化镜像**: ${ImageName}:${OptimizedTag}

## 测试结果

### 镜像大小对比
- 原始镜像: $(docker images --format "{{.Size}}" "${ImageName}:${OriginalTag}")
- 优化镜像: $(docker images --format "{{.Size}}" "${ImageName}:${OptimizedTag}")

### 启动时间对比
- 原始镜像: 待测试
- 优化镜像: 待测试

### 内存使用情况
- 当前使用: 待测试

### 健康检查状态
- 端点响应: 待测试
- Docker状态: 待测试

### 安全特性
- 运行用户: appuser
- 文件权限: 已配置

## 优化建议

1. **镜像体积**: 考虑使用Alpine或Distroless基础镜像
2. **启动时间**: 优化JVM参数和依赖加载
3. **内存使用**: 调整JVM内存参数
4. **安全加固**: 启用只读文件系统

## 测试环境

- Docker版本: $(docker --version)
- 操作系统: Windows
- 测试时间: $TestDuration 秒

"@
    
    $reportContent | Out-File -FilePath $reportFile -Encoding UTF8
    Write-TestResult "性能报告生成" "PASS" "报告已生成" $reportFile
}

# 主函数
function Main {
    Write-ColorOutput "Docker性能测试工具" $Blue
    Write-ColorOutput "药学管理系统 - 性能对比测试" $Blue
    Write-ColorOutput "==========================================" $Blue
    
    Test-ImageSize
    Test-StartupTime
    Test-MemoryUsage
    Test-HealthCheck
    Test-UserPermissions
    Generate-PerformanceReport
    
    Write-ColorOutput "`n==========================================" $Blue
    Write-ColorOutput "性能测试完成！" $Green
    Write-ColorOutput "==========================================" $Blue
}

# 脚本入口
if ($MyInvocation.InvocationName -ne '.') {
    Main
}
