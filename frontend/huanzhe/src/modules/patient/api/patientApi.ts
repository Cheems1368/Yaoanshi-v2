import { httpService, ApiResponse } from '@/shared/http/client'
import { API_ENDPOINTS } from '@/config/api'

// 患者信息接口
export interface PatientInfo {
  id?: number
  name: string
  realName?: string
  phone?: string
  mobile?: string
  age?: number
  gender?: string
  idCard?: string
  idType?: string
  ethnicity?: string
  height?: number
  weight?: number
  bloodType?: string
  allergies?: string
  avatar?: string
  emergencyContact?: {
    name: string
    phone: string
    relationship: string
  }
  isRealNameVerified?: boolean
  realNameVerifiedTime?: string
  createdAt?: string
  updatedAt?: string
  userId?: number
  address?: string
  insuranceCardNumber?: string
  allergyHistory?: string
  medicalHistory?: string
  familyHistory?: string
  personalHistory?: string
  patientType?: number
  insurance?: number
  birthday?: string
}

export interface PatientCreatePayload extends Partial<PatientInfo> {
  userId?: number
  address: string
  patientType: number
  insurance: number
  birthday: string
  insuranceCardNumber?: string
  allergyHistory?: string
  medicalHistory?: string
  familyHistory?: string
  personalHistory?: string
}

// 工单信息接口
export interface HelpOrder {
  id?: number
  patientId: number
  assignedPharmacistId?: number
  assignedDoctorId?: number
  title: string
  description: string
  intendedDrugs?: string[]
  attachments?: string[]
  toDoctor?: boolean
  status: string
  createdAt?: string
  updatedAt?: string
  timeline?: TimelineEvent[]
}

// 时间线事件接口
export interface TimelineEvent {
  at: string
  event: string
  by: number
  to?: number
}

// 处方信息接口
export interface Prescription {
  id?: number
  patientId: number
  doctorId: number
  prescriptionNumber: string
  diagnosis: string
  medications: PrescriptionMedication[]
  totalAmount: number
  status: string
  createdAt?: string
  updatedAt?: string
}

// 处方药品接口
export interface PrescriptionMedication {
  id?: number
  drugName: string
  specification: string
  quantity: number
  unit: string
  usage: string
  frequency: string
  duration: string
  notes?: string
}

// 分页查询参数
export interface PageQuery {
  pageNo: number
  pageSize: number
  [key: string]: any
}

// 分页响应数据
export interface PageResponse<T> {
  list: T[]
  total: number
}

// 患者 API 服务类
class PatientApiService {

  // ========== 认证相关 ==========

  // 患者登录
  async login(username: string, password: string): Promise<ApiResponse<{
    accessToken: string
    refreshToken: string
    expiresTime: number
    user: any
  }>> {
    return httpService.post(API_ENDPOINTS.AUTH.LOGIN, {
      username,
      password,
      clientId: 'patient-app'
    })
  }

  // 获取用户信息
  async getUserInfo(): Promise<ApiResponse<any>> {
    return httpService.get(API_ENDPOINTS.AUTH.USER_INFO)
  }

  // 退出登录
  async logout(): Promise<ApiResponse<void>> {
    return httpService.post(API_ENDPOINTS.AUTH.LOGOUT)
  }

  // ========== 患者信息管理 ==========

  // 获取患者信息
  async getPatientInfo(patientId?: number): Promise<ApiResponse<PatientInfo>> {
    const params = patientId ? { patientId } : undefined
    return httpService.get(API_ENDPOINTS.APP.PATIENT.PROFILE, params)
  }

  // 更新患者信息
  async updatePatientInfo(patientInfo: Partial<PatientInfo>): Promise<ApiResponse<void>> {
    return httpService.put(API_ENDPOINTS.PATIENT.UPDATE, patientInfo)
  }

  // 创建患者信息，使用患者端专用接口
  async createPatientInfo(patientInfo: PatientCreatePayload): Promise<ApiResponse<number>> {
    // 使用 /app/patient/profile 接口，而不是管理后台的 /admin-api/patient/list/create
    return httpService.post(API_ENDPOINTS.APP.PATIENT.PROFILE, patientInfo)
  }

  // ========== 工单管理 ==========

