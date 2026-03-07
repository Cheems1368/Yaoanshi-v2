# Docker 使用指南

## 🚀 快速开始

### 1. 选择适合的Docker版本

| 版本 | 大小 | 特点 | 适用场景 |
|------|------|------|----------|
| **alpine-slim** | 最小 | 可选字体/健康检查 | 生产环境推荐 |
| **distroless** | 小 | 最高安全性 | 安全要求极高 |
| **alpine** | 中等 | 完整功能 | 开发环境 |
| **minimal** | 中等 | 平衡版本 | 通用场景 |
| **optimized** | 较大 | 功能完整 | 历史兼容 |

### 2. 构建镜像

```bash
# 方式1：使用构建脚本（推荐）
powershell -ExecutionPolicy Bypass -File docker-build-optimized.ps1

# 方式2：手动构建
docker build -f Dockerfile.optimized-alpine-slim -t medicine-server:latest .
```

### 3. 运行容器

#### 生产环境（推荐）
```bash
# 使用 alpine-slim 版本
docker run -d \
  --name medicine-server \
  -p 8080:8080 \
  --read-only \
  --tmpfs /tmp \
  --tmpfs /app/logs \
  --memory=512m \
  --cpus=0.5 \
  --restart=unless-stopped \
  medicine-server:alpine-slim
```

#### 开发环境
```bash
# 使用 docker-compose
docker-compose -f docker-compose.dev.yml up -d

# 或直接运行
docker run -d \
  --name medicine-server-dev \
  -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  medicine-server:alpine
```

## 🔧 详细配置

### 环境变量配置

```bash
# 数据库配置
-e DB_HOST=localhost
-e DB_PORT=3306
-e DB_NAME=medicine
-e DB_USER=root
-e DB_PASSWORD=your_password

# JVM 配置
-e JAVA_OPTS="-Xms256m -Xmx512m"

# 应用配置
-e SERVER_PORT=8080
-e SPRING_PROFILES_ACTIVE=prod
```

### 资源限制

```bash
# 内存限制
--memory=512m

# CPU限制
--cpus=0.5

# 只读文件系统（生产环境）
--read-only

# 临时文件系统
--tmpfs /tmp
--tmpfs /app/logs
```

## 📊 监控与健康检查

### 健康检查
```bash
# 检查容器状态
docker ps

# 查看健康状态
docker inspect medicine-server | grep -A 5 Health

# 手动健康检查
curl http://localhost:8080/actuator/health
```

### 性能监控
```bash
# 运行性能测试
powershell -ExecutionPolicy Bypass -File docker-performance-test.ps1

# 查看资源使用
docker stats medicine-server
```

## 🔒 安全扫描

### 漏洞扫描
```bash
# 扫描镜像
powershell -ExecutionPolicy Bypass -File docker-security-scan-trivy.ps1 \
  -TargetType image -TargetName medicine-server:latest \
  -Severity CRITICAL,HIGH -Format table

# 扫描运行容器
powershell -ExecutionPolicy Bypass -File docker-security-scan-trivy.ps1 \
  -TargetType container -TargetName medicine-server \
  -Severity CRITICAL,HIGH -Format json
```

### 安全检查
```bash
# 运行安全检查
powershell -ExecutionPolicy Bypass -File docker-security-check-simple.ps1

# 验收检查
powershell -ExecutionPolicy Bypass -File docker-acceptance-check.ps1
```

## 🐳 Docker Compose 使用

### 开发环境
```bash
# 启动开发环境
docker-compose -f docker-compose.dev.yml up -d

# 查看日志
docker-compose -f docker-compose.dev.yml logs -f

# 停止服务
docker-compose -f docker-compose.dev.yml down
```

### 生产环境
```bash
# 启动生产环境
docker-compose -f docker-compose.prod.yml up -d

# 查看状态
docker-compose -f docker-compose.prod.yml ps

# 停止服务
docker-compose -f docker-compose.prod.yml down
```

## 🔍 故障排查

### 查看日志
```bash
# 查看容器日志
docker logs medicine-server

# 实时查看日志
docker logs -f medicine-server

# 查看最近100行日志
docker logs --tail 100 medicine-server
```

### 进入容器调试
```bash
# 进入容器
docker exec -it medicine-server sh

# 查看进程
docker exec medicine-server ps aux

# 查看网络
docker exec medicine-server netstat -tlnp
```

### 常见问题

1. **容器启动失败**
   - 检查端口是否被占用：`netstat -tlnp | grep 8080`
   - 查看容器日志：`docker logs medicine-server`

2. **内存不足**
   - 增加内存限制：`--memory=1g`
   - 调整JVM参数：`-e JAVA_OPTS="-Xms512m -Xmx1g"`

3. **健康检查失败**
   - 检查应用是否正常启动
   - 确认健康检查端点可访问

## 📈 性能优化建议

1. **镜像选择**：生产环境使用 `alpine-slim`
2. **资源限制**：合理设置内存和CPU限制
3. **只读文件系统**：提高安全性
4. **健康检查**：监控应用状态
5. **日志管理**：定期清理日志文件

## 🆘 获取帮助

- 查看详细文档：`README.md`
- 快速开始：`QUICK_START.md`
- 性能报告：`PERFORMANCE_REPORT.md`
- 安全报告：`docker-image-security-report.md`


