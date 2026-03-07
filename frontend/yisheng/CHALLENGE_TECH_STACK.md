# 舟山分岛医疗管理系统 · 挑战杯项目计划书核心技术说明

面向挑战杯项目计划书撰写需求，本文对系统前后端的核心技术框架、模块分工及部署策略进行精细化描述，帮助团队成员在“技术方案”“实施路径”“创新点”“风险与对策”等章节中引用。

## 1. 写作目的与范围

- **目的**：阐述支撑医疗业务的关键技术组件及其作用，突出方案的成熟度、可扩展性、安全性与可演示性，为评审提供工程化依据。
- **范围**：涵盖 Spring Boot 后端、Vue 管理后台、Bootstrap 预览版、Android 客户端、数据与中间件、容器化部署以及协作交付流程。

## 2. 总体架构概览

| 层级         | 技术选型 | 主要作用 |
| ------------ | -------- | -------- |
| 展示端       | Vue 3 + Element Plus 管理后台、Bootstrap 静态预览、Android MVVM 客户端 | 负责医生/药师日常操作、演示版快速展示、移动随访等多端触点，保证不同场景均可接入业务。 |
| 应用服务层   | Spring Boot 2.7.18 + Java 11，system/infra/drug/patient/doctor 等模块 | 承载核心业务逻辑与权限控制，通过模块化拆分实现“职责单一 + 可并行开发”。 |
| 数据与中间件 | MySQL 8、Redis 6、Redisson、Dynamic Datasource、MyBatis-Plus、AJ-Captcha、MinIO/对象存储 | 提供事务数据、缓存、分布式锁、主从读写、验证码、文件存储等支撑能力，提升响应与安全。 |
| DevOps       | Docker Compose、Spring Boot Admin、SkyWalking、Git + Maven + Vite + Gradle | 实现一键演示、链路监控、持续集成与快速构建，确保项目能在挑战杯现场稳定复现。 |

> **层级协同逻辑**：展示端通过 HTTPS 与应用服务层沟通；服务层通过数据与中间件保持高可用；DevOps 提供部署、监控与版本控制，从而形成闭环。

## 3. 后端核心技术框架（`medicinal-his-delivery/`）

### 3.1 基础栈与依赖管理

| 组件 | 作用 |
| ---- | ---- |
| Java 11 + Spring Boot 2.7.18 | 作为 LTS 组合，兼容企业级部署与云原生容器，提供 MVC/DI/安全/配置等基础设施。 |
| `medicine-dependencies` BOM  | 将 SpringDoc、Knife4j、MyBatis-Plus、MapStruct、Lombok、Redisson、Flowable、Resilience4j 等版本统一，可在多模块中引用而不冲突。 |
| `medicine-framework` Starter | 以 Spring Boot Starter 形式封装 Web、安全、Redis、任务、MQ、Excel、日志、字典、租户、数据权限、验证码、WebSocket、脱敏等横切能力，形成“拿来即用”的企业级基座。 |

**作用细化**：
- **一致性**：BOM + Starter 让所有子模块共享相同依赖与配置，降低集成风险。
- **扩展性**: 新增模块只需引入相应 Starter 即可具备统一的日志、权限、监控能力。
- **安全性**：Security、Captcha、数据脱敏 Starter 直接提供登录守卫、验证码、多租户白名单、敏感字段处理。

### 3.2 领域模块划分

| 模块 | 职责与价值 |
| ---- | ---------- |
| `medicine-module-system` | 用户、角色、租户、字典、权限树；提供多角色（药师/医生/管理员）管理、租户隔离与数据字典支撑。 |
| `medicine-module-infra`  | 文件/对象存储、配置、操作日志、短信等基础服务；为聊天图片、处方附件、短信提醒提供统一入口。 |
| `medicine-module-drug`   | 药品档案、处方模板、药理知识库；保证药师开方时的数据准确性与可追溯性。 |
| `medicine-module-patient`| 患者档案、随访记录、沟通历史；支撑医患互动与合规留痕。 |
| `medicine-module-doctor` | 医师资质认证、排班、工单指派；确保医生审核流程与工作单调度有章可循。 |
| `medicine-server`        | Spring Boot 启动容器，聚合各 `*-biz` 模块并暴露 REST API；自身不包含业务，仅负责装配、配置和统一入口。 |

