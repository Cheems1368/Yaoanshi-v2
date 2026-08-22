# 测试与质量门禁

本项目采用分层测试流程。Pull Request 只强制执行确定性高、无需真实密钥或外部服务的检查；依赖完整运行环境的集成与端到端测试在相关功能合并前和发布前执行。

## 1. Pull Request 必需检查

GitHub Actions 工作流 `CI` 并行执行：

| 检查 | 内容 | 通过标准 |
| --- | --- | --- |
| Backend tests and coverage | Spring Boot 编译、JUnit 测试、JaCoCo | 测试全部通过，行覆盖率不低于 60% |
| Patient app quality | ESLint、Vitest、类型检查、生产构建 | 全部命令退出码为 0 |
| Patient Android APK | Temurin 21、Web 构建、Capacitor 同步、生成配置漂移检查、`assembleDebug` | APK 构建成功且同步后无 tracked-file 漂移 |
| Pharmacist web build | 锁定依赖安装、生产构建 | 构建成功 |
| Doctor web build | 锁定依赖安装、生产构建 | 构建成功 |
| Required Quality Gate | 汇总以上五项 | 五项均为 success |

分支保护只绑定稳定上下文 `CI / Required Quality Gate`，避免内部作业改名或拆分时意外锁死合并。

## 2. 本地快速验证

在仓库根目录运行后端完整门禁：

```powershell
mvn -B -ntp clean verify -f backend-v2/pom.xml
```

患者端：

```powershell
Set-Location frontend/huanzhe
npm ci
npm run lint
npm run test:unit:ci
npm run build
```

患者端 Android APK（JDK 21 只用于 Android，默认后端 JDK 保持 17）：

```powershell
$env:JAVA21_HOME = [Environment]::GetEnvironmentVariable('JAVA21_HOME', 'User')
& "$env:JAVA21_HOME\bin\java.exe" -version
Set-Location frontend/huanzhe
npm run android:build
Test-Path android/app/build/outputs/apk/debug/app-debug.apk
```

`npm run android:build` 必须使用 Eclipse Temurin `21.0.12+8`，并生成非空的 `app-debug.apk`。执行 `npx cap sync android` 后如出现 tracked-file 变更，必须审查并提交真实配置更新或恢复纯行尾噪声，禁止在 CI 中跳过漂移检查。

药师端：

```powershell
Set-Location frontend/pharmacist
npm ci
npm run build
```

医生端：

```powershell
Set-Location frontend/yisheng/web-admin
npm ci
npm run build
```

## 3. 变更与测试对应关系

| 变更类型 | 最低测试要求 |
| --- | --- |
| 纯领域或服务逻辑 | 单元测试覆盖成功、边界和业务失败路径 |
| Controller/API/DTO | Controller 或契约测试；验证 `CommonResult<T>`、错误码、鉴权和字段兼容性 |
| 数据库迁移 | 在空库和已有数据副本上验证迁移；确认回滚或前向修复方案 |
| 患者端状态、路由、请求层 | Vitest 覆盖关键状态转换、成功和失败路径 |
| 医生/药师端交互 | 生产构建；有可测试逻辑时补单元测试；附关键页面截图 |
| 鉴权、权限、上传、通知 | 单元/集成测试外，必须执行对应 E2E 场景 |
| 仅文档或注释 | CI 仍需通过，可在 PR 中说明无新增行为测试 |

修复缺陷时，应先增加能够复现缺陷的测试，再提交最小修复。

## 4. 集成与端到端验证

端到端脚本位于 `frontend/huanzhe/scripts`，需要本地 backend-v2、数据库、Redis、对象存储和三个前端按场景启动。按变更范围选择执行：

```powershell
Set-Location frontend/huanzhe
npm run e2e:auth-bind
npm run e2e:auth-refresh
npm run e2e:auth-expiry
npm run e2e:prescription-review
npm run e2e:medication-family
npm run e2e:helporder-collab
npm run e2e:helporder-mobile
npm run e2e:pharmacist-patient
npm run e2e:admin-notifications
```

`e2e:helporder-mobile` 覆盖患者图片/语音工单、药师移动端认领/回复/升级/驳回/关闭、医生结构化 `ADOPTED` / `NOT_ADOPTED` 决策，以及患者最终结果可见性。真实语音服务仍需通过已批准的本地凭据做一次浏览器录音、上传和转写人工取证；无凭据时必须验证可恢复的降级路径。

`e2e:medication-family` 除用药任务、患者/家属打卡和解绑权限外，还覆盖药师从自报药物工单进入有效患者计划页、服务端预填、保存计划项以及 `TRANSFERRED` 状态回写。

E2E 结果应在 PR 的“测试证据”中记录：环境、测试账号类型、命令、测试数、覆盖率、场景结果和脱敏截图。真实 OCR、语音、短信或 AI 服务只能通过环境变量或批准的密钥工具注入，禁止写入仓库或日志。

