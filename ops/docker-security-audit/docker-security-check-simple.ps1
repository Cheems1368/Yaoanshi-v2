# Docker安全检查脚本 - 简化版
# 药学管理系统 - 自动化Docker安全检查

param(
    [string]$ImageName = "medicine-server:latest"
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
        [string]$Message = ""
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

# 检查Docker环境
function Test-DockerEnvironment {
    Write-ColorOutput "`n1. 检查Docker环境" $Blue
    Write-ColorOutput "----------------------------------------" $Blue
    
    try {
        $dockerVersion = docker --version
        Write-CheckResult "Docker安装检查" "PASS" "版本: $dockerVersion"
    }
    catch {
        Write-CheckResult "Docker安装检查" "FAIL" "Docker未安装或不可用"
        return $false
    }
    
    try {
        $dockerInfo = docker info 2>$null
        Write-CheckResult "Docker服务检查" "PASS" "Docker服务运行正常"
    }
    catch {
        Write-CheckResult "Docker服务检查" "FAIL" "Docker服务未运行"
        return $false
    }
    
    return $true
}

# 检查镜像大小
function Test-ImageSize {
    Write-ColorOutput "`n2. 检查镜像大小" $Blue
    Write-ColorOutput "----------------------------------------" $Blue
    
    try {
        $imageInfo = docker images $ImageName --format "{{.Size}}"
        if ($imageInfo) {
            Write-CheckResult "镜像存在检查" "PASS" "镜像: $ImageName"
            Write-CheckResult "镜像大小检查" "INFO" "镜像大小: $imageInfo"
        }
        else {
            Write-CheckResult "镜像存在检查" "FAIL" "镜像不存在: $ImageName"
        }
    }
    catch {
        Write-CheckResult "镜像大小检查" "FAIL" "无法获取镜像信息"
    }
}

# 检查Docker Compose配置
function Test-ContainerConfig {
    Write-ColorOutput "`n3. 检查容器配置" $Blue
    Write-ColorOutput "----------------------------------------" $Blue
    
    if (Test-Path "docker-compose.yml") {
        Write-CheckResult "Docker Compose配置" "PASS" "配置文件存在"
        
        $composeContent = Get-Content "docker-compose.yml" -Raw
        
        if ($composeContent -match "ports:") {
            Write-CheckResult "端口配置检查" "PASS" "端口配置存在"
        }
        else {
            Write-CheckResult "端口配置检查" "WARN" "未找到端口配置"
        }
        
        if ($composeContent -match "environment:") {
            Write-CheckResult "环境变量配置" "PASS" "环境变量配置存在"
        }
        else {
            Write-CheckResult "环境变量配置" "WARN" "未找到环境变量配置"
        }
        
        if ($composeContent -match "healthcheck:") {
            Write-CheckResult "健康检查配置" "PASS" "健康检查已配置"
        }
        else {
            Write-CheckResult "健康检查配置" "WARN" "建议配置健康检查"
        }
    }
    else {
        Write-CheckResult "Docker Compose配置" "FAIL" "未找到docker-compose.yml文件"
    }
}

# 生成检查报告
function Generate-Report {
    Write-ColorOutput "`n==========================================" $Blue
    Write-ColorOutput "Docker安全检查完成" $Blue
    Write-ColorOutput "==========================================" $Blue
    Write-ColorOutput "总检查项: $TotalChecks" $Blue
    Write-ColorOutput "通过: $PassedChecks" $Green
    Write-ColorOutput "警告: $WarningChecks" $Yellow
    Write-ColorOutput "失败: $FailedChecks" $Red
    
    # 生成报告文件
    $reportFile = "docker-security-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
    $reportContent = "Docker安全检查报告`n生成时间: $(Get-Date)`n项目: 药学管理系统`n镜像: $ImageName`n`n检查结果:`n总检查项: $TotalChecks`n通过: $PassedChecks`n警告: $WarningChecks`n失败: $FailedChecks`n`n建议:`n1. 定期更新基础镜像`n2. 使用多阶段构建优化镜像大小`n3. 配置资源限制和安全选项`n4. 使用Docker secrets管理敏感信息`n5. 定期进行安全扫描`n"
    
    $reportContent | Out-File -FilePath $reportFile -Encoding UTF8
    Write-ColorOutput "`n报告已生成: $reportFile" $Green
    
    if ($FailedChecks -eq 0) {
        if ($WarningChecks -eq 0) {
            Write-ColorOutput "`n所有安全检查通过！" $Green
            exit 0
        }
        else {
            Write-ColorOutput "`n安全检查完成，存在 $WarningChecks 个警告！" $Yellow
            exit 0
        }
    }
    else {
        Write-ColorOutput "`n存在 $FailedChecks 个安全检查失败！" $Red
        exit 1
    }
}

# 主函数
function Main {
    Write-ColorOutput "Docker安全检查工具" $Blue
    Write-ColorOutput "药学管理系统 - 容器安全检查" $Blue
    Write-ColorOutput "==========================================" $Blue
    
    if (-not (Test-DockerEnvironment)) {
        Write-ColorOutput "Docker环境检查失败，退出检查" $Red
        exit 1
    }
    
    Test-ImageSize
    Test-ContainerConfig
    Generate-Report
}

# 脚本入口
if ($MyInvocation.InvocationName -ne '.') {
    Main
}
