# 药按时 - 患者端应用

## 项目简介
这是药学服务平台"药按时"的患者端应用，基于 Ionic + Vue3 + Capacitor 开发。

## 技术栈
- **前端框架**: Vue 3
- **UI框架**: Ionic 8
- **构建工具**: Vite
- **移动端**: Capacitor
- **包管理器**: npm

## 项目结构
`
src/
 views/
    patient/           # 患者相关页面
       HelpOrderPage.vue    # 用药求助页面
       PrescriptionPage.vue # 我的处方页面
       ProfilePage.vue      # 个人资料页面
    TabsPage.vue       # 底部导航页面
 router/                # 路由配置
 components/           # 公共组件
 theme/                # 主题样式
`

## 环境配置
复制 .env.example 为 .env 并修改配置：
`
VITE_API_BASE=http://localhost:3000/v1
VITE_APP_NAME=药按时
VITE_APP_VERSION=1.0.0
VITE_DEBUG=true
`

## 启动项目

### 安装依赖
`ash
npm install
`

### 开发环境
`ash
npm run dev
`
访问: http://localhost:5173

### 构建生产版本
`ash
npm run build
`

### 移动端开发
`ash
# 添加平台
npx cap add android
npx cap add ios

# 同步到原生项目
npx cap sync

# 打开原生IDE
npx cap open android
npx cap open ios
`

## 功能特性
-  响应式设计，支持移动端
-  Material Design 风格
-  底部Tab导航
-  用药求助工单提交
-  处方记录查看
-  个人资料管理

## API接口
- 基础地址: http://localhost:3000/v1
- 患者端接口: /v1/patient/**
- 认证接口: /auth/**

## 开发说明
1. 项目使用 TypeScript 开发
2. 遵循 Vue 3 Composition API 规范
3. 使用 Ionic 组件库构建UI
4. 支持热重载开发
