# 舟山分岛医疗管理系统

面向药师与医生的全栈医疗管理解决方案，覆盖 Web 管理后台、Web 预览版以及 Android 客户端，后端基于 Spring Boot 提供统一的 REST API，支持工作单、处方审核、图文沟通等完整业务流程。

## 1. 项目总览

- **角色**：药师、医生；支持相同账号在 Web 与移动端登录。
- **客户端**：Vue 3 管理后台（`web-admin/`）、Bootstrap 静态预览版（`web-preview/`）、Android MVVM 客户端。
- **服务端**：Spring Boot 2.7.18 + Java 11，模块化拆分 system / infra / drug / patient / doctor 等业务域，并通过 Docker Compose 快速联调。

## 2. 仓库结构

```text
├── medicinal-his-delivery/            # Spring Boot 全量后端（同仓库下）
│   ├── docker-compose.yml             # 一键启动 MySQL、Redis、后端与前端镜像
│   ├── medicine-dependencies/         # 统一 BOM，声明所有三方依赖版本
│   ├── medicine-framework/            # 自研 Spring Boot Starter 及通用组件
│   ├── medicine-server/               # REST 容器，聚合业务模块
│   └── medicine-module-*/             # system、infra、drug、patient、doctor…
└── yisheng/
    ├── web-admin/                     # Vue 3 + Element Plus 管理后台
    │   ├── src/                       # 前端业务代码、Pinia、路由
    │   └── vite.config.js             # DevServer、代理、别名等配置
    ├── web-preview/                   # HTML/Bootstrap 静态预览
    ├── start-*.bat|.sh                # 本地启动脚本
    └── README.md                      # 当前文档
```

## 3. 核心技术栈

### 3.1 后端（`medicinal-his-delivery/`）

- **基础框架**：Java 11 + Spring Boot 2.7.18（`pom.xml` 中统一声明）。
- **依赖管理**：`medicine-dependencies` BOM 对 Spring Boot、SpringDoc、Knife4j、MyBatis-Plus、Druid、Dynamic Datasource、Redisson、Flowable、MapStruct、Lombok、Hutool、EasyExcel、MinIO、阿里/腾讯短信、JustAuth 等三方库进行版本收敛。
- **组件化**：`medicine-framework` 按 starter 封装 web、security、redis、file、monitor、job、mq、excel、test 以及操作日志、数据字典、租户、数据权限、错误码、IP、验证码、WebSocket、脱敏等业务横切能力。
- **业务模块**：`medicine-module-system/infra/drug/patient/doctor` 分层承载领域逻辑，`medicine-server` 仅负责装配、暴露 REST 接口与统一启动。
- **运行特性**：开启 Redis 缓存（TTL 1h）、MyBatis-Plus 全局策略、AJ-Captcha、WebSocket `/websocket/message`、多租户忽略清单、短信验证码策略以及 Screw 文档输出，默认 profile 为 `local`。
- **可观测性与保护**：集成 SpringDoc + Knife4j、SkyWalking、Spring Boot Admin、OpenTracing、Lock4j、Resilience4j，构建 API 文档、链路追踪与限流降级。

### 3.2 Web 管理后台（`yisheng/web-admin/`）

- **框架**：Vue 3、Vite、Element Plus、Vue Router 4、Pinia、Axios、Sass。
- **构建配置**：`vite.config.js` 设置 `@` 别名 -> `src/`，Dev Server 端口 3000，`/admin-api` 代理至 `http://localhost:48080`，并注入 `VITE_API_BASE_URL`。
- **状态管理**：`src/store/user.js` 通过 Pinia 维护 token / refresh token、本地缓存、角色推断（药师/医生）、登录与登出动作。
- **网络层**：`src/utils/request.js` 对 Axios 进行实例化，统一追加 `Authorization` Bearer 以及 `tenant-id` 头，集中处理常见状态码并使用 Element Plus `ElMessage` 提示。
- **路由与权限**：`src/router/index.js` 定义登录、Dashboard、博士/药师工作台、患者、处方、审核、工单、聊天、资料等页面，并通过 `meta.requiresAuth` 与 `meta.role` 配合全局守卫完成认证、角色重定向与 404 兜底。

### 3.3 Web 预览版（`yisheng/web-preview/`）

- 单个 `index.html` 即可运行，依赖 Bootstrap 5 与 Font Awesome。
- 通过 `python -m http.server` 或 `npx http-server` 在本地 8000 端口预览，也可直接部署至 GitHub Pages / Netlify / Vercel 等静态托管。

### 3.4 Android 客户端