### T7 / SEC-002 认证验收

除九场景矩阵外，refresh Cookie 变更必须证明：

1. session 登录/刷新响应仅包含 `accessToken`、`expiresIn`、`refreshExpiresIn`、`deviceId`，不包含 `refreshToken`。
2. `sessionStorage`、`localStorage`、请求 URL 和日志不出现 refresh token；Cookie 对页面 JavaScript 不可见。
3. 页面重载和真实 access-token 过期都能静默恢复，轮换后的旧 Cookie 返回既有无效令牌错误。
4. 缺少 `X-Auth-Refresh: 1` 的 refresh/logout 被拒绝；退出后 Cookie 被清理且不能续期。
5. 患者 Cookie 为 30 天持久 Cookie；专业端 Cookie 为浏览器会话 Cookie。
6. Android debug 构建通过，并在真机执行杀进程重启、JSON、图片、语音和预签名上传回归。

生产源码静态扫描至少覆盖：

```powershell
rg -n -i "sessionStorage[^\r\n]*refresh|localStorage[^\r\n]*refresh|refreshTokenByValue|[?&]refreshToken=|params\s*:\s*\{[^\r\n]*refreshToken" frontend/huanzhe/src frontend/pharmacist/src frontend/yisheng/web-admin/src
```

零命中时 `rg` 返回退出码 1，属于通过结果。

### T8 / SEC-005 AI 数据边界验收

后端定向回归：

```powershell
mvn -q "-Dtest=AiConsentServiceTest,AiPrivacyRedactorTest,AiAuditServiceTest,HttpAiUpstreamClientTest,AiChatServiceTest,AiChatControllerTest" test -f D:\yaoanshi\backend-v2\pom.xml
```

除全量 `mvn -q clean verify`、患者端 lint/Vitest/build/Android 门禁外，必须证明：

1. 只有旧 `ai_consent_granted_at` 的账户仍显示 `sec-005-v2` 声明；缺失或错误版本返回 `AI_CONSENT_POLICY_MISMATCH (40034)`，正确版本授权后 disclosure 与旧 Boolean 接口均为已同意。
2. 手机号、15/18 位身份证号、邮箱及带明确标签的姓名/患者号/患者 ID/就诊编号/就诊号/住院号/门诊号/病历号只以占位符到达模拟上游；药名、剂量和服药时间不被误脱敏。
3. 客户端 system 消息被忽略；服务端最多发送最近五条非 system 历史消息和固定 system prompt。
4. 非白名单主机、非安全协议、重定向及 `PENDING` 审计插入失败时，上游零调用。
5. 成功、限流、认证失败和网络异常均留下无正文审计；问题、回复、请求载荷和正文哈希不进入数据库或日志。
6. V13 在新库和已有 V12 库均可迁移；`expires_at` 为创建时间后 180 天，清理脚本先预览、再按 1000 条有界删除，T8 不运行生产清理。
7. 患者页每次进入重新请求 disclosure；加载失败时发送保持禁用，数据使用说明可随时重开，声明使用 Vue 纯文本转义且无 `v-html`。

生产源码静态扫描至少覆盖：

```powershell
rg -n "v-html|localStorage.*ai|question|reply|payload|content_hash" frontend/huanzhe/src/modules/patient/pages/AiAssistantPage.vue backend-v2/src/main/resources/db/migration/V13__ai_data_boundary.sql backend-v2/src/main/java/com/medicine/service/ai backend-v2/src/main/java/com/medicine/infrastructure/ai
```

任何命中都必须逐条人工判定；测试断言或业务响应字段可以存在，审计表字段、持久化语句和审计失败日志中不得出现问答正文。

## 5. 发布前检查

发布候选版本必须满足：

1. `main` 最新提交的 `CI / Required Quality Gate` 通过。
2. 涉及的 E2E 场景在与生产等价的预发布环境通过。
3. 数据库迁移已在备份副本验证，下一版本号与正式实施计划一致。
4. 患者移动端、医生端、药师端完成关键路径人工冒烟测试。
5. 鉴权过期、无权限、网络失败和第三方服务不可用路径均有验证记录。
6. 未出现未审查的高风险依赖更新、密钥泄漏或敏感日志。

## 6. 失败处理

CI 失败时先定位第一个失败作业，不要重复运行掩盖确定性问题。后端测试与 JaCoCo 报告会作为 `backend-test-reports` artifact 保留 14 天；Android debug APK 会作为 `patient-android-debug-apk` artifact 保留 14 天。仅当失败被确认是 GitHub Runner 或依赖仓库的瞬时故障时才重跑，并在 PR 中记录原因。

覆盖率阈值是项目基线而不是目标上限。新增业务代码应优先保持或提高覆盖率；需要下调阈值时必须单独 PR，说明原因和恢复计划。
