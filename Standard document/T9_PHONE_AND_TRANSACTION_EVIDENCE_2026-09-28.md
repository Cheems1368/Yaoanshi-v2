# PR C 真机与事务验证记录（2026-09-28）

## 版本、环境与边界

- 代码：`codex/mvp-v0.1.0-release`，修复提交 `04c7e49`。患者调试 APK SHA-256：`EB2DA7F75B9A8AC15C27B6261CF3029944BE727C41C095CF51D1FEFCB15F0A5B`；已安装包 `versionName=0.1.0`、`versionCode=1`。
- 会话失效回归使用会话修复构建的 APK，SHA-256：`EFEF8EA20B379BE79C13C49449A83F02133DF5B61B6F9CDCE848D30FEDE3D22E`；仅此段结果归属于该 APK。原哈希仅对应上表的先前步骤。后续添加的当前页跳转代码另有构建，尚无本记录中的真机行为证据。
- 真机：OPPO PDYT20，Android SDK ADB 序列号 `IBU8YTKBQ8W4TOS8`，连续 `device` 且 `get-state=device`。USB 调试已授权。调试 APK 用 `CAPACITOR_ANDROID_LOCAL_HTTP=true` 和 `VITE_APP_API_TARGET=http://localhost:8080` 构建；ADB reverse `tcp:8080`。该地址仅用于本轮 USB 隔离验收。
- 服务：`SPRING_PROFILES_ACTIVE=local` 的后端、一次性 MySQL 8.0 和 Redis 容器，独立 `yaoanshi-t9-acceptance` 网络，数据库 `t9_phone`，仅含合成患者 201、药师 101、医生 201、手机号 `13800000201`。未连接开发库或生产库；停用计划和通知表故障注入仅影响该隔离库。数据库从空库迁移至 V15。
- 本记录只证明列出的实际操作。截图均为合成数据，位于 [真机证据目录](evidence/t9-phone-2026-09-28)。2026-09-24 的 MySQL/MinIO 历史证据仍只用于未受本轮修改影响的链路。

## Android 真机步骤与结果

| 步骤 | 实际结果 | 证据 |
|---|---|---|
| 安装 APK、发送验证码、患者登录 | 隔离 API 健康检查 200；本地配置允许 `http://localhost` CORS 后，验证码发送及合成患者登录成功；今日 07:56 剂次正确显示。 | [打卡页](evidence/t9-phone-2026-09-28/checkin-layout.png) |
| 服药提醒 | 07:56 的本机系统通知实际出现，文案为通用用药提醒，无药名；通知对应 Android 系统闹钟。 | [系统通知](evidence/t9-phone-2026-09-28/alarm-fired.png) |
| 打卡布局 | 原 APK 在 1080×2400 真机中把时间、原因、按钮排到屏幕外；`04c7e49` 将今日提醒、提醒详情和记录更正表单纵向排列。覆盖安装后时间、原因、已服药／漏服／跳过按钮均可见。 | [修复后页面](evidence/t9-phone-2026-09-28/checkin-layout.png) |
| 离线缓存与持久化 | 撤销 ADB reverse、关闭 Wi-Fi/移动数据，先把应用退到后台再结束进程。离线重启仍显示 2 个缓存任务，首页显示“离线缓存任务”。应用私有 `no_backup/medical-cache-v1.enc` 存在；加密实现由既有单测覆盖，未把内容导出。 | [离线首页](evidence/t9-phone-2026-09-28/offline-cache.png) |
| 离线打卡与重放 | 离线对合成任务 61 选择“跳过”：真机提示“记录已加密保存，将在联网后自动同步”，隔离库此前打卡数为 0、剂次为 `PENDING`。退到后台、结束进程并离线重启后队列文件仍存在；恢复 ADB reverse 与网络并重新启动，数据库新增且仅新增 1 条 `SKIPPED`，剂次状态为 `SKIPPED`。 | [离线入队](evidence/t9-phone-2026-09-28/offline-queued.png)、[联网后首页](evidence/t9-phone-2026-09-28/replay-complete.png) |
| 停用后撤销提醒 | 药师通过接口停用计划 1，后端保留 1 个 `COMPLETED`、1 个 `SKIPPED`，取消 29 个未来剂次。手机同步前 Android 系统中有 2 个本应用未来闹钟；进入用药页完成同步后为 0。 | [停用后页面](evidence/t9-phone-2026-09-28/reminders-cancelled.png)、系统 `dumpsys alarm` 前后计数 2→0 |

