# 医生端登录 / 资料页回登录问题 — 改进修复方案

> 本文档基于 Codex 原始方案进行审查和改进，纠正了根因定位偏差、简化了修复策略、补充了遗漏项。

## 1. 根因分析（对 Codex 原方案的纠正）

### Codex 原方案描述

> "医生端当前用 legacy 48080 做登录/刷新/用户信息/通知 ticket，但工单列表走 backend-v2"

### 实际根因（更精确）

问题的核心不在于 auth API 模块本身的 URL 写法，而是 **`.env.development` 文件将 `VITE_API_BASE_URL` 设为 `http://localhost:48080`**，导致 axios 实例的 `baseURL` 被硬编码为 legacy 地址。所有请求（包括已在 vite proxy 中正确配置路由到 backend-v2 的 auth、notifications、help-orders）都直接发往 legacy 48080，完全绕过了 Vite dev proxy。

**故障链条：**

```
.env.development: VITE_API_BASE_URL=http://localhost:48080
        ↓
request.js: axios.create({ baseURL: 'http://localhost:48080' })
        ↓
所有请求（含 auth、notifications、help-orders）直接打到 48080
        ↓
Vite proxy 配置无效（绝对 URL 绕过 proxy）
        ↓
notification socket-ticket → 48080 无此端点 → 404/错误 → 乱码 toast
help-orders → 48080 无此端点或 token 不兼容 → 401
        ↓
401 触发 refresh → refresh 也打 48080 → 拿到 legacy token
        ↓
retry 仍然 401 → logoutAction(silent) → 清空登录态
        ↓
用户被踢回 /login
```

**关键发现：Vite proxy 配置本身是正确的。** `vite.config.js` 已正确配置：

| Proxy 路径 | 目标 |
|---|---|
| `/admin-api/system/auth` | backend-v2 (8080) |
| `/admin-api/notifications` | backend-v2 (8080) |
| `/ws/admin-notifications` | backend-v2 (8080) |
| `/admin-api/help-orders` | backend-v2 (8080) |
| `/admin-api` (fallback) | legacy (48080) |

但由于 axios `baseURL` 是绝对 URL `http://localhost:48080`，所有请求直接跳过了 Vite proxy。

### 药师端为何部分正常

药师端 `auth.js` 中 `createNotificationSocketTicket()` 显式覆盖了 `baseURL: backendV2BaseURL`（值为空字符串），空字符串使请求走 same-origin → Vite proxy → backend-v2。但药师端 login/refresh/getUserInfo 同样直接打 48080，只是因为 legacy 48080 恰好支持这些端点所以没有报错。

## 2. 改进修复策略

### Codex 原方案的问题

| 问题 | 说明 |
|---|---|
| 根因定位偏差 | 将问题归因于"auth API 模块 URL 写法"，实际是 .env 文件和 axios baseURL 策略问题 |
| 修复过于复杂 | 要求逐个 auth 函数添加显式 baseURL 覆盖，涉及 5+ 处修改 |
| 遗漏 .env 文件 | 完全没有提及 `.env.development` 是根因 |
| 药师端同步风险 | 提议"镜像同步"到药师端但未评估回归风险 |
| 生产一致性 | 未考虑生产部署中 nginx 反代场景下 baseURL 应为空 |
| 测试计划粗糙 | "手工烟测"缺乏可复现步骤，未利用已有自动化脚本 |
| skipErrorToast 缺实现 | 仅概念描述，无具体代码方案 |

### 改进修复方案（3 步替代 Codex 的 4 大块）

#### 步骤 1：修正 .env.development（根因修复，1 个文件）

**文件：** `frontend/yisheng/web-admin/.env.development`

```diff
 VITE_PORT=3002
-VITE_API_BASE_URL=http://localhost:48080
+VITE_API_BASE_URL=
 VITE_BACKEND_V2_API_BASE_URL=
```

**同步修正：** `frontend/yisheng/web-admin/.env.example`

```diff
 VITE_PORT=3002
-VITE_API_BASE_URL=http://localhost:48080
+VITE_API_BASE_URL=
```

**效果：** axios `baseURL` 变为空字符串 → 所有请求走 same-origin → Vite proxy 正确路由 → auth/notifications/help-orders 打 backend-v2，其余 fallback 打 legacy。

