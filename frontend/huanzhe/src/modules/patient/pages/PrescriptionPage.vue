<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>处方上传</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <div class="page-container">
        <section class="section-card">
          <h2 class="section-title">上传登记说明</h2>
          <p class="section-desc">当前先完成处方图片登记和药师复核流转，不依赖真实对象存储上传成功。</p>
          <p class="section-note">如本地对象存储未接入，当前页面仍可完成登记；图片回看会显示“待对象存储接入”。</p>
        </section>

        <section class="section-card">
          <h2 class="section-title">选择处方图片</h2>
          <input
            ref="fileInputRef"
            class="hidden-input"
            type="file"
            accept="image/png,image/jpeg,image/webp"
            @change="handleFileChange"
          />

          <div class="upload-actions">
            <ion-button expand="block" @click="triggerFilePicker">选择图片</ion-button>
          </div>

          <div v-if="selectedFile" class="selected-file">
            <div class="file-meta">
              <strong>{{ selectedFile.name }}</strong>
              <span>{{ formatSize(selectedFile.size) }}</span>
            </div>
            <img v-if="previewUrl" :src="previewUrl" alt="处方预览" class="preview-image" />

            <div class="drug-input-block">
              <label class="field-label" for="candidate-drug-names">本次新增药物</label>
              <textarea
                id="candidate-drug-names"
                v-model="candidateDrugNamesText"
                class="drug-names-input"
                rows="4"
                placeholder="例如：华法林、奥美拉唑。多个药物请用换行、逗号或顿号分隔"
              />
              <p class="field-hint">用于上传前用药风险提醒；若暂时无法确认药名，可直接提交，系统会跳过本次提醒。</p>
            </div>

            <ion-button class="upload-submit-button" expand="block" :disabled="uploading" @click="submitUpload">
              {{ uploading ? '正在登记...' : '提交登记' }}
            </ion-button>
          </div>
        </section>

        <section class="section-card">
          <h2 class="section-title">处方登记记录</h2>
          <div v-if="historyLoading" class="empty-state">
            <p>加载处方记录中...</p>
          </div>
          <div v-else-if="uploadRecords.length === 0" class="empty-state">
            <p>还没有登记记录，先选择一张处方图片。</p>
          </div>
          <div v-else class="record-list">
            <article v-for="record in uploadRecords" :key="record.id" class="record-card">
              <div class="record-header">
                <strong>处方 #{{ record.id }}</strong>
                <span class="status-tag">{{ mapStatus(record.status) }}</span>
              </div>
              <p>患者：{{ record.patientName }} / {{ record.hospitalPatientId }}</p>
              <p>OCR 状态：{{ mapOcrStatus(record.ocrStatus) }}</p>
              <p>提交时间：{{ formatDate(record.createdAt) }}</p>
              <p>附件：{{ record.attachments[0]?.originalFilename || '待对象存储接入' }}</p>
            </article>
          </div>
        </section>
      </div>

      <ion-loading :is-open="uploading" message="正在登记处方..." :duration="0" />
      <ion-toast :is-open="showToast" :message="toastMessage" :duration="2600" @did-dismiss="showToast = false" />

      <ion-modal :is-open="showDdiModal" :backdrop-dismiss="false" @did-dismiss="clearDdiPromptState">
        <div class="ddi-modal">
          <div class="ddi-modal-header">
            <h2>检测到用药风险提醒</h2>
            <p>系统根据当前用药计划和健康档案发现以下风险，请确认后再提交处方。</p>
          </div>

          <div class="ddi-modal-summary">
            <strong>本次新增药物：</strong>
            <span>{{ pendingDrugNamesLabel }}</span>
          </div>

          <div class="ddi-alert-list">
            <article v-for="(alert, index) in pendingDdiAlerts" :key="`${alert.type}-${alert.drugA}-${alert.drugB}-${index}`" class="ddi-alert-card">
              <div class="ddi-alert-top">
                <span :class="['severity-pill', severityClass(alert.severity)]">{{ severityLabel(alert.severity) }}</span>
                <span class="alert-type">{{ alertTypeLabel(alert.type) }}</span>
              </div>
              <h3>{{ alertPairText(alert) }}</h3>
              <p>{{ alert.message }}</p>
              <p v-if="alert.recommendedAction" class="ddi-recommendation">建议：{{ alert.recommendedAction }}</p>
            </article>
          </div>

          <div class="ddi-modal-actions">
            <ion-button fill="outline" color="medium" :disabled="uploading" @click="consultPharmacist">
              取消，咨询药师
            </ion-button>
            <ion-button :disabled="uploading" @click="confirmDdiAndUpload">
              已知晓，继续提交
            </ion-button>
          </div>
        </div>
      </ion-modal>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import {
  IonButton,
  IonContent,
  IonHeader,
  IonLoading,
  IonModal,
  IonPage,
  IonTitle,
  IonToast,
  IonToolbar
} from '@ionic/vue'
import { useRouter } from 'vue-router'
import {
  ddiService,
  prescriptionService,
  type AppDdiAlert,
  type PrescriptionDTO
} from '@/modules/patient/api/mvp'

