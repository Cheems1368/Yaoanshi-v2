<template>
  <section class="page-shell pro-page-shell">
    <header class="page-header pro-page-header">
      <div class="pro-page-header__copy">
        <h2>处方复核</h2>
        <p>集中处理患者上传的处方图片，完成人工录入、DDI 提示确认和最终复核。</p>
      </div>
      <div class="query-bar pro-toolbar">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="搜索患者姓名 / 院内患者 ID / 处方 ID"
          class="query-input pro-toolbar__grow"
          @keyup.enter="loadPrescriptions"
        />
        <el-select v-model="query.status" clearable placeholder="全部状态" class="query-select">
          <el-option label="待复核" value="UPLOADED" />
          <el-option label="已复核" value="PHARMACIST_CONFIRMED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </div>
    </header>

    <el-alert v-if="errorMessage" :closable="false" type="error" :title="errorMessage" show-icon />

    <section class="panel pro-table-panel">
      <div class="table-scroll pro-table-scroll">
        <el-table
          v-loading="loading"
          :data="prescriptions"
          border
          table-layout="auto"
          empty-text="暂无处方待处理"
        >
          <el-table-column prop="id" label="处方 ID" width="120" />
          <el-table-column prop="patientName" label="患者" min-width="160" show-overflow-tooltip />
          <el-table-column prop="hospitalPatientId" label="院内患者 ID" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="140">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ocrStatus" label="OCR 状态" min-width="200">
            <template #default="{ row }">
              <span class="pro-ellipsis">{{ ocrStatusText(row.ocrStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="提交时间" min-width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row.id)">进入复核</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-row pro-pagination-row">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" size="720px" :title="detail?.id ? `处方 #${detail.id}` : '处方详情'">
      <el-skeleton v-if="detailLoading" :rows="8" animated />

      <template v-else-if="detail">
        <section class="detail-section">
          <h3>处方概览</h3>
          <div class="summary-grid">
            <article class="summary-card">
              <strong>{{ detail.patientName }}</strong>
              <span>院内患者 ID：{{ detail.hospitalPatientId }}</span>
              <span>当前状态：{{ statusText(detail.status) }}</span>
            </article>
            <article class="summary-card">
              <strong>OCR</strong>
              <span>{{ ocrStatusText(detail.ocrStatus) }}</span>
              <span>置信度：{{ detail.ocrConfidence || '--' }}</span>
            </article>
          </div>
        </section>

        <section class="detail-section">
          <h3>附件信息</h3>
          <div v-if="detail.attachments.length" class="attachment-list">
            <article v-for="attachment in detail.attachments" :key="attachment.id" class="attachment-card">
              <strong>{{ attachment.originalFilename }}</strong>
              <span>{{ attachment.mimeType }} / {{ formatSize(attachment.sizeBytes) }}</span>
              <span>对象键：{{ attachment.objectKey }}</span>
              <span>
                {{
                  attachment.downloadUrl
                    ? '对象存储待接入，可使用当前下载地址占位'
                    : '待对象存储接入'
                }}
              </span>
            </article>
          </div>
          <el-empty v-else description="暂无附件元数据" />
        </section>

        <section class="detail-section">
          <div class="section-header">
            <h3>人工录入药品项</h3>
            <el-button @click="addItem">新增药品项</el-button>
          </div>
          <div class="item-list">
            <article v-for="(item, index) in editableItems" :key="index" class="item-card">
              <div class="item-grid">
                <el-input v-model="item.drugName" placeholder="药品名称" />
                <el-input v-model="item.drugSpec" placeholder="规格" />
                <el-input v-model="item.dosage" placeholder="剂量" />
                <el-input v-model="item.frequency" placeholder="频次" />
                <el-input v-model="item.route" placeholder="给药途径" />
                <el-input-number v-model="item.durationDays" :min="1" :max="365" controls-position="right" />
              </div>
              <div class="item-actions">
                <el-button link type="danger" @click="removeItem(index)">删除</el-button>
              </div>
            </article>
          </div>
        </section>

        <section class="detail-section" v-if="detail.pendingDdiScreenings.length">
          <h3>DDI 风险提示</h3>
          <el-alert
            title="检测到高风险联用，必须先逐条确认后才能完成复核。"
            type="warning"
            :closable="false"
            show-icon
            class="ddi-banner"
          />
          <div class="ddi-list">
            <article v-for="screening in detail.pendingDdiScreenings" :key="screening.id" class="ddi-card">
              <div class="ddi-header">
                <strong>{{ screening.severity }} 风险</strong>
                <el-tag :type="screening.confirmStatus === 'CONFIRMED_CONTINUE' ? 'success' : 'danger'" effect="plain">
                  {{ screening.confirmStatus === 'CONFIRMED_CONTINUE' ? '已确认' : '待确认' }}
                </el-tag>
              </div>
              <p>{{ screening.message || '检测到高风险相互作用' }}</p>
              <p>{{ screening.recommendedAction || '请药师明确记录继续使用理由。' }}</p>
              <el-button
                v-if="screening.confirmStatus !== 'CONFIRMED_CONTINUE'"
                type="warning"
                plain
                @click="handleConfirmScreening(screening.id)"
              >
                确认继续
              </el-button>
            </article>
          </div>
        </section>

        <section class="drawer-actions">
          <el-button v-if="detail.patientId" type="info" plain @click="goToPatientPlan">前往患者计划</el-button>
          <el-button @click="handleReject">驳回处方</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleConfirm">提交复核</el-button>
        </section>
      </template>
    </el-drawer>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  confirmDdiScreening,
  confirmPrescription,
  getPrescriptionDetail,
  getPrescriptions,
  rejectPrescription
} from '@/modules/pharmacist/api/prescriptions'

const router = useRouter()
const loading = ref(false)
const detailLoading = ref(false)
const submitLoading = ref(false)
const errorMessage = ref('')
const prescriptions = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref(null)
const editableItems = ref([])

const query = reactive({
  keyword: '',
  status: '',
  pageNo: 1,
  pageSize: 10
})

const loadPrescriptions = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await getPrescriptions({
      keyword: query.keyword || undefined,
      status: query.status || undefined,
      pageNo: query.pageNo,
      pageSize: query.pageSize
    })
    prescriptions.value = response.data.list || []
    total.value = response.data.total || 0
  } catch (error) {
    prescriptions.value = []
    total.value = 0
    errorMessage.value = error.message || '处方列表加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const openDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const response = await getPrescriptionDetail(id)
    detail.value = response.data
    editableItems.value = response.data.items?.length
      ? response.data.items.map((item) => ({
          drugName: item.drugName || '',
          drugSpec: item.drugSpec || '',
          dosage: item.dosage || '',
          frequency: item.frequency || '',
          route: item.route || '',
          durationDays: item.durationDays || 7
        }))
      : [createEmptyItem()]
  } catch (error) {
    ElMessage.error(error.message || '处方详情加载失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const createEmptyItem = () => ({
  drugName: '',
  drugSpec: '',
  dosage: '',
  frequency: '',
  route: '',
  durationDays: 7
})

