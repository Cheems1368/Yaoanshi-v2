<template>
  <section class="panel">
    <div class="panel-header">
      <div>
        <h3>用药计划维护</h3>
        <span>在患者详情页内完成创建、版本更新、启用和任务查看。</span>
      </div>
      <div class="header-actions">
        <el-button @click="resetDraft">新建草稿</el-button>
        <el-button :loading="loading" @click="reloadCurrentPlan">刷新</el-button>
      </div>
    </div>

    <el-alert v-if="errorMessage" type="error" :closable="false" :title="errorMessage" show-icon />

    <div class="summary-grid">
      <article class="summary-card">
        <span class="card-label">当前版本</span>
        <strong>{{ currentPlanLabel }}</strong>
        <p>状态：{{ statusText(form.status || 'DRAFT') }}</p>
        <p>启用时间：{{ displayDate(form.confirmedAt) }}</p>
      </article>
      <article class="summary-card">
        <span class="card-label">近 7 天任务</span>
        <strong>{{ taskRows.length }}</strong>
        <p>默认展示今天起 7 天内任务</p>
        <p>依从报告：{{ adherenceReport ? '已生成' : '待生成' }}</p>
      </article>
    </div>

    <el-form label-position="top" class="detail-form">
      <div class="form-grid">
        <el-form-item label="计划名称">
          <el-input v-model="form.planName" placeholder="例如：高血压随访用药计划" />
        </el-form-item>

        <el-form-item label="来源类型">
          <el-select v-model="form.sourceType" placeholder="请选择来源">
            <el-option label="手工录入" value="MANUAL" />
            <el-option label="处方转计划" value="PRESCRIPTION" />
          </el-select>
        </el-form-item>

        <el-form-item label="生效开始">
          <el-date-picker
            v-model="form.effectiveFrom"
            type="datetime"
            placeholder="请选择生效时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="生效结束">
          <el-date-picker
            v-model="form.effectiveTo"
            type="datetime"
            placeholder="可选"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </div>

      <el-form-item label="计划备注">
        <el-input v-model="form.notes" type="textarea" :rows="3" placeholder="记录用药说明、计划变更原因等" />
      </el-form-item>
    </el-form>

    <section class="items-section">
      <div class="section-header">
        <h4>计划药品项</h4>
        <el-button @click="addItem">新增药品</el-button>
      </div>
      <div class="item-list">
        <article v-for="(item, index) in form.items" :key="index" class="item-card">
          <div class="item-grid">
            <el-input v-model="item.drugName" placeholder="药品名称" />
            <el-input v-model="item.drugSpec" placeholder="规格" />
            <el-input v-model="item.dosage" placeholder="剂量" />
            <el-select v-model="item.frequencyCode" placeholder="频次">
              <el-option label="QD (每日一次)" value="QD" />
              <el-option label="BID (每日两次)" value="BID" />
              <el-option label="TID (每日三次)" value="TID" />
              <el-option label="QID (每日四次)" value="QID" />
              <el-option label="HS (睡前)" value="HS" />
              <el-option label="PRN (必要时)" value="PRN" />
            </el-select>
            <el-input v-model="item.timePointsText" placeholder="服药时间，多个用逗号分隔，如 08:00,14:00,20:00" />
            <el-select v-model="item.mealRelation" placeholder="与进餐关系">
              <el-option label="餐后" value="AFTER_MEAL" />
              <el-option label="餐前" value="BEFORE_MEAL" />
              <el-option label="不限" value="ANYTIME" />
            </el-select>
            <el-input v-model="item.route" placeholder="给药途径，例如 PO" />
            <el-input v-model="item.instructions" placeholder="用药说明" />
          </div>
          <div class="item-actions">
            <span>药品项 #{{ index + 1 }}</span>
            <el-button link type="danger" @click="removeItem(index)">删除</el-button>
          </div>
        </article>
      </div>
    </section>

    <div class="plan-actions">
      <el-button type="primary" :loading="saving" @click="saveDraft">保存草稿</el-button>
      <el-button type="success" :loading="saving" @click="saveAndActivate">保存并启用</el-button>
      <el-button v-if="form.id" type="warning" plain :loading="activating" @click="activateCurrentVersion">
        启用当前版本
      </el-button>
    </div>

    <section class="tasks-section">
      <div class="section-header">
        <h4>未来 7 天任务</h4>
        <el-button :disabled="!form.id" @click="reloadDerivedData">刷新任务与报表</el-button>
      </div>
      <div class="table-scroll pro-table-scroll">
        <el-table :data="taskRows" border table-layout="auto" empty-text="暂无任务">
          <el-table-column prop="scheduledAt" label="计划时间" min-width="180">
            <template #default="{ row }">{{ displayDate(row.scheduledAt) }}</template>
          </el-table-column>
          <el-table-column prop="drugName" label="药品" min-width="160" show-overflow-tooltip />
          <el-table-column prop="dosage" label="剂量" min-width="120" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" min-width="120">
            <template #default="{ row }">{{ taskStatusText(row.status) }}</template>
          </el-table-column>
          <el-table-column prop="notificationStatus" label="提醒状态" min-width="140" show-overflow-tooltip />
        </el-table>
      </div>
    </section>

    <section class="report-section">
      <div class="section-header">
        <h4>依从性报表</h4>
      </div>
      <pre v-if="adherenceReport" class="report-box">{{ adherenceReport }}</pre>
      <el-empty v-else description="启用计划后可查看报表摘要" />
    </section>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  activateMedicationPlan,
  createMedicationPlan,
  getMedicationPlan,
  getMedicationPlanAdherenceReport,
  getMedicationPlanTasks,
  updateMedicationPlan
} from '@/modules/pharmacist/api/medicationPlans'

