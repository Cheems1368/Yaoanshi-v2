# Docker验收检查脚本
# 药学管理系统 - 自动化验收测试

param(
    [string]$ImageName = "medicine-server",
    [string]$Tag = "latest",
    [string]$Environment = "prod"
)

# 颜色定义
$Red = "Red"
$Green = "Green"
$Yellow = "Yellow"
$Blue = "Blue"

# 检查结果统计
$TotalChecks = 0
$PassedChecks = 0
$FailedChecks = 0
$WarningChecks = 0

# 日志函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

function Write-CheckResult {
    param(
        [string]$CheckName,
        [string]$Status,
        [string]$Message = "",
        [string]$Severity = "Info"
    )
    
    $script:TotalChecks++
    
    switch ($Status) {
        "PASS" {
            Write-ColorOutput "✓ $CheckName : 通过" $Green
            $script:PassedChecks++
        }
        "FAIL" {
            Write-ColorOutput "✗ $CheckName : 失败" $Red
            $script:FailedChecks++
        }
        "WARN" {
            Write-ColorOutput "⚠ $CheckName : 警告" $Yellow
            $script:WarningChecks++
        }
    }
    
    if ($Message) {
        Write-ColorOutput "  $Message" $Blue
    }
}

# 1. 镜像体积检查
function Test-ImageSize {
    Write-ColorOutput "`n[1] 检查镜像体积" $Blue
    
    try {
        $imageSize = docker images --format "{{.Size}}" $ImageName`:$Tag
        if ($imageSize) {
            # 解析镜像大小
            $sizeStr = $imageSize -replace 'GB', '' -replace 'MB', '' -replace 'KB', ''
            $sizeValue = [double]$sizeStr
            
            if ($imageSize -like "*GB*") {
                $sizeValue = $sizeValue * 1024
            }
            elseif ($imageSize -like "*KB*") {
                $sizeValue = $sizeValue / 1024
            }
            
            if ($sizeValue -le 300) {
                Write-CheckResult "镜像体积检查" "PASS" "镜像大小: $imageSize (≤ 300MB)"
            }
            elseif ($sizeValue -le 500) {
                Write-CheckResult "镜像体积检查" "WARN" "镜像大小: $imageSize (建议 ≤ 300MB)"
            }
            else {
                Write-CheckResult "镜像体积检查" "FAIL" "镜像过大: $imageSize (目标 ≤ 300MB)"
            }
        }
        else {
            Write-CheckResult "镜像体积检查" "FAIL" "无法获取镜像大小"
        }
    }
    catch {
        Write-CheckResult "镜像体积检查" "FAIL" "镜像不存在或无法访问"
    }
}

# 2. 健康检查配置
function Test-HealthCheck {
    Write-ColorOutput "`n[2] 检查健康检查配置" $Blue
    
    try {
        $healthCheck = docker inspect $ImageName`:$Tag --format="{{.Config.Healthcheck}}" 2>$null
        if ($healthCheck -and $healthCheck -ne "null") {
            Write-CheckResult "健康检查配置" "PASS" "已配置健康检查"
        }
        else {
            Write-CheckResult "健康检查配置" "FAIL" "未配置健康检查"
        }
    }
    catch {
        Write-CheckResult "健康检查配置" "FAIL" "无法检查健康检查配置"
    }
}

# 3. 用户权限检查
function Test-UserPermissions {
    Write-ColorOutput "`n[3] 检查用户权限" $Blue
    
    try {
        $user = docker run --rm $ImageName`:$Tag whoami 2>$null
        if ($user -eq "appuser") {
            Write-CheckResult "非root用户检查" "PASS" "使用非root用户: $user"
        }
        elseif ($user -eq "root") {
            Write-CheckResult "非root用户检查" "FAIL" "使用root用户运行"
        }
        else {
            Write-CheckResult "非root用户检查" "WARN" "用户: $user (建议使用非root用户)"
        }
    }
    catch {
        Write-CheckResult "非root用户检查" "FAIL" "无法检查用户权限"
    }
}

# 4. 只读文件系统检查
function Test-ReadOnlyFilesystem {
    Write-ColorOutput "`n[4] 检查只读文件系统" $Blue
    
    try {
        $readOnly = docker inspect $ImageName`:$Tag --format="{{.Config.ReadonlyRootfs}}" 2>$null
        if ($readOnly -eq "true") {
            Write-CheckResult "只读文件系统" "PASS" "已启用只读文件系统"
        }
        else {
            Write-CheckResult "只读文件系统" "WARN" "未启用只读文件系统"
        }
    }
    catch {
        Write-CheckResult "只读文件系统" "WARN" "无法检查只读文件系统配置"
    }
}

# 5. 安全扫描检查
function Test-SecurityScan {
    Write-ColorOutput "`n[5] 检查安全漏洞" $Blue
    
    try {
        $trivyVersion = & trivy --version 2>$null
        Write-ColorOutput "✓ Trivy已安装: $trivyVersion" $Green
        
        # 执行漏洞扫描
        $scanResult = & trivy image --severity HIGH,CRITICAL --format json $ImageName`:$Tag 2>$null
        $vulnerabilities = $scanResult | ConvertFrom-Json
        
        $highVulns = ($vulnerabilities.Results | ForEach-Object { $_.Vulnerabilities } | Where-Object { $_.Severity -eq "HIGH" }).Count
        $criticalVulns = ($vulnerabilities.Results | ForEach-Object { $_.Vulnerabilities } | Where-Object { $_.Severity -eq "CRITICAL" }).Count
        
        if ($criticalVulns -eq 0 -and $highVulns -eq 0) {
            Write-CheckResult "安全漏洞扫描" "PASS" "无高危和严重漏洞"
        }
        elseif ($criticalVulns -eq 0) {
            Write-CheckResult "安全漏洞扫描" "WARN" "发现 $highVulns 个高危漏洞"
        }
        else {
            Write-CheckResult "安全漏洞扫描" "FAIL" "发现 $criticalVulns 个严重漏洞, $highVulns 个高危漏洞"
        }
    }
    catch {
        Write-CheckResult "安全漏洞扫描" "WARN" "Trivy未安装或扫描失败"
    }
}

# 6. 容器资源使用检查
function Test-ResourceUsage {
    Write-ColorOutput "`n[6] 检查容器资源使用" $Blue
    
    try {
        # 启动容器进行测试
        $containerId = docker run -d --name test-container $ImageName`:$Tag
        Start-Sleep -Seconds 10
        
        # 获取资源使用情况
        $stats = docker stats --no-stream --format "{{.CPUPerc}},{{.MemUsage}}" test-container
        $cpuUsage = ($stats -split ',')[0]
        $memUsage = ($stats -split ',')[1]
        
        Write-CheckResult "资源使用检查" "PASS" "CPU: $cpuUsage, 内存: $memUsage"
        
        # 清理测试容器
        docker stop test-container
        docker rm test-container
    }
    catch {
        Write-CheckResult "资源使用检查" "WARN" "无法检查资源使用情况"
    }
}

# 7. 冒烟测试
function Test-SmokeTest {
    Write-ColorOutput "`n[7] 执行冒烟测试" $Blue
    
    try {
        # 启动容器
        $containerId = docker run -d --name smoke-test-container -p 48081:48080 $ImageName`:$Tag
        Start-Sleep -Seconds 30
        
        # 检查健康端点
        try {
            $healthResponse = Invoke-WebRequest -Uri "http://localhost:48081/actuator/health" -TimeoutSec 10
            if ($healthResponse.StatusCode -eq 200) {
                Write-CheckResult "健康端点测试" "PASS" "健康检查端点响应正常"
            }
            else {
                Write-CheckResult "健康端点测试" "FAIL" "健康检查端点响应异常: $($healthResponse.StatusCode)"
            }
        }
        catch {
            Write-CheckResult "健康端点测试" "FAIL" "健康检查端点无法访问"
        }
        
        # 清理测试容器
        docker stop smoke-test-container
        docker rm smoke-test-container
    }
    catch {
        Write-CheckResult "冒烟测试" "FAIL" "冒烟测试执行失败"
    }
}

# 8. 日志格式检查
function Test-LogFormat {
    Write-ColorOutput "`n[8] 检查日志格式" $Blue
    
    try {
        # 启动容器并检查日志格式
        $containerId = docker run -d --name log-test-container $ImageName`:$Tag
        Start-Sleep -Seconds 5
        
        $logs = docker logs log-test-container 2>&1
        if ($logs -match '{"timestamp"|"level"|"message"') {
            Write-CheckResult "日志格式检查" "PASS" "日志格式为JSON"
        }
        else {
            Write-CheckResult "日志格式检查" "WARN" "日志格式非JSON"
        }
        
        # 清理测试容器
        docker stop log-test-container
        docker rm log-test-container
    }
    catch {
        Write-CheckResult "日志格式检查" "WARN" "无法检查日志格式"
    }
}

# 生成验收报告
function Generate-AcceptanceReport {
    Write-ColorOutput "`n==========================================" $Blue
    Write-ColorOutput "验收检查完成" $Blue
    Write-ColorOutput "==========================================" $Blue
    Write-ColorOutput "总检查项: $TotalChecks" $Blue
    Write-ColorOutput "通过: $PassedChecks" $Green
    Write-ColorOutput "警告: $WarningChecks" $Yellow
    Write-ColorOutput "失败: $FailedChecks" $Red
    
    # 生成报告文件
    $reportFile = "acceptance-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"
    $reportContent = @"
# Docker验收检查报告

**检查时间**: $(Get-Date)
**镜像名称**: $ImageName`:$Tag
**环境**: $Environment

## 检查结果
- 总检查项: $TotalChecks
- 通过: $PassedChecks
- 警告: $WarningChecks
- 失败: $FailedChecks

## 验收标准
- [ ] 镜像体积 ≤ 300MB
- [ ] 配置健康检查
- [ ] 使用非root用户
- [ ] 启用只读文件系统
- [ ] 无高危安全漏洞
- [ ] 资源使用正常
- [ ] 冒烟测试通过
- [ ] 日志格式规范

## 建议
1. 定期更新基础镜像
2. 持续监控安全漏洞
3. 优化资源使用
4. 完善监控告警

"@
    
    $reportContent | Out-File -FilePath $reportFile -Encoding UTF8
    Write-ColorOutput "`n验收报告已生成: $reportFile" $Green
    
    if ($FailedChecks -eq 0) {
        if ($WarningChecks -eq 0) {
            Write-ColorOutput "`n🎉 所有验收检查通过！" $Green
            exit 0
        }
        else {
            Write-ColorOutput "`n⚠️ 验收检查完成，存在 $WarningChecks 个警告！" $Yellow
            exit 0
        }
    }
    else {
        Write-ColorOutput "`n❌ 存在 $FailedChecks 个验收检查失败！" $Red
        exit 1
    }
}

# 主函数
function Main {
    Write-ColorOutput "Docker验收检查工具" $Blue
    Write-ColorOutput "药学管理系统 - 自动化验收测试" $Blue
    Write-ColorOutput "==========================================" $Blue
    
    Test-ImageSize
    Test-HealthCheck
    Test-UserPermissions
    Test-ReadOnlyFilesystem
    Test-SecurityScan
    Test-ResourceUsage
    Test-SmokeTest
    Test-LogFormat
    Generate-AcceptanceReport
}

# 脚本入口
if ($MyInvocation.InvocationName -ne '.') {
    Main
}

