// API配置
export const API_CONFIG = {
  // 基础配置
  // 注意：患者端使用两种接口：
  // - /admin-api/* 走 48080 (管理后台服务)
  // - /app/* 走 48081 (患者端服务)
  // 实际请求通过 vite 代理转发，这里保留默认值用于直接请求场景
  BASE_URL: 'http://localhost:48081',
  API_PREFIX: '/admin-api',
  
  // 患者端专用API路径
  PATIENT_API: '/admin-api/patient',
  HELP_ORDER_API: '/admin-api/help-order', 
  PRESCRIPTION_API: '/admin-api/prescription',
  AUTH_API: '/admin-api/system/auth',
  
  // 文件上传
  UPLOAD_URL: '/admin-api/infra/file/upload',
  
  // 请求超时时间
  TIMEOUT: 10000,
  
  // 请求重试次数
  RETRY_COUNT: 3
}

// API端点定义
export const API_ENDPOINTS = {
  // 认证相关
  AUTH: {
    LOGIN: '/admin-api/system/auth/login',
    LOGOUT: '/admin-api/system/auth/logout',
    REFRESH_TOKEN: '/admin-api/system/auth/refresh-token',
    USER_INFO: '/admin-api/system/auth/get-permission-info'
  },
  
  // 患者相关 - 使用实际存在的接口
  PATIENT: {
    LIST: '/admin-api/patient/list/page',
    CREATE: '/admin-api/patient/list/create',
    GET: '/admin-api/patient/list/get',
    UPDATE: '/admin-api/patient/list/update',
    DELETE: '/admin-api/patient/list/delete',
    EXPORT: '/admin-api/patient/list/export-excel'
  },
  
  // 工单相关 - 管理后台接口
  HELP_ORDER: {
    CREATE: '/system/help-order-test/create',
    PAGE: '/system/help-order-test/page',
    GET: '/system/help-order-test/get',
    UPDATE: '/system/help-order-test/update',
    DELETE: '/system/help-order-test/delete',
    ASSIGN: '/system/help-order-test/assign',
    ESCALATE: '/system/help-order-test/escalate',
    REJECT: '/system/help-order-test/reject',
    EXPORT: '/system/help-order-test/export'
  },
  
  // 处方相关 - 使用实际存在的接口
  PRESCRIPTION: {
    PAGE: '/admin-api/prescription/prescriptions/page',
    CREATE: '/admin-api/prescription/prescriptions/create',
    GET: '/admin-api/prescription/prescriptions/get',
    UPDATE: '/admin-api/prescription/prescriptions/update',
    DELETE: '/admin-api/prescription/prescriptions/delete'
  },
  
  // App端接口 - 患者端专用
  APP: {
    // 患者档案
    PATIENT: {
      PROFILE: '/app/patient/profile'
    },

    // 用药求助工单
    HELP_ORDER: {
      CREATE: '/app/help-order/create',
      PAGE: '/app/help-order/page',
      GET: '/app/help-order/get'
    },

    // 电子处方
    PRESCRIPTION: {
      CREATE: '/app/prescription/create',
      GET_BY_CODE: '/app/prescription/code/{prescriptionCode}',
      GET_BY_ORDER: '/app/prescription/order/{orderId}',
      GET_PATIENT: '/app/prescription/patient/{patientId}',
      CONFIRM: '/app/prescription/confirm/{prescriptionId}'
    },
    
    // 聊天
    CHAT: {
      SEND: '/app/chat/send',
      HISTORY: '/app/chat/history/{orderId}',
      UNREAD_COUNT: '/app/chat/unread-count',
      MARK_READ: '/app/chat/mark-read',
      RECALL: '/app/chat/recall/{messageId}'
    },
    
    // 血压记录
    BLOOD_PRESSURE: {
      CREATE: '/app/blood-pressure/create',
      RECENT: '/app/blood-pressure/recent',
      TREND: '/app/blood-pressure/trend'
    }
  },
  
  // 文件上传
  FILE: {
    UPLOAD: '/admin-api/infra/file/upload'
  }
}

// 响应状态码
export const RESPONSE_CODES = {
  SUCCESS: 0,
  ERROR: 1,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500
}

// 工单状态
export const HELP_ORDER_STATUS = {
  SUBMITTED: 'SUBMITTED',
  PHARMACIST_REVIEW: 'PHARMACIST_REVIEW', 
  TO_DOCTOR: 'TO_DOCTOR',
  DOCTOR_REVIEW: 'DOCTOR_REVIEW',
  DONE: 'DONE',
  REJECTED: 'REJECTED'
}

// 工单状态显示文本
export const HELP_ORDER_STATUS_TEXT = {
  SUBMITTED: '已提交',
  PHARMACIST_REVIEW: '药师审核中',
  TO_DOCTOR: '转医生处理',
  DOCTOR_REVIEW: '医生审核中', 
  DONE: '已完成',
  REJECTED: '已驳回'
}