> **模块协作**：工作单在 doctor 模块生成 → patient 模块关联患者 → drug 模块提供处方模板 → system 模块校验角色权限 → infra 模块记录日志并分发短信。

### 3.3 数据访问、可靠性与安全机制

| 能力 | 作用 |
| ---- | ---- |
| MyBatis-Plus + Dynamic Datasource | 通过代码生成/Wrapper 减少样板代码，支持主从/多库；逻辑删除、雪花 ID、分页等全局参数在 `application.yaml` 一致配置。 |
| Redisson + Lock4j | 在工作单抢占、处方审核等高并发场景下提供分布式锁与限流策略，防止重复审核或串单。 |
| Resilience4j | 对接短信、对象存储等第三方服务时提供熔断、重试、降级策略。 |
| AJ-Captcha + Spring Security | 登录与短信验证场景叠加滑块/点选等验证码，结合 Spring Security 的认证授权流水线，阻断恶意访问。 |
| Knife4j + SpringDoc | 自动生成 `/v3/api-docs`、`/swagger-ui`，支持挑战杯展示或对外开放时的 API 调试。 |

### 3.4 配置、监控与部署

- **集中配置**：`application.yaml` 通过 `profiles` 控制环境，统一声明 Redis TTL、MyBatis-Plus 配置、WebSocket `/websocket/message` 路径、多租户忽略表、短信验证码策略、Screw 数据文档开关等，确保所有模块遵循相同治理策略。
- **可观测性**：Spring Boot Admin Client + SkyWalking Agent + OpenTracing 让服务节点注册到监控中心，实时查看启停、线程、慢 SQL 以及调用链，便于答辩时展示“可观测闭环”。
- **容器化部署**：`docker-compose.yml` 组合 MySQL、Redis、medicine-server 以及前端 Nginx 镜像，利用环境变量注入数据库/Redis 连接，可在挑战杯现场通过 `docker-compose up -d` 一键启动演示环境；同样适合后续上云部署。

## 4. 前端核心技术框架

### 4.1 Vue 3 管理后台（`yisheng/web-admin/`）

| 组件 | 作用 |
| ---- | ---- |
| Vite + Vue 3 + Element Plus + Sass | 提供现代化组件化开发体验，Element Plus 负责 UI 规范、Sass 统一配色；Vite 提供极速热更新，便于现场演示。 |
| Vue Router 4 | 定义登录、Dashboard、Doctor/Pharmacist 工作台、患者、处方、审核、工单、聊天、Profile 等路由，并通过 `meta.requiresAuth`、`meta.role` 控制访问。 |
| Pinia（`src/store/user.js`） | 管理 token、refreshToken、用户信息、权限集合；`derivePrimaryRole` 负责从多字段推断主要角色，`resolveDisplayName` 处理乱码/昵称问题，保证 UX。 |
| Axios 封装（`src/utils/request.js`） | 统一设置 `Authorization Bearer`、`tenant-id`、超时 & 错误提示；拦截器处理 401/403/404/500 及网络异常，自动触发登出或消息提醒。 |
| 组件结构（`src/views` + `Layout.vue`） | 以工作台/患者/处方/工单/聊天为维度拆分视图，`Layout` 负责顶栏+侧栏+面包屑，保证管理端一致的交互体验。 |

**作用细化**：
- **权限可视化**：路由守卫根据 Pinia 中的角色动态重定向，确保医生/药师看到各自工作台，提升竞赛评分中的“逻辑严谨性”。
- **多租户对接**：Axios 默认写入 `tenant-id`，与后端多租户配置对齐，展示真实SaaS场景。
- **易演示**：Vite Dev Server 3000 端口、自带代理 `/admin-api` → `http://localhost:48080`，无需额外配置即可联调。

