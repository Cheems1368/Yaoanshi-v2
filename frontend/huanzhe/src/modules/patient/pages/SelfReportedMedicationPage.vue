<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/tabs/home" />
        </ion-buttons>
        <ion-title>记录新药品</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <ion-refresher slot="fixed" @ionRefresh="handleRefresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>
      <!-- 家属代录提示 -->
      <div v-if="isProxyMode" class="proxy-banner">
        您正在代替 <strong>{{ proxyPatientName }}</strong> 操作
      </div>

      <!-- 录入表单 -->
      <ion-card class="form-card">
        <ion-card-header>
          <ion-card-title class="card-title">添加药品</ion-card-title>
          <ion-card-subtitle class="card-subtitle">记录您自行购买或使用的药品，药师将在工作时间内处理</ion-card-subtitle>
        </ion-card-header>
        <ion-card-content>
          <div v-for="(drug, index) in drugs" :key="index" class="drug-entry">
            <div class="drug-entry-header">
              <span class="drug-entry-label">药品 {{ index + 1 }}</span>
              <button v-if="drugs.length > 1" class="remove-btn" @click="removeDrug(index)">删除</button>
            </div>
            <div class="drug-input-wrap">
              <ion-item class="form-item">
                <ion-label position="stacked">药品名称 <span class="required">*</span></ion-label>
                <ion-input
                  v-model="drug.drugName"
                  placeholder="中文名称或拼音首字母（如 aspl）"
                  :clearInput="true"
                  autocomplete="off"
                  @ionInput="onDrugNameInput(index, $event)"
                  @ionBlur="clearSuggestions(index)"
                />
              </ion-item>
              <div v-if="drug.suggestions.length > 0" class="suggestion-list">
                <div
                  v-for="s in drug.suggestions"
                  :key="s.id"
                  class="suggestion-item"
                  @mousedown.prevent="selectSuggestion(index, s)"
                >
                  <span class="suggestion-name">{{ s.nameCn }}</span>
                  <span v-if="s.category" class="suggestion-category">{{ s.category }}</span>
                </div>
              </div>
            </div>
            <ion-item class="form-item">
              <ion-label position="stacked">剂量（可选）</ion-label>
              <ion-input v-model="drug.dosage" placeholder="如：400mg、1片" />
            </ion-item>
          </div>

          <button class="add-drug-btn" @click="addDrug">+ 继续添加药品</button>

          <ion-item class="form-item notes-item">
            <ion-label position="stacked">备注（可选）</ion-label>
            <ion-textarea v-model="notes" placeholder="如：感冒自行购买的退烧药" :rows="3" />
          </ion-item>

          <ion-button
            expand="block"
            class="submit-btn"
            :disabled="!canSubmit || submitting"
            @click="handleSubmit"
          >
            <ion-spinner v-if="submitting" name="crescent" />
            <span v-else>提交审核</span>
          </ion-button>
        </ion-card-content>
      </ion-card>

      <!-- 自报药物列表 -->
      <ion-card class="list-card">
        <ion-card-header>
          <ion-card-title class="card-title">我的自报药品</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <div v-if="listLoading" class="empty-state">
            <ion-spinner name="crescent" />
          </div>
          <div v-else-if="medications.length === 0" class="empty-state">
            <p class="empty-text">暂无自报药品记录</p>
          </div>
          <div v-else class="med-list">
            <div v-for="med in medications" :key="med.id" class="med-item">
              <div class="med-info">
                <div class="med-name">{{ med.drugName }}</div>
                <div v-if="med.dosage" class="med-meta">{{ med.dosage }}</div>
                <div v-if="med.notes" class="med-meta">备注：{{ med.notes }}</div>
                <div class="med-meta">{{ formatDate(med.createdAt) }}</div>
              </div>
              <div class="med-right">
                <ion-badge :color="statusColor(med.status)" class="status-badge">
                  {{ statusText(med.status) }}
                </ion-badge>
                <button v-if="med.status === 'PENDING'" class="action-btn withdraw-btn" @click="handleWithdraw(med.id)">
                  撤回
                </button>
                <button v-if="med.status === 'ACTIVE'" class="action-btn stop-btn" @click="handleStop(med.id)">
                  停用
                </button>
              </div>
            </div>
          </div>
        </ion-card-content>
      </ion-card>

      <!-- DDI 确认弹窗 -->
      <ion-modal :is-open="showDdiModal" @did-dismiss="showDdiModal = false">
        <ion-header>
          <ion-toolbar>
            <ion-title>发现用药风险</ion-title>
          </ion-toolbar>
        </ion-header>
        <ion-content class="modal-content">
          <div class="ddi-alerts">
            <div v-for="(alert, i) in ddiAlerts" :key="i" class="ddi-alert" :class="`ddi-${alert.severity.toLowerCase()}`">
              <div class="alert-severity">{{ alert.severity === 'HIGH' ? '高风险' : '中风险' }}</div>
              <div class="alert-drugs">{{ alert.drugA }} + {{ alert.drugB }}</div>
              <div class="alert-message">{{ alert.patientSummary || alert.message }}</div>
            </div>
          </div>

          <div v-if="currentPlanDrugs.length > 0" class="plan-drugs-section">
            <div class="plan-drugs-title">当前用药计划</div>
            <div class="plan-drugs-list">{{ currentPlanDrugs.join('、') }}</div>
          </div>

          <div class="disclaimer">
            本提示仅供参考，不替代专业医药指导。提交后药师将在工作时间内处理您的请求。
          </div>

          <div class="modal-actions">
            <ion-button expand="block" @click="confirmAndSubmit">已知晓，继续提交</ion-button>
            <ion-button expand="block" fill="outline" @click="showDdiModal = false">取消</ion-button>
          </div>
        </ion-content>
      </ion-modal>

      <ion-toast :is-open="showToast" :message="toastMessage" :duration="2400" @did-dismiss="showToast = false" />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonBackButton,
  IonBadge,
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonModal,
  IonPage,
  IonRefresher,
  IonRefresherContent,
  IonSpinner,
  IonTextarea,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewWillEnter
} from '@ionic/vue'
import { computed, ref } from 'vue'
import { ddiService, drugCatalogService, medicationService, patientService, selfReportedMedicationService, type AppDdiAlert, type DrugCatalogDTO, type SelfReportedMedicationDTO } from '@/modules/patient/api/mvp'

