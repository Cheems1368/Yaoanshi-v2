#!/bin/bash
# =============================================
# 数据库迁移执行脚本
# 自动执行版本化的数据库迁移
# =============================================

set -e

# 配置
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-medicine}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-123456}"
MIGRATIONS_DIR="${MIGRATIONS_DIR:-sql/migrations}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查数据库连接
check_database_connection() {
    log_info "Checking database connection..."
    
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -e "SELECT 1" > /dev/null 2>&1; then
        log_success "Database connection successful"
        return 0
    else
        log_error "Failed to connect to database"
        return 1
    fi
}

# 创建迁移记录表
create_migrations_table() {
    log_info "Creating migrations table if not exists..."
    
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" << EOF
CREATE TABLE IF NOT EXISTS schema_migrations (
    version VARCHAR(50) PRIMARY KEY,
    description TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    execution_time_ms INT,
    success BOOLEAN DEFAULT TRUE,
    error_message TEXT
);
EOF
    
    log_success "Migrations table ready"
}

# 获取已执行的迁移版本
get_executed_migrations() {
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -sN -e "SELECT version FROM schema_migrations WHERE success = TRUE ORDER BY version" 2>/dev/null || echo ""
}

# 获取待执行的迁移文件
get_pending_migrations() {
    local executed_migrations="$1"
    local pending_migrations=()
    
    if [ ! -d "$MIGRATIONS_DIR" ]; then
        log_warning "Migrations directory $MIGRATIONS_DIR does not exist"
        return 0
    fi
    
    for migration_file in "$MIGRATIONS_DIR"/*.sql; do
        if [ -f "$migration_file" ]; then
            local version=$(basename "$migration_file" .sql | cut -d'__' -f1)
            if ! echo "$executed_migrations" | grep -q "^$version$"; then
                pending_migrations+=("$migration_file")
            fi
        fi
    done
    
    printf '%s\n' "${pending_migrations[@]}"
}

# 执行单个迁移
execute_migration() {
    local migration_file="$1"
    local start_time=$(date +%s%3N)
    
    log_info "Executing migration: $(basename "$migration_file")"
    
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" < "$migration_file"; then
        local end_time=$(date +%s%3N)
        local execution_time=$((end_time - start_time))
        
        # 记录迁移成功
        local version=$(basename "$migration_file" .sql | cut -d'__' -f1)
        local description=$(basename "$migration_file" .sql | cut -d'__' -f2- | tr '_' ' ')
        
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" << EOF
INSERT INTO schema_migrations (version, description, executed_at, execution_time_ms, success) 
VALUES ('$version', '$description', NOW(), $execution_time, TRUE)
ON DUPLICATE KEY UPDATE 
    executed_at = NOW(),
    execution_time_ms = $execution_time,
    success = TRUE,
    error_message = NULL;
EOF
        
        log_success "Migration $(basename "$migration_file") executed successfully (${execution_time}ms)"
        return 0
    else
        local end_time=$(date +%s%3N)
        local execution_time=$((end_time - start_time))
        
        # 记录迁移失败
        local version=$(basename "$migration_file" .sql | cut -d'__' -f1)
        local description=$(basename "$migration_file" .sql | cut -d'__' -f2- | tr '_' ' ')
        
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" << EOF
INSERT INTO schema_migrations (version, description, executed_at, execution_time_ms, success, error_message) 
VALUES ('$version', '$description', NOW(), $execution_time, FALSE, 'Migration execution failed')
ON DUPLICATE KEY UPDATE 
    executed_at = NOW(),
    execution_time_ms = $execution_time,
    success = FALSE,
    error_message = 'Migration execution failed';
EOF
        
        log_error "Migration $(basename "$migration_file") failed"
        return 1
    fi
}

# 显示迁移状态
show_migration_status() {
    log_info "Migration Status:"
    echo "=================="
    
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" << EOF
SELECT 
    version as 'Version',
    description as 'Description',
    executed_at as 'Executed At',
    execution_time_ms as 'Time (ms)',
    CASE WHEN success THEN 'SUCCESS' ELSE 'FAILED' END as 'Status',
    error_message as 'Error Message'
FROM schema_migrations 
ORDER BY executed_at DESC;
EOF
}

# 主函数
main() {
    log_info "Starting database migration process..."
    log_info "====================================="
    
    # 检查数据库连接
    if ! check_database_connection; then
        exit 1
    fi
    
    # 创建迁移记录表
    create_migrations_table
    
    # 获取已执行的迁移
    local executed_migrations=$(get_executed_migrations)
    log_info "Executed migrations: $(echo "$executed_migrations" | wc -l)"
    
    # 获取待执行的迁移
    local pending_migrations=$(get_pending_migrations "$executed_migrations")
    local pending_count=$(echo "$pending_migrations" | wc -l)
    
    if [ "$pending_count" -eq 0 ]; then
        log_success "No pending migrations found"
    else
        log_info "Found $pending_count pending migrations"
        
        # 执行待执行的迁移
        local failed_migrations=0
        while IFS= read -r migration_file; do
            if [ -n "$migration_file" ]; then
                if ! execute_migration "$migration_file"; then
                    ((failed_migrations++))
                fi
            fi
        done <<< "$pending_migrations"
        
        if [ "$failed_migrations" -eq 0 ]; then
            log_success "All migrations executed successfully"
        else
            log_error "$failed_migrations migration(s) failed"
            exit 1
        fi
    fi
    
    # 显示迁移状态
    echo
    show_migration_status
    
    log_success "Migration process completed"
}

# 显示帮助信息
show_help() {
    echo "Database Migration Script"
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help              Show this help message"
    echo "  --status                Show migration status only"
    echo "  --host HOST             Database host (default: localhost)"
    echo "  --port PORT             Database port (default: 3306)"
    echo "  --database DATABASE     Database name (default: medicine)"
    echo "  --user USER             Database user (default: root)"
    echo "  --password PASSWORD     Database password (default: 123456)"
    echo "  --migrations-dir DIR    Migrations directory (default: sql/migrations)"
    echo ""
    echo "Environment Variables:"
    echo "  DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, MIGRATIONS_DIR"
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        --status)
            check_database_connection
            create_migrations_table
            show_migration_status
            exit 0
            ;;
        --host)
            DB_HOST="$2"
            shift 2
            ;;
        --port)
            DB_PORT="$2"
            shift 2
            ;;
        --database)
            DB_NAME="$2"
            shift 2
            ;;
        --user)
            DB_USER="$2"
            shift 2
            ;;
        --password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        --migrations-dir)
            MIGRATIONS_DIR="$2"
            shift 2
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# 执行主函数
main
