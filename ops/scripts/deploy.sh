#!/bin/bash

# 部署脚本
# 用于自动化部署用药打卡系统

set -e

# 配置参数
APP_NAME="medicine-server"
APP_VERSION="${APP_VERSION:-latest}"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-}"
DOCKER_IMAGE="${DOCKER_REGISTRY:+$DOCKER_REGISTRY/}${APP_NAME}:${APP_VERSION}"
DEPLOY_ENV="${DEPLOY_ENV:-prod}"
BACKUP_BEFORE_DEPLOY="${BACKUP_BEFORE_DEPLOY:-true}"
HEALTH_CHECK_TIMEOUT="${HEALTH_CHECK_TIMEOUT:-300}"
ROLLBACK_ON_FAILURE="${ROLLBACK_ON_FAILURE:-true}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

log_debug() {
    echo -e "${BLUE}[DEBUG]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查部署依赖..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装"
        exit 1
    fi
    
    if ! command -v curl &> /dev/null; then
        log_error "curl未安装"
        exit 1
    fi
    
    log_info "依赖检查完成"
}

# 预部署检查
pre_deploy_check() {
    log_info "执行预部署检查..."
    
    # 检查Docker服务状态
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker服务未运行"
        exit 1
    fi
    
    # 检查端口占用
    local port=$(grep "SERVER_PORT" .env 2>/dev/null | cut -d'=' -f2 || echo "48080")
    if netstat -tuln | grep -q ":$port "; then
        log_warn "端口 $port 已被占用"
    fi
    
    # 检查磁盘空间
    local available_space=$(df -h . | awk 'NR==2 {print $4}')
    log_info "可用磁盘空间: $available_space"
    
    # 检查内存
    local available_memory=$(free -h | awk 'NR==2 {print $7}')
    log_info "可用内存: $available_memory"
    
    log_info "预部署检查完成"
}

# 备份当前部署
backup_current_deployment() {
    if [ "$BACKUP_BEFORE_DEPLOY" = "true" ]; then
        log_info "备份当前部署..."
        
        local timestamp=$(date +"%Y%m%d_%H%M%S")
        local backup_dir="backup_${timestamp}"
        
        mkdir -p "$backup_dir"
        
        # 备份配置文件
        cp -r .env* "$backup_dir/" 2>/dev/null || true
        cp -r application*.yml "$backup_dir/" 2>/dev/null || true
        cp -r docker-compose.yml "$backup_dir/" 2>/dev/null || true
        
        # 备份数据库
        if [ -f "scripts/backup-restore.sh" ]; then
            ./scripts/backup-restore.sh backup tables
        fi
        
        log_info "部署备份完成: $backup_dir"
    fi
}

# 构建Docker镜像
build_docker_image() {
    log_info "构建Docker镜像..."
    
    if [ -f "Dockerfile" ]; then
        docker build -t "$DOCKER_IMAGE" .
        log_info "Docker镜像构建完成: $DOCKER_IMAGE"
    else
        log_warn "未找到Dockerfile，跳过镜像构建"
    fi
}

# 拉取Docker镜像
pull_docker_image() {
    log_info "拉取Docker镜像..."
    
    if [ -n "$DOCKER_REGISTRY" ]; then
        docker pull "$DOCKER_IMAGE"
        log_info "Docker镜像拉取完成: $DOCKER_IMAGE"
    else
        log_warn "未配置Docker仓库，跳过镜像拉取"
    fi
}

# 停止服务
stop_services() {
    log_info "停止服务..."
    
    if [ -f "docker-compose.yml" ]; then
        docker-compose down
        log_info "服务停止完成"
    else
        log_warn "未找到docker-compose.yml文件"
    fi
}

# 启动服务
start_services() {
    log_info "启动服务..."
    
    if [ -f "docker-compose.yml" ]; then
        docker-compose up -d
        log_info "服务启动完成"
    else
        log_error "未找到docker-compose.yml文件"
        exit 1
    fi
}

# 健康检查
health_check() {
    log_info "执行健康检查..."
    
    local port=$(grep "SERVER_PORT" .env 2>/dev/null | cut -d'=' -f2 || echo "48080")
    local health_url="http://localhost:$port/actuator/health"
    local timeout=$HEALTH_CHECK_TIMEOUT
    local interval=10
    local elapsed=0
    
    while [ $elapsed -lt $timeout ]; do
        if curl -s -f "$health_url" > /dev/null 2>&1; then
            log_info "健康检查通过"
            return 0
        fi
        
        log_info "等待服务启动... (${elapsed}s/${timeout}s)"
        sleep $interval
        elapsed=$((elapsed + interval))
    done
    
    log_error "健康检查超时"
    return 1
}

