# Phase 4 验收清单

## 验收范围

- 药师端患者详情内嵌用药计划维护
- 专业端实时通知闭环
- 浏览器级联调验证
- 本地串行启动与回滚说明

## 功能验收

- [ ] 药师在 `/patient/info` 可创建用药计划
- [ ] 药师在 `/patient/info` 可编辑最新用药计划
- [ ] 药师在 `/patient/info` 可保存并启用计划
- [ ] 药师在 `/patient/info` 可查看计划任务与依从性报告
- [ ] `/prescription/prescriptionDetail` 完成复核后可跳到患者详情计划面板
- [ ] 患者提交帮助工单后，药师端收到实时提醒并刷新队列
- [ ] 药师升级帮助工单后，医生端收到实时提醒并刷新队列
- [ ] 工单关闭后，患者侧结果可见，`notification_log` 状态正确入库

## 技术验收

- [x] `backend-v2` 编译通过
- [x] `frontend/huanzhe` build 通过
- [x] `frontend/pharmacist` build 通过
- [x] `frontend/yisheng/web-admin` build 通过
- [x] `backend-v2/scripts/smoke-core-flows-local.ps1` 通过
- [x] `frontend/huanzhe/scripts/e2e-auth-refresh.mjs` 通过
- [x] `frontend/huanzhe/scripts/e2e-pharmacist-patient.mjs` 通过
- [x] `frontend/huanzhe/scripts/e2e-admin-notifications.mjs` 通过
- [x] 真实过期 E2E 在短 TTL 模式下通过

## 运行环境验收

- [x] `3001`、`3002`、`3003`、`8080` 无端口冲突
- [x] legacy `48080` 登录可用
- [x] `backend-v2` `local` profile 可通过环境变量覆盖 JWT TTL
- [x] `ops/scripts/phase4-local-run.ps1` 可串行拉起并执行验证

## 证据留存

- 构建日志
- 后端 smoke 输出
- 浏览器 E2E 输出
- 若真实过期验证启用，保留短 TTL 启动参数记录

## 已知不纳入本轮

- `GET /app-api/prescriptions`
- 患者处方历史持久化浏览
- challenge-cup AI 真连通扩展能力
