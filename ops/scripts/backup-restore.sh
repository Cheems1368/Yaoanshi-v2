#!/bin/bash

# 备份恢复工具
# 用于数据库备份和恢复操作

set -e

# 配置参数
DB_HOST="${DB_HOST:-medicine-mysql}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-medicine}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-123456}"
BACKUP_DIR="${BACKUP_DIR:-/backup}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查依赖..."
    
    if ! command -v mysqldump &> /dev/null; then
        log_error "mysqldump未安装"
        exit 1
    fi
    
    if ! command -v mysql &> /dev/null; then
        log_error "mysql客户端未安装"
        exit 1
    fi
    
    if ! command -v gzip &> /dev/null; then
        log_error "gzip未安装"
        exit 1
    fi
    
    log_info "依赖检查完成"
}

# 创建备份目录
create_backup_dir() {
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
        log_info "创建备份目录: $BACKUP_DIR"
    fi
}

# 数据库备份
backup_database() {
    local backup_type="${1:-full}"
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_file="$BACKUP_DIR/medicine_${backup_type}_${timestamp}.sql.gz"
    
    log_info "开始数据库备份..."
    log_info "备份类型: $backup_type"
    log_info "备份文件: $backup_file"
    
    # 构建mysqldump命令
    local dump_cmd="mysqldump -h$DB_HOST -P$DB_PORT -u$DB_USERNAME -p$DB_PASSWORD"
    
    case $backup_type in
        "full")
            dump_cmd="$dump_cmd --single-transaction --routines --triggers --events --all-databases"
            ;;
        "schema")
            dump_cmd="$dump_cmd --no-data --routines --triggers --events $DB_NAME"
            ;;
        "data")
            dump_cmd="$dump_cmd --no-create-info --single-transaction $DB_NAME"
            ;;
        "tables")
            dump_cmd="$dump_cmd --single-transaction $DB_NAME"
            ;;
        *)
            log_error "不支持的备份类型: $backup_type"
            exit 1
            ;;
    esac
    
    # 执行备份
    if $dump_cmd | gzip > "$backup_file"; then
        log_info "数据库备份成功: $backup_file"
        
        # 获取备份文件大小
        local file_size=$(du -h "$backup_file" | cut -f1)
        log_info "备份文件大小: $file_size"
        
        # 验证备份文件
        if gzip -t "$backup_file"; then
            log_info "备份文件验证成功"
        else
            log_error "备份文件验证失败"
            exit 1
        fi
    else
        log_error "数据库备份失败"
        exit 1
    fi
}

# 数据库恢复
restore_database() {
    local backup_file="$1"
    
    if [ -z "$backup_file" ]; then
        log_error "请指定备份文件"
        exit 1
    fi
    
    if [ ! -f "$backup_file" ]; then
        log_error "备份文件不存在: $backup_file"
        exit 1
    fi
    
    log_info "开始数据库恢复..."
    log_info "备份文件: $backup_file"
    
    # 确认恢复操作
    read -p "确认要恢复数据库吗？这将覆盖现有数据！(y/N): " confirm
    if [[ ! $confirm =~ ^[Yy]$ ]]; then
        log_info "恢复操作已取消"
        exit 0
    fi
    
    # 检查备份文件类型
    if [[ "$backup_file" == *.gz ]]; then
        # 压缩文件
        if gunzip -c "$backup_file" | mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD"; then
            log_info "数据库恢复成功"
        else
            log_error "数据库恢复失败"
            exit 1
        fi
    else
        # 普通文件
        if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" < "$backup_file"; then
            log_info "数据库恢复成功"
        else
            log_error "数据库恢复失败"
            exit 1
        fi
    fi
}

# 清理旧备份
cleanup_old_backups() {
    log_info "清理超过 $RETENTION_DAYS 天的旧备份..."
    
    local deleted_count=0
    while IFS= read -r -d '' file; do
        rm "$file"
        ((deleted_count++))
        log_info "删除旧备份: $(basename "$file")"
    done < <(find "$BACKUP_DIR" -name "medicine_*.sql.gz" -type f -mtime +$RETENTION_DAYS -print0)
    
    if [ $deleted_count -eq 0 ]; then
        log_info "没有找到需要清理的旧备份"
    else
        log_info "清理完成，共删除 $deleted_count 个旧备份"
    fi
}

# 列出备份文件
list_backups() {
    log_info "备份文件列表:"
    
    if [ ! -d "$BACKUP_DIR" ] || [ -z "$(ls -A "$BACKUP_DIR" 2>/dev/null)" ]; then
        log_warn "备份目录为空"
        return
    fi
    
    echo "文件名                    大小        修改时间"
    echo "----------------------------------------"
    
    for file in "$BACKUP_DIR"/medicine_*.sql.gz; do
        if [ -f "$file" ]; then
            local filename=$(basename "$file")
            local size=$(du -h "$file" | cut -f1)
            local mtime=$(stat -c %y "$file" | cut -d' ' -f1,2 | cut -d'.' -f1)
            printf "%-25s %-10s %s\n" "$filename" "$size" "$mtime"
        fi
    done
}

# 验证数据库连接
test_connection() {
    log_info "测试数据库连接..."
    
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1; then
        log_info "数据库连接正常"
        return 0
    else
        log_error "数据库连接失败"
        return 1
    fi
}

# 显示帮助信息
show_help() {
    cat << EOF
用药打卡系统数据库备份恢复工具

用法: $0 [命令] [选项]

命令:
    backup [类型]     执行数据库备份
    restore <文件>    从备份文件恢复数据库
    list             列出所有备份文件
    cleanup          清理旧备份文件
    test             测试数据库连接
    help             显示此帮助信息

备份类型:
    full             完整备份（所有数据库）
    schema           仅结构备份
    data             仅数据备份
    tables           表备份（默认）

选项:
    -h, --help       显示帮助信息
    -d, --dir        指定备份目录（默认: $BACKUP_DIR）
    -r, --retention  指定保留天数（默认: $RETENTION_DAYS）

环境变量:
    DB_HOST          数据库主机（默认: $DB_HOST）
    DB_PORT          数据库端口（默认: $DB_PORT）
    DB_NAME          数据库名称（默认: $DB_NAME）
    DB_USERNAME      数据库用户名（默认: $DB_USERNAME）
    DB_PASSWORD      数据库密码（默认: $DB_PASSWORD）
    BACKUP_DIR       备份目录（默认: $BACKUP_DIR）
    RETENTION_DAYS   保留天数（默认: $RETENTION_DAYS）

示例:
    $0 backup full                    # 完整备份
    $0 backup tables                  # 表备份
    $0 restore medicine_full_20240106_120000.sql.gz  # 恢复备份
    $0 list                           # 列出备份文件
    $0 cleanup                        # 清理旧备份
    $0 test                           # 测试连接

EOF
}

# 主函数
main() {
    local command="${1:-help}"
    
    case $command in
        "backup")
            check_dependencies
            create_backup_dir
            test_connection || exit 1
            backup_database "${2:-tables}"
            cleanup_old_backups
            ;;
        "restore")
            check_dependencies
            test_connection || exit 1
            restore_database "$2"
            ;;
        "list")
            list_backups
            ;;
        "cleanup")
            create_backup_dir
            cleanup_old_backups
            ;;
        "test")
            check_dependencies
            test_connection
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            log_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 脚本入口
if [ "${BASH_SOURCE[0]}" == "${0}" ]; then
    main "$@"
fi
