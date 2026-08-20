import { httpService } from '@/shared/http/client'

export interface SessionTokenDTO {
  accessToken: string
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

export interface FileAssetDTO {
  id: number
  objectKey: string
  originalFilename: string
  contentType: string
  sizeBytes: number
  businessType: string
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
  ocrRawText?: string | null
  ocrFailureReason?: string | null
  attachments: PrescriptionAttachmentDTO[]
  items?: PrescriptionItemDTO[] | null
}

export interface PrescriptionItemDTO {
  id: number
  drugName: string
  drugSpec?: string | null
  dosage?: string | null
  frequency?: string | null
  route?: string | null
  durationDays?: number | null
  source?: string | null
}

export interface PlanItemSummaryDTO {
  drugName: string
  dosage?: string | null
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
  items?: PlanItemSummaryDTO[] | null
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
  reminderStage?: 'UPCOMING' | 'DUE' | 'FAMILY_REMINDER_DUE' | 'OVERDUE' | 'HANDLED' | string | null
  overdueMinutes?: number | null
  familyReminderDue?: boolean | null
  familyBindingCount?: number | null
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
  description?: string | null
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
  attachmentFileAssetIds?: number[]
  voiceAttachment?: HelpOrderVoiceAttachmentPayload
  events?: HelpOrderEventDTO[]
}

export interface HelpOrderVoiceTranscriptDTO {
  status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'UNCONFIGURED' | string
  text?: string | null
  provider?: string | null
  errorCode?: string | null
  transcribedAt?: string | null
}

export interface HelpOrderVoiceAttachmentPayload {
  playbackFileAssetId: number
  transcriptSourceFileAssetId: number
  durationSeconds: number
}

export interface HelpOrderAttachmentDTO {
  id?: number | null
  fileAssetId: number
  attachmentType: string
  originalFilename: string
  mimeType: string
  sizeBytes: number
  downloadUrl: string
  durationSeconds?: number | null
  voiceTranscript?: HelpOrderVoiceTranscriptDTO | null
}

export interface HelpOrderEventDTO {
  id: number
  eventType: string
  fromStatus?: string | null
  toStatus?: string | null
  operatorType: string
  operatorId: number
  operatorName?: string | null
  content?: string | null
  attachmentFileAssetId?: number | null
  attachments?: HelpOrderAttachmentDTO[]
  eventAt?: string | null
}

export interface AppDdiAlert {
  type: 'INTERACTION' | 'CONTRAINDICATION' | string
  severity: 'HIGH' | 'MEDIUM' | string
  drugA: string
  drugB: string
  message: string
  patientSummary?: string | null
  recommendedAction?: string | null
}

export interface SelfReportedMedicationDTO {
  id: number
  patientId: number
  drugName: string
  dosage?: string | null
  status: 'PENDING' | 'ACTIVE' | 'STOPPED' | 'TRANSFERRED' | string
  notes?: string | null
  operatorType: string
  operatorAccountId: number
  workOrderId?: number | null
  transferredPlanItemId?: number | null
  createdAt?: string | null
}

export interface SelfReportedDrugEntry {
  drugName: string
  dosage?: string
}

export interface SelfReportedMedicationCreateRequest {
  drugs: SelfReportedDrugEntry[]
  notes?: string
}

export interface AppDdiScreenResult {
  alerts: AppDdiAlert[]
}

export interface PageResult<T> {
  pageNo: number
  pageSize: number
  total: number
  list: T[]
}

export interface EducationContentCardDTO {
  id: number
  contentCode: string
  title: string
  summary?: string | null
  body?: string | null
  category?: string | null
  categoryName?: string | null
  diagnosisTags?: string[] | null
  drugTags?: string[] | null
  populationTags?: string[] | null
  source?: string | null
  reviewStatus?: string | null
  reviewedBy?: string | null
  reviewedAt?: string | null
  updatedAt?: string | null
}

export interface PatientProfileDTO {
  patientId?: number
  hospitalPatientId?: string
  name?: string
  phone?: string
  age?: number
  gender?: string
  idType?: string
  ethnicity?: string
  height?: number
  weight?: number
  bloodType?: string
  allergies?: string
  avatar?: string
  address?: string
  patientType?: number
  insurance?: number
  identityMaterialArchived?: boolean
  realNameVerified?: boolean
  realNameVerifiedTime?: string
  createdAt?: string
  updatedAt?: string
  operatorType?: string
  emergencyContact?: {
    name?: string
    phone?: string
    relationship?: string
  }
}

export interface PatientHealthProfileDTO {
  patientId?: number
  submitTime?: string
  layer1?: {
    allergy?: string
    diagnosis?: string
  }
  layer2?: {
    medicalHistory?: string
    smokingDrinking?: string
    liverKidney?: string
    currentMedication?: string
  }
  layer3?: {
    detailedSymptoms?: string
    familyHistory?: string
    surgeryHistory?: string
    socialHistory?: string
  }
}

export const appAuthService = {
  sendOtp(mobile: string, scene = 'APP_LOGIN') {
    return httpService.post<boolean>('/app-api/auth/send-otp', { mobile, scene })
  },

  login(payload: { mobile: string; otpCode: string; deviceId: string; deviceName?: string; platform?: string }) {
    return httpService.post<SessionTokenDTO>('/app-api/auth/session/login', payload)
  },

  logout() {
    return httpService.post<boolean>('/app-api/auth/session/logout', {}, {
      headers: { 'X-Auth-Refresh': '1' },
      withCredentials: true
    })
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
  },

  getProfile() {
    return httpService.get<PatientProfileDTO>('/app-api/patients/me/profile')
  },

  updateProfile(payload: Partial<PatientProfileDTO>) {
    return httpService.put<PatientProfileDTO>('/app-api/patients/me/profile', payload)
  },

  getHealthProfile() {
    return httpService.get<PatientHealthProfileDTO>('/app-api/patients/me/health-profile')
  },

  updateHealthProfile(payload: Partial<PatientHealthProfileDTO>) {
    return httpService.put<PatientHealthProfileDTO>('/app-api/patients/me/health-profile', payload)
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
  },

  replyHelpOrder(
    id: number,
    payload: { content?: string; attachmentFileAssetIds?: number[]; voiceAttachment?: HelpOrderVoiceAttachmentPayload }
  ) {
    return httpService.post<HelpOrderDTO>(`/app-api/help-orders/${id}/reply`, payload)
  },

  presignImage(payload: { filename: string; contentType: string; sizeBytes: number; businessType: 'HELP_ORDER_IMAGE' }) {
    return httpService.post<FilePresignResponse>('/app-api/files/presign', payload)
  },

  registerImage(payload: {
    objectKey: string
    originalFilename: string
    contentType: string
    sizeBytes: number
    sha256?: string
    businessType: 'HELP_ORDER_IMAGE'
  }) {
    return httpService.post<FileAssetDTO>('/app-api/files/register', payload)
  },

  presignAudio(payload: {
    filename: string
    contentType: string
    sizeBytes: number
    businessType: 'HELP_ORDER_AUDIO' | 'HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE'
  }) {
    return httpService.post<FilePresignResponse>('/app-api/files/presign', payload)
  },

  registerAudio(payload: {
    objectKey: string
    originalFilename: string
    contentType: string
    sizeBytes: number
    sha256?: string
    businessType: 'HELP_ORDER_AUDIO' | 'HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE'
  }) {
    return httpService.post<FileAssetDTO>('/app-api/files/register', payload)
  },

  transcribeAttachment(orderId: number, attachmentId: number) {
    return httpService.post<HelpOrderVoiceTranscriptDTO>(
      `/app-api/help-orders/${orderId}/attachments/${attachmentId}/transcribe`,
      {}
    )
  },

  async uploadToPresignedUrl(uploadUrl: string, file: File, contentType: string) {
    const response = await fetch(uploadUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': contentType
      },
      body: file
    })

