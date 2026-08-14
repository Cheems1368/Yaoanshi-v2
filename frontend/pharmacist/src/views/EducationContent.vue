<template>
  <section class="page-shell pro-page-shell">
    <header class="page-header pro-page-header">
      <div class="pro-page-header__copy">
        <h2>用药教育内容</h2>
        <p>通过 CSV 维护已审核教育卡片，患者端只展示 REVIEWED 内容。</p>
      </div>
    </header>

    <section class="panel pro-table-panel">
      <div class="query-bar pro-toolbar">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="搜索标题 / 摘要 / 正文"
          class="query-input pro-toolbar__grow"
          @keyup.enter="handleSearch"
        />
        <el-input v-model="query.category" clearable placeholder="分类编码" class="query-select" @keyup.enter="handleSearch" />
        <el-select v-model="query.reviewStatus" clearable placeholder="审核状态" class="query-select">
          <el-option v-for="item in reviewStatusOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <div class="toolbar-actions pro-toolbar__actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="loadCards">刷新</el-button>
          <el-button @click="downloadTemplate">下载模板</el-button>
          <el-button @click="triggerImport">CSV 导入</el-button>
        </div>
      </div>

      <el-alert v-if="error" :closable="false" type="error" :title="error" show-icon />

      <div class="table-scroll pro-table-scroll">
        <el-table v-loading="loading" :data="cards" border table-layout="auto" empty-text="暂无教育内容">
          <el-table-column prop="contentCode" label="内容编码" min-width="160" show-overflow-tooltip />
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" min-width="140">
            <template #default="{ row }">{{ row.categoryName || row.category || '--' }}</template>
          </el-table-column>
          <el-table-column prop="reviewStatus" label="状态" width="130">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.reviewStatus)" effect="plain">{{ row.reviewStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标签" min-width="260">
            <template #default="{ row }">
              <div class="tag-list">
                <el-tag v-for="tag in mergedTags(row)" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
                <span v-if="mergedTags(row).length === 0">--</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="reviewedAt" label="审核时间" min-width="180">
            <template #default="{ row }">{{ formatDate(row.reviewedAt) }}</template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" min-width="180">
            <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
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

    <input ref="importInputRef" class="hidden-file-input" type="file" accept=".csv,text/csv" @change="handleFileChange" />

    <el-dialog v-model="previewVisible" title="教育内容 CSV 预览" width="760px">
      <div v-if="importResult" class="import-summary">
        <el-statistic title="总行数" :value="importResult.totalRows || 0" />
        <el-statistic title="新增" :value="importResult.createCount || 0" />
        <el-statistic title="覆盖更新" :value="importResult.updateCount || 0" />
        <el-statistic title="错误行" :value="importResult.errorCount || 0" />
        <el-statistic title="警告" :value="importResult.warningCount || 0" />
      </div>
      <el-table :data="importResult?.rows || []" border max-height="360" table-layout="auto">
        <el-table-column prop="rowNumber" label="行号" width="80" />
        <el-table-column prop="key" label="内容编码" min-width="160" show-overflow-tooltip />
        <el-table-column prop="action" label="动作" width="110">
          <template #default="{ row }">
            <el-tag :type="row.action === 'ERROR' ? 'danger' : row.action === 'UPDATE' ? 'warning' : 'success'" effect="plain">
              {{ row.action }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="错误" min-width="220">
          <template #default="{ row }">{{ formatImportMessages(row.errors) }}</template>
        </el-table-column>
        <el-table-column label="警告" min-width="220">
          <template #default="{ row }">{{ formatImportMessages(row.warnings) }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="previewVisible = false">关闭</el-button>
          <el-button
            type="primary"
            :disabled="!importResult || importResult.errorCount > 0"
            :loading="applying"
            @click="applyImport"
          >
            确认导入
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getEducationCards, importEducationCards } from '@/modules/pharmacist/api/education'

const reviewStatusOptions = ['DRAFT', 'REVIEWED', 'INACTIVE']
const csvHeader = 'contentCode,title,summary,body,category,categoryName,diagnosisTags,drugTags,populationTags,source,reviewStatus,reviewedBy,reviewedAt'

const loading = ref(false)
const error = ref('')
const cards = ref([])
const total = ref(0)
const importInputRef = ref()
const previewVisible = ref(false)
const importFile = ref(null)
const importResult = ref(null)
const applying = ref(false)

const query = reactive({
  keyword: '',
  category: '',
  reviewStatus: '',
  pageNo: 1,
  pageSize: 10
})

const loadCards = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await getEducationCards({
      keyword: query.keyword || undefined,
      category: query.category || undefined,
      reviewStatus: query.reviewStatus || undefined,
      pageNo: query.pageNo,
      pageSize: query.pageSize
    })
    cards.value = response.data?.list || []
    total.value = response.data?.total || 0
  } catch (err) {
    cards.value = []
    total.value = 0
    error.value = err.message || '教育内容加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNo = 1
  loadCards()
}

const handlePageChange = (page) => {
  query.pageNo = page
  loadCards()
}

const triggerImport = () => {
  importInputRef.value?.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  importFile.value = file
  try {
    const response = await importEducationCards(file, true)
    importResult.value = response.data
    previewVisible.value = true
  } catch (err) {
    importResult.value = null
    ElMessage.error(err.message || 'CSV 预览失败')
  }
}

const applyImport = async () => {
  if (!importFile.value) return
  applying.value = true
  try {
    const response = await importEducationCards(importFile.value, false)
    importResult.value = response.data
    ElMessage.success('CSV 导入完成')
    await loadCards()
  } catch (err) {
    ElMessage.error(err.message || 'CSV 导入失败')
  } finally {
    applying.value = false
  }
}

const downloadTemplate = () => {
  const blob = new Blob([`${csvHeader}\n`], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'education-content-template.csv'
  link.click()
  URL.revokeObjectURL(url)
}

const mergedTags = (row) => {
  return [...(row.diagnosisTags || []), ...(row.drugTags || []), ...(row.populationTags || [])]
}

const statusTagType = (status) => {
  if (status === 'REVIEWED') return 'success'
  if (status === 'INACTIVE') return 'info'
  return 'warning'
}

const formatImportMessages = (messages) => {
  return Array.isArray(messages) && messages.length > 0 ? messages.join('；') : '--'
}

const formatDate = (value) => {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

watch(
  () => [query.reviewStatus],
  () => handleSearch()
)

loadCards()
</script>

<style lang="scss" scoped>
.query-bar {
  margin-bottom: 16px;
}

.query-select {
  width: 180px;
}

.hidden-file-input {
  display: none;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.import-summary {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1024px) {
  .query-select {
    width: 100%;
  }

  .import-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
