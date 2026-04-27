<template>
  <div class="ddi-risk-list">
    <article
      v-for="(alert, index) in alerts"
      :key="cardKey(alert, index)"
      class="ddi-risk-card"
      :class="[
        `ddi-risk-card--${(alert.severity || '').toLowerCase()}`,
        { 'is-expanded': isExpanded(alert, index) }
      ]"
      role="button"
      tabindex="0"
      @click="toggle(alert, index)"
      @keydown.enter.prevent="toggle(alert, index)"
      @keydown.space.prevent="toggle(alert, index)"
    >
      <div class="ddi-risk-card__head">
        <div class="ddi-risk-card__badges">
          <el-tag size="small" effect="plain" :type="severityTagType(alert.severity)">
            {{ severityLabel(alert.severity) }}
          </el-tag>
          <el-tag size="small" effect="plain" type="info">
            {{ alertTypeLabel(alert.alertType) }}
          </el-tag>
          <el-tag
            v-if="showConfirmState && alert.confirmStatus"
            size="small"
            effect="plain"
            :type="alert.confirmStatus === 'CONFIRMED_CONTINUE' ? 'success' : 'warning'"
          >
            {{ alert.confirmStatus === 'CONFIRMED_CONTINUE' ? '已确认' : '待确认' }}
          </el-tag>
        </div>
      </div>

      <div class="ddi-risk-card__title">
        <strong>{{ alert.drugA }} + {{ alert.drugB }}</strong>
      </div>

      <div class="ddi-risk-card__subhead">
        <div class="ddi-risk-card__meta">
          <span v-if="alert.ruleCode" class="ddi-risk-card__meta-item ddi-risk-card__meta-item--code">
            {{ alert.ruleCode }}
          </span>
          <span
            v-if="alert.alertType === 'CONTRAINDICATION' && alert.conditionType"
            class="ddi-risk-card__meta-item"
          >
            条件类型：{{ conditionTypeLabel(alert.conditionType) }}
          </span>
        </div>
        <button
          type="button"
          class="ddi-risk-card__toggle"
          @click.stop="toggle(alert, index)"
        >
          <span>{{ isExpanded(alert, index) ? '收起详情' : '查看详情' }}</span>
          <el-icon><component :is="isExpanded(alert, index) ? ArrowUp : ArrowDown" /></el-icon>
        </button>
      </div>

      <p class="ddi-risk-card__message">{{ alert.message || '暂无风险说明' }}</p>
      <p v-if="alert.recommendedAction" class="ddi-risk-card__action">
        <span class="ddi-risk-card__action-label">建议动作</span>
        <span>{{ alert.recommendedAction }}</span>
      </p>

      <div v-if="isExpanded(alert, index)" class="ddi-risk-card__details">
        <template v-if="hasExpandedContent(alert)">
          <div v-if="alert.mechanismDesc" class="ddi-risk-card__detail-row">
            <span class="ddi-risk-card__detail-label">机制说明</span>
            <p>{{ alert.mechanismDesc }}</p>
          </div>
          <div v-if="alert.alternativeRecommendation" class="ddi-risk-card__detail-row">
            <span class="ddi-risk-card__detail-label">替代建议</span>
            <p>{{ alert.alternativeRecommendation }}</p>
          </div>
          <div v-if="alert.referenceSource" class="ddi-risk-card__detail-row">
            <span class="ddi-risk-card__detail-label">参考来源</span>
            <p>{{ alert.referenceSource }}</p>
          </div>
          <div v-if="alert.patientSummary" class="ddi-risk-card__detail-row">
            <span class="ddi-risk-card__detail-label">患者摘要</span>
            <p>{{ alert.patientSummary }}</p>
          </div>
          <div
            v-if="showConfirmationNote && alert.confirmationNote"
            class="ddi-risk-card__detail-row"
          >
            <span class="ddi-risk-card__detail-label">确认备注</span>
            <p>{{ alert.confirmationNote }}</p>
          </div>
        </template>
        <p v-else class="ddi-risk-card__empty">暂无详细参考</p>
      </div>

      <div v-if="showConfirmButton && alert.confirmStatus !== 'CONFIRMED_CONTINUE'" class="ddi-risk-card__footer">
        <el-button
          type="warning"
          plain
          @click.stop="$emit('confirm', alert.screeningId)"
        >
          确认继续
        </el-button>
      </div>
    </article>
  </div>
</template>

<script setup>
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'

