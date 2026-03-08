# Claude Opus 4.6 + GPT-5.3 Codex 双机联动协作规划

## Context

药按时 MVP 项目已完成 Phase 0-4 的主体开发，当前处于收尾阶段。根据用户对两套 AI 系统的能力测试结果，需要建立一套分工明确、可持续的双机协作机制，既覆盖当前剩余工作的具体分配，也建立面向 P2 扩展和日常迭代的通用协作框架。

**能力矩阵（用户测试结论）：**

| 能力域 | 最优执行者 |
|--------|-----------|
| 软件工程与代码生成 | GPT-5.3 Codex |
| 代码理解、推理与质量 | GPT-5.3 Codex |
| 调试、测试与维护 | GPT-5.3 Codex |
| 数据工程与后端服务 | Claude Opus 4.6 |
| 前端与产品工程 | Claude Opus 4.6 |
| Web与桌面自动化(静态) | Claude Opus 4.6 |

**一句话定位：** Claude 是产品/数据/前端/验收主机；Codex 是施工/质量/测试/CI 主机。设计先由 Claude 锁边界，施工优先交给 Codex，关键交付再回 Claude 做产品与运行态验收。

---

## Part A: 通用协作框架

### A0. 三条铁律

1. **Claude 不直接长期承担高频后端施工。** 它负责定义、审稿、前端、浏览器验收、环境集成。
2. **Codex 不重新定义需求。** 它负责施工、质量、测试、CI、skills 门禁。
3. **所有交接都必须引用正式文档来源 + progress.txt 当前状态。** 否则两台机会越跑越偏。

### A1. 职责划分原则

**Claude Opus 4.6 主导（产品/数据/前端/验收主机）：**

A. **数据与产品方案裁决**
   - schema 设计
   - DTO / API contract 裁决
   - 状态机与边界条件裁决
   - 前端页面流与交互规范
   - P2 功能设计

B. **前端实现**
   - Vue 组件、路由、状态管理、UI 对齐
   - 菜单隔离、非 MVP 菜单隐藏
   - 前端构建产物优化

C. **静态自动化与浏览器验收**
   - Playwright / 浏览器静态巡检
   - 登录、打卡、工单、回写固定路径冒烟
   - 菜单 / 角色 / 文案 / 流程检查

D. **运维交付面**
   - docker-compose、ops/scripts
   - schema migration 脚本
   - 文档导出与验收清单

**GPT-5.3 Codex 主导（施工/质量/测试/CI 双核主机）：**

A. **骨架与批量施工**
   - backend-v2 脚手架
   - 批量生成 DTO / VO / Controller / Mapper / Service 骨架
   - 测试骨架
   - CI 配置骨架

B. **质量与合规**
   - `security-best-practices` 安全审查
   - `medicine-contract-and-architecture` 架构合规
   - 单元测试 / 集成测试编写与维护
   - `gh-fix-ci` CI/CD 流水线调试

C. **调试、测试、维护**
   - 启动错误、构建失败定位与修复
   - 回归验证
   - CI 爆炸修复
   - 缺失测试补齐

D. **技能生命周期管理**
   - `skill-manager`、`skill-evolution-manager`

### A2. 分级协作流

不同类型的任务使用不同的协作流程，而非统一流。

#### 流程 1：纯设计型任务

适用于：状态机设计、告警策略、权限边界、OCR 失败回退路径等。

```
Claude 定方案 → Codex 合规检查 → Claude 定稿
```

#### 流程 2：骨架/批量代码型任务

适用于：新模块脚手架、DTO/Mapper/Service 批量生成、单元测试模板、CI YAML/脚本模板等。

```
Claude 定约束（数据结构 + 接口方案 + 命名规范）
    → Codex 施工（批量生成代码）
    → Codex 自测（编译 + 单元测试）
    → Claude 审是否偏离产品
```

#### 流程 3：前端页面与流程型任务

适用于：Vue 页面开发、路由、交互、UI 对齐等。

