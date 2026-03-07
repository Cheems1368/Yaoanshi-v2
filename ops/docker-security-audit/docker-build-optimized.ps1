# Docker优化构建脚本
# 药学管理系统 - 自动化构建、扫描、优化

param(
    [string]$ImageName = "medicine-server",
    [string]$Tag = "latest",
    [string]$Environment = "prod",
    [switch]$ScanSecurity = $true,
    [switch]$PushToRegistry = $false,
    [string]$Registry = ""
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

function Write-Step {
    param(
        [string]$Step,
        [string]$Message
    )
    Write-ColorOutput "`n[$Step] $Message" $Blue
}

# 检查依赖
function Test-Dependencies {
    Write-Step "1" "检查构建依赖"
    
    $dependencies = @("docker", "mvn", "java")
    foreach ($dep in $dependencies) {
        try {
            $version = & $dep --version 2>$null
            Write-ColorOutput "✓ $dep 已安装" $Green
        }
        catch {
            Write-ColorOutput "✗ $dep 未安装" $Red
            exit 1
        }
    }
}

# 清理构建缓存
function Clear-BuildCache {
    Write-Step "2" "清理构建缓存"
    
    try {
        docker builder prune -f
        Write-ColorOutput "✓ 构建缓存已清理" $Green
    }
    catch {
        Write-ColorOutput "⚠ 清理构建缓存失败，继续构建" $Yellow
    }
}

# 构建镜像
function Build-Image {
    Write-Step "3" "构建优化镜像"
    
    $buildArgs = @(
        "build",
        "-f", "docker-security-audit/Dockerfile.optimized-v2",
        "-t", "${ImageName}:${Tag}",
        "-t", "${ImageName}:$(Get-Date -Format 'yyyyMMdd-HHmmss')",
        "--build-arg", "BUILD_DATE=$(Get-Date -Format 'yyyy-MM-ddTHH:mm:ssZ')",
        "--build-arg", "VCS_REF=$(git rev-parse --short HEAD 2>$null; if ($LASTEXITCODE -ne 0) { 'unknown' })",
        "--build-arg", "VERSION=$Tag",
        "."
    )
    
    if ($Environment -eq "prod") {
        $buildArgs += "--no-cache"
    }
    
    try {
        & docker @buildArgs
        Write-ColorOutput "✓ 镜像构建成功: ${ImageName}:${Tag}" $Green
    }
    catch {
        Write-ColorOutput "✗ 镜像构建失败" $Red
        exit 1
    }
}

# 安全扫描
function Scan-ImageSecurity {
    if (-not $ScanSecurity) {
        Write-ColorOutput "⚠ 跳过安全扫描" $Yellow
        return
    }
    
    Write-Step "4" "执行安全扫描"
    
    # 检查Trivy是否安装
    try {
        $trivyVersion = & trivy --version 2>$null
        Write-ColorOutput "✓ Trivy已安装: $trivyVersion" $Green
        
        # 执行漏洞扫描
        Write-ColorOutput "正在扫描镜像漏洞..." $Blue
        & trivy image --severity HIGH,CRITICAL --format table "${ImageName}:${Tag}"
        
        # 生成JSON报告
        $reportFile = "security-scan-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
        & trivy image --severity HIGH,CRITICAL --format json "${ImageName}:${Tag}" | Out-File -FilePath $reportFile -Encoding UTF8
        Write-ColorOutput "✓ 安全扫描报告已生成: $reportFile" $Green
    }
    catch {
        Write-ColorOutput "⚠ Trivy未安装，跳过安全扫描" $Yellow
        Write-ColorOutput "  安装命令: choco install trivy" $Blue
    }
}

# 镜像分析
function Analyze-Image {
    Write-Step "5" "分析镜像信息"
    
    # 镜像大小
        $imageSize = docker images --format "{{.Size}}" "${ImageName}:${Tag}"
    Write-ColorOutput "镜像大小: $imageSize" $Blue
    
    # 镜像层数
    $layerCount = (docker history "${ImageName}:${Tag}" --format "{{.CreatedBy}}" | Measure-Object).Count
    Write-ColorOutput "镜像层数: $layerCount" $Blue
    
    # 基础镜像信息
    $baseImage = docker history "${ImageName}:${Tag}" --format "{{.CreatedBy}}" | Select-Object -Last 1
    Write-ColorOutput "基础镜像: $baseImage" $Blue
    
    # 检查是否使用非root用户
    $userInfo = docker run --rm "${ImageName}:${Tag}" whoami 2>$null
    if ($userInfo -eq "appuser") {
        Write-ColorOutput "✓ 使用非root用户运行" $Green
    } else {
        Write-ColorOutput "⚠ 未使用非root用户" $Yellow
    }
}

# 生成SBOM
function Generate-SBOM {
    Write-Step "6" "生成软件清单(SBOM)"
    
    try {
        $syftVersion = & syft --version 2>$null
        Write-ColorOutput "✓ Syft已安装: $syftVersion" $Green
        
        $sbomFile = "sbom-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
        & syft packages "${ImageName}:${Tag}" -o json | Out-File -FilePath $sbomFile -Encoding UTF8
        Write-ColorOutput "✓ SBOM已生成: $sbomFile" $Green
    }
    catch {
        Write-ColorOutput "⚠ Syft未安装，跳过SBOM生成" $Yellow
        Write-ColorOutput "  安装命令: choco install syft" $Blue
    }
}

# 推送镜像
function Push-Image {
    if (-not $PushToRegistry) {
        Write-ColorOutput "⚠ 跳过镜像推送" $Yellow
        return
    }
    
    Write-Step "7" "推送镜像到仓库"
    
    if ([string]::IsNullOrEmpty($Registry)) {
        Write-ColorOutput "✗ 未指定镜像仓库地址" $Red
        return
    }
    
    try {
        $fullImageName = "${Registry}/${ImageName}:${Tag}"
        docker tag "${ImageName}:${Tag}" $fullImageName
        docker push $fullImageName
        Write-ColorOutput "✓ 镜像已推送到: $fullImageName" $Green
    }
    catch {
        Write-ColorOutput "✗ 镜像推送失败" $Red
    }
}

# 生成构建报告
function Generate-BuildReport {
    Write-Step "8" "生成构建报告"
    
    $reportFile = "build-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"
    $reportContent = @"
# Docker镜像构建报告

**构建时间**: $(Get-Date)
**镜像名称**: ${ImageName}:${Tag}
**环境**: $Environment
**Git提交**: $(git rev-parse --short HEAD 2>$null; if ($LASTEXITCODE -ne 0) { 'unknown' })

## 构建信息
- 镜像大小: $(docker images --format "{{.Size}}" "${ImageName}:${Tag}")
- 镜像层数: $((docker history "${ImageName}:${Tag}" --format "{{.CreatedBy}}" | Measure-Object).Count)
- 基础镜像: $(docker history "${ImageName}:${Tag}" --format "{{.CreatedBy}}" | Select-Object -Last 1)

## 安全扫描
- 扫描工具: Trivy
- 扫描时间: $(Get-Date)
- 报告文件: security-scan-*.json

## 优化措施
1. 使用多阶段构建
2. 非root用户运行
3. 最小化基础镜像
4. 只读文件系统
5. 健康检查配置
6. 资源限制设置

## 部署建议
- 开发环境: docker-compose -f docker-compose.dev.yml up -d
- 生产环境: docker-compose -f docker-compose.prod.yml up -d

"@
    
    $reportContent | Out-File -FilePath $reportFile -Encoding UTF8
    Write-ColorOutput "✓ 构建报告已生成: $reportFile" $Green
}

# 主函数
function Main {
    Write-ColorOutput "Docker优化构建工具" $Blue
    Write-ColorOutput "药学管理系统 - 自动化构建流程" $Blue
    Write-ColorOutput "==========================================" $Blue
    
    Test-Dependencies
    Clear-BuildCache
    Build-Image
    Scan-ImageSecurity
    Analyze-Image
    Generate-SBOM
    Push-Image
    Generate-BuildReport
    
    Write-ColorOutput "`n==========================================" $Blue
    Write-ColorOutput "构建完成！" $Green
    Write-ColorOutput "==========================================" $Blue
}

# 脚本入口
if ($MyInvocation.InvocationName -ne '.') {
    Main
}