const props = defineProps({
  alerts: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: String,
    default: ''
  },
  showConfirmButton: {
    type: Boolean,
    default: false
  },
  showConfirmState: {
    type: Boolean,
    default: false
  },
  showConfirmationNote: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const cardKey = (alert, index) => String(alert.cardKey || alert.screeningId || `${alert.ruleCode || 'rule'}-${index}`)

const isExpanded = (alert, index) => props.modelValue === cardKey(alert, index)

const toggle = (alert, index) => {
  const next = isExpanded(alert, index) ? '' : cardKey(alert, index)
  emit('update:modelValue', next)
}

const hasExpandedContent = (alert) =>
  Boolean(
    alert.mechanismDesc ||
      alert.alternativeRecommendation ||
      alert.referenceSource ||
      alert.patientSummary ||
      (props.showConfirmationNote && alert.confirmationNote)
  )

const severityTagType = (severity) => {
  if (severity === 'HIGH') return 'danger'
  if (severity === 'MEDIUM') return 'warning'
  return 'info'
}

const severityLabel = (severity) => {
  if (severity === 'HIGH') return '高风险'
  if (severity === 'MEDIUM') return '中风险'
  if (severity === 'LOW') return '低风险'
  return severity || '风险'
}

const alertTypeLabel = (alertType) => {
  if (alertType === 'CONTRAINDICATION') return '禁忌'
  return '相互作用'
}

const conditionTypeLabel = (conditionType) => {
  const map = {
    DISEASE: '疾病',
    ALLERGY: '过敏',
    ORGAN_FUNCTION: '器官功能'
  }
  return map[conditionType] || conditionType || '--'
}
</script>

<style lang="scss" scoped>
.ddi-risk-list {
  display: grid;
  gap: 16px;
  min-width: 0;
  padding-right: 4px;
}

.ddi-risk-card {
  --ddi-accent: var(--pro-border-color);
  --ddi-accent-bg: var(--pro-bg-secondary);
  --ddi-border: var(--pro-border-color);
  border-radius: 16px;
  border: 1px solid var(--ddi-border);
  background: var(--pro-bg-primary);
  box-shadow: var(--pro-shadow-sm);
  padding: 20px;
  transition:
    border-color 240ms var(--ease-standard, cubic-bezier(0.2, 0, 0, 1)),
    box-shadow 240ms var(--ease-standard, cubic-bezier(0.2, 0, 0, 1)),
    transform 160ms var(--ease-standard, cubic-bezier(0.2, 0, 0, 1));
  cursor: pointer;
  outline: none;
  position: relative;
  min-width: 0;
  height: auto;
  overflow: visible;
}

.ddi-risk-card::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: var(--ddi-accent);
}

.ddi-risk-card:hover,
.ddi-risk-card:focus-visible {
  border-color: var(--ddi-accent);
  box-shadow: var(--pro-shadow-md);
}

.ddi-risk-card.is-expanded {
  border-color: var(--ddi-accent);
}

.ddi-risk-card--high {
  --ddi-accent: var(--pro-danger);
  --ddi-accent-bg: var(--pro-danger-bg);
  --ddi-border: var(--pro-danger);
}

.ddi-risk-card--medium {
  --ddi-accent: var(--pro-warning);
  --ddi-accent-bg: var(--pro-warning-bg);
  --ddi-border: var(--pro-warning);
}

.ddi-risk-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.ddi-risk-card__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ddi-risk-card__toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid var(--pro-border-color);
  border-radius: 12px;
  background: var(--pro-bg-secondary);
  color: var(--pro-text-secondary);
  cursor: pointer;
  font-size: 12px;
  white-space: nowrap;
}

.ddi-risk-card__title {
  margin-top: 12px;
}

.ddi-risk-card__title strong {
  display: block;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--pro-text-primary);
}

.ddi-risk-card__subhead {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.ddi-risk-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ddi-risk-card__meta-item {
  min-height: 24px;
  display: inline-flex;
  align-items: center;
  padding: 0 8px;
  border-radius: 8px;
  background: var(--pro-bg-secondary);
  color: var(--pro-text-secondary);
  font-size: 12px;
  line-height: 1.4;
}

.ddi-risk-card__meta-item--code {
  background: var(--ddi-accent-bg);
  color: var(--ddi-accent);
}

.ddi-risk-card__message,
.ddi-risk-card__empty,
.ddi-risk-card__detail-row p {
  margin: 8px 0 0;
  color: var(--pro-text-secondary);
  line-height: 1.6;
  font-size: 14px;
}

.ddi-risk-card__action {
  margin: 12px 0 0;
  padding: 12px;
  display: grid;
  gap: 4px;
  border-radius: 12px;
  background: var(--ddi-accent-bg);
  color: var(--pro-text-primary);
  font-size: 14px;
  line-height: 1.6;
}

.ddi-risk-card__action-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--ddi-accent);
}

.ddi-risk-card__details {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--pro-border-color);
  display: grid;
  gap: 12px;
  min-width: 0;
}

.ddi-risk-card__detail-row {
  display: grid;
  padding: 12px;
  gap: 4px;
  border: 1px solid var(--pro-border-color);
  border-radius: 12px;
  background: var(--pro-bg-secondary);
}

.ddi-risk-card__detail-label {
  font-size: 12px;
  color: var(--pro-text-secondary);
}

.ddi-risk-card__footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  min-width: 0;
}

@media (max-width: 768px) {
  .ddi-risk-card {
    padding: 16px;
  }

  .ddi-risk-card__head,
  .ddi-risk-card__subhead {
    flex-direction: column;
    align-items: flex-start;
  }

  .ddi-risk-card__title strong {
    font-size: 20px;
  }

  .ddi-risk-card__toggle {
    width: 100%;
    justify-content: center;
  }
}
</style>
