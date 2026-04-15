# 本地 Docker 环境说明

## backend-v2 环境文件约定

- `backend-v2/.env.local` 仅供 `backend-v2/scripts/run-local-with-env.ps1` 这类本机 PowerShell 启动脚本使用。
- `ops/.env.backend-v2.local` 仅供 `ops/docker-compose.yml` 和 Portainer 本地栈使用。
- `ops/.env.backend-v2.local` 是可选文件。缺省情况下，Docker 栈仍可启动；AI/OCR 默认值继续使用 `backend-v2/src/main/resources/application.yml` 中的应用配置。
- 只有在需要启用或覆盖 AI/OCR secret 时，才需要创建 `ops/.env.backend-v2.local`。
- 不要把宿主机地址（例如 `127.0.0.1:13306`、`127.0.0.1:9000`）写进 `ops/.env.backend-v2.local`，容器内地址仍以 `ops/docker-compose.yml` 中的 `mysql`、`redis`、`minio` 为准。

## 启动前准备

1. 可选：复制 `ops/.env.backend-v2.local.example` 为 `ops/.env.backend-v2.local`
2. 如需 AI/OCR 增强能力，在 `ops/.env.backend-v2.local` 中填写 `MEDICINE_AI_API_KEY`，并按需填写 `MEDICINE_OCR_API_KEY`
3. 保持数据库、Redis、MinIO 地址配置继续由 `ops/docker-compose.yml` 提供

## 最小恢复命令

在 `D:/yaoanshi/ops` 目录执行：

```powershell
docker compose up -d backend-v2 admin
```

## 验证

```powershell
docker compose config
docker logs --since 1m yaoshian-backend-v2
docker logs --since 1m yaoshian-pharmacist-admin
curl http://localhost:8081/health
```