    if (!response.ok) {
      throw new Error('IMAGE_UPLOAD_FAILED')
    }
  }
}

export const appNotificationService = {
  createSocketTicket() {
    return httpService.post<{ ticket: string; expiresInSeconds: number }>('/app-api/notifications/socket-ticket')
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

export const ddiService = {
  screen(payload: { newDrugNames: string[] }) {
    return httpService.post<AppDdiScreenResult>('/app-api/ddi/screen', payload)
  }
}

export interface DrugCatalogDTO {
  id: number
  nameCn: string
  nameEn?: string | null
  pinyinInitials: string
  category?: string | null
}

export const drugCatalogService = {
  search(q: string, limit = 10) {
    return httpService.get<DrugCatalogDTO[]>('/app-api/drugs/search', { q, limit })
  }
}

export const aiConsentService = {
  getConsentStatus() {
    return httpService.get<boolean>('/app-api/ai/consent')
  },

  grantConsent() {
    return httpService.post<boolean>('/app-api/ai/consent', {})
  }
}

export const selfReportedMedicationService = {
  create(payload: SelfReportedMedicationCreateRequest, priority = 'NORMAL') {
    return httpService.post<SelfReportedMedicationDTO[]>(
      `/app-api/self-reported-medications?priority=${priority}`,
      payload
    )
  },

  list() {
    return httpService.get<SelfReportedMedicationDTO[]>('/app-api/self-reported-medications')
  },

  withdraw(id: number) {
    return httpService.post<void>(`/app-api/self-reported-medications/${id}/withdraw`, {})
  },

  stop(id: number) {
    return httpService.post<void>(`/app-api/self-reported-medications/${id}/stop`, {})
  }
}

export const prescriptionService = {
  list(params?: { status?: string; pageNo?: number; pageSize?: number }) {
    return httpService.get<PageResult<PrescriptionDTO>>('/app-api/prescriptions', params)
  },

  presignUpload(payload: { filename: string; contentType: string; sizeBytes: number; businessType: 'PRESCRIPTION' }) {
    return httpService.post<FilePresignResponse>('/app-api/files/presign', payload)
  },

  async uploadToPresignedUrl(uploadUrl: string, file: File, contentType: string) {
    const response = await fetch(uploadUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': contentType
      },
      body: file
    })

    if (!response.ok) {
      throw new Error('IMAGE_UPLOAD_FAILED')
    }
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

export const educationService = {
  recommendations(limit = 5) {
    return httpService.get<EducationContentCardDTO[]>('/app-api/education/recommendations', { limit })
  },

  list(params?: { keyword?: string; category?: string; pageNo?: number; pageSize?: number }) {
    return httpService.get<PageResult<EducationContentCardDTO>>('/app-api/education/cards', params)
  },

  detail(id: number) {
    return httpService.get<EducationContentCardDTO>(`/app-api/education/cards/${id}`)
  }
}
