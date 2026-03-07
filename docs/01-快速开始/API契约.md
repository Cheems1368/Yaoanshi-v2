# API契约 - 用药打卡系统

## 📋 资源目录冻结

### 核心资源
- **Auth**: 认证授权相关接口
- **HelpOrders**: 工单管理接口
- **Advices**: 药师建议接口
- **Prescriptions**: 处方管理接口
- **Profiles**: 患者档案接口
- **Medication-Plans**: 用药计划接口
- **Todos**: 今日待办接口
- **Checkins**: 打卡记录接口
- **Reports**: 报告生成接口
- **Notifications**: 通知提醒接口
- **Quality-Gate**: 质量门禁相关接口

## 🔐 匿名白名单

### 允许匿名访问的接口
```yaml
# 认证相关
- /admin-api/system/auth/login
- /admin-api/system/auth/logout
- /admin-api/system/auth/refresh-token

# 验证码相关
- /admin-api/system/captcha/get
- /admin-api/system/captcha/check

# 健康检查
- /actuator/health
- /actuator/info

# 静态资源
- /static/**
- /css/**
- /js/**
- /images/**

# 文档相关
- /swagger-ui/**
- /v3/api-docs/**
- /doc.html

# 工单测试接口（仅开发环境）
- /admin-api/system/help-order-test/**
- /admin-api/system/help-order/simple/**
```

### 需要认证的接口
- 除上述白名单外的所有接口均需要JWT认证
- 需要角色权限的接口需要相应的角色守卫

## 📊 统一响应格式

### 成功响应
```json
{
  "code": 0,
  "data": {
    // 具体数据
  },
  "msg": "成功"
}
```

### 错误响应
```json
{
  "code": 400,
  "data": null,
  "msg": "请求参数不正确"
}
```

### 错误码分类
- **0**: 成功
- **400-499**: 客户端错误（参数、权限等）
- **500-599**: 服务端错误（系统异常等）
- **900-999**: 自定义业务错误

## 🏥 用药打卡核心API

### 1. 用药计划管理

#### 创建用药计划
```http
POST /admin-api/medication/med-plan/create
Content-Type: application/json
Authorization: Bearer {token}

{
  "patientId": 1,
  "planName": "高血压用药计划",
  "startDate": "2025-01-06",
  "endDate": "2025-04-06",
  "description": "高血压患者用药计划",
  "instructions": "按时服药，注意监测血压",
  "items": [
    {
      "drugName": "氨氯地平",
      "dosage": "5mg",
      "frequency": "每日一次",
      "timePoints": ["08:00"],
      "mealRelation": 1,
      "instructions": "餐前服用",
      "sortOrder": 1
    }
  ]
}
```

#### 获取用药计划
```http
GET /admin-api/medication/med-plan/get?id=1
Authorization: Bearer {token}
```

#### 更新用药计划
```http
PUT /admin-api/medication/med-plan/update
Content-Type: application/json
Authorization: Bearer {token}

{
  "id": 1,
  "planName": "高血压用药计划(更新)",
  "description": "更新后的用药计划",
  "items": [...]
}
```

#### 计划状态操作
```http
POST /admin-api/medication/med-plan/pause?id=1
POST /admin-api/medication/med-plan/resume?id=1
POST /admin-api/medication/med-plan/terminate?id=1
Authorization: Bearer {token}
```

### 2. 今日待办管理

#### 获取今日待办
```http
GET /admin-api/medication/today-task/page?patientId=1&taskDate=2025-01-06
Authorization: Bearer {token}
```

#### 完成待办任务
```http
POST /admin-api/medication/today-task/complete
Content-Type: application/json
Authorization: Bearer {token}

{
  "id": 1,
  "completedTime": "2025-01-06T08:05:00",
  "notes": "按时服药"
}
```

#### 跳过待办任务
```http
POST /admin-api/medication/today-task/skip
Content-Type: application/json
Authorization: Bearer {token}

{
  "id": 1,
  "notes": "忘记服药"
}
```

### 3. 打卡记录管理

#### 提交打卡
```http
POST /admin-api/medication/checkin/submit
Content-Type: application/json
Authorization: Bearer {token}

{
  "todoId": 1,
  "checkinTime": "2025-01-06T08:05:00",
  "status": "COMPLETED",
  "notes": "按时服药",
  "evidence": {
    "images": ["image1.jpg", "image2.jpg"],
    "audio": "audio1.mp3"
  },
  "clientNonce": "unique-client-nonce"
}
```

#### 撤销打卡
```http
POST /admin-api/medication/checkin/revoke
Content-Type: application/json
Authorization: Bearer {token}

{
  "checkinId": 1,
  "reason": "误操作",
  "notes": "撤销原因说明"
}
```

#### 获取打卡记录
```http
GET /admin-api/medication/checkin/page?patientId=1&startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer {token}
```

### 4. 依从统计

#### 获取依从统计
```http
GET /admin-api/medication/adherence/stats?patientId=1&period=30
Authorization: Bearer {token}
```

响应示例：
```json
{
  "code": 0,
  "data": {
    "patientId": 1,
    "period": 30,
    "totalTasks": 90,
    "completedTasks": 85,
    "adherenceRate": 94.4,
    "missedCount": 5,
    "delayedCount": 3,
    "topMedications": [
      {
        "drugName": "氨氯地平",
        "completionRate": 96.7
      }
    ],
    "statisticsDate": "2025-01-06"
  },
  "msg": "成功"
}
```

### 5. 报告生成

#### 生成报告
```http
POST /admin-api/medication/report/generate
Content-Type: application/json
Authorization: Bearer {token}

{
  "patientId": 1,
  "reportType": "QUARTERLY",
  "startDate": "2025-01-01",
  "endDate": "2025-03-31"
}
```