const addItem = () => {
  editableItems.value.push(createEmptyItem())
}

const removeItem = (index) => {
  if (editableItems.value.length === 1) {
    editableItems.value[0] = createEmptyItem()
    return
  }
  editableItems.value.splice(index, 1)
}

const handleSearch = () => {
  query.pageNo = 1
  loadPrescriptions()
}

const handlePageChange = (pageNo) => {
  query.pageNo = pageNo
  loadPrescriptions()
}

const buildSubmitItems = () =>
  editableItems.value
    .filter((item) => item.drugName && item.drugName.trim())
    .map((item) => ({
      drugName: item.drugName.trim(),
      drugSpec: item.drugSpec?.trim() || '',
      dosage: item.dosage?.trim() || '',
      frequency: item.frequency?.trim() || '',
      route: item.route?.trim() || '',
      durationDays: item.durationDays || 7
    }))

const reloadCurrentDetail = async () => {
  if (!detail.value?.id) {
    return
  }
  const response = await getPrescriptionDetail(detail.value.id)
  detail.value = response.data
}

const handleConfirm = async () => {
  if (!detail.value?.id) {
    return
  }
  const items = buildSubmitItems()
  if (!items.length) {
    ElMessage.warning('至少录入一条药品项后才能提交复核')
    return
  }
  submitLoading.value = true
  try {
    const response = await confirmPrescription(detail.value.id, { items })
    detail.value = response.data
    ElMessage.success('处方复核已完成')
    await loadPrescriptions()
  } catch (error) {
    if (error.message === 'DDI_CONFIRM_REQUIRED') {
      await reloadCurrentDetail()
      ElMessage.warning('检测到高风险联用，请先确认 DDI 风险后再继续提交')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleConfirmScreening = async (screeningId) => {
  try {
    await confirmDdiScreening(screeningId, { note: '药师已确认继续使用' })
    await reloadCurrentDetail()
    ElMessage.success('DDI 风险已确认')
  } catch (error) {
    ElMessage.error(error.message || 'DDI 风险确认失败')
  }
}

const handleReject = async () => {
  if (!detail.value?.id) {
    return
  }
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回处方', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：图片不清晰，需要重新上传',
      inputValidator: (input) => Boolean(input && input.trim()),
      inputErrorMessage: '请填写驳回原因'
    })
    const response = await rejectPrescription(detail.value.id, { reason: value })
    detail.value = response.data
    ElMessage.success('处方已驳回')
    await loadPrescriptions()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '处方驳回失败')
    }
  }
}

