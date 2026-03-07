#!/bin/bash

# 配置管理工具
# 用于管理应用配置和环境变量

set -e

# 配置参数
CONFIG_DIR="${CONFIG_DIR:-./config}"
ENV_FILE="${ENV_FILE:-.env}"
BACKUP_DIR="${BACKUP_DIR:-./config/backup}"

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
    log_info "检查依赖..."
    
    if ! command -v jq &> /dev/null; then
        log_error "jq未安装，请先安装jq"
        exit 1
    fi
    
    if ! command -v yq &> /dev/null; then
        log_warn "yq未安装，YAML处理功能将不可用"
    fi
    
    log_info "依赖检查完成"
}

# 创建配置目录
create_config_dirs() {
    mkdir -p "$CONFIG_DIR"
    mkdir -p "$BACKUP_DIR"
    log_info "配置目录已创建"
}

# 备份配置文件
backup_config() {
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_file="$BACKUP_DIR/config_backup_$timestamp.tar.gz"
    
    log_info "备份配置文件..."
    
    # 收集配置文件
    local config_files=()
    
    # 环境文件
    if [ -f "$ENV_FILE" ]; then
        config_files+=("$ENV_FILE")
    fi
    
    # 应用配置文件
    if [ -f "application.yml" ]; then
        config_files+=("application.yml")
    fi
    
    if [ -f "application-prod.yml" ]; then
        config_files+=("application-prod.yml")
    fi
    
    if [ -f "application-performance.yml" ]; then
        config_files+=("application-performance.yml")
    fi
    
    # Docker配置
    if [ -f "docker-compose.yml" ]; then
        config_files+=("docker-compose.yml")
    fi
    
    if [ -f "docker.env" ]; then
        config_files+=("docker.env")
    fi
    
    # 创建备份
    if [ ${#config_files[@]} -gt 0 ]; then
        tar -czf "$backup_file" "${config_files[@]}"
        log_info "配置备份完成: $backup_file"
        
        # 获取备份文件大小
        local file_size=$(du -h "$backup_file" | cut -f1)
        log_info "备份文件大小: $file_size"
    else
        log_warn "没有找到配置文件"
    fi
}

# 恢复配置文件
restore_config() {
    local backup_file="$1"
    
    if [ -z "$backup_file" ]; then
        log_error "请指定备份文件"
        exit 1
    fi
    
    if [ ! -f "$backup_file" ]; then
        log_error "备份文件不存在: $backup_file"
        exit 1
    fi
    
    log_info "恢复配置文件..."
    log_info "备份文件: $backup_file"
    
    # 确认恢复操作
    read -p "确认要恢复配置文件吗？这将覆盖现有配置！(y/N): " confirm
    if [[ ! $confirm =~ ^[Yy]$ ]]; then
        log_info "恢复操作已取消"
        exit 0
    fi
    
    # 先备份当前配置
    backup_config
    
    # 恢复配置
    tar -xzf "$backup_file"
    log_info "配置恢复完成"
}

# 验证配置文件
validate_config() {
    log_info "验证配置文件..."
    
    local errors=0
    
    # 验证环境文件
    if [ -f "$ENV_FILE" ]; then
        log_info "验证环境文件: $ENV_FILE"
        
        # 检查必需的环境变量
        local required_vars=("DB_HOST" "DB_PORT" "DB_NAME" "DB_USERNAME" "DB_PASSWORD")
        
        for var in "${required_vars[@]}"; do
            if ! grep -q "^$var=" "$ENV_FILE"; then
                log_error "缺少必需的环境变量: $var"
                ((errors++))
            fi
        done
        
        # 检查变量值
        while IFS='=' read -r key value; do
            if [ -n "$key" ] && [ -z "$value" ]; then
                log_error "环境变量 $key 没有值"
                ((errors++))
            fi
        done < "$ENV_FILE"
    else
        log_warn "环境文件不存在: $ENV_FILE"
    fi
    
    # 验证YAML配置文件
    if command -v yq &> /dev/null; then
        for yaml_file in application*.yml; do
            if [ -f "$yaml_file" ]; then
                log_info "验证YAML文件: $yaml_file"
                
                if ! yq eval '.' "$yaml_file" > /dev/null 2>&1; then
                    log_error "YAML文件格式错误: $yaml_file"
                    ((errors++))
                fi
            fi
        done
    fi
    
    # 验证Docker Compose文件
    if [ -f "docker-compose.yml" ]; then
        log_info "验证Docker Compose文件"
        
        if ! docker-compose config > /dev/null 2>&1; then
            log_error "Docker Compose文件格式错误"
            ((errors++))
        fi
    fi
    
    if [ $errors -eq 0 ]; then
        log_info "配置文件验证通过"
    else
        log_error "配置文件验证失败，发现 $errors 个错误"
        exit 1
    fi
}

# 生成配置模板
generate_template() {
    local template_type="${1:-env}"
    
    case $template_type in
        "env")
            generate_env_template
            ;;
        "yaml")
            generate_yaml_template
            ;;
        "docker")
            generate_docker_template
            ;;
        *)
            log_error "不支持的模板类型: $template_type"
            exit 1
            ;;
    esac
}