const MAX_SIZE_BYTES = 10 * 1024 * 1024

const router = useRouter()
const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const previewUrl = ref('')
const candidateDrugNamesText = ref('')
const uploading = ref(false)
const historyLoading = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const uploadRecords = ref<PrescriptionDTO[]>([])
const showDdiModal = ref(false)
const pendingDdiAlerts = ref<AppDdiAlert[]>([])
const pendingUploadFile = ref<File | null>(null)
const pendingUploadDrugNames = ref<string[]>([])

const pendingDrugNamesLabel = computed(() => pendingUploadDrugNames.value.join('、') || '--')

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const loadUploadHistory = async () => {
  historyLoading.value = true
  try {
    const result = await prescriptionService.list({ pageNo: 1, pageSize: 20 })
    uploadRecords.value = result.data?.list ?? []
  } catch (error: any) {
    showMessage(error?.message || '处方记录加载失败，请稍后重试')
  } finally {
    historyLoading.value = false
  }
}

const revokePreview = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

const clearDdiPromptState = () => {
  showDdiModal.value = false
  pendingDdiAlerts.value = []
  pendingUploadFile.value = null
  pendingUploadDrugNames.value = []
}

const resetSelection = () => {
  selectedFile.value = null
  candidateDrugNamesText.value = ''
  revokePreview()
  clearDdiPromptState()
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const triggerFilePicker = () => {
  fileInputRef.value?.click()
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) {
    return
  }
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    resetSelection()
    showMessage('仅支持 JPG、PNG、WEBP 图片')
    return
  }
  if (file.size > MAX_SIZE_BYTES) {
    resetSelection()
    showMessage('图片不能超过 10MB')
    return
  }
  selectedFile.value = file
  revokePreview()
  clearDdiPromptState()
  previewUrl.value = URL.createObjectURL(file)
}

const parseDrugNames = (rawValue: string) =>
  Array.from(
    new Set(
      rawValue
        .split(/[\n,，;；、]+/)
        .map((item) => item.trim())
        .filter(Boolean)
    )
  )

const severityLabel = (severity?: string | null) => {
  if (severity === 'HIGH') return '高风险'
  if (severity === 'MEDIUM') return '中风险'
  return severity || '风险提示'
}

const severityClass = (severity?: string | null) => {
  if (severity === 'HIGH') return 'severity-pill--high'
  if (severity === 'MEDIUM') return 'severity-pill--medium'
  return 'severity-pill--default'
}

const alertTypeLabel = (type?: string | null) => {
  if (type === 'CONTRAINDICATION') return '禁忌提醒'
  if (type === 'INTERACTION') return '相互作用'
  return type || '风险提示'
}

const alertPairText = (alert: AppDdiAlert) => {
  if (alert.type === 'CONTRAINDICATION') {
    return `${alert.drugB} 与 ${alert.drugA}`
  }
  return `${alert.drugA} + ${alert.drugB}`
}

const buildConsultDescription = (file: File | null, newDrugNames: string[], alerts: AppDdiAlert[]) => {
  const lines = [
    '我在上传处方前收到了用药风险提醒，想请药师帮我判断是否继续提交。',
    file ? `处方图片：${file.name}` : '处方图片：未记录',
    `本次新增药物：${newDrugNames.join('、') || '未填写'}`,
    '系统提示：'
  ]

  alerts.slice(0, 5).forEach((alert, index) => {
    const base = `${index + 1}. [${severityLabel(alert.severity)}][${alertTypeLabel(alert.type)}] ${alertPairText(alert)}：${alert.message}`
    lines.push(alert.recommendedAction ? `${base} 建议：${alert.recommendedAction}` : base)
  })

  if (alerts.length > 5) {
    lines.push(`其余 ${alerts.length - 5} 条风险提醒仍保留在处方上传页。`)
  }

  return lines.join('\n')
}

const registerUpload = async (file: File, options?: { screeningDegraded?: boolean; acknowledgedRisk?: boolean }) => {
  const presign = await prescriptionService.presignUpload({
    filename: file.name,
    contentType: file.type,
    sizeBytes: file.size,
    businessType: 'PRESCRIPTION'
  })

  const result = await prescriptionService.registerUpload({
    objectKey: presign.data.objectKey,
    originalFilename: file.name,
    contentType: file.type,
    sizeBytes: file.size
  })

  uploadRecords.value = [result.data, ...uploadRecords.value]

  if (options?.screeningDegraded) {
    showMessage('处方登记已提交，用药风险提醒暂不可用，药师仍会继续复核')
  } else if (options?.acknowledgedRisk) {
    showMessage('已确认风险提醒并提交处方，等待药师复核')
  } else {
    showMessage('处方登记已提交，等待药师复核')
  }

  resetSelection()
}

