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
            <div class="overview-item">
              <div class="overview-number">{{ records.length }}</div>
              <div class="overview-label">近 30 天</div>
            </div>
            <div class="overview-divider"></div>
            <div class="overview-item">
              <div class="overview-number overview-number--done">{{ completedCount }}</div>
              <div class="overview-label">已服药</div>
            </div>
            <div class="overview-divider"></div>
            <div class="overview-item">
              <div class="overview-number">{{ effectiveCount }}</div>
              <div class="overview-label">有效记录</div>
            </div>
          </ion-card-content>
        </ion-card>

        <div v-if="loading" class="loading-state">
          <ion-spinner name="crescent" />
        </div>

        <EmptyState
          v-else-if="records.length === 0"
          type="no-records"
          title="最近 30 天暂无服药记录"
        />

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
            <div v-if="!record.effective" class="record-note record-note--muted">已被后续更正覆盖</div>

            <div v-if="record.effective" class="record-actions">
              <label>实际服药时间（补录可改，北京时间）<input v-model="takenAtByRecordId[record.id]" type="datetime-local" /></label>
              <input v-model="reasonByRecordId[record.id]" placeholder="跳过或漏服原因（选填）" />
              <ion-button size="small" color="success" fill="outline" @click="correctRecord(record, 'TAKEN')">
                更正为已服药
              </ion-button>
              <ion-button size="small" color="warning" fill="outline" @click="correctRecord(record, 'MISSED')">
                更正为漏服
              </ion-button>
              <ion-button size="small" color="medium" fill="outline" @click="correctRecord(record, 'SKIPPED')">更正为跳过</ion-button>
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
  IonPage,
  IonSpinner,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewWillEnter
} from '@ionic/vue'
import { computed, ref } from 'vue'
import { medicationService, type CheckinRecordDTO } from '@/modules/patient/api/mvp'
import { toShanghaiIso } from '@/modules/patient/services/offlineMedication'

const loading = ref(false)
const records = ref<CheckinRecordDTO[]>([])
const showToast = ref(false)
const toastMessage = ref('')
const takenAtByRecordId = ref<Record<number, string>>({})
const reasonByRecordId = ref<Record<number, string>>({})

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

const correctRecord = async (record: CheckinRecordDTO, result: 'TAKEN' | 'MISSED' | 'SKIPPED') => {
  try {
    await medicationService.correctCheckin(record.id, {
      result,
      actualTakenAt: result === 'TAKEN' ? toShanghaiIso(takenAtByRecordId.value[record.id]) : undefined,
      note: '记录页更正',
      skipReason: reasonByRecordId.value[record.id]?.trim()
    })
    showMessage(result === 'TAKEN' ? '已更正为已服药' : result === 'SKIPPED' ? '已更正为跳过' : '已更正为漏服')
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
  const map: Record<string, string> = { TAKEN: '已服药', MISSED: '漏服', SKIPPED: '跳过' }
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
  --background: var(--patient-bg-page);
}

.container {
  padding: var(--patient-space-4);
  display: flex;
  flex-direction: column;
  gap: var(--patient-space-3);
}

/* ── 概览卡片 ── */
.overview-card {
  margin: 0;
}

.overview-grid {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: var(--patient-space-4) var(--patient-space-3) var(--patient-space-3);
}

.overview-item {
  flex: 1;
  text-align: center;
}

.overview-number {
  font-size: var(--patient-font-page-title);
  font-weight: var(--patient-font-bold);
  line-height: var(--patient-leading-tight);
  color: var(--patient-primary);
}

.overview-number--done {
  color: var(--patient-success);
}

.overview-label {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
  margin-top: var(--patient-space-1);
}

.overview-divider {
  width: 1px;
  height: 40px;
  background: var(--patient-border);
  flex-shrink: 0;
}

/* ── 加载状态 ── */
.loading-state {
  padding: var(--patient-space-8) var(--patient-space-4);
  text-align: center;
}

/* ── 记录卡片 ── */
.record-card {
  margin: 0;
}

.record-header {
  display: flex;
  justify-content: space-between;
  gap: var(--patient-space-3);
  align-items: flex-start;
}

.record-title {
  font-size: var(--patient-font-subtitle);
  font-weight: var(--patient-font-bold);
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-tight);
}

.record-meta {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
  margin-top: var(--patient-space-1);
  line-height: var(--patient-leading-normal);
}

.record-note {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
  margin-top: var(--patient-space-2);
  line-height: var(--patient-leading-normal);
}

.record-note--muted {
  color: var(--patient-text-muted);
}

.record-actions {
  display: flex;
  gap: var(--patient-space-3);
  margin-top: var(--patient-space-4);
}
</style>