# 生成环境变量模板
generate_env_template() {
    local template_file="$CONFIG_DIR/env.template"
    
    log_info "生成环境变量模板: $template_file"
    
    cat > "$template_file" << 'EOF'
# 用药打卡系统环境配置模板
# 复制此文件为 .env 并修改相应值

# 数据库配置
DB_HOST=medicine-mysql
DB_PORT=3306
DB_NAME=medicine
DB_USERNAME=root
DB_PASSWORD=your_password_here

# Redis配置
REDIS_HOST=medicine-redis
REDIS_PORT=6379
REDIS_PASSWORD=

# 应用配置
SERVER_PORT=48080
SPRING_PROFILES_ACTIVE=prod

# JVM配置
JAVA_OPTS=-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=100

# 性能配置
PERFORMANCE_MONITORING_ENABLED=true
SLOW_QUERY_THRESHOLD=1000
SLOW_API_THRESHOLD=500

# 缓存配置
CACHE_DEFAULT_TTL=3600
CACHE_PATIENT_PROFILE_TTL=1800
CACHE_MEDICATION_PLAN_TTL=900

# 熔断器配置
CIRCUIT_BREAKER_ENABLED=true
CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD=50
CIRCUIT_BREAKER_SLOW_CALL_RATE_THRESHOLD=50

# 日志配置
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_COM_SQLAPK_MEDICINE=DEBUG

# 监控配置
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when-authorized
EOF
    
    log_info "环境变量模板生成完成"
}

# 生成YAML配置模板
generate_yaml_template() {
    local template_file="$CONFIG_DIR/application.template.yml"
    
    log_info "生成YAML配置模板: $template_file"
    
    cat > "$template_file" << 'EOF'
# 用药打卡系统应用配置模板
# 复制此文件为 application.yml 并修改相应值

server:
  port: ${SERVER_PORT:48080}
  servlet:
    context-path: /

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:prod}
  
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:medicine}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
    
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 5000ms
    
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 3000ms

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    root: ${LOG_LEVEL_ROOT:INFO}
    com.sqlapk.medicine: ${LOG_LEVEL_COM_SQLAPK_MEDICINE:DEBUG}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
EOF
    
    log_info "YAML配置模板生成完成"
}

