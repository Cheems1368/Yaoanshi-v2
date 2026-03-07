# =============================================
# 数据库备份脚本
# 自动备份MySQL数据库到本地文件
# =============================================

$BackupDir = "backups"
$Date = Get-Date -Format "yyyyMMdd-HHmmss"
$BackupFile = "$BackupDir\medicine-backup-$Date.sql"

# 创建备份目录
if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
    Write-Host "✅ 创建备份目录: $BackupDir" -ForegroundColor Green
}

# 检查容器是否运行
$containerRunning = docker ps --format "{{.Names}}" | Select-String -Pattern "medicine-mysql"
if (-not $containerRunning) {
    Write-Host "❌ MySQL容器未运行，请先启动容器" -ForegroundColor Red
    exit 1
}

Write-Host "🔄 开始备份数据库..." -ForegroundColor Blue

# 执行备份
docker exec medicine-mysql mysqldump -uroot -p123456 medicine > $BackupFile

if ($LASTEXITCODE -eq 0) {
    $fileSize = (Get-Item $BackupFile).Length / 1MB
    Write-Host "✅ 备份完成: $BackupFile (大小: $([math]::Round($fileSize, 2)) MB)" -ForegroundColor Green
    
    # 保留最近7天的备份
    Write-Host "🧹 清理7天前的备份文件..." -ForegroundColor Yellow
    Get-ChildItem -Path $BackupDir -Filter "medicine-backup-*.sql" | 
        Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-7) } | 
        Remove-Item -Force
    
    Write-Host "✅ 备份任务完成" -ForegroundColor Green
} else {
    Write-Host "❌ 备份失败" -ForegroundColor Red
    exit 1
}

