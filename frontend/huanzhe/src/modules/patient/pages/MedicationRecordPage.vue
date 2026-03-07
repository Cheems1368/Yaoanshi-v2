<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/tabs/home" />
        </ion-buttons>
        <ion-title>服药记录</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <div class="container">
        <ion-card class="overview-card">
          <ion-card-content class="overview-grid">
            <div>
              <div class="overview-number">{{ records.length }}</div>
              <div class="overview-label">近 30 天记录</div>
            </div>
            <div>
              <div class="overview-number">{{ effectiveCount }}</div>
              <div class="overview-label">有效记录</div>
            </div>
            <div>
              <div class="overview-number">{{ completedCount }}</div>
              <div class="overview-label">已服药</div>
            </div>
          </ion-card-content>
        </ion-card>

        <ion-card v-if="loading" class="empty-card">
          <ion-card-content class="empty-state">
            <ion-spinner name="crescent" />
          </ion-card-content>
        </ion-card>

        <ion-card v-else-if="records.length === 0" class="empty-card">
          <ion-card-content class="empty-state">
            <ion-icon :icon="calendarOutline" class="empty-icon" />
            <p class="empty-text">最近 30 天暂无服药记录</p>
          </ion-card-content>
        </ion-card>

        <ion-card v-for="record in records" :key="record.id" class="record-card">
          <ion-card-content>
            <div class="record-header">
              <div>
                <div class="record-title">任务 #{{ record.doseTaskId }}</div>
                <div class="record-meta">{{ formatDateTime(record.actualTakenAt || record.createdAt) }}</div>
                <div class="record-meta">提交人：{{ operatorText(record.operatorType) }}</div>
              </div>
              <ion-badge :color="resultColor(record.result)">{{ resultText(record.result) }}</ion-badge>
            </div>

            <div v-if="record.note" class="record-note">{{ record.note }}</div>
            <div v-if="record.skipReason" class="record-note">原因：{{ record.skipReason }}</div>
            <div v-if="record.correction" class="record-note">这是更正记录</div>
            <div v-if="!record.effective" class="record-note muted">已被后续更正覆盖</div>

            <div v-if="record.effective" class="record-actions">
              <ion-button size="small" color="success" fill="outline" @click="correctRecord(record, 'TAKEN')">
                更正为已服药
              </ion-button>
              <ion-button size="small" color="warning" fill="outline" @click="correctRecord(record, 'MISSED')">
                更正为漏服
              </ion-button>
            </div>
          </ion-card-content>
        </ion-card>
      </div>

      <ion-toast :is-open="showToast" :message="toastMessage" :duration="2400" @did-dismiss="showToast = false" />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonBackButton,
  IonBadge,
  IonButtons,
  IonButton,
  IonCard,
  IonCardContent,
  IonContent,
  IonHeader,
  IonIcon,
  IonPage,
  IonSpinner,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewWillEnter
} from '@ionic/vue'
import { computed, ref } from 'vue'
import { calendarOutline } from 'ionicons/icons'
import { medicationService, type CheckinRecordDTO } from '@/modules/patient/api/mvp'

const loading = ref(false)
const records = ref<CheckinRecordDTO[]>([])
const showToast = ref(false)
const toastMessage = ref('')

const effectiveCount = computed(() => records.value.filter((record) => record.effective).length)
const completedCount = computed(() => records.value.filter((record) => record.effective && record.result === 'TAKEN').length)

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const loadRecords = async () => {
  loading.value = true
  try {
    const response = await medicationService.getCheckins({ pageNo: 1, pageSize: 30 })
    records.value = response.data?.list ?? []
  } catch (error: any) {
    records.value = []
    showMessage(error?.message || '服药记录加载失败')
  } finally {
    loading.value = false
  }
}

const correctRecord = async (record: CheckinRecordDTO, result: 'TAKEN' | 'MISSED') => {
  try {
    await medicationService.correctCheckin(record.id, {
      result,
      actualTakenAt: new Date().toISOString(),
      note: '记录页更正',
      skipReason: result === 'MISSED' ? 'PATIENT_REPORTED' : ''
    })
    showMessage(result === 'TAKEN' ? '已更正为已服药' : '已更正为漏服')
    await loadRecords()
  } catch (error: any) {
    showMessage(error?.message || '更正失败')
  }
}

const formatDateTime = (value?: string | null) => {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

const resultText = (result: string) => {
  const map: Record<string, string> = {
    TAKEN: '已服药',
    MISSED: '漏服',
    SKIPPED: '跳过'
  }
  return map[result] || result
}

const resultColor = (result: string) => {
  if (result === 'TAKEN') return 'success'
  if (result === 'MISSED') return 'danger'
  return 'warning'
}

const operatorText = (operatorType: string) => {
  return operatorType === 'FAMILY' ? '亲情号' : '本人'
}

onIonViewWillEnter(loadRecords)
</script>

<style scoped>
.page-content {
  --background: #f6f8fb;
}

.container {
  padding: 16px;
}

.overview-card,
.record-card,
.empty-card {
  border-radius: 18px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.overview-grid {
  display: flex;
  justify-content: space-between;
  text-align: center;
}

.overview-number {
  font-size: 28px;
  font-weight: 700;
  color: #1d4ed8;
}

.overview-label,
.record-meta,
.record-note,
.empty-text {
  color: #64748b;
}

.record-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.record-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.record-note {
  margin-top: 10px;
}

.record-note.muted {
  color: #94a3b8;
}

.record-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.empty-state {
  text-align: center;
  padding: 32px 12px;
}

.empty-icon {
  font-size: 36px;
  color: #94a3b8;
}
</style>
