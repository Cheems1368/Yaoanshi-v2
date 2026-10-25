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
          <el-option label="OCR 已预填" value="OCR_DONE" />
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

        <section v-if="detail.ocrRawText || detail.ocrFailureReason" class="detail-section">
          <h3>OCR 识别详情</h3>
          <el-alert
            v-if="detail.ocrFailureReason"
            type="warning"
            :closable="false"
            :title="`OCR 需要人工兜底：${detail.ocrFailureReason}`"
            show-icon
          />
          <el-collapse v-if="detail.ocrRawText">
            <el-collapse-item title="查看 OCR 原文" name="ocr-raw">
              <pre class="ocr-raw">{{ detail.ocrRawText }}</pre>
            </el-collapse-item>
          </el-collapse>
        </section>

        <section class="detail-section">
          <h3>附件信息</h3>
          <div v-if="detail.attachments.length" class="attachment-list">
            <article v-for="attachment in detail.attachments" :key="attachment.id" class="attachment-card">
              <strong>{{ attachment.originalFilename }}</strong>
              <el-image
                v-if="attachment.downloadUrl"
                class="prescription-image"
                :src="attachment.downloadUrl"
                :preview-src-list="[attachment.downloadUrl]"
                preview-teleported
                fit="contain"
                alt="原始处方图片，点击放大核对"
              />
              <span>{{ attachment.mimeType }} / {{ formatSize(attachment.sizeBytes) }}</span>
              <span>对象键：{{ attachment.objectKey }}</span>
              <span>
                {{
                  attachment.downloadUrl
                    ? '点击图片放大核对原始处方'
                    : '原始处方图片暂不可用'
                }}
              </span>
            </article>
          </div>
          <el-empty v-else description="暂无附件元数据" />
        </section>

        <section class="detail-section">
          <div class="section-header">
            <h3>药品项（OCR 预填 / 人工确认）</h3>
            <el-button @click="addItem">新增药品项</el-button>
          </div>
          <div class="item-list">
            <article v-for="(item, index) in editableItems" :key="index" class="item-card">
              <div class="item-heading">
                <el-tag size="small" :type="item.source === 'OCR' ? 'success' : 'info'" effect="plain">
                  {{ item.source === 'OCR' ? 'OCR 草稿' : '人工录入' }}
                </el-tag>
              </div>
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
          <AdminDdiRiskCardList
            v-model="expandedScreeningKey"
            :alerts="detail.pendingDdiScreenings"
            :show-confirm-button="true"
            :show-confirm-state="true"
            :show-confirmation-note="true"
            @confirm="handleConfirmScreening"
          />
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
import AdminDdiRiskCardList from '@/components/ddi/AdminDdiRiskCardList.vue'
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
const expandedScreeningKey = ref('')

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
    expandedScreeningKey.value = ''
    editableItems.value = response.data.items?.length
      ? response.data.items.map((item) => ({
          drugName: item.drugName || '',
          drugSpec: item.drugSpec || '',
          dosage: item.dosage || '',
          frequency: item.frequency || '',
          route: item.route || '',
          durationDays: item.durationDays ?? null,
          source: item.source || 'MANUAL'
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
  durationDays: null,
  source: 'MANUAL'
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
      durationDays: item.durationDays ?? null
    }))

const reloadCurrentDetail = async () => {
  if (!detail.value?.id) {
    return
  }
  const response = await getPrescriptionDetail(detail.value.id)
  detail.value = response.data
  expandedScreeningKey.value = ''
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
    if ((error.response?.data?.code ?? error.code) === 40013 ||
        (error.response?.data?.msg ?? error.message) === 'DDI_CONFIRM_REQUIRED') {
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
    case 'OCR_DONE':
      return 'OCR 已预填'
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
    case 'HIGH_CONFIDENCE':
      return '高置信度，识别完成'
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

.ddi-list {
  display: none;
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

.prescription-image {
  width: 100%;
  height: 240px;
  border-radius: var(--pro-radius-sm);
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

.item-heading {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 2px;
}

.ocr-raw {
  margin: 0;
  padding: 12px;
  border-radius: var(--pro-radius-sm);
  background: var(--pro-bg-secondary);
  color: var(--pro-text-primary);
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
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
