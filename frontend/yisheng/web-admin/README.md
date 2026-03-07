# 舟山分岛医疗管理系统 - Web管理端

## 项目简介

这是舟山分岛医疗管理系统的Web管理端，专为医师和药师设计，提供处方审核、工单处理、患者管理等功能。

## 功能特性

### 医师端功能
- **处方审核**: 审核药师提交的处方建议
- **电子签名**: 完成处方的电子签名
- **患者管理**: 查看和管理患者信息
- **处方管理**: 管理处方历史和状态

### 药师端功能
- **工单处理**: 处理患者提交的咨询和开药请求
- **用药建议**: 提供专业的用药建议和指导
- **患者对话**: 与患者进行实时沟通
- **随访提醒**: 管理患者随访和提醒

## 技术栈

- **前端框架**: Vue 3
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **样式预处理**: Sass

## 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖

```bash
cd web-admin
npm install
```

### 启动开发服务器

```bash
npm run dev
```

或者使用Windows批处理文件：

```bash
start-dev.bat
```

访问 http://localhost:5173 查看应用。

### 构建生产版本

```bash
npm run build
```

## 项目结构

```
web-admin/
├── src/
│   ├── api/              # API接口
│   │   ├── auth.js       # 认证相关
│   │   ├── prescription.js # 处方相关
│   │   ├── workorder.js  # 工单相关
│   │   ├── patient.js    # 患者相关
│   │   └── chat.js       # 聊天相关
│   ├── components/       # 公共组件
│   ├── layout/          # 布局组件
│   ├── router/          # 路由配置
│   ├── store/           # 状态管理
│   ├── styles/          # 样式文件
│   ├── utils/           # 工具函数
│   ├── views/           # 页面组件
│   │   ├── DoctorDashboard.vue      # 医师工作台
│   │   ├── PharmacistDashboard.vue   # 药师工作台
│   │   └── Login.vue                 # 登录页面
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── package.json         # 项目配置
├── vite.config.js       # Vite配置
└── README.md           # 项目说明
```

## 用户角色

### 医师 (Doctor)
- 登录后自动跳转到医师工作台
- 主要功能：处方审核、电子签名
- 菜单：医师工作台、处方审核、患者管理、处方管理

### 药师 (Pharmacist)
- 登录后自动跳转到药师工作台
- 主要功能：工单处理、用药建议、患者对话
- 菜单：药师工作台、工单处理、患者对话、患者管理

## 设计规范

### 颜色方案
- **主品牌蓝**: #2979FF
- **辅色浅蓝**: #DBEAFF
- **主文字色**: #333333
- **次级文字色**: #556C8A
- **提示框背景**: rgba(207, 231, 255, 0.57)

### 医疗系统专用色
- **高风险/紧急**: #FF4757 (红色)
- **中等风险/待处理**: #FFA502 (黄色)
- **低风险/已完成**: #2ED573 (绿色)
- **信息/进行中**: #3742FA (蓝色)

## API接口

### 认证接口
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 用户登出
- `GET /auth/user-info` - 获取用户信息

### 处方接口
- `GET /api/prescriptions/pending` - 获取待审核处方
- `GET /api/prescriptions/{id}` - 获取处方详情
- `POST /api/prescriptions/{id}/review` - 审核处方
- `POST /api/prescriptions/{id}/sign` - 电子签名

### 工单接口
- `GET /api/workorders` - 获取工单列表
- `GET /api/workorders/{id}` - 获取工单详情
- `POST /api/workorders/{id}/process` - 处理工单
- `POST /api/workorders/{id}/complete` - 完成工单

### 患者接口
- `GET /api/patients` - 获取患者列表
- `GET /api/patients/{id}` - 获取患者详情
- `GET /api/patients/{id}/health-record` - 获取健康档案

### 聊天接口
- `GET /api/chat/list` - 获取聊天列表
- `GET /api/chat/{id}/messages` - 获取聊天消息
- `POST /api/chat/{id}/send` - 发送消息

## 开发说明

### 添加新页面
1. 在 `src/views/` 目录下创建Vue组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在 `src/layout/Layout.vue` 中添加菜单项（如需要）

### 添加新API
1. 在 `src/api/` 目录下创建API文件
2. 使用 `request` 工具发送HTTP请求
3. 在组件中导入并使用API函数

### 样式开发
- 使用Sass预处理器
- 遵循BEM命名规范
- 使用CSS变量定义颜色和尺寸
- 响应式设计，支持移动端

## 部署说明

### 开发环境
```bash
npm run dev
```

### 生产环境
```bash
npm run build
```

构建后的文件在 `dist/` 目录中，可以部署到任何静态文件服务器。

## 注意事项

1. 确保后端API服务正常运行
2. 检查API接口地址配置
3. 确保用户角色权限正确设置
4. 定期更新依赖包版本

## 联系方式

如有问题或建议，请联系开发团队。