const goToPatientPlan = () => {
  if (!detail.value?.patientId) {
    return
  }
  detailVisible.value = false
  router.push({
    path: '/patient/info',
    query: {
      id: String(detail.value.patientId),
      focus: 'plan'
    }
  })
}

const statusTagType = (status) => {
  switch (status) {
    case 'PHARMACIST_CONFIRMED':
      return 'success'
    case 'REJECTED':
      return 'danger'
    default:
      return 'warning'
  }
}

const statusText = (status) => {
  switch (status) {
    case 'UPLOADED':
      return '待复核'
    case 'PHARMACIST_CONFIRMED':
      return '已复核'
    case 'REJECTED':
      return '已驳回'
    default:
      return status || '--'
  }
}

const ocrStatusText = (status) => {
  switch (status) {
    case 'LOW_CONFIDENCE':
      return '低置信度，转人工复核'
    case 'SUCCESS':
      return '识别完成'
    default:
      return status || '待人工复核'
  }
}

const formatDate = (value) => (value ? new Date(value).toLocaleString('zh-CN') : '--')

const formatSize = (size) => {
  if (!size && size !== 0) {
    return '--'
  }
  if (size < 1024 * 1024) {
    return `${Math.round(size / 1024)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

loadPrescriptions()
</script>

<style lang="scss" scoped>
.query-bar {
  flex: 0 1 720px;
  justify-content: flex-end;
}

.query-select {
  width: 160px;
}

.detail-section h3,
.summary-card strong,
.attachment-card strong,
.ddi-card strong {
  margin: 0;
  color: var(--pro-text-primary);
}

.summary-card span,
.attachment-card span,
.ddi-card p {
  margin: 0;
  color: var(--pro-text-secondary);
}

.detail-section {
  display: grid;
  gap: 16px;
  margin-bottom: 24px;
}

.summary-grid,
.item-grid,
.attachment-list,
.ddi-list {
  display: grid;
  gap: 16px;
}

.summary-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.summary-card,
.attachment-card,
.item-card,
.ddi-card {
  display: grid;
  gap: 8px;
  padding: 16px;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-md);
  background: var(--pro-bg-secondary);
}

.item-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.item-actions,
.drawer-actions,
.section-header,
.ddi-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.drawer-actions {
  justify-content: flex-end;
}

.ddi-banner {
  margin-bottom: 4px;
}

@media (max-width: 1024px) {
  .query-select {
    width: 100%;
  }

  .summary-grid,
  .item-grid {
    display: grid;
  }
}
</style>