### 4.2 Web 预览版（`yisheng/web-preview/`）

- **技术**：纯 HTML + Bootstrap 5 + Font Awesome + 原生 JS。
- **作用**：
  - 在无法运行完整前后端的场景下，快速展示登录、仪表盘、医生/药师卡片等 UI 原型。
  - 可直接通过浏览器打开或使用 `python -m http.server 8000`/`npx http-server` 托管，适合“展板展示”“线上提交材料”的静态演示需求。
  - 作为 PPT/手册中的截图来源，保证视觉一致。

### 4.3 Android 客户端

| 组件 | 作用 |
| ---- | ---- |
| MVVM 架构 + Kotlin | View 负责渲染，ViewModel 负责状态，Repository 连接数据源；保证解耦、易测试。 |
| Room + Coroutines/Flow | 提供离线缓存、异步数据流，支撑在网络不佳时的随访记录和草稿保存。 |
| Hilt 依赖注入 | 统一管理 Repository、API、数据库实例；降低手动注入成本。 |
| Jetpack Navigation + Material Design | 提供路由动画与原生 UI 指南，界面体验与官方一致，提升评审印象。 |
| 模块化目录 | `data/database|model|repository`、`ui/login|main|pharmacist|doctor|prescription|workorder|patient|chat`、`service/`、`receiver/`，便于分工与代码审查。 |

**作用细化**：
- **多端协同**：移动端可实现药师/医生在外场景下的随访、处方确认与聊天，展示“医疗协同闭环”亮点。
- **可演示性**：Android Studio 可直接运行模拟器，结合后端 API 验证真实流程，满足挑战杯对“可实际运行”要求。

## 5. 集成与交付流程

1. **版本控制**：采用 Git + Pull Request，前后端/移动端在同仓库，便于跨端联动与代码审查。
2. **构建体系**：后端使用 Maven Wrapper（`mvnw`）保证环境一致；前端使用 npm + Vite；Android 使用 Gradle Wrapper。编译命令可写入 CI。
3. **CI/CD 建议**：Jenkins/GitHub Actions 执行 `mvn verify`、`npm run build`、`./gradlew assembleRelease`，产出后端 JAR、前端静态资源、Android APK，并可进一步构建 Docker 镜像。
4. **一键演示环境**：`docker-compose up -d` 即可启动 MySQL + Redis + Spring Boot + Nginx 前端，挑战杯现场只需一台安装 Docker 的电脑即可复现全部功能。
5. **监控与巡检**: 借助 Spring Boot Admin/SkyWalking，对应用健康、日志、依赖调用链进行展示，作为计划书中“运维手段”与“风控措施”的支撑材料。

## 6. 项目计划书撰写提示

- **技术方案章节**：引用第 3、4 节的表格与描述，突出“模块化 + 多端协同 + 安全合规”。
- **实施路径**：按“（1）后端模块搭建 →（2）管理后台联调 →（3）Android 功能完善 →（4）Docker 演示环境/监控集成”划分 Milestone，每阶段对应可交付成果。
- **创新点**：
  1. 前后端解耦 + 多端接入（Web/预览/移动）的“全触点”医疗协同。
  2. 药师-医生双重审核、工作单调度与聊天随访形成闭环，结合分布式锁与限流确保流程可靠。
  3. 引入 SkyWalking、AJ-Captcha、Resilience4j、Lock4j 等企业级组件，在竞赛作品中展现“可观测性 + 安全性”优势。
- **风险与对策**：强调 BOM + Docker Compose 控制依赖一致性；Pinia 状态管理与动态路由保证角色隔离；Flowable/Lock4j 保障工作流一致性；监控体系提供故障预警。
- **附录引用**：可在计划书中附注“详细技术栈见附录 X：核心技术框架说明（引自 `CHALLENGE_TECH_STACK.md`）”，确保材料结构清晰。

---

> 若计划书仍需“进度甘特图 / 资源配置 / 成本测算”内容，可在此文档基础上追加章节，保持同样的表格化、条目化描述，确保评审能够一目了然。***