const isProxyMode = ref(false)
const proxyPatientName = ref('')

const drugs = ref([{ drugName: '', dosage: '', suggestions: [] as DrugCatalogDTO[] }])

const drugInputTimers: ReturnType<typeof setTimeout>[] = []

const onDrugNameInput = (index: number, event: CustomEvent) => {
  const q = (event.detail?.value ?? '').trim()
  clearTimeout(drugInputTimers[index])
  if (!q) {
    drugs.value[index].suggestions = []
    return
  }
  drugInputTimers[index] = setTimeout(async () => {
    try {
      const res = await drugCatalogService.search(q, 8)
      drugs.value[index].suggestions = res.data ?? []
    } catch {
      drugs.value[index].suggestions = []
    }
  }, 280)
}

const selectSuggestion = (index: number, item: DrugCatalogDTO) => {
  drugs.value[index].drugName = item.nameCn
  drugs.value[index].suggestions = []
}

const clearSuggestions = (index: number) => {
  setTimeout(() => {
    drugs.value[index].suggestions = []
  }, 150)
}
const notes = ref('')
const submitting = ref(false)

const medications = ref<SelfReportedMedicationDTO[]>([])
const listLoading = ref(false)

const showDdiModal = ref(false)
const ddiAlerts = ref<AppDdiAlert[]>([])
const currentPlanDrugs = ref<string[]>([])
const pendingPriority = ref('NORMAL')

const showToast = ref(false)
const toastMessage = ref('')

const canSubmit = computed(() => drugs.value.some((d) => d.drugName.trim().length > 0))

const addDrug = () => {
  drugs.value.push({ drugName: '', dosage: '', suggestions: [] })
}

const removeDrug = (index: number) => {
  drugs.value.splice(index, 1)
}

