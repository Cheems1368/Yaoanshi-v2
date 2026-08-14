# 本地 Docker 环境说明

## backend-v2 环境文件约定

- `backend-v2/.env.local` 仅供 `backend-v2/scripts/run-local-with-env.ps1` 这类 host-run 本机 PowerShell 启动脚本使用。
- `ops/.env.backend-v2.local` 仅供 `ops/docker-compose.yml` 和 Portainer 本地栈使用；Docker 不会自动读取 `backend-v2/.env.local`。
- `ops/.env.backend-v2.local` 是可选文件。缺省情况下，Docker 栈仍可启动；AI/OCR 默认值继续使用 `backend-v2/src/main/resources/application.yml` 中的应用配置。
- 只有在需要启用或覆盖 AI、OCR 或 XFYUN secret 时，才需要创建 `ops/.env.backend-v2.local`。
- 不要把宿主机地址（例如 `127.0.0.1:13306`、`127.0.0.1:9000`）写进 `ops/.env.backend-v2.local`，容器内地址仍以 `ops/docker-compose.yml` 中的 `mysql`、`redis`、`minio` 为准。

## 启动前准备

1. 可选：复制 `ops/.env.backend-v2.local.example` 为 `ops/.env.backend-v2.local`
2. 如需 AI/OCR 增强能力，在 `ops/.env.backend-v2.local` 中填写 `MEDICINE_AI_API_KEY`，并按需填写 `MEDICINE_OCR_API_KEY`
3. host-run 语音转写配置写入 `backend-v2/.env.local`；Docker 语音转写配置单独写入 `ops/.env.backend-v2.local`。这些值不要提交到 Git，也不要假设两份文件会自动同步
4. 保持数据库、Redis、MinIO 地址配置继续由 `ops/docker-compose.yml` 提供

## 讯飞语音转写配置

host-run 在 `D:/yaoanshi/backend-v2/.env.local` 中补充；Docker 在 `D:/yaoanshi/ops/.env.backend-v2.local` 中补充相同的七个键：

```dotenv
MEDICINE_XFYUN_APP_ID=replace-with-your-xfyun-app-id
MEDICINE_XFYUN_API_KEY=replace-with-your-xfyun-api-key
MEDICINE_XFYUN_API_SECRET=replace-with-your-xfyun-api-secret
MEDICINE_XFYUN_BASE_URL=wss://iat-api.xfyun.cn/v2/iat
MEDICINE_XFYUN_PRIMARY_DOMAIN=xfime-mianqie
MEDICINE_XFYUN_FALLBACK_DOMAIN=iat
MEDICINE_XFYUN_TIMEOUT_MS=30000
```

host-run 重新启动本机后端：

```powershell
powershell -ExecutionPolicy Bypass -File D:\yaoanshi\backend-v2\scripts\run-local-with-env.ps1
```

Docker 后端必须重新构建后再启动：

```powershell
docker compose -f D:\yaoanshi\ops\docker-compose.yml up -d --build mysql redis minio backend-v2
```

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

## 患者端真机 Live Reload 演示

前置条件：

- Android 手机已开启 USB 调试，并通过数据线连接电脑。
- `ANDROID_HOME` 或 `ANDROID_SDK_ROOT` 指向本机 Android SDK，例如 `D:\ANDROIDSDK`。
- JDK 21 可用；脚本会优先读取 `-JavaHome`、`JAVA21_HOME`、`JDK21_HOME`，也会尝试本机 `D:\Java\java21`。
- 本地测试账号 `pharmacist` / `doctor` 已通过 `ops/scripts/ensure-local-professional-test-accounts.ps1` 准备。

启动演示栈：

```powershell
powershell -ExecutionPolicy Bypass -File D:\yaoanshi\ops\scripts\start-mobile-demo-stack.ps1
```

`-SkipDocker` 只适用于已经明确确认后端容器来自最新源码的情况。只要 `backend-v2` 源码、依赖或 Dockerfile 发生变化，就必须去掉 `-SkipDocker` 并重新构建；不得复用旧镜像作为验收结果。

按现场指定的患者手机号和院内 ID 重置演示数据：

```powershell
powershell -ExecutionPolicy Bypass -File D:\yaoanshi\ops\scripts\reset-mobile-demo-data.ps1 `
  -Mobile 13900000001 `
  -HospitalPatientId DEMO-STROKE-001 `
  -Name 张三
```

可选：先构建患者端 Android debug 包：

```powershell
cd D:\yaoanshi\frontend\huanzhe
npm run android:build
```

运行患者端到手机：

```powershell
cd D:\yaoanshi\frontend\huanzhe
npm run android:live
```

验收路径：

1. 手机打开“药按时”。
2. 使用固定演示验证码登录。
3. 绑定现场指定患者。
4. 药师端创建并激活用药计划。
5. 手机首页和“用药”页能看到计划名、今日任务、药品、剂量和时间。
6. 处方页使用“拍照上传”或“相册/电子处方”选择图片，完成预览和登记，列表出现记录。
