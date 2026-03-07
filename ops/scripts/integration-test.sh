#!/bin/bash

# 集成服务测试脚本
# 用于测试患者档案、药品信息、用户权限等集成服务

set -e

# 配置变量
BASE_URL="http://localhost:8080"
MEDICATION_API_BASE="${BASE_URL}/medication"
INTEGRATION_API_BASE="${MEDICATION_API_BASE}/integration"

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

# 检查服务是否可用
check_service() {
    log_info "检查服务是否可用..."
    
    if curl -s -f "${BASE_URL}/actuator/health" > /dev/null; then
        log_info "服务可用"
        return 0
    else
        log_error "服务不可用"
        return 1
    fi
}

# 测试用药计划验证
test_medication_plan_validation() {
    log_info "测试用药计划验证..."
    
    local request_data='{
        "patientId": 1,
        "drugId": 1,
        "userId": 1
    }'
    
    local response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "$request_data" \
        "${INTEGRATION_API_BASE}/validate-medication-plan")
    
    if echo "$response" | grep -q '"code":0'; then
        log_info "用药计划验证测试通过"
        echo "$response" | jq '.'
    else
        log_error "用药计划验证测试失败"
        echo "$response"
        return 1
    fi
}

# 测试报告生成验证
test_report_generation_validation() {
    log_info "测试报告生成验证..."
    
    local request_data='{
        "patientId": 1,
        "userId": 1,
        "reportType": 1
    }'
    
    local response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "$request_data" \
        "${INTEGRATION_API_BASE}/validate-report-generation")
    
    if echo "$response" | grep -q '"code":0'; then
        log_info "报告生成验证测试通过"
        echo "$response" | jq '.'
    else
        log_error "报告生成验证测试失败"
        echo "$response"
        return 1
    fi
}

# 测试集成服务统计
test_integration_stats() {
    log_info "测试集成服务统计..."
    
    local response=$(curl -s -X GET "${INTEGRATION_API_BASE}/stats")
    
    if echo "$response" | grep -q '"code":0'; then
        log_info "集成服务统计测试通过"
        echo "$response" | jq '.'
    else
        log_error "集成服务统计测试失败"
        echo "$response"
        return 1
    fi
}

# 测试指定服务统计
test_specific_service_stats() {
    log_info "测试指定服务统计..."
    
    local service_name="medication-plan-validation"
    local response=$(curl -s -X GET "${INTEGRATION_API_BASE}/stats/${service_name}")
    
    if echo "$response" | grep -q '"code":0'; then
        log_info "指定服务统计测试通过"
        echo "$response" | jq '.'
    else
        log_warn "指定服务统计测试失败（可能是服务不存在）"
        echo "$response"
    fi
}

# 测试清理统计
test_clear_stats() {
    log_info "测试清理统计..."
    
    local response=$(curl -s -X DELETE "${INTEGRATION_API_BASE}/stats")
    
    if echo "$response" | grep -q '"code":0'; then
        log_info "清理统计测试通过"
    else
        log_error "清理统计测试失败"
        echo "$response"
        return 1
    fi
}

# 性能测试
performance_test() {
    log_info "开始性能测试..."
    
    local test_count=100
    local success_count=0
    local failure_count=0
    
    for i in $(seq 1 $test_count); do
        local request_data='{
            "patientId": 1,
            "drugId": 1,
            "userId": 1
        }'
        
        local response=$(curl -s -X POST \
            -H "Content-Type: application/json" \
            -d "$request_data" \
            "${INTEGRATION_API_BASE}/validate-medication-plan")
        
        if echo "$response" | grep -q '"code":0'; then
            ((success_count++))
        else
            ((failure_count++))
        fi
        
        # 显示进度
        if [ $((i % 10)) -eq 0 ]; then
            log_info "进度: $i/$test_count"
        fi
    done
    
    log_info "性能测试完成:"
    log_info "  总请求数: $test_count"
    log_info "  成功数: $success_count"
    log_info "  失败数: $failure_count"
    log_info "  成功率: $(echo "scale=2; $success_count * 100 / $test_count" | bc)%"
}

# 压力测试
stress_test() {
    log_info "开始压力测试..."
    
    local concurrent_users=10
    local requests_per_user=50
    
    for i in $(seq 1 $concurrent_users); do
        (
            for j in $(seq 1 $requests_per_user); do
                local request_data='{
                    "patientId": 1,
                    "drugId": 1,
                    "userId": 1
                }'
                
                curl -s -X POST \
                    -H "Content-Type: application/json" \
                    -d "$request_data" \
                    "${INTEGRATION_API_BASE}/validate-medication-plan" > /dev/null
            done
        ) &
    done
    
    wait
    log_info "压力测试完成"
}

# 主函数
main() {
    log_info "开始集成服务测试..."
    
    # 检查服务是否可用
    if ! check_service; then
        log_error "服务不可用，退出测试"
        exit 1
    fi
    
    # 运行测试
    test_medication_plan_validation
    test_report_generation_validation
    test_integration_stats
    test_specific_service_stats
    
    # 性能测试
    if [ "$1" = "--performance" ]; then
        performance_test
    fi
    
    # 压力测试
    if [ "$1" = "--stress" ]; then
        stress_test
    fi
    
    # 清理测试
    test_clear_stats
    
    log_info "集成服务测试完成"
}

# 帮助信息
show_help() {
    echo "集成服务测试脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  --performance    运行性能测试"
    echo "  --stress        运行压力测试"
    echo "  --help          显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0                    # 运行基本测试"
    echo "  $0 --performance      # 运行性能测试"
    echo "  $0 --stress          # 运行压力测试"
}

# 检查参数
if [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# 运行主函数
main "$@"