const toast = (msg: string) => {
  toastMessage.value = msg
  showToast.value = true
}

const loadList = async () => {
  listLoading.value = true
  try {
    const res = await selfReportedMedicationService.list()
    medications.value = res.data ?? []
  } catch {
    medications.value = []
  } finally {
    listLoading.value = false
  }
}

const handleSubmit = async () => {
  const validDrugs = drugs.value.filter((d) => d.drugName.trim())
  if (validDrugs.length === 0) return

  try {
    // DDI 筛查
    const screenRes = await ddiService.screen({ newDrugNames: validDrugs.map((d) => d.drugName.trim()) })
    const alerts = screenRes.data?.alerts ?? []

    if (alerts.length > 0) {
      ddiAlerts.value = alerts
      pendingPriority.value = alerts.some((a) => a.severity === 'HIGH') ? 'URGENT' : 'NORMAL'

      // 加载当前计划药物用于展示
      try {
        const planRes = await medicationService.getActiveMedicationPlan()
        if (planRes.data?.items?.length) {
          currentPlanDrugs.value = planRes.data.items.map((i) => i.drugName).filter(Boolean) as string[]
        } else if (planRes.data?.planName) {
          currentPlanDrugs.value = [planRes.data.planName]
        } else {
          currentPlanDrugs.value = []
        }
      } catch {
        currentPlanDrugs.value = []
      }

      showDdiModal.value = true
    } else {
      await doCreate('NORMAL')
    }
  } catch (e: any) {
    // DDI 筛查失败时仍允许提交，优先级设为 NORMAL
    await doCreate('NORMAL')
  }
}

const confirmAndSubmit = async () => {
  showDdiModal.value = false
  await doCreate(pendingPriority.value)
}

const doCreate = async (priority: string) => {
  submitting.value = true
  const validDrugs = drugs.value.filter((d) => d.drugName.trim()).map((d) => ({
    drugName: d.drugName.trim(),
    dosage: d.dosage?.trim() || undefined
  }))
  try {
    await selfReportedMedicationService.create({ drugs: validDrugs, notes: notes.value.trim() || undefined }, priority)
    toast('已提交，药师将在工作时间内处理')
    drugs.value = [{ drugName: '', dosage: '', suggestions: [] as DrugCatalogDTO[] }]
    notes.value = ''
    await loadList()
  } catch (e: any) {
    toast(e?.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleWithdraw = async (id: number) => {
  try {
    await selfReportedMedicationService.withdraw(id)
    toast('已撤回')
    await loadList()
  } catch (e: any) {
    toast(e?.message || '撤回失败')
  }
}

const handleStop = async (id: number) => {
  try {
    await selfReportedMedicationService.stop(id)
    toast('已申请停用')
    await loadList()
  } catch (e: any) {
    toast(e?.message || '操作失败')
  }
}

const statusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    ACTIVE: '已确认',
    STOPPED: '已停用',
    TRANSFERRED: '已转正式计划'
  }
  return map[status] || status
}

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    ACTIVE: 'success',
    STOPPED: 'medium',
    TRANSFERRED: 'primary'
  }
  return map[status] || 'medium'
}

const formatDate = (val?: string | null) => {
  if (!val) return ''
  const d = new Date(val)
  return Number.isNaN(d.getTime()) ? val : d.toLocaleDateString('zh-CN')
}

async function handleRefresh(event: CustomEvent) {
  try {
    await loadList()
  } finally {
    (event.target as HTMLIonRefresherElement).complete()
  }
}

onIonViewWillEnter(async () => {
  try {
    const profile = await patientService.getProfile()
    if (profile.data?.operatorType === 'FAMILY') {
      isProxyMode.value = true
      proxyPatientName.value = profile.data.name ?? ''
    } else {
      isProxyMode.value = false
      proxyPatientName.value = ''
    }
  } catch {
    // ignore, proxy banner is non-critical
  }
  await loadList()
})
</script>

<style scoped>
.page-content {
  --background: var(--patient-bg-page);
}

.proxy-banner {
  background: rgba(245, 158, 11, 0.12);
  color: var(--patient-warning);
  text-align: center;
  padding: var(--patient-space-3) var(--patient-space-4);
  font-size: var(--patient-font-helper);
}