# 回滚部署
rollback_deployment() {
    log_warn "执行部署回滚..."
    
    # 停止当前服务
    stop_services
    
    # 恢复备份
    local latest_backup=$(ls -t backup_* 2>/dev/null | head -n1)
    if [ -n "$latest_backup" ]; then
        log_info "恢复备份: $latest_backup"
        cp -r "$latest_backup"/* .
        
        # 重启服务
        start_services
        
        if health_check; then
            log_info "回滚成功"
        else
            log_error "回滚失败"
            exit 1
        fi
    else
        log_error "未找到备份文件"
        exit 1
    fi
}

# 清理资源
cleanup() {
    log_info "清理资源..."
    
    # 清理未使用的Docker镜像
    docker image prune -f
    
    # 清理未使用的容器
    docker container prune -f
    
    # 清理未使用的网络
    docker network prune -f
    
    log_info "资源清理完成"
}

# 显示部署状态
show_deployment_status() {
    log_info "部署状态:"
    
    echo "服务状态:"
    docker-compose ps
    
    echo ""
    echo "容器资源使用:"
    docker stats --no-stream
    
    echo ""
    echo "磁盘使用:"
    df -h
    
    echo ""
    echo "内存使用:"
    free -h
}

# 完整部署流程
full_deploy() {
    log_info "开始完整部署流程..."
    
    # 预部署检查
    pre_deploy_check
    
    # 备份当前部署
    backup_current_deployment
    
    # 构建或拉取镜像
    if [ -n "$DOCKER_REGISTRY" ]; then
        pull_docker_image
    else
        build_docker_image
    fi
    
    # 停止服务
    stop_services
    
    # 启动服务
    start_services
    
    # 健康检查
    if health_check; then
        log_info "部署成功"
        
        # 清理资源
        cleanup
        
        # 显示状态
        show_deployment_status
    else
        log_error "部署失败"
        
        # 回滚
        if [ "$ROLLBACK_ON_FAILURE" = "true" ]; then
            rollback_deployment
        fi
        
        exit 1
    fi
}

# 快速部署（仅重启服务）
quick_deploy() {
    log_info "开始快速部署..."
    
    stop_services
    start_services
    
    if health_check; then
        log_info "快速部署成功"
        show_deployment_status
    else
        log_error "快速部署失败"
        exit 1
    fi
}

# 显示帮助信息
show_help() {
    cat << EOF
用药打卡系统部署工具

用法: $0 [命令] [选项]

命令:
    deploy             完整部署（默认）
    quick              快速部署（仅重启服务）
    build              构建Docker镜像
    pull               拉取Docker镜像
    start              启动服务
    stop               停止服务
    restart            重启服务
    status             显示部署状态
    health             健康检查
    rollback           回滚部署
    cleanup            清理资源
    help               显示此帮助信息

选项:
    -h, --help         显示帮助信息
    -v, --version      指定应用版本（默认: $APP_VERSION）
    -e, --env          指定部署环境（默认: $DEPLOY_ENV）
    -r, --registry     指定Docker仓库
    --no-backup        跳过备份
    --no-rollback      失败时不回滚
    --timeout          健康检查超时时间（秒，默认: $HEALTH_CHECK_TIMEOUT）

环境变量:
    APP_VERSION        应用版本（默认: $APP_VERSION）
    DOCKER_REGISTRY    Docker仓库地址
    DEPLOY_ENV         部署环境（默认: $DEPLOY_ENV）
    BACKUP_BEFORE_DEPLOY 部署前是否备份（默认: $BACKUP_BEFORE_DEPLOY）
    HEALTH_CHECK_TIMEOUT 健康检查超时时间（默认: $HEALTH_CHECK_TIMEOUT）
    ROLLBACK_ON_FAILURE 失败时是否回滚（默认: $ROLLBACK_ON_FAILURE）

示例:
    $0 deploy                           # 完整部署
    $0 quick                            # 快速部署
    $0 build                            # 构建镜像
    $0 pull                             # 拉取镜像
    $0 start                            # 启动服务
    $0 stop                             # 停止服务
    $0 restart                          # 重启服务
    $0 status                           # 显示状态
    $0 health                           # 健康检查
    $0 rollback                         # 回滚部署
    $0 cleanup                          # 清理资源

EOF
}

# 主函数
main() {
    local command="${1:-deploy}"
    
    case $command in
        "deploy")
            check_dependencies
            full_deploy
            ;;
        "quick")
            check_dependencies
            quick_deploy
            ;;
        "build")
            check_dependencies
            build_docker_image
            ;;
        "pull")
            check_dependencies
            pull_docker_image
            ;;
        "start")
            check_dependencies
            start_services
            health_check
            ;;
        "stop")
            check_dependencies
            stop_services
            ;;
        "restart")
            check_dependencies
            stop_services
            start_services
            health_check
            ;;
        "status")
            show_deployment_status
            ;;
        "health")
            health_check
            ;;
        "rollback")
            check_dependencies
            rollback_deployment
            ;;
        "cleanup")
            check_dependencies
            cleanup
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
