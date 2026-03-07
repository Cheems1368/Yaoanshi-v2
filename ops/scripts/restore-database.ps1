# =============================================
# 数据库恢复脚本
# 从备份文件恢复MySQL数据库
# =============================================

param(
    [Parameter(Mandatory=$true)]
    [string]$BackupFile
)

# 检查备份文件是否存在
if (-not (Test-Path $BackupFile)) {
    Write-Host "❌ 备份文件不存在: $BackupFile" -ForegroundColor Red
    exit 1
}

# 检查容器是否运行
$containerRunning = docker ps --format "{{.Names}}" | Select-String -Pattern "medicine-mysql"
if (-not $containerRunning) {
    Write-Host "❌ MySQL容器未运行，请先启动容器" -ForegroundColor Red
    exit 1
}

Write-Host "⚠️  警告: 此操作将覆盖现有数据库数据！" -ForegroundColor Yellow
$confirm = Read-Host "确认恢复? (yes/no)"
if ($confirm -ne "yes") {
    Write-Host "❌ 操作已取消" -ForegroundColor Red
    exit 0
}

Write-Host "🔄 开始恢复数据库..." -ForegroundColor Blue

# 执行恢复
Get-Content $BackupFile | docker exec -i medicine-mysql mysql -uroot -p123456 medicine

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 恢复完成" -ForegroundColor Green
} else {
    Write-Host "❌ 恢复失败" -ForegroundColor Red
    exit 1
}