.form-card,
.list-card {
  margin: var(--patient-space-4);
  border-radius: var(--patient-radius-xl);
  box-shadow: var(--patient-shadow-md);
}

.card-title {
  font-weight: 700;
}

.card-subtitle {
  font-size: 13px;
  margin-top: 4px;
}

.drug-entry {
  border: 1px solid var(--patient-border);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}

.drug-entry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.drug-entry-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--patient-text-secondary);
}

.remove-btn {
  border: none;
  background: none;
  color: var(--patient-danger);
  font-size: 13px;
  cursor: pointer;
}

.drug-input-wrap {
  position: relative;
}

.suggestion-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--patient-bg-card);
  border: 1px solid var(--patient-border);
  border-radius: var(--patient-radius-md);
  box-shadow: var(--patient-shadow-md);
  z-index: 100;
  overflow: hidden;
  max-height: 200px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--patient-bg-page);
  font-size: 14px;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-item:active {
  background: var(--patient-bg-page);
}

.suggestion-name {
  font-weight: 500;
  color: var(--patient-text-primary);
}

.suggestion-category {
  font-size: 11px;
  color: var(--patient-text-muted);
  background: var(--patient-bg-page);
  padding: 2px 6px;
  border-radius: 4px;
}

.form-item {
  --padding-start: 0;
  --inner-padding-end: 0;
  margin-bottom: 4px;
}

.notes-item {
  margin-top: 12px;
}

.required {
  color: var(--patient-danger);
}

.add-drug-btn {
  display: block;
  width: 100%;
  padding: 10px;
  background: none;
  border: 1px dashed var(--patient-text-muted);
  border-radius: 10px;
  color: var(--patient-text-secondary);
  font-size: 14px;
  margin: 8px 0;
  cursor: pointer;
}

.submit-btn {
  margin-top: 16px;
}

.med-list {
  display: grid;
  gap: 12px;
}

.med-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--patient-border);
}

.med-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.med-name {
  font-weight: 600;
  color: var(--patient-text-primary);
}

.med-meta {
  font-size: 12px;
  color: var(--patient-text-secondary);
  margin-top: 2px;
}

.med-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.status-badge {
  font-size: 11px;
}

.action-btn {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
}

.withdraw-btn {
  background: rgba(239, 68, 68, 0.1);
  color: var(--patient-danger);
}

.stop-btn {
  background: var(--patient-bg-page);
  color: var(--patient-text-secondary);
}

.empty-state {
  padding: 24px;
  text-align: center;
}

.empty-text {
  color: var(--patient-text-muted);
  font-size: 14px;
}

/* Modal styles */
.modal-content {
  --padding-top: 16px;
  --padding-start: 16px;
  --padding-end: 16px;
}

.ddi-alerts {
  display: grid;
  gap: 12px;
  margin-bottom: 16px;
}

.ddi-alert {
  padding: 12px;
  border-radius: 10px;
}

.ddi-high {
  background: rgba(239, 68, 68, 0.08);
  border-left: 4px solid var(--patient-danger);
}

.ddi-medium {
  background: rgba(245, 158, 11, 0.08);
  border-left: 4px solid var(--patient-warning);
}

.alert-severity {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--patient-text-secondary);
  margin-bottom: 4px;
}

.alert-drugs {
  font-weight: 600;
  color: var(--patient-text-primary);
  margin-bottom: 4px;
}

.alert-message {
  font-size: 13px;
  color: var(--patient-text-secondary);
}

.plan-drugs-section {
  background: var(--patient-bg-page);
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 16px;
}

.plan-drugs-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--patient-text-secondary);
  margin-bottom: 6px;
}

.plan-drugs-list {
  font-size: 13px;
  color: var(--patient-text-primary);
}

.disclaimer {
  font-size: 12px;
  color: var(--patient-text-muted);
  text-align: center;
  padding: 8px 0 16px;
}

.modal-actions {
  display: grid;
  gap: 8px;
  padding-bottom: 24px;
}
</style>