**为什么 legacy fallback 仍然有效：** `vite.config.js` 中 `legacyApiTarget` 的默认值是 `'http://localhost:48080'`（`env.VITE_API_BASE_URL || 'http://localhost:48080'`），proxy 的 server-side target 不受 `.env` 中 `VITE_API_BASE_URL` 清空的影响。

> 注意：`VITE_API_BASE_URL` 在 vite.config.js 中还参与 `legacyApiTarget` 计算。清空 .env 后，`legacyApiTarget` 会使用默认值 `http://localhost:48080`，proxy fallback 依旧正常。

#### 步骤 2：修复乱码 + 对齐错误处理（3 个文件）

**文件 2a：** `frontend/yisheng/web-admin/src/utils/request.js`

改动要点：
- 引入 `extractMessage()` 函数，与药师端 `request.js` 对齐
- 将所有乱码中文替换为正确 UTF-8 文本
- 新增 `skipErrorToast` 请求选项支持

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { API_SUCCESS_CODES } from '@/utils/constants'

const DEFAULT_ERROR_MESSAGE = '请求失败'

const extractMessage = (payload, fallback = DEFAULT_ERROR_MESSAGE) => {
  if (!payload || typeof payload !== 'object') {
    return fallback
  }
  const candidates = [payload.msg, payload.message, payload.error, payload.detail]
  const firstReadable = candidates.find((item) => typeof item === 'string' && item.trim())
  return firstReadable || fallback
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

service.interceptors.request.use(
  async (config) => {
    const userStore = useUserStore()
    let accessToken = userStore.token
    config.headers = config.headers || {}

    if (!accessToken && !config.skipAuthHeader && !config.skipAuthRefresh) {
      accessToken = await userStore.refreshAccessToken()
    }

    if (accessToken && !config.skipAuthHeader) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    config.headers['tenant-id'] = '1'
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const payload = response.data || {}
    const code = payload.code
    const message = extractMessage(payload)

    if (API_SUCCESS_CODES.includes(code)) {
      return payload
    }

    if (!response.config?.skipErrorToast) {
      ElMessage.error(message)
    }
    return Promise.reject(new Error(message))
  },
  async (error) => {
    const status = error.response?.status
    const payload = error.response?.data || {}
    const message = extractMessage(payload)
    const skipToast = error.config?.skipErrorToast

    if (status === 401) {
      const originalRequest = error.config || {}
      const userStore = useUserStore()

      if (!originalRequest._retry && !originalRequest.skipAuthRefresh) {
        originalRequest._retry = true
        const nextAccessToken = await userStore.refreshAccessToken()

        if (nextAccessToken) {
          originalRequest.headers = {
            ...(originalRequest.headers || {}),
            Authorization: `Bearer ${nextAccessToken}`
          }
          return service(originalRequest)
        }
      }

      await userStore.logoutAction({ silent: true })
      if (!skipToast) {
        ElMessage.error('登录已过期，请重新登录')
      }
      return Promise.reject(error)
    }

    if (!skipToast) {
      if (status === 403) {
        ElMessage.error('没有权限访问该资源')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status >= 500) {
        ElMessage.error(message === DEFAULT_ERROR_MESSAGE ? '服务端发生错误' : message)
      } else {
        ElMessage.error(message)
      }
    }

    return Promise.reject(error)
  }
)

export default service
```

**文件 2b：** `frontend/yisheng/web-admin/src/store/user.js`

改动要点：仅替换乱码字符串为正确 UTF-8 中文，不改业务逻辑。

| 行号 | 乱码 | 正确文本 |
|------|------|----------|
| 20 | `'鍖荤敓'` | `'医生'` |
| 21 | `'鎮ｈ€?'` | `'患者'` |
| 52 | `'鍖荤敓'` / `'鍖诲笀'` | `'医生'` / `'医师'` |
| 131 | `'鐧诲綍鍝嶅簲缂哄皯璁块棶浠ょ墝'` | `'登录响应缺少访问令牌'` |
| 161 | `'鐧诲綍澶辫触'` | `'登录失败'` |
| 167 | 同 131 | `'登录响应缺少访问令牌'` |
| 174 | `'鐧诲綍鎴愬姛'` | `'登录成功'` |
| 177 | `'鐧诲綍閿欒'` | `'登录错误'` |
| 191 | `'鐧诲嚭璇锋眰澶辫触'` | `'登出请求失败'` |
| 195 | `'宸查€€鍑虹櫥褰?'` | `'已退出登录'` |
| 211 | `'鑾峰彇鐢ㄦ埛淇℃伅澶辫触'` | `'获取用户信息失败'` |
| 216 | `'鏈幏鍙栧埌鐢ㄦ埛淇℃伅'` | `'未获取到用户信息'` |
| 229 | `'鑾峰彇鐢ㄦ埛淇℃伅澶辫触'` | `'获取用户信息失败'` |

**文件 2c：** `frontend/yisheng/web-admin/src/utils/adminNotifications.js`

改动要点：
1. 替换所有乱码中文为正确 UTF-8 文本（对齐药师端文案风格）
2. 给 `createNotificationSocketTicket()` 调用添加 `skipErrorToast: true`，失败时仅 console.error + 自动重连，不弹 toast

```javascript
// notificationTitle 和 notificationMessage 中的乱码替换：
const map = {
  HELP_ORDER_SUBMITTED: '新工单到达',     // 原: '鏂板伐鍗曞埌杈?'
  HELP_ORDER_ESCALATED: '工单已升级',     // 原: '宸ュ崟宸插崌绾?'
  HELP_ORDER_REPLY: '工单有新回复',       // 原: '宸ュ崟鏈夋柊鍥炲'
  HELP_ORDER_CLOSED: '工单已关闭'         // 原: '宸ュ崟宸插叧闂?'
}
// 默认值：'新通知'（原: '鏂伴€氱煡'）
```

```javascript
// notificationMessage 函数修正（对齐药师端文案风格）：
HELP_ORDER_ESCALATED → '有工单已升级到医生队列，请尽快处理。'
HELP_ORDER_REPLY     → '工单链路出现新的回复。'
HELP_ORDER_CLOSED    → '有工单已经关闭，可刷新查看结果。'
default              → '收到新的业务通知。'
```

#### 步骤 3：为 notification ticket 请求添加静默错误（1 个文件）

**文件：** `frontend/yisheng/web-admin/src/api/auth.js`

在 `createNotificationSocketTicket` 中添加 `skipErrorToast: true`：

```javascript
export function createNotificationSocketTicket() {
  return request({
    url: '/admin-api/notifications/socket-ticket',
    method: 'post',
    skipErrorToast: true
  })
}
```

这样通知 ticket 请求失败时，response 拦截器不会弹出 ElMessage.error toast，仅由 adminNotifications.js 中的 `catch` 块做 console.error + scheduleReconnect。

## 3. 药师端影响评估

### Codex 原方案："同步镜像到药师端"

**改进建议：本轮不动药师端。**

原因：
1. 药师端当前登录/刷新/用户信息请求走 legacy 48080 是**有意设计**，且已稳定运行
2. 药师端 `createNotificationSocketTicket` 已通过 `baseURL: backendV2BaseURL`（空字符串）正确走 Vite proxy
3. 强行对齐可能引入回归，收益不明确
4. 如果后续需要药师端也统一走 same-origin proxy，应作为**独立任务**单独处理和测试

**药师端唯一建议的可选改进：**
- 在 `createNotificationSocketTicket()` 中也添加 `skipErrorToast: true`，与医生端一致
- 这是低风险改动，可以随本轮一起做

## 4. 变更文件清单（精简后）

| 文件 | 改动类型 | 说明 |
|------|----------|------|
| `frontend/yisheng/web-admin/.env.development` | 修改 | 清空 VITE_API_BASE_URL（根因修复） |
| `frontend/yisheng/web-admin/.env.example` | 修改 | 同步清空 |
| `frontend/yisheng/web-admin/src/utils/request.js` | 重写 | 修复乱码 + 引入 extractMessage + 添加 skipErrorToast |
| `frontend/yisheng/web-admin/src/store/user.js` | 修改 | 修复 15 处乱码字符串 |
| `frontend/yisheng/web-admin/src/utils/adminNotifications.js` | 修改 | 修复 9 处乱码字符串 + 对齐文案 |
| `frontend/yisheng/web-admin/src/api/auth.js` | 修改 | notification ticket 添加 skipErrorToast |
| （可选）`frontend/pharmacist/src/api/auth.js` | 修改 | notification ticket 添加 skipErrorToast |

总计 **6 个文件**（含 1 个可选），比 Codex 方案的涉及面更小、更精准。

## 5. 测试计划（改进版）

### 5.1 构建验证（自动化）

```bash
cd frontend/yisheng/web-admin && npm run build
cd frontend/pharmacist && npm run build
```

### 5.2 后端 smoke 验证（自动化）

利用已有的 `smoke-admin-auth-local.ps1`，验证 backend-v2 admin auth 链路完整性：

```powershell
powershell -ExecutionPolicy Bypass -File D:/yaoanshi/backend-v2/scripts/smoke-admin-auth-local.ps1
```

预期结果：login → get-permission-info → refresh-token → logout → post-logout refresh 全部通过。

### 5.3 医生端浏览器烟测（手工，附具体步骤）

**前置条件：**
- backend-v2 运行在 http://127.0.0.1:8080
- legacy stack 运行在 48080/16379/13306
- 医生端 dev server 运行在 http://localhost:3002

**测试用例：**

| # | 步骤 | 预期结果 |
|---|------|----------|
| 1 | 打开 http://localhost:3002/login | 显示医生登录页，无乱码 |
| 2 | 输入 doctor / 123456 登录 | 显示"登录成功"（正确中文），跳转到 /workorders |
| 3 | 打开 DevTools → Network | login、get-permission-info 走 localhost:3002（proxy 到 backend-v2），无 48080 直连 |
| 4 | 观察 Network 中 socket-ticket | 请求走 localhost:3002/admin-api/notifications/socket-ticket（proxy 到 backend-v2） |
| 5 | 如果 socket-ticket 失败 | 无 toast 弹出（仅 console 告警），页面不受影响 |
| 6 | 观察 help-orders 请求 | 走 localhost:3002/admin-api/help-orders（proxy 到 backend-v2） |
| 7 | 点击侧边栏"个人资料" | 导航到 /profile，页面正常渲染，不被踢回 /login |
| 8 | 在 /profile 页面刷新浏览器 | 通过 refreshToken 恢复会话，仍在 /profile |
| 9 | 在 /profile 页面等待 30 秒 | 无连续错误 toast（notification 重连仅 console 输出） |
| 10 | 点击退出登录 | 显示"已退出登录"（正确中文），跳转到 /login |

### 5.4 回归检查

| 检查项 | 说明 |
|--------|------|
| 药师端登录/刷新/通知 | 不应因共用层调整而回退（本轮不改药师端核心） |
| 医生端工单列表 | 应正常加载（如后端有数据） |
| 医生端通知连接 | 失败时仅 console 记录，不弹连续错误提示 |

## 6. 风险与假设

### 假设

1. backend-v2 的 `/admin-api/system/auth/*` 已是专业端标准认证入口（由 `AdminAuthController` 验证确认）
2. backend-v2 通过 legacy-auth datasource 读取现有专业账号，用户无需迁移
3. 本次不修改后端 Java 代码
4. 如果统一到 backend-v2 后 doctor 账号仍有 401，需单独排查后端 admin auth 对 doctor role 的支持

### 风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 清空 VITE_API_BASE_URL 后其他 legacy 接口断裂 | 低 — vite proxy fallback `/admin-api` → 48080 仍有效 | 医生端目前仅用 auth + help-orders + notifications，全部已在 proxy 中指向 backend-v2 |
| doctor 角色在 backend-v2 admin auth 未测试 | 中 — smoke 脚本默认用 pharmacist 角色 | 增加 `-Role doctor` 参数跑一次 smoke |
| .env.local 覆盖 | 低 — 开发者可能有本地 .env.local 覆盖 VITE_API_BASE_URL | 在 PR 描述中注明清理本地 .env.local |

## 7. 与 Codex 原方案的对比总结

| 维度 | Codex 原方案 | 改进方案 |
|------|-------------|----------|
| 根因定位 | "混用两套后端链路" | `.env.development` 的 `VITE_API_BASE_URL` 导致 axios 绕过 Vite proxy |
| 核心修复 | 5+ 个 auth 函数逐个添加 baseURL 覆盖 | 1 个 .env 文件清空即可（proxy 本已正确配置） |
| 变更文件数 | 未明确列出，涉及 auth + request + store + notifications + 药师端镜像 | 6 个文件（含 1 可选），逐一列出 |
| 药师端处理 | "同步镜像" | 本轮不动药师端核心，仅可选添加 skipErrorToast |
| skipErrorToast | 仅概念描述 | 完整实现方案：request.js 拦截器 + auth.js 调用处 |
| 生产一致性 | 未提及 | 明确：dev/prod 均走 same-origin，nginx 反代场景天然兼容 |
| 测试计划 | "手工烟测" 4 条 | 10 条具体步骤 + 已有 smoke 脚本复用 + doctor 角色验证 |