#### 获取报告
```http
GET /admin-api/medication/report/get?id=1
Authorization: Bearer {token}
```

#### 导出报告
```http
GET /admin-api/medication/report/export?id=1&format=PDF
Authorization: Bearer {token}
```

### 6. 通知提醒

#### 获取通知列表
```http
GET /admin-api/medication/notification/page?patientId=1&unreadOnly=true
Authorization: Bearer {token}
```

#### 标记通知已读
```http
POST /admin-api/medication/notification/mark-read
Content-Type: application/json
Authorization: Bearer {token}

{
  "notificationIds": [1, 2, 3]
}
```

## 🔒 角色权限

### 患者角色 (PATIENT)
- 查看自己的用药计划
- 查看自己的今日待办
- 提交打卡记录
- 查看自己的依从统计
- 查看自己的报告

### 药师角色 (PHARMACIST)
- 查看分配给自己的患者数据
- 创建用药计划
- 修改用药计划
- 查看患者依从统计
- 发送用药提醒

### 医生角色 (DOCTOR)
- 查看所有患者数据
- 审核用药计划
- 签发处方
- 查看季度报告
- 修改患者档案

### 管理员角色 (ADMIN)
- 所有权限
- 用户管理
- 角色权限管理
- 系统配置

## 📝 审计日志点

### 关键操作审计
- 用户登录/登出
- 用药计划创建/修改/删除
- 打卡提交/撤销
- 报告生成/导出
- 角色权限变更
- 系统配置修改

### 审计日志格式
```json
{
  "traceId": "trace-123456",
  "userId": 1,
  "userType": 1,
  "module": "medication",
  "name": "创建用药计划",
  "type": 1,
  "content": "为患者ID:1创建高血压用药计划",
  "requestMethod": "POST",
  "requestUrl": "/admin-api/medication/med-plan/create",
  "userIp": "192.168.1.100",
  "startTime": "2025-01-06T10:00:00",
  "duration": 150,
  "resultCode": 0,
  "resultMsg": "成功"
}
```

## 🚀 Mock数据

### 用药计划Mock
```json
{
  "patientId": 1,
  "planName": "高血压用药计划",
  "startDate": "2025-01-06",
  "endDate": "2025-04-06",
  "description": "高血压患者用药计划",
  "instructions": "按时服药，注意监测血压",
  "items": [
    {
      "drugName": "氨氯地平",
      "dosage": "5mg",
      "frequency": "每日一次",
      "timePoints": ["08:00"],
      "mealRelation": 1,
      "instructions": "餐前服用",
      "sortOrder": 1
    },
    {
      "drugName": "美托洛尔",
      "dosage": "25mg",
      "frequency": "每日两次",
      "timePoints": ["08:00", "20:00"],
      "mealRelation": 1,
      "instructions": "餐前服用",
      "sortOrder": 2
    }
  ]
}
```

### 今日待办Mock
```json
{
  "patientId": 1,
  "taskDate": "2025-01-06",
  "tasks": [
    {
      "id": 1,
      "planItemId": 1,
      "drugName": "氨氯地平",
      "dosage": "5mg",
      "scheduledTime": "08:00",
      "status": "PENDING",
      "mealRelation": "餐前",
      "instructions": "餐前服用"
    },
    {
      "id": 2,
      "planItemId": 2,
      "drugName": "美托洛尔",
      "dosage": "25mg",
      "scheduledTime": "08:00",
      "status": "COMPLETED",
      "completedTime": "2025-01-06T08:05:00",
      "mealRelation": "餐前",
      "instructions": "餐前服用"
    }
  ]
}
```

## 🔧 质量门禁接口

### 质量检查接口
```http
# 静态代码检查
GET /admin-api/quality-gate/checkstyle/check
GET /admin-api/quality-gate/spotbugs/check
GET /admin-api/quality-gate/pmd/check

# 测试覆盖率检查
GET /admin-api/quality-gate/coverage/check
GET /admin-api/quality-gate/coverage/report

# 安全扫描
GET /admin-api/quality-gate/security/scan
GET /admin-api/quality-gate/dependency/check

# 质量门禁状态
GET /admin-api/quality-gate/status
GET /admin-api/quality-gate/report
```

### 质量门禁响应格式
```json
{
  "code": 200,
  "msg": "质量检查完成",
  "data": {
    "checkstyle": {
      "status": "PASS",
      "violations": 0,
      "details": "代码风格检查通过"
    },
    "spotbugs": {
      "status": "PASS", 
      "bugs": 0,
      "details": "静态分析检查通过"
    },
    "coverage": {
      "status": "PASS",
      "percentage": 85.5,
      "threshold": 80.0,
      "details": "测试覆盖率达标"
    },
    "security": {
      "status": "PASS",
      "vulnerabilities": 0,
      "details": "安全扫描通过"
    },
    "overall": {
      "status": "PASS",
      "timestamp": "2025-01-05T15:30:00",
      "details": "所有质量门禁检查通过"
    }
  }
}
```

## ✅ 验收标准

### 契约冻结验收
- [x] 所有API接口文档完整
- [x] 请求/响应格式统一
- [x] 错误码分类清晰
- [x] Mock数据可用

### 安全基线验收
- [x] 匿名白名单生效
- [x] JWT认证正常
- [x] 角色权限控制
- [x] 审计日志记录

### 环境配置验收
- [x] 四套Profile配置完整
- [x] 密钥环境变量化
- [x] 数据库连接正常
- [x] Redis连接正常

### 质量门禁验收
- [x] 静态代码检查配置完成
- [x] 测试覆盖率检查配置完成
- [x] 安全扫描配置完成
- [x] CI/CD集成配置完成
