# 测试与质量门禁

本项目采用分层测试流程。Pull Request 只强制执行确定性高、无需真实密钥或外部服务的检查；依赖完整运行环境的集成与端到端测试在相关功能合并前和发布前执行。

## 1. Pull Request 必需检查

GitHub Actions 工作流 `CI` 并行执行：

| 检查 | 内容 | 通过标准 |
| --- | --- | --- |
| Backend tests and coverage | Spring Boot 编译、JUnit 测试、JaCoCo | 测试全部通过，行覆盖率不低于 60% |
| Patient app quality | ESLint、Vitest、类型检查、生产构建 | 全部命令退出码为 0 |
| Pharmacist web build | 锁定依赖安装、生产构建 | 构建成功 |
| Doctor web build | 锁定依赖安装、生产构建 | 构建成功 |
| Required Quality Gate | 汇总以上四项 | 四项均为 success |

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

## 5. 发布前检查

发布候选版本必须满足：

1. `main` 最新提交的 `CI / Required Quality Gate` 通过。
2. 涉及的 E2E 场景在与生产等价的预发布环境通过。
3. 数据库迁移已在备份副本验证，下一版本号与正式实施计划一致。
4. 患者移动端、医生端、药师端完成关键路径人工冒烟测试。
5. 鉴权过期、无权限、网络失败和第三方服务不可用路径均有验证记录。
6. 未出现未审查的高风险依赖更新、密钥泄漏或敏感日志。

## 6. 失败处理

CI 失败时先定位第一个失败作业，不要重复运行掩盖确定性问题。后端测试与 JaCoCo 报告会作为 `backend-test-reports` artifact 保留 14 天。仅当失败被确认是 GitHub Runner 或依赖仓库的瞬时故障时才重跑，并在 PR 中记录原因。

覆盖率阈值是项目基线而不是目标上限。新增业务代码应优先保持或提高覆盖率；需要下调阈值时必须单独 PR，说明原因和恢复计划。