```
Claude 实现
    → Claude 浏览器静态验收
    → Codex 审质量与测试补齐
```

#### 流程 4：调试/修复型任务

适用于：启动错误、构建失败、测试回归、CI 失败等。

```
Codex 主导定位与修复
    → Claude 做运行态 / 浏览器验证
```

### A3. 交接协议

**从 Claude 向 Codex 交接时，必须输出：**
1. 变更文件清单（路径 + 简要说明）
2. 已执行的验证命令及结果
3. progress.txt 的当前状态
4. 待 Codex 执行的具体指令（如：审查安全、补充测试、检查架构合规）
5. **依据的正式文档/章节**（这次实现依据的是 `Standard document/` 中的哪几个文档、哪些段落）

**从 Codex 向 Claude 交接时，必须输出：**
1. 审查报告或生成的代码文件
2. 发现的问题列表（severity + 文件位置 + 建议）
3. 测试结果（通过/失败 + 日志）
4. 需要 Claude 进一步处理的 action items
5. **依据的正式文档/章节**（这次审查/施工依据的是 `Standard document/` 中的哪几个文档、哪些段落）

**共享状态源：**
- `progress.txt` — 当前执行记忆和状态源（双方共享）
- `CLAUDE.md` — Claude Opus 4.6 操作手册
- `CODEX.md` — GPT-5.3 Codex 操作手册
- `Standard document/*` — 权威规范文档（双方共同遵守）

### A4. 验证门控

每项工作完成后，按以下顺序验证（最快失败优先）：

| 步骤 | 执行者 | 验证项 | 理由 |
|------|--------|--------|------|
| 1 | 实现方（Claude/Codex） | 本地构建/类型检查通过 | 最快反馈 |
| 2 | Codex | 架构合规检查（`medicine-contract-and-architecture`） | 路径前缀/DTO 名/边界层违反应最早死 |
| 3 | Codex | 安全审查（`security-best-practices`） | 合规通过后再查安全 |
| 4 | Codex | 单元/集成测试覆盖并通过 | 功能正确性 |
| 5 | Claude | 本地运行时烟雾测试（PowerShell scripts） | 运行态验证 |
| 6 | Claude | 浏览器 E2E 测试（Playwright scripts） | 端到端验证 |
| 7 | 双方 | progress.txt 更新 | 状态同步 |

---

## Part B: 当前剩余工作分工

### 当前状态摘要

- Phase 4 试运行收尾已完成，本地验证通过
- 剩余 In Progress：Challenge-cup AI runtime E2E（依赖真实 JWT + AI upstream key）
- 三大 Blocker：对象存储真实集成、AI 上游 key 未配置、混合服务依赖

### B1. 即时可执行任务

| # | 任务 | 主执行者 | 辅助者 | 依赖 |
|---|------|---------|--------|------|
| 1 | 全量代码安全审查（backend-v2 + 三前端） | Codex (`security-best-practices`) | Claude 修复 | 无 |
| 2 | 架构合规性审查（backend-v2 对照 BACKEND_STRUCTURE.md） | Codex (`medicine-contract-and-architecture`) | Claude 修复 | 无 |
| 3 | 补充缺失单元测试（目标覆盖核心 service 层） | Codex | Claude 验证运行 | 无 |
| 4 | 前端构建产物优化（bundle size 分析、chunk 拆分） | Claude | Codex 审查 | 无 |
| 5 | 文档对齐检查（progress.txt vs 实际代码状态） | Claude 主盘点 | Codex 复核 progress.txt 与代码/测试命令一致性 | 无 |

### B2. 环境就绪后可执行任务

