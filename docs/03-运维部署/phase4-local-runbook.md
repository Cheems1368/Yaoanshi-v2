# Phase 4 本地联调与回滚说明

## 适用范围

- `backend-v2` 本地 `local` profile
- 患者端 `frontend/huanzhe`
- 药师端 `frontend/pharmacist`
- 医生端 `frontend/yisheng/web-admin`
- legacy 登录服务与菜单来源 `http://127.0.0.1:48080`

## 端口约定

- 患者端: `3001`
- 医生端: `3002`
- 药师端: `3003`
- `backend-v2`: `8080`
- legacy backend: `48080`

## 前置条件

- JDK 17 已安装，默认路径 `D:\Java\Java 17`
- 本机 MySQL 已准备好 `backend-v2` 使用的库
- legacy Docker 栈可通过 `backend/medicinal-his-delivery/docker-compose.yml` 启动
- 当前 shell 已注入以下环境变量

```powershell
$env:MEDICINE_DB_PASSWORD="***"
$env:MEDICINE_LEGACY_DB_PASSWORD="***"
```

## 串行联调

### 推荐一键脚本

默认执行构建、启动、后端 smoke、401 刷新回归、药师患者冒烟、实时通知冒烟:

```powershell
powershell -ExecutionPolicy Bypass -File D:\yaoanshi\ops\scripts\phase4-local-run.ps1 -SkipExpiryE2E
```

需要同时验证真实过期场景时，用短 TTL 重启 `backend-v2`:

```powershell
powershell -ExecutionPolicy Bypass -File D:\yaoanshi\ops\scripts\phase4-local-run.ps1 -JwtAccessTokenSeconds 2
```

说明: 短 TTL 模式下脚本会跳过实时通知 E2E，只保留真实过期验证，避免 2 秒 access token 干扰通知协作场景。

### 手动顺序

1. 启动 legacy 依赖

```powershell
docker compose -f D:\yaoanshi\backend\medicinal-his-delivery\docker-compose.yml up -d mysql redis server
```

2. 启动 `backend-v2`

```powershell
$env:MEDICINE_DB_PASSWORD="***"
$env:MEDICINE_LEGACY_DB_PASSWORD="***"
$env:MEDICINE_REDIS_PORT="16379"
D:\Java\Java 17\bin\java.exe -jar D:\yaoanshi\backend-v2\target\backend-v2-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

3. 分别启动三个前端 dev server

```powershell
cd D:\yaoanshi\frontend\huanzhe; npm run dev -- --host 127.0.0.1 --port 3001
cd D:\yaoanshi\frontend\yisheng\web-admin; npm run dev -- --host 127.0.0.1 --port 3002
cd D:\yaoanshi\frontend\pharmacist; npm run dev -- --host 127.0.0.1 --port 3003
```

4. 运行验证

```powershell
powershell -ExecutionPolicy Bypass -File D:\yaoanshi\backend-v2\scripts\smoke-core-flows-local.ps1
node D:\yaoanshi\frontend\huanzhe\scripts\e2e-auth-refresh.mjs
node D:\yaoanshi\frontend\huanzhe\scripts\e2e-pharmacist-patient.mjs
node D:\yaoanshi\frontend\huanzhe\scripts\e2e-admin-notifications.mjs
```

## 回滚说明

### 页面或代理异常

- 保留 legacy `48080` 不动
- 先停止三个 Vite 进程，再重启对应前端
- 如果问题来自 `backend-v2` 新接口或 WebSocket，直接停掉 `8080` 上的 `backend-v2`

### `backend-v2` 回滚

1. 停止当前 `8080` Java 进程
2. 重新启动上一个已验证的 `backend-v2` 包
3. 保持 legacy Docker 栈与前端端口不变

### 数据回滚边界

- 本轮不修改 legacy `backend`
- `notification_log` 只新增推送状态回写，不影响 legacy 登录与菜单
- 对象存储仍为 stub，附件链路回滚只需回退 `backend-v2` 版本

## 已知限制

- 对象存储仍为 stub，不做真实文件 PUT
- AI `base-url` / `api-key` 仍依赖环境注入
- 全栈本地联调仍依赖混合运行时
