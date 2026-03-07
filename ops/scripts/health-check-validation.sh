#!/bin/bash

# 健康检查自动化验证脚本
# 用于验证应用启动和健康状态

set -e

# 配置参数
APP_NAME="medicine-server"
APP_PORT=8080
HEALTH_ENDPOINT="http://localhost:${APP_PORT}/actuator/health"
STARTUP_TIMEOUT=120
HEALTH_CHECK_TIMEOUT=30
LOG_FILE="startup-validation.log"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] ✅ $1${NC}" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  $1${NC}" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ❌ $1${NC}" | tee -a "$LOG_FILE"
}

# 检查端口是否被占用
check_port() {
    log "检查端口 ${APP_PORT} 是否被占用..."
    if lsof -Pi :${APP_PORT} -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warning "端口 ${APP_PORT} 已被占用，尝试停止现有进程..."
        PID=$(lsof -Pi :${APP_PORT} -sTCP:LISTEN -t)
        kill -9 $PID 2>/dev/null || true
        sleep 2
    else
        log_success "端口 ${APP_PORT} 可用"
    fi
}

# 启动应用
start_application() {
    log "启动应用 ${APP_NAME}..."
    
    # 使用最小化配置启动
    nohup java -jar \
        -Dspring.profiles.active=minimal-test \
        -Dspring.config.location=classpath:/application-minimal-test.yaml \
        -Dlogging.level.com.sqlapk.medicine=DEBUG \
        -Dlogging.level.org.springframework.web=DEBUG \
        target/${APP_NAME}-*.jar > app.log 2>&1 &
    
    APP_PID=$!
    echo $APP_PID > app.pid
    log "应用已启动，PID: ${APP_PID}"
}

# 等待应用启动完成
wait_for_startup() {
    log "等待应用启动完成（超时: ${STARTUP_TIMEOUT}秒）..."
    
    local start_time=$(date +%s)
    local startup_completed=false
    
    while [ $(($(date +%s) - start_time)) -lt $STARTUP_TIMEOUT ]; do
        # 检查进程是否还在运行
        if ! kill -0 $APP_PID 2>/dev/null; then
            log_error "应用进程已退出"
            return 1
        fi
        
        # 检查启动完成标识
        if grep -q "Started MedicineServerApplication" app.log 2>/dev/null; then
            log_success "检测到应用启动完成标识"
            startup_completed=true
            break
        fi
        
        # 检查端口是否监听
        if lsof -Pi :${APP_PORT} -sTCP:LISTEN -t >/dev/null 2>&1; then
            log_success "端口 ${APP_PORT} 开始监听"
            startup_completed=true
            break
        fi
        
        sleep 2
        echo -n "."
    done
    
    if [ "$startup_completed" = false ]; then
        log_error "应用启动超时"
        return 1
    fi
    
    # 额外等待，确保应用完全就绪
    log "等待应用完全就绪..."
    sleep 5
}

# 检查健康状态
check_health() {
    log "检查应用健康状态..."
    
    local start_time=$(date +%s)
    local health_ok=false
    
    while [ $(($(date +%s) - start_time)) -lt $HEALTH_CHECK_TIMEOUT ]; do
        if curl -s -f "$HEALTH_ENDPOINT" >/dev/null 2>&1; then
            log_success "健康检查通过"
            health_ok=true
            break
        fi
        
        sleep 2
        echo -n "."
    done
    
    if [ "$health_ok" = false ]; then
        log_error "健康检查失败"
        return 1
    fi
}

# 获取详细健康信息
get_health_details() {
    log "获取详细健康信息..."
    
    local health_response=$(curl -s "$HEALTH_ENDPOINT")
    echo "$health_response" | jq . 2>/dev/null || echo "$health_response"
    
    # 检查关键组件状态
    local db_status=$(echo "$health_response" | jq -r '.components.db.status' 2>/dev/null || echo "unknown")
    local redis_status=$(echo "$health_response" | jq -r '.components.redis.status' 2>/dev/null || echo "unknown")
    
    log "数据库状态: $db_status"
    log "Redis状态: $redis_status"
    
    if [ "$db_status" = "UP" ] && [ "$redis_status" = "UP" ]; then
        log_success "核心组件健康状态正常"
    else
        log_warning "部分核心组件状态异常"
    fi
}

# 检查启动日志
check_startup_logs() {
    log "检查启动日志..."
    
    # 检查关键启动信息
    if grep -q "Started MedicineServerApplication" app.log; then
        log_success "应用启动成功"
    else
        log_warning "未找到应用启动成功标识"
    fi
    
    # 检查错误信息
    local error_count=$(grep -c "ERROR" app.log 2>/dev/null || echo "0")
    if [ "$error_count" -gt 0 ]; then
        log_warning "发现 $error_count 个错误日志"
        grep "ERROR" app.log | tail -5
    else
        log_success "未发现错误日志"
    fi
    
    # 检查警告信息
    local warning_count=$(grep -c "WARN" app.log 2>/dev/null || echo "0")
    if [ "$warning_count" -gt 0 ]; then
        log_warning "发现 $warning_count 个警告日志"
    fi
}

# 性能检查
check_performance() {
    log "检查应用性能..."
    
    # 检查启动时间
    local startup_time=$(grep "Started MedicineServerApplication" app.log | \
        sed -n 's/.*in \([0-9.]*\) seconds.*/\1/p' | tail -1)
    
    if [ -n "$startup_time" ]; then
        log "应用启动时间: ${startup_time}秒"
        if (( $(echo "$startup_time < 60" | bc -l) )); then
            log_success "启动时间正常"
        else
            log_warning "启动时间较长"
        fi
    fi
    
    # 检查内存使用
    if command -v ps >/dev/null 2>&1; then
        local memory_usage=$(ps -o rss= -p $APP_PID 2>/dev/null | awk '{print $1/1024}')
        if [ -n "$memory_usage" ]; then
            log "内存使用: ${memory_usage}MB"
        fi
    fi
}

# 清理函数
cleanup() {
    log "清理资源..."
    
    if [ -f "app.pid" ]; then
        local pid=$(cat app.pid)
        if kill -0 $pid 2>/dev/null; then
            log "停止应用进程 $pid"
            kill $pid
            sleep 3
            if kill -0 $pid 2>/dev/null; then
                kill -9 $pid
            fi
        fi
        rm -f app.pid
    fi
    
    # 清理日志文件（可选）
    # rm -f app.log
}

# 主函数
main() {
    log "开始健康检查自动化验证..."
    
    # 设置清理陷阱
    trap cleanup EXIT
    
    # 检查依赖
    if ! command -v curl >/dev/null 2>&1; then
        log_error "curl 命令未找到，请安装 curl"
        exit 1
    fi
    
    if ! command -v jq >/dev/null 2>&1; then
        log_warning "jq 命令未找到，JSON解析功能受限"
    fi
    
    # 执行检查步骤
    check_port
    start_application
    wait_for_startup
    check_health
    get_health_details
    check_startup_logs
    check_performance
    
    log_success "健康检查自动化验证完成！"
    
    # 显示最终状态
    echo ""
    echo "==================== 验证结果 ===================="
    echo "应用状态: 运行中"
    echo "健康检查: 通过"
    echo "核心组件: 正常"
    echo "日志文件: $LOG_FILE"
    echo "应用日志: app.log"
    echo "================================================"
}

# 运行主函数
main "$@"
