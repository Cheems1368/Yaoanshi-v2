import { httpService } from '@/shared/http/client'

export interface TokenPair {
  accessToken: string
  refreshToken: string
  expiresIn: number
  refreshExpiresIn: number
  deviceId: string
}

export interface BindPatientRequest {
  mobile: string
  hospitalPatientId: string
  name: string
}

export interface FilePresignResponse {
  objectKey: string
  uploadUrl: string
  downloadUrl: string
  bucket: string
  filename: string
  contentType: string
  sizeBytes: number
}

export interface PrescriptionAttachmentDTO {
  id: number
  fileAssetId: number
  attachmentType: string
  originalFilename: string
  mimeType: string
  sizeBytes: number
  objectKey: string
  downloadUrl: string
}

export interface PrescriptionDTO {
  id: number
  patientId: number
  patientName: string
  hospitalPatientId: string
  status: string
  sourceType: string
  submittedByType: string
  submittedById: number
  reviewedByPharmacistId?: number | null
  reviewedAt?: string | null
  createdAt: string
  rejectReason?: string | null
  ocrStatus?: string | null
  ocrConfidence?: string | null
  attachments: PrescriptionAttachmentDTO[]
}

export interface MedicationPlanDTO {
  id: number
  patientId: number
  patientName?: string | null
  planName: string
  versionNo: number
  status: string
  sourceType: string
  sourcePrescriptionId?: number | null
  effectiveFrom?: string | null
  effectiveTo?: string | null
  notes?: string | null
  createdByPharmacistId?: number | null
  confirmedAt?: string | null
  createdAt?: string | null
}

export interface DoseTaskDTO {
  id: number
  patientId: number
  planId: number
  planItemId: number
  taskDate?: string | null
  scheduledAt?: string | null
  status: string
  notificationStatus?: string | null
  lastEffectiveCheckinId?: number | null
  lockedAfter?: string | null
  drugName: string
  dosage: string
  frequencyCode: string
  route?: string | null
  mealRelation?: string | null
  instructions?: string | null
}

export interface CheckinRecordDTO {
  id: number
  doseTaskId: number
  patientId: number
  operatorType: string
  operatorId: number
  result: string
  actualTakenAt?: string | null
  note?: string | null
  skipReason?: string | null
  correction: boolean
  supersedesRecordId?: number | null
  effective: boolean
  createdAt?: string | null
}

export interface HelpOrderDTO {
  id: number
  orderNo: string
  patientId: number
  patientName?: string | null
  submittedByType: string
  submittedById: number
  category: string
  title: string
  description: string
  status: string
  priority: string
  requiresOfflineEmergency?: boolean
  currentHandlerType?: string | null
  currentHandlerId?: number | null
  currentHandlerName?: string | null
  responsiblePharmacistId?: number | null
  assignedDoctorId?: number | null
  assignedDoctorName?: string | null
  firstRespondedAt?: string | null
  closedAt?: string | null
  rejectReason?: string | null
  serviceNotice?: string | null
  createdAt?: string | null
}

export interface PageResult<T> {
  pageNo: number
  pageSize: number
  total: number
  list: T[]
}

export const appAuthService = {
  sendOtp(mobile: string, scene = 'APP_LOGIN') {
    return httpService.post<boolean>('/app-api/auth/send-otp', { mobile, scene })
  },

  login(payload: { mobile: string; otpCode: string; deviceId: string; deviceName?: string; platform?: string }) {
    return httpService.post<TokenPair>('/app-api/auth/login', payload)
  },

  refreshToken(refreshToken: string, deviceId: string) {
    return httpService.post<TokenPair>('/app-api/auth/refresh-token', { refreshToken, deviceId })
  },

  logout(deviceId: string) {
    return httpService.post<boolean>('/app-api/auth/logout', { deviceId })
  },

  getCurrentAccount() {
    return httpService.get('/app-api/auth/me')
  }
}

export const patientService = {
  bindPatient(payload: BindPatientRequest) {
    return httpService.post('/app-api/patients/bind', payload)
  },

  getCurrentPatient() {
    return httpService.get('/app-api/patients/me')
  },

  getFamilyBindings() {
    return httpService.get('/app-api/patients/me/family-bindings')
  },

  createFamilyBinding(payload: { familyMobile: string; familyDisplayName?: string; relationshipType?: string }) {
    return httpService.post('/app-api/patients/me/family-bindings', payload)
  },

  deleteFamilyBinding(id: number) {
    return httpService.delete(`/app-api/patients/me/family-bindings/${id}`)
  }
}

export const medicationService = {
  getActiveMedicationPlan() {
    return httpService.get<MedicationPlanDTO | null>('/app-api/medication-plans/active')
  },

  getTodayDoseTasks(date?: string) {
    return httpService.get<DoseTaskDTO[]>('/app-api/dose-tasks/today', date ? { date } : undefined)
  },

  getCheckins(params?: Record<string, unknown>) {
    return httpService.get<PageResult<CheckinRecordDTO>>('/app-api/checkins', params)
  },

  submitCheckin(payload: {
    doseTaskId: number
    result: string
    actualTakenAt?: string
    note?: string
    skipReason?: string
  }) {
    return httpService.post<CheckinRecordDTO>('/app-api/checkins', payload)
  },

  correctCheckin(
    checkinId: number,
    payload: {
      result: string
      actualTakenAt?: string
      note?: string
      skipReason?: string
    }
  ) {
    return httpService.put<CheckinRecordDTO>(`/app-api/checkins/${checkinId}/correct`, payload)
  }
}

export const helpOrderService = {
  getHelpOrders(params?: Record<string, unknown>) {
    return httpService.get<PageResult<HelpOrderDTO>>('/app-api/help-orders', params)
  },

  getHelpOrderDetail(id: number) {
    return httpService.get<HelpOrderDTO>(`/app-api/help-orders/${id}`)
  },

  createHelpOrder(payload: Record<string, unknown>) {
    return httpService.post<HelpOrderDTO>('/app-api/help-orders', payload)
  }
}

export const assessmentService = {
  submitKnowledge(payload: Record<string, unknown>) {
    return httpService.post('/app-api/assessments/knowledge', payload)
  },

  submitSatisfaction(payload: Record<string, unknown>) {
    return httpService.post('/app-api/assessments/satisfaction', payload)
  }
}

export const prescriptionService = {
  presignUpload(payload: { filename: string; contentType: string; sizeBytes: number; businessType: 'PRESCRIPTION' }) {
    return httpService.post<FilePresignResponse>('/app-api/files/presign', payload)
  },

  registerUpload(payload: {
    objectKey: string
    originalFilename: string
    contentType: string
    sizeBytes: number
    sha256?: string
  }) {
    return httpService.post<PrescriptionDTO>('/app-api/prescriptions/uploads', payload)
  }
}
