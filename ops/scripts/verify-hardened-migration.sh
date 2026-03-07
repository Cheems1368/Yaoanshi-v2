#!/bin/bash
# =============================================
# 强化迁移方案验证脚本
# 验证tenant_id约束、Docker集成和持久化机制
# =============================================

set -e

# 配置
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-medicine}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-123456}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${BLUE}[VERIFY]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
}

# 测试函数
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    log_info "Running test: $test_name"
    
    if eval "$test_command"; then
        log_success "$test_name"
        return 0
    else
        log_error "$test_name"
        return 1
    fi
}

# 检查数据库连接
check_database_connection() {
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -e "SELECT 1" > /dev/null 2>&1; then
        return 0
    else
        log_error "Database connection failed"
        return 1
    fi
}

# 验证tenant_id约束
test_tenant_id_constraints() {
    # 检查用户表的tenant_id约束
    local user_constraint=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -sN -e "SELECT COUNT(*) FROM system_users WHERE tenant_id IS NULL" 2>/dev/null)
    
    if [ "$user_constraint" -eq 0 ]; then
        return 0
    else
        log_error "Found $user_constraint users without tenant_id"
        return 1
    fi
}

# 验证用户名称修复
test_user_name_fix() {
    local doctor_nickname=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -sN -e "SELECT nickname FROM system_users WHERE username = 'doctor' AND tenant_id = 1" 2>/dev/null)
    local patient_nickname=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -sN -e "SELECT nickname FROM system_users WHERE username = 'patient' AND tenant_id = 1" 2>/dev/null)
    local pharmacist_nickname=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -sN -e "SELECT nickname FROM system_users WHERE username = 'pharmacist' AND tenant_id = 1" 2>/dev/null)
    
    if [ "$doctor_nickname" = "医生" ] && [ "$patient_nickname" = "患者" ] && [ "$pharmacist_nickname" = "药师" ]; then
        return 0
    else
        log_error "User name fix failed. Doctor: $doctor_nickname, Patient: $patient_nickname, Pharmacist: $pharmacist_nickname"
        return 1
    fi
}

# 验证患者权限配置
test_patient_permissions() {
    local permission_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -sN -e "SELECT COUNT(*) FROM system_role_menu rm JOIN system_role r ON rm.role_id = r.id JOIN system_menu m ON rm.menu_id = m.id WHERE r.code = 'patient' AND r.tenant_id = 1 AND m.tenant_id = 1 AND rm.tenant_id = 1" 2>/dev/null)
    
    if [ "$permission_count" -gt 0 ]; then
        return 0
    else
        log_error "No patient permissions found"
        return 1
    fi
}

# 验证迁移记录表
test_migration_table() {
    local table_exists=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -sN -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME' AND table_name = 'schema_migrations'" 2>/dev/null)
    
    if [ "$table_exists" -eq 1 ]; then
        return 0
    else
        log_error "Migration table does not exist"
        return 1
    fi
}

# 验证Docker集成文件
test_docker_integration() {
    local migration_script_exists=false
    local docker_compose_updated=false
    
    if [ -f "sql/docker-entrypoint-apply-migrations.sh" ]; then
        migration_script_exists=true
    fi
    
    if grep -q "docker-entrypoint-apply-migrations.sh" docker-compose.yml 2>/dev/null; then
        docker_compose_updated=true
    fi
    
    if [ "$migration_script_exists" = true ] && [ "$docker_compose_updated" = true ]; then
        return 0
    else
        log_error "Docker integration incomplete. Script exists: $migration_script_exists, Compose updated: $docker_compose_updated"
        return 1
    fi
}

# 验证脚本权限
test_script_permissions() {
    local scripts_executable=true
    
    for script in scripts/run-migrations.sh scripts/smoke-test.sh scripts/ci-cd-pipeline.sh; do
        if [ -f "$script" ] && [ ! -x "$script" ]; then
            scripts_executable=false
            break
        fi
    done
    
    if [ "$scripts_executable" = true ]; then
        return 0
    else
        log_error "Some scripts are not executable"
        return 1
    fi
}

# 显示验证结果
show_verification_results() {
    log_info "Verification Results Summary:"
    echo "================================"
    
    # 显示用户信息
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" << EOF
SELECT 
    username as 'Username',
    nickname as 'Nickname',
    remark as 'Remark',
    tenant_id as 'Tenant ID'
FROM system_users 
WHERE username IN ('doctor', 'patient', 'pharmacist') AND tenant_id = 1
ORDER BY username;
EOF
    
    echo
    # 显示患者权限
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" << EOF
SELECT 
    r.name as 'Role Name',
    m.name as 'Menu Name',
    m.permission as 'Permission'
FROM system_role r
JOIN system_role_menu rm ON r.id = rm.role_id
JOIN system_menu m ON rm.menu_id = m.id
WHERE r.code = 'patient' AND r.tenant_id = 1 AND m.tenant_id = 1 AND rm.tenant_id = 1
ORDER BY m.permission;
EOF
    
    echo
    # 显示迁移状态
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" << EOF
SELECT 
    version as 'Version',
    description as 'Description',
    executed_at as 'Executed At',
    CASE WHEN success THEN 'SUCCESS' ELSE 'FAILED' END as 'Status'
FROM schema_migrations 
ORDER BY executed_at DESC;
EOF
}

# 主函数
main() {
    log_info "Starting Hardened Migration Verification..."
    log_info "=========================================="
    
    # 检查数据库连接
    run_test "Database Connection Check" "check_database_connection"
    
    # 验证强化功能
    run_test "Tenant ID Constraints" "test_tenant_id_constraints"
    run_test "User Name Fix" "test_user_name_fix"
    run_test "Patient Permissions" "test_patient_permissions"
    run_test "Migration Table" "test_migration_table"
    run_test "Docker Integration" "test_docker_integration"
    run_test "Script Permissions" "test_script_permissions"
    
    # 显示详细结果
    echo
    show_verification_results
    
    log_success "Hardened migration verification completed"
}

# 显示帮助信息
show_help() {
    echo "Hardened Migration Verification Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help              Show this help message"
    echo "  --host HOST             Database host (default: localhost)"
    echo "  --port PORT             Database port (default: 3306)"
    echo "  --database DATABASE     Database name (default: medicine)"
    echo "  --user USER             Database user (default: root)"
    echo "  --password PASSWORD     Database password (default: 123456)"
    echo ""
    echo "Environment Variables:"
    echo "  DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD"
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
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
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# 执行主函数
main