  // 创建工单
  async createHelpOrder(helpOrder: Omit<HelpOrder, 'id' | 'status' | 'createdAt' | 'updatedAt'>): Promise<ApiResponse<HelpOrder>> {
    return httpService.post(API_ENDPOINTS.APP.HELP_ORDER.CREATE, helpOrder)
  }

  // 获取工单列表
  async getHelpOrderList(query: PageQuery): Promise<ApiResponse<PageResponse<HelpOrder>>> {
    return httpService.get(API_ENDPOINTS.HELP_ORDER.PAGE, query)
  }

  // 获取工单详情
  async getHelpOrderDetail(helpOrderId: number): Promise<ApiResponse<HelpOrder>> {
    return httpService.get(`${API_ENDPOINTS.HELP_ORDER.GET}?id=${helpOrderId}`)
  }

  // 更新工单状态
  async updateHelpOrderStatus(helpOrderId: number, status: string, reason?: string): Promise<ApiResponse<void>> {
    return httpService.put(`${API_ENDPOINTS.HELP_ORDER.UPDATE}?id=${helpOrderId}`, {
      status,
      reason
    })
  }

  // ========== 处方管理 ==========

  // 获取处方列表，使用 App 端接口
  async getPrescriptionList(query: PageQuery): Promise<ApiResponse<PageResponse<Prescription>>> {
    // 使用 App 端接口获取患者处方
    const patientId = query.patientId || 1
    const limit = query.pageSize || 10
    return httpService.get(`/app/prescription/patient/${patientId}?limit=${limit}`)
  }

  // 获取处方详情，使用 App 端接口
  async getPrescriptionDetail(prescriptionId: number): Promise<ApiResponse<Prescription>> {
    // 这里需要根据实际情况调整，可能需要通过处方编号获取
    return httpService.get(`/app/prescription/code/${prescriptionId}`)
  }

  // ========== 文件上传 ==========

  // 上传文件
  async uploadFile(file: File, onProgress?: (progress: number) => void): Promise<ApiResponse<{
    url: string
    filename: string
    size: number
  }>> {
    return httpService.upload(API_ENDPOINTS.FILE.UPLOAD, file, onProgress)
  }

  // ========== 健康数据 ==========

  // 提交健康数据
  async submitHealthData(healthData: any): Promise<ApiResponse<void>> {
    // 这里可以根据实际 API 接口调整
    return httpService.post('/admin-api/patient/health-data', healthData)
  }

  // 获取健康数据
  async getHealthData(patientId: number): Promise<ApiResponse<any>> {
    return httpService.get(`/admin-api/patient/health-data?patientId=${patientId}`)
  }

  // ========== App 端专用接口 ==========

  // 创建血压记录
  async createBloodPressure(data: {
    systolic: number;
    diastolic: number;
    heartRate?: number;
    recordTime?: string;
    notes?: string;
  }): Promise<ApiResponse<any>> {
    return httpService.post('/app/blood-pressure/create', data)
  }

  // 获取最近血压记录
  async getRecentBloodPressure(patientId: number, limit: number = 10): Promise<ApiResponse<any>> {
    return httpService.get(`/app/blood-pressure/recent?patientId=${patientId}&limit=${limit}`)
  }

  // 获取血压趋势
  async getBloodPressureTrend(patientId: number, days: number = 30): Promise<ApiResponse<any>> {
    return httpService.get(`/app/blood-pressure/trend?patientId=${patientId}&days=${days}`)
  }

  // 发送聊天消息
  async sendChatMessage(data: {
    orderId: number;
    content: string;
    messageType?: string;
  }, senderId: number, senderType: string): Promise<ApiResponse<any>> {
    return httpService.post('/app/chat/send', data, {
      params: { senderId, senderType }
    })
  }

  // 获取聊天历史
  async getChatHistory(orderId: number): Promise<ApiResponse<any>> {
    return httpService.get(`/app/chat/history/${orderId}`)
  }

  // 获取未读消息数量
  async getUnreadMessageCount(userId: number): Promise<ApiResponse<any>> {
    return httpService.get(`/app/chat/unread-count?userId=${userId}`)
  }

  // 标记消息为已读
  async markMessagesAsRead(userId: number, orderId: number): Promise<ApiResponse<any>> {
    return httpService.post('/app/chat/mark-read', null, {
      params: { userId, orderId }
    })
  }
}

// 创建 API 服务实例
export const patientApiService = new PatientApiService()
