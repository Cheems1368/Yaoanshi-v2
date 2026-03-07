#!/bin/bash
# =============================================
# CI/CD 集成脚本
# 在构建管道中运行迁移和冒烟测试
# =============================================

set -e

# 配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
MIGRATIONS_SCRIPT="$SCRIPT_DIR/run-migrations.sh"
SMOKE_TEST_SCRIPT="$SCRIPT_DIR/smoke-test.sh"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${BLUE}[CI/CD]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "Checking dependencies..."
    
    local missing_deps=()
    
    if ! command -v docker &> /dev/null; then
        missing_deps+=("docker")
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        missing_deps+=("docker-compose")
    fi
    
    if ! command -v mysql &> /dev/null; then
        missing_deps+=("mysql-client")
    fi
    
    if ! command -v curl &> /dev/null; then
        missing_deps+=("curl")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        log_error "Missing dependencies: ${missing_deps[*]}"
        log_error "Please install the missing dependencies and try again."
        exit 1
    fi
    
    log_success "All dependencies available"
}

# 启动服务
start_services() {
    log_info "Starting services..."
    
    cd "$PROJECT_ROOT"
    
    # 启动数据库和Redis
    docker-compose up -d mysql redis
    
    # 等待服务就绪
    log_info "Waiting for services to be ready..."
    sleep 30
    
    # 检查服务状态
    if docker-compose ps mysql | grep -q "Up"; then
        log_success "MySQL service is running"
    else
        log_error "MySQL service failed to start"
        exit 1
    fi
    
    if docker-compose ps redis | grep -q "Up"; then
        log_success "Redis service is running"
    else
        log_error "Redis service failed to start"
        exit 1
    fi
}

# 运行数据库迁移
run_migrations() {
    log_info "Running database migrations..."
    
    if [ -f "$MIGRATIONS_SCRIPT" ]; then
        chmod +x "$MIGRATIONS_SCRIPT"
        "$MIGRATIONS_SCRIPT"
        log_success "Database migrations completed"
    else
        log_error "Migration script not found: $MIGRATIONS_SCRIPT"
        exit 1
    fi
}

# 启动应用服务
start_application() {
    log_info "Starting application services..."
    
    cd "$PROJECT_ROOT"
    
    # 构建并启动后端服务
    docker-compose up -d server
    
    # 等待后端服务就绪
    log_info "Waiting for backend service to be ready..."
    local max_attempts=30
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s --connect-timeout 5 "http://localhost:48080/actuator/health" > /dev/null 2>&1; then
            log_success "Backend service is running"
            break
        fi
        
        ((attempt++))
        log_info "Attempt $attempt/$max_attempts - waiting for backend service..."
        sleep 10
    done
    
    if [ $attempt -eq $max_attempts ]; then
        log_error "Backend service failed to start within timeout"
        exit 1
    fi
}

# 运行冒烟测试
run_smoke_tests() {
    log_info "Running smoke tests..."
    
    if [ -f "$SMOKE_TEST_SCRIPT" ]; then
        chmod +x "$SMOKE_TEST_SCRIPT"
        "$SMOKE_TEST_SCRIPT"
        log_success "Smoke tests completed"
    else
        log_error "Smoke test script not found: $SMOKE_TEST_SCRIPT"
        exit 1
    fi
}

# 清理服务
cleanup_services() {
    log_info "Cleaning up services..."
    
    cd "$PROJECT_ROOT"
    docker-compose down -v
    
    log_success "Services cleaned up"
}

# 显示使用帮助
show_help() {
    echo "CI/CD Integration Script for Medical HIS System"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help              Show this help message"
    echo "  --skip-migrations       Skip database migrations"
    echo "  --skip-smoke-tests      Skip smoke tests"
    echo "  --skip-cleanup          Skip cleanup after tests"
    echo "  --migrations-only       Run only database migrations"
    echo "  --tests-only            Run only smoke tests"
    echo ""
    echo "Environment Variables:"
    echo "  DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD"
    echo "  MIGRATIONS_DIR, SMOKE_TEST_DIR"
}

# 主函数
main() {
    local skip_migrations=false
    local skip_smoke_tests=false
    local skip_cleanup=false
    local migrations_only=false
    local tests_only=false
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            --skip-migrations)
                skip_migrations=true
                shift
                ;;
            --skip-smoke-tests)
                skip_smoke_tests=true
                shift
                ;;
            --skip-cleanup)
                skip_cleanup=true
                shift
                ;;
            --migrations-only)
                migrations_only=true
                shift
                ;;
            --tests-only)
                tests_only=true
                shift
                ;;
            *)
                log_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    log_info "Starting CI/CD pipeline..."
    log_info "=========================="
    
    # 检查依赖
    check_dependencies
    
    if [ "$tests_only" = true ]; then
        # 仅运行测试（假设服务已启动）
        run_smoke_tests
        exit 0
    fi
    
    if [ "$migrations_only" = true ]; then
        # 仅运行迁移
        start_services
        run_migrations
        if [ "$skip_cleanup" = false ]; then
            cleanup_services
        fi
        exit 0
    fi
    
    # 完整流程
    start_services
    
    if [ "$skip_migrations" = false ]; then
        run_migrations
    else
        log_warning "Skipping database migrations"
    fi
    
    start_application
    
    if [ "$skip_smoke_tests" = false ]; then
        run_smoke_tests
    else
        log_warning "Skipping smoke tests"
    fi
    
    if [ "$skip_cleanup" = false ]; then
        cleanup_services
    else
        log_warning "Skipping cleanup - services will remain running"
    fi
    
    log_success "CI/CD pipeline completed successfully"
}

# 错误处理
trap 'log_error "Pipeline failed at line $LINENO"; cleanup_services; exit 1' ERR

# 执行主函数
main "$@"