const submitUpload = async () => {
  if (!selectedFile.value) {
    showMessage('请先选择一张处方图片')
    return
  }

  const file = selectedFile.value
  const newDrugNames = parseDrugNames(candidateDrugNamesText.value)

  uploading.value = true
  try {
    if (newDrugNames.length > 0) {
      try {
        const screenResult = await ddiService.screen({ newDrugNames })
        const alerts = screenResult.data?.alerts ?? []
        if (alerts.length > 0) {
          pendingUploadFile.value = file
          pendingUploadDrugNames.value = newDrugNames
          pendingDdiAlerts.value = alerts
          showDdiModal.value = true
          return
        }
      } catch {
        await registerUpload(file, { screeningDegraded: true })
        return
      }
    }

    await registerUpload(file)
  } catch (error: any) {
    showMessage(error?.message || '处方登记失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

const confirmDdiAndUpload = async () => {
  const file = pendingUploadFile.value
  if (!file) {
    clearDdiPromptState()
    return
  }

  clearDdiPromptState()
  uploading.value = true
  try {
    await registerUpload(file, { acknowledgedRisk: true })
  } catch (error: any) {
    showMessage(error?.message || '处方登记失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

const consultPharmacist = async () => {
  const file = pendingUploadFile.value
  const newDrugNames = [...pendingUploadDrugNames.value]
  const alerts = [...pendingDdiAlerts.value]

  clearDdiPromptState()

  await router.push({
    path: '/tabs/help-order',
    query: {
      category: 'MEDICATION_CONSULTATION',
      title: '处方上传前用药风险咨询',
      description: buildConsultDescription(file, newDrugNames, alerts)
    }
  })
}

const formatSize = (size: number) => {
  if (size < 1024 * 1024) {
    return `${Math.round(size / 1024)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

const formatDate = (value?: string | null) => {
  if (!value) {
    return '--'
  }
  return new Date(value).toLocaleString('zh-CN')
}

const mapStatus = (status?: string | null) => {
  switch (status) {
    case 'UPLOADED':
      return '待药师复核'
    case 'PHARMACIST_CONFIRMED':
      return '已完成复核'
    case 'REJECTED':
      return '已驳回'
    default:
      return status || '--'
  }
}

const mapOcrStatus = (status?: string | null) => {
  switch (status) {
    case 'LOW_CONFIDENCE':
      return '低置信度，转人工复核'
    case 'SUCCESS':
      return '识别完成'
    default:
      return status || '待人工复核'
  }
}

onBeforeUnmount(() => {
  revokePreview()
})

onMounted(() => {
  loadUploadHistory()
})
</script>

<style scoped>
.page-content {
  --background: #f5f7fb;
}

.page-container {
  max-width: 560px;
  margin: 0 auto;
  padding: 16px;
}

.section-card {
  background: #fff;
  border-radius: 18px;
  padding: 18px;
  margin-bottom: 16px;
  box-shadow: 0 8px 20px rgba(15, 36, 80, 0.08);
}

.section-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.section-desc,
.section-note,
.record-card p,
.empty-state p {
  margin: 6px 0 0;
  color: #4b5563;
  line-height: 1.6;
}

.section-note,
.field-hint {
  font-size: 13px;
  color: #6b7280;
}

.hidden-input {
  display: none;
}

.upload-actions {
  margin-top: 12px;
}

.selected-file {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.file-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  color: #1f2937;
}

.preview-image {
  width: 100%;
  border-radius: 14px;
  object-fit: cover;
  border: 1px solid #dbe3f0;
}

.drug-input-block {
  display: grid;
  gap: 8px;
}

.field-label {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.drug-names-input {
  width: 100%;
  min-height: 108px;
  padding: 12px 14px;
  border: 1px solid #dbe3f0;
  border-radius: 14px;
  resize: vertical;
  font: inherit;
  line-height: 1.6;
  color: #1f2937;
  background: #f8fafc;
}

.drug-names-input:focus {
  outline: 2px solid rgba(37, 99, 235, 0.14);
  border-color: #93c5fd;
}

.record-list {
  display: grid;
  gap: 12px;
  margin-top: 8px;
}

.record-card {
  border: 1px solid #dbe3f0;
  border-radius: 14px;
  padding: 14px;
  background: #f8fafc;
}

.record-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  color: #111827;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 12px;
}

ion-modal {
  --width: min(92vw, 560px);
  --height: auto;
  --border-radius: 24px;
}

.ddi-modal {
  padding: 20px 18px 24px;
  background: #fff;
}

.ddi-modal-header h2 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.ddi-modal-header p,
.ddi-modal-summary {
  margin: 10px 0 0;
  color: #475569;
  line-height: 1.6;
}

.ddi-alert-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.ddi-alert-card {
  padding: 14px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.ddi-alert-top {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.ddi-alert-card h3 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
}

.ddi-alert-card p {
  margin: 8px 0 0;
  color: #475569;
  line-height: 1.6;
}

.ddi-recommendation {
  color: #0f172a;
  font-weight: 500;
}

.severity-pill,
.alert-type {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.severity-pill--high {
  background: #fee2e2;
  color: #b91c1c;
}

.severity-pill--medium {
  background: #ffedd5;
  color: #c2410c;
}

.severity-pill--default {
  background: #e2e8f0;
  color: #334155;
}

.alert-type {
  background: #e0f2fe;
  color: #0369a1;
}

.ddi-modal-actions {
  display: grid;
  gap: 10px;
  margin-top: 18px;
}
</style>