# 生成Docker配置模板
generate_docker_template() {
    local template_file="$CONFIG_DIR/docker-compose.template.yml"
    
    log_info "生成Docker配置模板: $template_file"
    
    cat > "$template_file" << 'EOF'
# 用药打卡系统Docker Compose配置模板
# 复制此文件为 docker-compose.yml 并修改相应值

version: '3.8'

services:
  medicine-mysql:
    image: mysql:8.0
    container_name: medicine-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD:123456}
      MYSQL_DATABASE: ${DB_NAME:medicine}
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    ports:
      - "${DB_PORT:3306}:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
    command: --default-authentication-plugin=mysql_native_password
    restart: unless-stopped

  medicine-redis:
    image: redis:7-alpine
    container_name: medicine-redis
    ports:
      - "${REDIS_PORT:6379}:6379"
    volumes:
      - redis_data:/data
    restart: unless-stopped

  medicine-server:
    image: medicine-server:latest
    container_name: medicine-server
    environment:
      - JAVA_OPTS=${JAVA_OPTS:--Xms2g -Xmx4g -XX:+UseG1GC}
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:prod}
      - DB_HOST=${DB_HOST:medicine-mysql}
      - DB_PORT=${DB_PORT:3306}
      - DB_NAME=${DB_NAME:medicine}
      - DB_USERNAME=${DB_USERNAME:root}
      - DB_PASSWORD=${DB_PASSWORD:123456}
      - REDIS_HOST=${REDIS_HOST:medicine-redis}
      - REDIS_PORT=${REDIS_PORT:6379}
    ports:
      - "${SERVER_PORT:48080}:48080"
    volumes:
      - ./logs:/app/logs
    depends_on:
      - medicine-mysql
      - medicine-redis
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:
EOF
    
    log_info "Docker配置模板生成完成"
}

# 列出配置文件
list_configs() {
    log_info "配置文件列表:"
    
    echo "环境配置文件:"
    for file in .env*; do
        if [ -f "$file" ]; then
            local size=$(du -h "$file" | cut -f1)
            local mtime=$(stat -c %y "$file" | cut -d' ' -f1,2 | cut -d'.' -f1)
            printf "  %-30s %-10s %s\n" "$file" "$size" "$mtime"
        fi
    done
    
    echo ""
    echo "应用配置文件:"
    for file in application*.yml; do
        if [ -f "$file" ]; then
            local size=$(du -h "$file" | cut -f1)
            local mtime=$(stat -c %y "$file" | cut -d' ' -f1,2 | cut -d'.' -f1)
            printf "  %-30s %-10s %s\n" "$file" "$size" "$mtime"
        fi
    done
    
    echo ""
    echo "Docker配置文件:"
    for file in docker-compose.yml docker.env*; do
        if [ -f "$file" ]; then
            local size=$(du -h "$file" | cut -f1)
            local mtime=$(stat -c %y "$file" | cut -d' ' -f1,2 | cut -d'.' -f1)
            printf "  %-30s %-10s %s\n" "$file" "$size" "$mtime"
        fi
    done
}

# 显示帮助信息
show_help() {
    cat << EOF
用药打卡系统配置管理工具

用法: $0 [命令] [选项]

命令:
    backup             备份配置文件
    restore <文件>     从备份文件恢复配置
    validate           验证配置文件
    generate <类型>    生成配置模板
    list               列出配置文件
    help               显示此帮助信息

模板类型:
    env                环境变量模板
    yaml               YAML配置模板
    docker             Docker配置模板

选项:
    -h, --help         显示帮助信息
    -d, --dir          指定配置目录（默认: $CONFIG_DIR）
    -b, --backup-dir   指定备份目录（默认: $BACKUP_DIR）

环境变量:
    CONFIG_DIR         配置目录（默认: $CONFIG_DIR）
    BACKUP_DIR         备份目录（默认: $BACKUP_DIR）

示例:
    $0 backup                           # 备份配置
    $0 restore config_backup_20240106_120000.tar.gz  # 恢复配置
    $0 validate                         # 验证配置
    $0 generate env                      # 生成环境变量模板
    $0 generate yaml                     # 生成YAML配置模板
    $0 generate docker                   # 生成Docker配置模板
    $0 list                              # 列出配置文件

EOF
}

# 主函数
main() {
    local command="${1:-help}"
    
    case $command in
        "backup")
            check_dependencies
            create_config_dirs
            backup_config
            ;;
        "restore")
            check_dependencies
            restore_config "$2"
            ;;
        "validate")
            check_dependencies
            validate_config
            ;;
        "generate")
            create_config_dirs
            generate_template "$2"
            ;;
        "list")
            list_configs
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
