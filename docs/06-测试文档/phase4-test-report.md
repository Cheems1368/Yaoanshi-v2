# Phase 4 测试报告

日期: `2026-03-07`

## 已完成验证

- `mvn -q -DskipTests compile -f D:\yaoanshi\backend-v2\pom.xml`
- `mvn -q "-Dtest=JwtTokenServiceTest,NotificationDispatchServiceTest,AdminHelpOrderServiceTest" test -f D:\yaoanshi\backend-v2\pom.xml`
- `mvn -q "-Dtest=AdminNotificationRealtimeServiceTest,NotificationDispatchServiceTest,AdminHelpOrderServiceTest" test -f D:\yaoanshi\backend-v2\pom.xml`
- `npm run build` in `D:\yaoanshi\frontend\huanzhe`
- `npm run build` in `D:\yaoanshi\frontend\pharmacist`
- `npm run build` in `D:\yaoanshi\frontend\yisheng\web-admin`
- `node --check` for:
  - `frontend/huanzhe/scripts/e2e-auth-refresh.mjs`
  - `frontend/huanzhe/scripts/e2e-auth-expiry.mjs`
  - `frontend/huanzhe/scripts/e2e-pharmacist-patient.mjs`
  - `frontend/huanzhe/scripts/e2e-admin-notifications.mjs`

## 已覆盖场景

- 专业端 WebSocket 通道 `/ws/admin-notifications` 代码与单测
- `notification_log` 推送状态回写
- 药师患者详情页内的用药计划创建、编辑、启用、任务与依从性报告面板
- 处方复核到患者计划面板的跳转衔接
- 患者端 401 刷新回归脚本
- 患者端真实过期脚本
- 药师浏览器冒烟脚本
- 专业端实时通知浏览器冒烟脚本
- `ops/scripts/phase4-local-run.ps1` 串行启动与验证脚本

## 运行结果

- `powershell -ExecutionPolicy Bypass -File D:\yaoanshi\backend-v2\scripts\smoke-core-flows-local.ps1` passed
- `node D:\yaoanshi\frontend\huanzhe\scripts\e2e-auth-refresh.mjs` passed
- `node D:\yaoanshi\frontend\huanzhe\scripts\e2e-pharmacist-patient.mjs` passed
- `node D:\yaoanshi\frontend\huanzhe\scripts\e2e-admin-notifications.mjs` passed under normal JWT TTL
- `powershell -ExecutionPolicy Bypass -File D:\yaoanshi\ops\scripts\phase4-local-run.ps1 -SkipBuild -SkipExpiryE2E` passed
- `node D:\yaoanshi\frontend\huanzhe\scripts\e2e-auth-expiry.mjs` passed under `-JwtAccessTokenSeconds 2`
- `powershell -ExecutionPolicy Bypass -File D:\yaoanshi\ops\scripts\phase4-local-run.ps1 -SkipBuild -JwtAccessTokenSeconds 2` passed after short-TTL skip of realtime-notification E2E

## 已知限制

- 对象存储仍为 stub，仅验证元数据注册与失败记录
- AI upstream 仍依赖环境变量，不纳入本轮 P0 验收
- 当前本地环境仍依赖混合运行时，不是单命令全栈编排
