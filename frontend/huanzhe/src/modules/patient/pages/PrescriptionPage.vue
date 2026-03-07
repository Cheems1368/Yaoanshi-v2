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
          <p class="section-desc">本轮先完成处方图片登记和药师复核流转，不依赖真实对象存储上传成功。</p>
          <p class="section-note">如果本地对象存储未接入，当前页面仍可完成登记；图片回看会显示“待对象存储接入”。</p>
        </section>

        <section class="section-card">
          <h2 class="section-title">选择处方图片</h2>
          <input ref="fileInputRef" class="hidden-input" type="file" accept="image/png,image/jpeg,image/webp" @change="handleFileChange" />

          <div class="upload-actions">
            <ion-button expand="block" @click="triggerFilePicker">选择图片</ion-button>
          </div>

          <div v-if="selectedFile" class="selected-file">
            <div class="file-meta">
              <strong>{{ selectedFile.name }}</strong>
              <span>{{ formatSize(selectedFile.size) }}</span>
            </div>
            <img v-if="previewUrl" :src="previewUrl" alt="处方预览" class="preview-image" />
            <ion-button expand="block" :disabled="uploading" @click="submitUpload">
              {{ uploading ? '正在登记...' : '提交登记' }}
            </ion-button>
          </div>
        </section>

        <section class="section-card">
          <h2 class="section-title">本次会话登记结果</h2>
          <div v-if="uploadRecords.length === 0" class="empty-state">
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
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import {
  IonButton,
  IonContent,
  IonHeader,
  IonLoading,
  IonPage,
  IonTitle,
  IonToast,
  IonToolbar
} from '@ionic/vue'
import { prescriptionService, type PrescriptionDTO } from '@/modules/patient/api/mvp'

const MAX_SIZE_BYTES = 10 * 1024 * 1024

const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const previewUrl = ref('')
const uploading = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const uploadRecords = ref<PrescriptionDTO[]>([])

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const revokePreview = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

const resetSelection = () => {
  selectedFile.value = null
  revokePreview()
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
  previewUrl.value = URL.createObjectURL(file)
}

const submitUpload = async () => {
  if (!selectedFile.value) {
    showMessage('请先选择一张处方图片')
    return
  }

  uploading.value = true
  try {
    const file = selectedFile.value
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
    showMessage('处方登记已提交，等待药师复核')
    resetSelection()
  } catch (error: any) {
    showMessage(error?.message || '处方登记失败，请稍后重试')
  } finally {
    uploading.value = false
  }
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

.section-note {
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
</style>