const props = defineProps({
  patientId: {
    type: Number,
    required: true
  },
  latestPlanId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['plan-updated'])

const loading = ref(false)
const saving = ref(false)
const activating = ref(false)
const errorMessage = ref('')
const taskRows = ref([])
const adherenceReport = ref('')

const createEmptyItem = () => ({
  drugName: '',
  drugSpec: '',
  dosage: '',
  frequencyCode: 'BID',
  timePointsText: '08:00,20:00',
  mealRelation: 'AFTER_MEAL',
  route: 'PO',
  instructions: ''
})

const form = reactive({
  id: null,
  versionNo: null,
  status: 'DRAFT',
  planName: '',
  sourceType: 'MANUAL',
  effectiveFrom: '',
  effectiveTo: '',
  notes: '',
  confirmedAt: '',
  items: [createEmptyItem()]
})

const currentPlanLabel = computed(() => {
  if (!form.id) {
    return '未创建'
  }
  return `V${form.versionNo || 1} / #${form.id}`
})

const hydratePlan = (plan) => {
  form.id = plan?.id || null
  form.versionNo = plan?.versionNo || null
  form.status = plan?.status || 'DRAFT'
  form.planName = plan?.planName || ''
  form.sourceType = plan?.sourceType || 'MANUAL'
  form.effectiveFrom = toPickerValue(plan?.effectiveFrom) || toPickerValue(new Date().toISOString())
  form.effectiveTo = toPickerValue(plan?.effectiveTo)
  form.notes = plan?.notes || ''
  form.confirmedAt = plan?.confirmedAt || ''
  form.items = plan?.items?.length
    ? plan.items.map((item) => ({
        drugName: item.drugName || '',
        drugSpec: item.drugSpec || '',
        dosage: item.dosage || '',
        frequencyCode: item.frequencyCode || 'BID',
        timePointsText: Array.isArray(item.timePoints) ? item.timePoints.join(',') : '',
        mealRelation: item.mealRelation || 'AFTER_MEAL',
        route: item.route || 'PO',
        instructions: item.instructions || ''
      }))
    : [createEmptyItem()]
}

const resetDraft = () => {
  errorMessage.value = ''
  taskRows.value = []
  adherenceReport.value = ''
  hydratePlan({
    status: 'DRAFT',
    sourceType: 'MANUAL',
    effectiveFrom: new Date().toISOString(),
    items: [createEmptyItem()]
  })
}

const loadPlan = async (planId) => {
  if (!planId) {
    resetDraft()
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await getMedicationPlan(planId)
    hydratePlan(response.data)
    await loadDerivedData(response.data.id)
  } catch (error) {
    errorMessage.value = error.message || '用药计划加载失败，请稍后重试'
    resetDraft()
  } finally {
    loading.value = false
  }
}

const loadDerivedData = async (planId) => {
  if (!planId) {
    taskRows.value = []
    adherenceReport.value = ''
    return
  }
  const today = new Date()
  const dateFrom = formatDateOnly(today)
  const dateTo = formatDateOnly(new Date(today.getTime() + 6 * 24 * 60 * 60 * 1000))
  const [tasksResponse, reportResponse] = await Promise.all([
    getMedicationPlanTasks(planId, { dateFrom, dateTo }),
    getMedicationPlanAdherenceReport(planId, { dateFrom, dateTo })
  ])
  taskRows.value = tasksResponse.data || []
  adherenceReport.value = reportResponse.data || ''
}

const reloadCurrentPlan = async () => {
  await loadPlan(props.latestPlanId || form.id)
}

const reloadDerivedData = async () => {
  try {
    await loadDerivedData(form.id)
  } catch (error) {
    errorMessage.value = error.message || '计划任务加载失败，请稍后重试'
  }
}

const addItem = () => {
  form.items.push(createEmptyItem())
}

const removeItem = (index) => {
  if (form.items.length === 1) {
    form.items[0] = createEmptyItem()
    return
  }
  form.items.splice(index, 1)
}

const buildPayload = (status) => {
  const items = form.items
    .map((item, index) => ({
      drugName: item.drugName?.trim() || '',
      drugSpec: item.drugSpec?.trim() || '',
      dosage: item.dosage?.trim() || '',
      frequencyCode: item.frequencyCode || '',
      timePoints: splitTimePoints(item.timePointsText),
      mealRelation: item.mealRelation || '',
      route: item.route?.trim() || '',
      instructions: item.instructions?.trim() || '',
      sortOrder: index
    }))
    .filter((item) => item.drugName)

  if (!form.planName?.trim()) {
    throw new Error('请先填写计划名称')
  }
  if (!items.length) {
    throw new Error('请至少录入一条药品项')
  }
  if (items.some((item) => !item.dosage || !item.frequencyCode || item.timePoints.length === 0)) {
    throw new Error('每条药品项都需要填写剂量、频次和服药时间')
  }
  validateAllTimePoints(form.items)

  return {
    patientId: props.patientId,
    planName: form.planName.trim(),
    status,
    sourceType: form.sourceType || 'MANUAL',
    effectiveFrom: toIsoString(form.effectiveFrom),
    effectiveTo: toIsoString(form.effectiveTo),
    notes: form.notes?.trim() || '',
    items
  }
}

const persistPlan = async (status) => {
  saving.value = true
  errorMessage.value = ''
  try {
    const payload = buildPayload(status)
    const response = form.id
      ? await updateMedicationPlan(form.id, payload)
      : await createMedicationPlan(payload)
    hydratePlan(response.data)
    await loadDerivedData(response.data.id)
    emit('plan-updated', response.data)
    ElMessage.success(status === 'ACTIVE' ? '用药计划已保存并启用' : '用药计划草稿已保存')
  } catch (error) {
    errorMessage.value = error.message || '用药计划保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

const saveDraft = async () => {
  await persistPlan('DRAFT')
}

const saveAndActivate = async () => {
  await persistPlan('ACTIVE')
}

const activateCurrentVersion = async () => {
  if (!form.id) {
    return
  }
  activating.value = true
  errorMessage.value = ''
  try {
    const response = await activateMedicationPlan(form.id)
    hydratePlan(response.data)
    await loadDerivedData(response.data.id)
    emit('plan-updated', response.data)
    ElMessage.success('当前版本已启用')
  } catch (error) {
    errorMessage.value = error.message || '计划启用失败，请稍后重试'
  } finally {
    activating.value = false
  }
}

const displayDate = (value) => {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

const statusText = (status) => {
  if (status === 'ACTIVE') return '已启用'
  if (status === 'DRAFT') return '草稿'
  return status || '--'
}

const taskStatusText = (status) => {
  const map = {
    PENDING: '待执行',
    COMPLETED: '已完成',
    MISSED: '已漏服',
    SKIPPED: '已跳过',
    CANCELLED: '已取消'
  }
  return map[status] || status || '--'
}

const HH_MM_RE = /^([01]\d|2[0-3]):[0-5]\d$/

const splitTimePoints = (value) =>
  (value || '')
    .replace(/，/g, ',')
    .split(',')
    .map((entry) => entry.trim())
    .filter(Boolean)

const validateAllTimePoints = (items) => {
  for (let i = 0; i < items.length; i++) {
    const item = items[i]
    if (!item.drugName?.trim()) continue
    const points = splitTimePoints(item.timePointsText)
    if (points.length === 0) {
      throw new Error(`药品项 #${i + 1}「${item.drugName}」缺少服药时间`)
    }
    for (const tp of points) {
      if (!HH_MM_RE.test(tp)) {
        throw new Error(
          `药品项 #${i + 1}「${item.drugName}」的服药时间「${tp}」格式错误，需为 HH:mm（24小时制），如 08:00、20:00`
        )
      }
    }
  }
}

const toIsoString = (value) => {
  if (!value) {
    return null
  }
  const date = new Date(value.replace(' ', 'T'))
  return Number.isNaN(date.getTime()) ? null : date.toISOString()
}

const toPickerValue = (value) => {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  const pad = (input) => String(input).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const formatDateOnly = (date) => {
  const pad = (input) => String(input).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

watch(
  () => props.latestPlanId,
  async (planId) => {
    await loadPlan(planId)
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.panel {
  min-width: 0;
  display: grid;
  gap: 20px;
  padding: 20px;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-lg);
  background: var(--pro-bg-primary);
  box-shadow: var(--pro-shadow-sm);
}

.panel-header,
.header-actions,
.section-header,
.plan-actions,
.item-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-header h3,
.section-header h4,
.summary-card strong {
  margin: 0;
  color: var(--pro-text-primary);
}

.panel-header span,
.summary-card p,
.card-label,
.item-actions span {
  margin: 0;
  color: var(--pro-text-secondary);
}

.summary-grid,
.form-grid,
.item-grid,
.item-list {
  display: grid;
  gap: 16px;
  min-width: 0;
}

.summary-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-grid,
.item-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.summary-card,
.item-card,
.report-box {
  padding: 16px;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-md);
  background: var(--pro-bg-secondary);
}

.summary-card,
.item-card {
  display: grid;
  gap: 8px;
}

.report-box {
  margin: 0;
  overflow: auto;
  white-space: pre-wrap;
  font-family: Consolas, Monaco, monospace;
}

@media (max-width: 1024px) {
  .panel-header,
  .header-actions,
  .section-header,
  .plan-actions,
  .form-grid,
  .item-grid,
  .summary-grid {
    display: grid;
  }
}
</style>
