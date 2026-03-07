#!/bin/bash
# =============================================
# 医疗HIS系统冒烟测试脚本
# 测试患者表单提交和药师登录流程
# =============================================

set -e  # 遇到错误立即退出

# 配置变量
BASE_URL="http://localhost:48080"
ADMIN_URL="http://localhost:8080"
PATIENT_URL="http://localhost:5173"
PHARMACIST_URL="http://localhost:3000"

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED_TESTS++))
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED_TESTS++))
}

# 测试函数
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    ((TOTAL_TESTS++))
    log_info "Running test: $test_name"
    
    if eval "$test_command"; then
        log_success "$test_name"
        return 0
    else
        log_error "$test_name"
        return 1
    fi
}

# 检查服务是否运行
check_service() {
    local service_name="$1"
    local url="$2"
    
    if curl -s --connect-timeout 5 "$url" > /dev/null 2>&1; then
        return 0
    else
        log_error "Service $service_name is not running at $url"
        return 1
    fi
}

# 获取访问令牌
get_token() {
    local username="$1"
    local password="$2"
    
    local response=$(curl -s -X POST "$BASE_URL/admin-api/system/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    
    echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4
}

# 测试患者权限
test_patient_permissions() {
    local token=$(get_token "patient" "123456")
    
    if [ -z "$token" ]; then
        log_error "Failed to get patient token"
        return 1
    fi
    
    # 测试患者信息创建权限
    local response=$(curl -s -X POST "$BASE_URL/admin-api/patient/list/create" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{
            "userId": 135,
            "address": "测试地址",
            "patientType": 1,
            "insurance": 1,
            "birthday": "1990-01-01T00:00:00",
            "name": "测试患者",
            "phone": "13800000000"
        }')
    
    if echo "$response" | grep -q '"code":0'; then
        return 0
    else
        log_error "Patient permission test failed: $response"
        return 1
    fi
}

# 测试药师登录和仪表盘访问
test_pharmacist_dashboard() {
    local token=$(get_token "pharmacist" "123456")
    
    if [ -z "$token" ]; then
        log_error "Failed to get pharmacist token"
        return 1
    fi
    
    # 测试获取用户信息
    local user_info=$(curl -s -X GET "$BASE_URL/admin-api/system/auth/get-permission-info" \
        -H "Authorization: Bearer $token")
    
    if echo "$user_info" | grep -q '"code":0'; then
        # 检查用户角色
        if echo "$user_info" | grep -q '"pharmacist"'; then
            return 0
        else
            log_error "Pharmacist role not found in user info"
            return 1
        fi
    else
        log_error "Failed to get pharmacist user info: $user_info"
        return 1
    fi
}

# 测试工单创建
test_help_order_creation() {
    local token=$(get_token "patient" "123456")
    
    if [ -z "$token" ]; then
        log_error "Failed to get patient token for help order test"
        return 1
    fi
    
    local response=$(curl -s -X POST "$BASE_URL/admin-api/help-orders/create" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{
            "patientId": 135,
            "title": "测试工单",
            "description": "这是一个测试工单",
            "intendedDrugs": ["测试药品"],
            "attachments": [],
            "toDoctor": false
        }')
    
    if echo "$response" | grep -q '"code":0'; then
        return 0
    else
        log_error "Help order creation test failed: $response"
        return 1
    fi
}

# 测试数据库连接
test_database_connection() {
    if mysql -uroot -p123456 -D medicine -e "SELECT 1" > /dev/null 2>&1; then
        return 0
    else
        log_error "Database connection failed"
        return 1
    fi
}

# 测试用户名称显示
test_user_name_display() {
    local result=$(mysql -uroot -p123456 -D medicine -e "SELECT nickname FROM system_users WHERE username = 'pharmacist'" 2>/dev/null | tail -n 1)
    
    if [ "$result" = "药师" ]; then
        return 0
    else
        log_error "User name display test failed. Expected '药师', got '$result'"
        return 1
    fi
}

# 主测试函数
main() {
    log_info "Starting Medical HIS System Smoke Tests..."
    log_info "=========================================="
    
    # 检查服务状态
    run_test "Backend Service Check" "check_service 'Backend' '$BASE_URL'"
    run_test "Database Connection Check" "test_database_connection"
    
    # 测试用户名称修复
    run_test "User Name Display Test" "test_user_name_display"
    
    # 测试患者权限
    run_test "Patient Permission Test" "test_patient_permissions"
    
    # 测试药师登录和仪表盘
    run_test "Pharmacist Dashboard Test" "test_pharmacist_dashboard"
    
    # 测试工单创建
    run_test "Help Order Creation Test" "test_help_order_creation"
    
    # 输出测试结果
    log_info "=========================================="
    log_info "Test Results Summary:"
    log_info "Total Tests: $TOTAL_TESTS"
    log_success "Passed: $PASSED_TESTS"
    
    if [ $FAILED_TESTS -gt 0 ]; then
        log_error "Failed: $FAILED_TESTS"
        exit 1
    else
        log_success "All tests passed!"
        exit 0
    fi
}

# 检查依赖
check_dependencies() {
    local missing_deps=()
    
    if ! command -v curl &> /dev/null; then
        missing_deps+=("curl")
    fi
    
    if ! command -v mysql &> /dev/null; then
        missing_deps+=("mysql")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        log_error "Missing dependencies: ${missing_deps[*]}"
        log_error "Please install the missing dependencies and try again."
        exit 1
    fi
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    check_dependencies
    main "$@"
fi
