#!/bin/bash

# 性能测试脚本
# 用于建立性能基线和验证系统性能

echo "=== 用药打卡系统性能测试 ==="
echo "测试时间: $(date)"
echo ""

# 基础配置
BASE_URL="http://localhost:48080"
HEALTH_ENDPOINT="$BASE_URL/actuator/health"
LOGIN_ENDPOINT="$BASE_URL/admin-api/system/auth/login"
PATIENT_ENDPOINT="$BASE_URL/admin-api/patient/list/page"
MEDICATION_ENDPOINT="$BASE_URL/admin-api/medication/plan/page"

# 测试用户凭据
USERNAME="admin"
PASSWORD="admin123"

# 创建临时文件
TEMP_DIR=$(mktemp -d)
LOGIN_RESPONSE="$TEMP_DIR/login_response.json"
PERFORMANCE_LOG="$TEMP_DIR/performance.log"

# 清理函数
cleanup() {
    rm -rf "$TEMP_DIR"
}
trap cleanup EXIT

# 获取访问令牌
get_access_token() {
    echo "=== 获取访问令牌 ==="
    curl -s -X POST "$LOGIN_ENDPOINT" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" \
        -o "$LOGIN_RESPONSE"
    
    if [ $? -eq 0 ]; then
        TOKEN=$(jq -r '.data.accessToken' "$LOGIN_RESPONSE" 2>/dev/null)
        if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
            echo "访问令牌获取成功"
            return 0
        fi
    fi
    
    echo "访问令牌获取失败"
    return 1
}

# 性能测试函数
performance_test() {
    local endpoint="$1"
    local description="$2"
    local method="${3:-GET}"
    local data="${4:-}"
    local headers="${5:-}"
    
    echo ""
    echo "=== $description ==="
    
    # 构建curl命令
    local curl_cmd="curl -w '@curl-format.txt' -o /dev/null -s"
    
    if [ "$method" = "POST" ]; then
        curl_cmd="$curl_cmd -X POST"
    fi
    
    if [ -n "$data" ]; then
        curl_cmd="$curl_cmd -d '$data'"
    fi
    
    if [ -n "$headers" ]; then
        curl_cmd="$curl_cmd -H '$headers'"
    fi
    
    curl_cmd="$curl_cmd '$endpoint'"
    
    # 执行测试
    echo "执行命令: $curl_cmd"
    eval $curl_cmd | tee -a "$PERFORMANCE_LOG"
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        echo "测试成功"
    else
        echo "测试失败"
    fi
}

# 并发测试函数
concurrent_test() {
    local endpoint="$1"
    local description="$2"
    local concurrent_users="$3"
    local total_requests="$4"
    
    echo ""
    echo "=== $description (并发测试) ==="
    echo "并发用户数: $concurrent_users"
    echo "总请求数: $total_requests"
    
    # 使用ab进行并发测试
    ab -n "$total_requests" -c "$concurrent_users" "$endpoint" 2>/dev/null | tee -a "$PERFORMANCE_LOG"
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        echo "并发测试完成"
    else
        echo "并发测试失败"
    fi
}

# 主测试流程
main() {
    echo "开始性能测试..."
    
    # 1. 健康检查
    performance_test "$HEALTH_ENDPOINT" "健康检查响应时间"
    
    # 2. 登录测试
    if get_access_token; then
        # 3. 患者分页测试
        performance_test "$PATIENT_ENDPOINT?pageNo=1&pageSize=10" \
                        "患者分页查询响应时间" \
                        "GET" \
                        "" \
                        "Authorization: Bearer $TOKEN"
        
        # 4. 用药计划分页测试
        performance_test "$MEDICATION_ENDPOINT?pageNo=1&pageSize=10" \
                        "用药计划分页查询响应时间" \
                        "GET" \
                        "" \
                        "Authorization: Bearer $TOKEN"
        
        # 5. 并发测试
        concurrent_test "$HEALTH_ENDPOINT" "健康检查并发测试" 10 100
        concurrent_test "$PATIENT_ENDPOINT?pageNo=1&pageSize=10" "患者分页并发测试" 5 50
        
    else
        echo "跳过需要认证的测试"
    fi
    
    # 6. 系统资源监控
    echo ""
    echo "=== 系统资源使用情况 ==="
    docker stats --no-stream | tee -a "$PERFORMANCE_LOG"
    
    # 7. 数据库连接状态
    echo ""
    echo "=== 数据库连接状态 ==="
    docker exec -it medicine-mysql mysql -uroot -p123456 -e "SHOW STATUS LIKE 'Threads_connected';" 2>/dev/null | tee -a "$PERFORMANCE_LOG"
    
    # 8. Redis状态
    echo ""
    echo "=== Redis状态 ==="
    docker exec -it medicine-redis redis-cli info memory 2>/dev/null | tee -a "$PERFORMANCE_LOG"
    
    # 9. 生成性能报告
    echo ""
    echo "=== 性能测试报告 ==="
    echo "测试完成时间: $(date)"
    echo "性能日志文件: $PERFORMANCE_LOG"
    
    # 分析结果
    echo ""
    echo "=== 性能分析 ==="
    
    # 检查响应时间
    if [ -f "$PERFORMANCE_LOG" ]; then
        echo "平均响应时间分析:"
        grep "time_total" "$PERFORMANCE_LOG" | awk '{sum+=$2; count++} END {if(count>0) print "平均响应时间: " sum/count "秒"}'
        
        echo "最大响应时间:"
        grep "time_total" "$PERFORMANCE_LOG" | awk '{if(max<$2) max=$2} END {print "最大响应时间: " max "秒"}'
    fi
    
    echo ""
    echo "性能测试完成！"
}

# 检查依赖
check_dependencies() {
    echo "检查测试依赖..."
    
    # 检查curl
    if ! command -v curl &> /dev/null; then
        echo "错误: curl未安装"
        exit 1
    fi
    
    # 检查jq
    if ! command -v jq &> /dev/null; then
        echo "错误: jq未安装"
        exit 1
    fi
    
    # 检查ab
    if ! command -v ab &> /dev/null; then
        echo "警告: ab未安装，跳过并发测试"
    fi
    
    # 检查docker
    if ! command -v docker &> /dev/null; then
        echo "警告: docker未安装，跳过容器状态检查"
    fi
    
    echo "依赖检查完成"
}

# 创建curl格式文件
create_curl_format() {
    cat > curl-format.txt << 'EOF'
     time_namelookup:  %{time_namelookup}\n
        time_connect:  %{time_connect}\n
     time_appconnect:  %{time_appconnect}\n
    time_pretransfer:  %{time_pretransfer}\n
       time_redirect:  %{time_redirect}\n
  time_starttransfer:  %{time_starttransfer}\n
                     ----------\n
          time_total:  %{time_total}\n
EOF
}

# 脚本入口
if [ "${BASH_SOURCE[0]}" == "${0}" ]; then
    check_dependencies
    create_curl_format
    main
fi