- **架构**：MVVM（View + ViewModel + Repository + Room 数据层）。
- **技术栈**：Kotlin、Room Persistence Library、Hilt 依赖注入、Material Design 组件、Jetpack Navigation、Coroutines + Flow。
- **目录布局**：`app/src/main/java/com/medical/pharmacy/` 下包含 data（database/model/repository）、di、ui（login/main/pharmacist/doctor/prescription/workorder/patient/chat）、service、receiver 等模块，与 `res/`、`build.gradle` 配合完成构建。

## 4. 功能维度

### 4.1 药师工作台

- 接收并初审工作单。
- 填写并调整用药建议/处方草稿。
- 与医生或患者进行图文/消息沟通。
- 进行用药随访与提醒。

### 4.2 医生工作台

- 审核药师草稿处方，并可再次编辑。
- 完成电子签名确认，或直接接管高风险患者。
- 查看患者、处方、工单等综合信息面板。

## 5. 快速开始

### 5.1 启动后端（Docker Compose）

```bash
cd medicinal-his-delivery
docker-compose up -d
```

- 会拉起 `medicine-mysql` (13306:3306)、`medicine-redis` (16379:6379)、`medicine-server` (48080:48080) 以及 Nginx 打包后的前端镜像 (8080:80)。
- 可通过修改 `.env` 或传参覆盖 `MASTER_DATASOURCE_URL/USERNAME/PASSWORD`、`REDIS_HOST` 等环境变量。

### 5.2 启动 Web 管理后台

```bash
cd yisheng/web-admin
npm install
npm run dev
# 访问 http://localhost:3000
```

> 默认代理到本地 `http://localhost:48080/admin-api`。若后端部署在其他地址，设置 `VITE_API_BASE_URL` 即可。

### 5.3 启动 Web 预览版

```bash
cd yisheng/web-preview
python -m http.server 8000
# 或 npx http-server
# 访问 http://localhost:8000
```

### 5.4 运行 Android 客户端

1. 准备 Android Studio Arctic Fox 及以上、Android SDK 24+、JDK 8+。
2. 使用 Android Studio 打开项目，等待 Gradle Sync。
3. 选择模拟器或真机，点击运行。
4. 测试账号：用户名 `admin`，密码 `123456`，可切换药师/医生角色。

## 6. 运行配置与端口

| 模块/服务        | 默认端口 | 说明 |
| ---------------- | -------- | ------------------------------------------------ |
| Web 管理后台     | 3000     | `npm run dev` 开发端口，自动代理 `/admin-api`。 |
| Web 预览 (Python)| 8000     | `python -m http.server 8000`。                   |
| medicine-server  | 48080    | Spring Boot REST API，Docker Compose 映射。      |
| MySQL            | 13306    | 宿主机访问 `medicine-mysql` 数据库。             |
| Redis            | 16379    | 宿主机访问 `medicine-redis`。                    |
| 前端 Nginx 容器  | 8080     | 部署版 Web Admin（Docker）。                     |

### 6.1 前端环境变量

- `VITE_API_BASE_URL`：Axios `baseURL`，默认为 `http://localhost:48080`。
- `tenant-id`：在 `src/utils/request.js` 里写死为 `1`，如需多租户请结合后端 `medicine.tenant` 配置调整。

## 7. 常见问题

- **ECONNREFUSED / 登录失败**：大多数情况下是后端或数据库尚未通过 Docker Compose 启动。确认 `medicine-server`、MySQL、Redis 容器运行正常，再刷新前端。
- **角色跳转异常**：Pinia `userStore` 会根据接口返回的 `roles`/`role`/`nickname` 进行推断，请确保用户角色中包含 `doctor` 或 `pharmacist` 关键字。

## 8. 部署建议

1. 使用 `docker-compose.yml` 作为基础模板，在生产环境中将 MySQL、Redis 改为托管服务，`medicine-server` 设置 `SPRING_PROFILES_ACTIVE=prod` 并覆盖数据库、Redis、MinIO、第三方短信参数。
2. Web 管理后台可通过 `npm run build` 生成静态资源，交由 Nginx/Apache/OSS 托管；或者直接使用 Compose 中的前端镜像。
3. Android 客户端打包前在 `app/build.gradle` 配置签名，并通过 `./gradlew assembleRelease` 生成 APK/AAB。

## 9. 贡献指南

1. Fork 仓库并创建功能分支。
2. 完成开发后保证 lint / 单元测试通过。
3. 提交 Pull Request，描述修改点与验证方式。

## 10. 许可证

项目采用 **MIT License**，详情参见根目录 `LICENSE`。

## 11. 联系方式

- 邮箱：`your-email@example.com`
- 项目地址：`[GitHub 项目链接]`

## 12. 更新日志

### v1.0.0（2024-01-15）

- 初始版本发布。
- 提供药师 / 医生双角色登录。
- 包含 Web 管理后台 + Web 预览版 + Android 客户端三端能力。
