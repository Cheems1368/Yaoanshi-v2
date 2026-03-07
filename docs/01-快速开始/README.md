# 快速开始

本目录包含项目介绍、快速开始指南、API契约和项目状态信息。

## 📋 文档列表

| 文档 | 描述 |
|------|------|
| [项目介绍.md](项目介绍.md) | 项目概述、架构设计、核心功能 |
| [API契约.md](API契约.md) | API接口规范和契约 |
| [项目状态.md](项目状态.md) | 开发进度和完成情况 |

## 🚀 快速开始

### 1. 环境准备

```bash
# 检查Java版本
java -version  # 需要 Java 11+

# 检查Docker
docker --version
docker-compose --version
```

### 2. 启动服务

```bash
# 克隆项目（如果还没有）
git clone <repository-url>
cd yaoanshi

# 启动所有服务
docker compose up -d

# 等待服务启动（约30-60秒）
docker compose ps
```

### 3. 验证部署

```bash
# 健康检查
curl http://localhost:48080/actuator/health

# 访问API文档
open http://localhost:48080/swagger-ui/index.html
```

### 4. 登录系统

```bash
# 使用默认账号登录
curl -X POST "http://localhost:48080/admin-api/system/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"000000"}'
```

## 📖 下一步

- 查看 [项目介绍](项目介绍.md) 了解系统架构
- 参考 [API契约](API契约.md) 进行接口开发
- 查看 [项目状态](项目状态.md) 了解开发进度

## 🔗 相关文档

- [开发指南](../02-开发指南/)
- [运维部署](../03-运维部署/)
- [故障排查](../05-故障排查/)