调试中用 ADB 在应用**前台直接** `force-stop` 曾使 WebView 磁盘中的刷新 Cookie 落后于服务端轮换后的哈希，重新联网时会话失效且本地队列被清理。按用户确认的规则修复后，用下表的隔离库失效注入复现同一拒绝分支。此注入证明客户端拒绝后的处理；不声称消除了 WebView Cookie 落盘时序，也不把注入等同于一次自然发生的 Cookie 竞态。

| 会话失效回归步骤 | 实际结果 | 证据 |
|---|---|---|
| 完全断网、进程重启后对合成剂次 122 跳过 | 手机提示“记录已加密保存”；隔离库仍 `PENDING`、打卡 0 条。 | [离线入队](evidence/t9-phone-2026-09-28/session-expiry-queued.png) |
| 前台强制结束进程；仅在 `t9_phone` 将合成账号 301 的 3 个刷新哈希替换为互不相同的测试哈希；恢复联网并启动 | 服务端拒绝旧 Cookie，应用显示登录页；加密缓存文件从 8709 字节缩为 373 字节而未删除。再次断网启动仍停在登录页、文件仍在，不会以封存队列恢复离线任务。 | [失效后登录页](evidence/t9-phone-2026-09-28/session-expiry-login.png)、ADB 私有缓存文件计数 |
| 同一合成账号 `13800000201` 重新获取验证码并登录 | 队列自动重放，剂次 122 变 `SKIPPED`，隔离库恰好 1 条 `SKIPPED` 打卡；手机重新缓存当前任务。 | [重登后首页](evidence/t9-phone-2026-09-28/session-expiry-replayed.png)、隔离库计数 0→1 |
| 再对合成剂次 152 离线跳过、注入同样的刷新拒绝，改用合成账号 `13800000202` 登录 | 旧队列未重放：剂次 152 仍 `PENDING`、打卡 0；新账号进入未绑定页面；应用私有加密缓存目录为空。 | [换号后绑定页](evidence/t9-phone-2026-09-28/session-switch-cleared.png)、ADB 文件列表及隔离库计数 |

队列原有的逐条 48 小时到期时间未延长。主动退出、解绑清除逻辑仍需单独完成真机验收；旧版队列迁移和已取消剂次真机重放也待验证。

## 后端 Docker 故障与工单链

使用装有 `04c7e49` 对应 JAR 的隔离后端，合成工单 1 的患者提交后状态 `SUBMITTED`、事件数 1。仅在 `t9_phone` 临时重命名 `notification_log`：药师接单返回 HTTP 500，状态仍为 `SUBMITTED`、事件数仍为 1；恢复表后重试成功，状态 `PHARMACIST_IN_PROGRESS`、事件数 2。再次临时重命名该表：患者回复返回 HTTP 500，状态及事件数均不变；恢复后重试成功，事件数 3。两次故障注入后均恢复原表。服务层事务使业务状态、事件和通知日志同成败。

同一合成工单随后由药师升级给医生 201，状态 `WAITING_DOCTOR`；医生以 `ADOPTED` 决策，状态 `CLOSED`。最终事件顺序为 `SUBMITTED`、`ASSIGNED`、`PATIENT_REPLY`、`ESCALATED_TO_DOCTOR`、`DOCTOR_DECISION`；通知日志依次指向药师、患者、药师、医生、患者、药师，合成工单无重复事件。

## 本提交验证与剩余门槛

- 后端 `mvn verify`：226 项测试与 JaCoCo 通过；随后 Docker 中的两项事务故障注入与恢复通过。
- 患者端 `npm run lint`、`npm run test:unit:ci`（88/88）、`npm run build`、`npm run android:build` 通过。`npm run e2e:mvp-release` 在 Vite 3001 测试服务器启动后通过，输出 `familyBinding`、`knowledgeAssessment`、`satisfaction`、`notificationPolling` 均为 true，覆盖 390×844 与 430×932。首次未启动服务器时连接被拒，此次失败不代表功能断言失败。
- 尚需按 [验收矩阵](T9_ACCEPTANCE_MATRIX.md) 完成 PRD 剩余真实链路、真机主动退出/解绑/旧版缓存迁移/取消后离线重放、专业端浏览器剩余项与最终 HEAD 检查。PR C 保持 Draft；Tag/Release 另待最终确认。
- 会话修复后患者端 lint、92/92 单测、Web 构建及上述 Android 调试 APK 构建通过；最终提交的 CI 尚待核对。