| # | 任务 | 主执行者 | 辅助者 | 依赖 |
|---|------|---------|--------|------|
| 6 | 对象存储真实集成（S3 compatible） | Claude（后端方案裁决 + 集成配置） | Codex（测试编写 + 骨架施工） | S3 endpoint 配置 |
| 7 | AI upstream 真实集成验证 | Claude（E2E 脚本 + 后端配置） | Codex（错误处理审查：fallback/timeout/retry/key 缺失错误分级） | AI key + base-url |
| 8 | Challenge-cup AI runtime E2E | Claude（浏览器自动化） | Codex（测试健壮性审查） | #7 完成 |
| 9 | OCR 真实集成 | Claude（后端方案裁决 + 前端对接） | Codex（安全审查：文件上传大小/类型/恶意内容/防路径穿越过 `security-best-practices`） | OCR provider |

### B3. 生产部署准备

| # | 任务 | 主执行者 | 辅助者 |
|---|------|---------|--------|
| 10 | Docker 镜像构建 + compose 生产配置 | Claude | Codex 审查（Dockerfile 体积、基础镜像选择、暴露端口最小化、secrets 注入方式、compose 开发/生产变量隔离） |
| 11 | CI/CD 流水线搭建 | Codex (`gh-fix-ci`) | Claude 验证 |
| 12 | 生产环境 schema migration 脚本 | Claude | Codex 审查 |
| 13 | 接口文档生成（SpringDoc 导出） | Claude | — |
| 14 | 测试报告/验收清单完善 | Claude (`doc` skill 可辅助) | Codex |

### B4. P2 扩展规划（后续）

| # | 任务 | 主执行者 | 辅助者 |
|---|------|---------|--------|
| 15 | 科普内容库后端 + 前端 | Claude（方案裁决）+ Codex（骨架施工） | Codex 测试 |
| 16 | 内容运营能力 | Claude（方案裁决）+ Codex（骨架施工） | Codex 审查 |
| 17 | 扩病种策略 | Claude（数据/方案） | Codex（施工/测试/质量） |
| 18 | 性能优化（API/数据库） | Codex（分析 + 施工） | Claude（验证） |

---

## Part C: Codex Skills 使用映射

| Skill | 触发场景 | 协作方式 |
|-------|---------|---------|
| `medicine-contract-and-architecture` | 任何 backend-v2 代码变更后 | Codex 审查 → Claude 修复 |
| `security-best-practices` | 每次重要功能完成后 | Codex 出安全报告 → Claude 修复 |
| `gh-fix-ci` | CI 失败时 | Codex 诊断 → Claude/Codex 修复 |
| `playwright-interactive` | 需要持久浏览器调试时 | 配合 Claude 浏览器自动化 |
| `doc` | 生成 Word 交付物时 | Codex 或 Claude 均可执行 |
| `spreadsheet` | 导出数据/报表分析时 | Codex 或 Claude 均可执行 |
| `figma-implement-design` | 新 UI 设计稿到代码时 | Codex 翻译 → Claude 对齐产品规范 |
| `skill-evolution-manager` | Skill 需要基于反馈迭代时 | Codex 执行 |
| `skill-manager` | Skill 版本检查/更新时 | Codex 执行 |

---

## Part D: 执行优先级

**立即开始（无外部依赖）：**
1. Codex: 全量安全审查 + 架构合规审查
2. Claude: 文档对齐检查 + 前端构建优化
3. Codex: 补充核心 service 单元测试

**环境就绪后：**
4. Claude 定方案 + Codex 施工: 对象存储 → AI 集成 → Challenge-cup E2E
5. Claude 定方案 + Codex 施工/安全审查: OCR 真实集成

**部署阶段：**
6. Claude: Docker/compose + schema migration
7. Codex: CI/CD 流水线
8. 双方: 最终验收

---

## 验证方式

- 编译验证: `mvn -q -DskipTests compile -f D:/yaoanshi/backend-v2/pom.xml`
- 前端构建: `npm run build` 分别在三个前端目录
- 单元测试: `mvn test -f D:/yaoanshi/backend-v2/pom.xml`
- 本地烟雾: `powershell -ExecutionPolicy Bypass -File D:/yaoanshi/ops/scripts/phase4-local-run.ps1`
- E2E: 各 Playwright 脚本在 `frontend/huanzhe/scripts/e2e-*.mjs`
