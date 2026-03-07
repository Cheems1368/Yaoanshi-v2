# Quality Gate Test Script (Fixed Encoding)
# 质量门禁测试脚本（修复编码问题）

param(
    [switch]$Verbose
)

# Set console encoding to UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "=== Quality Gate Configuration Test ===" -ForegroundColor Green
Write-Host "=== 质量门禁配置测试 ===" -ForegroundColor Green

# Initialize counters
$TotalChecks = 0
$PassedChecks = 0
$FailedChecks = 0
$WarningChecks = 0

# Check function
function Test-Check {
    param(
        [string]$CheckName,
        [string]$Result,
        [string]$Expected,
        [bool]$IsWarning = $false
    )
    
    $script:TotalChecks++
    
    if ($Result -eq $Expected) {
        Write-Host "✓ $CheckName : PASSED" -ForegroundColor Green
        $script:PassedChecks++
    } else {
        if ($IsWarning) {
            Write-Host "⚠ $CheckName : WARNING" -ForegroundColor Yellow
            $script:WarningChecks++
        } else {
            Write-Host "✗ $CheckName : FAILED" -ForegroundColor Red
            $script:FailedChecks++
        }
    }
}

# Check configuration files
Write-Host "`n1. Checking Configuration Files..." -ForegroundColor Yellow
Write-Host "1. 检查配置文件..." -ForegroundColor Yellow

$ConfigFiles = @("checkstyle.xml", "spotbugs-exclude.xml", "owasp-suppressions.xml", "sonar-project.properties")

foreach ($file in $ConfigFiles) {
    if (Test-Path $file) {
        Test-Check "$file exists" "exists" "exists"
    } else {
        Test-Check "$file exists" "missing" "exists"
    }
}

# Check pom.xml plugin configuration
Write-Host "`n2. Checking pom.xml Plugin Configuration..." -ForegroundColor Yellow
Write-Host "2. 检查pom.xml插件配置..." -ForegroundColor Yellow

if (Test-Path "pom.xml") {
    $PomContent = Get-Content "pom.xml" -Raw -Encoding UTF8
    $Plugins = @("maven-checkstyle-plugin", "spotbugs-maven-plugin", "maven-pmd-plugin", "maven-enforcer-plugin", "jacoco-maven-plugin", "dependency-check-maven")
    
    foreach ($plugin in $Plugins) {
        if ($PomContent -match $plugin) {
            Test-Check "$plugin configured" "configured" "configured"
        } else {
            Test-Check "$plugin configured" "not configured" "configured"
        }
    }
} else {
    Test-Check "pom.xml exists" "missing" "exists"
}

# Check environment
Write-Host "`n3. Checking Environment..." -ForegroundColor Yellow
Write-Host "3. 检查环境..." -ForegroundColor Yellow

# Check Java
try {
    $JavaVersion = java -version 2>&1 | Select-String "version"
    if ($JavaVersion) {
        Test-Check "Java installed" "installed" "installed"
        Write-Host "  Java Version: $($JavaVersion.Line)" -ForegroundColor Cyan
    } else {
        Test-Check "Java installed" "not installed" "installed"
    }
} catch {
    Test-Check "Java installed" "not installed" "installed"
}

# Check Maven
try {
    $MavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    if ($MavenVersion) {
        Test-Check "Maven installed" "installed" "installed"
        Write-Host "  Maven Version: $($MavenVersion.Line)" -ForegroundColor Cyan
    } else {
        Test-Check "Maven installed" "not installed" "installed"
    }
} catch {
    Test-Check "Maven installed" "not installed" "installed"
}

# Check Git
try {
    $GitVersion = git --version 2>&1
    if ($GitVersion -match "git version") {
        Test-Check "Git installed" "installed" "installed"
        Write-Host "  Git Version: $GitVersion" -ForegroundColor Cyan
    } else {
        Test-Check "Git installed" "not installed" "installed"
    }
} catch {
    Test-Check "Git installed" "not installed" "installed"
}

# Check project structure
Write-Host "`n4. Checking Project Structure..." -ForegroundColor Yellow
Write-Host "4. 检查项目结构..." -ForegroundColor Yellow

$ProjectDirs = @("medicine-module-medication", "medicine-framework", "medicine-server")
foreach ($dir in $ProjectDirs) {
    if (Test-Path $dir) {
        Test-Check "$dir directory exists" "exists" "exists"
    } else {
        Test-Check "$dir directory exists" "missing" "exists"
    }
}

# Check test files
Write-Host "`n5. Checking Test Files..." -ForegroundColor Yellow
Write-Host "5. 检查测试文件..." -ForegroundColor Yellow

$TestFiles = @(
    "medicine-module-medication/medicine-module-medication-biz/src/test/java",
    "scripts/sonarqube-integration.sh",
    "scripts/owasp-dependency-check.sh",
    "scripts/performance-baseline-test.sh"
)

foreach ($file in $TestFiles) {
    if (Test-Path $file) {
        Test-Check "$file exists" "exists" "exists"
    } else {
        Test-Check "$file exists" "missing" "exists"
    }
}

# Generate summary
Write-Host "`n=== Test Summary ===" -ForegroundColor Green
Write-Host "=== 测试总结 ===" -ForegroundColor Green

Write-Host "Total Checks: $TotalChecks" -ForegroundColor White
Write-Host "Passed: $PassedChecks" -ForegroundColor Green
Write-Host "Warnings: $WarningChecks" -ForegroundColor Yellow
Write-Host "Failed: $FailedChecks" -ForegroundColor Red

$PassRate = if ($TotalChecks -gt 0) { [math]::Round(($PassedChecks / $TotalChecks) * 100, 2) } else { 0 }
Write-Host "Pass Rate: $PassRate%" -ForegroundColor Cyan

# Determine status
if ($FailedChecks -eq 0) {
    if ($WarningChecks -eq 0) {
        Write-Host "`n🎉 All tests passed! Quality gate is ready." -ForegroundColor Green
        Write-Host "🎉 所有测试通过！质量门禁已就绪。" -ForegroundColor Green
        $ExitCode = 0
    } else {
        Write-Host "`n⚠️ Tests completed with warnings. Review warnings before proceeding." -ForegroundColor Yellow
        Write-Host "⚠️ 测试完成但有警告。请检查警告后再继续。" -ForegroundColor Yellow
        $ExitCode = 0
    }
} else {
    Write-Host "`n❌ Tests failed. Please fix issues before proceeding." -ForegroundColor Red
    Write-Host "❌ 测试失败。请修复问题后再继续。" -ForegroundColor Red
    $ExitCode = 1
}

# Recommendations
Write-Host "`n=== Recommendations ===" -ForegroundColor Cyan
Write-Host "=== 建议 ===" -ForegroundColor Cyan

if ($FailedChecks -gt 0) {
    Write-Host "1. Install missing tools (Java 11, Maven 3.6+)" -ForegroundColor White
    Write-Host "2. Configure environment variables (JAVA_HOME, MAVEN_HOME)" -ForegroundColor White
    Write-Host "3. Check project configuration files" -ForegroundColor White
} else {
    Write-Host "1. Quality gate configuration is ready" -ForegroundColor White
    Write-Host "2. Consider running integration tests" -ForegroundColor White
    Write-Host "3. Set up CI/CD pipeline" -ForegroundColor White
}

Write-Host "`n=== Quality Gate Test Complete ===" -ForegroundColor Green
exit $ExitCode

