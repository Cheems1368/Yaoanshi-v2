<template>
  <div class="empty-state">
    <div class="empty-state__illustration" aria-hidden="true">
      <svg v-if="type === 'no-plan'" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect x="16" y="24" width="88" height="76" rx="10" stroke="#E2E8F0" stroke-width="3" fill="#F8FAFC"/>
        <rect x="16" y="24" width="88" height="22" rx="10" fill="#DBEAFF"/>
        <rect x="16" y="35" width="88" height="11" fill="#DBEAFF"/>
        <line x1="36" y1="16" x2="36" y2="34" stroke="#2979FF" stroke-width="3" stroke-linecap="round"/>
        <line x1="84" y1="16" x2="84" y2="34" stroke="#2979FF" stroke-width="3" stroke-linecap="round"/>
        <circle cx="42" cy="62" r="4" fill="#E2E8F0"/>
        <circle cx="60" cy="62" r="4" fill="#E2E8F0"/>
        <circle cx="78" cy="62" r="4" fill="#E2E8F0"/>
        <circle cx="42" cy="80" r="4" fill="#E2E8F0"/>
        <circle cx="60" cy="80" r="4" fill="#E2E8F0"/>
        <circle cx="78" cy="80" r="4" fill="#E2E8F0"/>
      </svg>

      <svg v-else-if="type === 'no-records'" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect x="20" y="16" width="80" height="88" rx="10" stroke="#E2E8F0" stroke-width="3" fill="#F8FAFC"/>
        <rect x="20" y="16" width="80" height="26" rx="10" fill="#DBEAFF"/>
        <rect x="20" y="31" width="80" height="11" fill="#DBEAFF"/>
        <circle cx="38" cy="29" r="5" fill="#2979FF"/>
        <line x1="33" y1="56" x2="87" y2="56" stroke="#E2E8F0" stroke-width="3" stroke-linecap="round"/>
        <line x1="33" y1="70" x2="75" y2="70" stroke="#E2E8F0" stroke-width="3" stroke-linecap="round"/>
        <line x1="33" y1="84" x2="80" y2="84" stroke="#E2E8F0" stroke-width="3" stroke-linecap="round"/>
      </svg>

      <svg v-else-if="type === 'no-orders'" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect x="14" y="28" width="72" height="56" rx="12" fill="#DBEAFF" stroke="#2979FF" stroke-width="2.5"/>
        <circle cx="90" cy="36" r="16" fill="#F8FAFC" stroke="#E2E8F0" stroke-width="2.5"/>
        <line x1="84" y1="36" x2="96" y2="36" stroke="#E2E8F0" stroke-width="2.5" stroke-linecap="round"/>
        <line x1="90" y1="30" x2="90" y2="42" stroke="#E2E8F0" stroke-width="2.5" stroke-linecap="round"/>
        <line x1="30" y1="52" x2="70" y2="52" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
        <line x1="30" y1="64" x2="58" y2="64" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
        <path d="M26 84 L34 76" stroke="#2979FF" stroke-width="2.5" stroke-linecap="round"/>
      </svg>

      <svg v-else-if="type === 'no-results'" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="52" cy="52" r="30" stroke="#E2E8F0" stroke-width="3" fill="#F8FAFC"/>
        <circle cx="52" cy="52" r="30" stroke="#DBEAFF" stroke-width="3"/>
        <line x1="73" y1="73" x2="96" y2="96" stroke="#2979FF" stroke-width="3.5" stroke-linecap="round"/>
        <line x1="42" y1="42" x2="62" y2="62" stroke="#E2E8F0" stroke-width="2.5" stroke-linecap="round"/>
        <line x1="62" y1="42" x2="42" y2="62" stroke="#E2E8F0" stroke-width="2.5" stroke-linecap="round"/>
      </svg>

      <svg v-else viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect x="24" y="32" width="72" height="72" rx="12" fill="#F8FAFC" stroke="#E2E8F0" stroke-width="3"/>
        <path d="M48 20 L72 20 L80 32 L40 32 Z" fill="#DBEAFF" stroke="#E2E8F0" stroke-width="2" stroke-linejoin="round"/>
        <line x1="38" y1="58" x2="82" y2="58" stroke="#E2E8F0" stroke-width="2.5" stroke-linecap="round"/>
        <line x1="38" y1="72" x2="72" y2="72" stroke="#E2E8F0" stroke-width="2.5" stroke-linecap="round"/>
        <circle cx="60" cy="90" r="6" fill="#DBEAFF"/>
        <line x1="60" y1="86" x2="60" y2="88" stroke="#2979FF" stroke-width="2" stroke-linecap="round"/>
        <circle cx="60" cy="92" r="1" fill="#2979FF"/>
      </svg>
    </div>

    <p class="empty-state__title">{{ title || defaultTitles[type] }}</p>
    <p v-if="description || defaultDescriptions[type]" class="empty-state__desc">
      {{ description || defaultDescriptions[type] }}
    </p>
    <div v-if="$slots.action" class="empty-state__action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  type?: 'no-data' | 'no-plan' | 'no-records' | 'no-orders' | 'no-results'
  title?: string
  description?: string
}>(), {
  type: 'no-data',
})

const defaultTitles: Record<string, string> = {
  'no-data': '暂无数据',
  'no-plan': '暂无用药计划',
  'no-records': '暂无记录',
  'no-orders': '暂无求助单',
  'no-results': '未找到相关结果',
}

const defaultDescriptions: Record<string, string> = {
  'no-data': '',
  'no-plan': '您目前没有用药计划，请联系医生或药剂师',
  'no-records': '还没有服药记录，按时用药后会显示在这里',
  'no-orders': '您还没有提交过求助，有问题随时发起',
  'no-results': '尝试换个关键词重新搜索',
}
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--patient-space-8, 32px) var(--patient-space-4, 16px);
  text-align: center;
}

.empty-state__illustration {
  width: 120px;
  height: 120px;
  margin-bottom: var(--patient-space-4, 16px);
}

.empty-state__illustration svg {
  width: 100%;
  height: 100%;
}

.empty-state__title {
  font-size: var(--patient-font-subtitle, 19px);
  font-weight: var(--patient-font-medium, 500);
  color: var(--patient-text-secondary, #556C8A);
  margin: 0 0 var(--patient-space-2, 8px);
  line-height: var(--patient-leading-normal, 1.5);
}

.empty-state__desc {
  font-size: var(--patient-font-helper, 15px);
  color: var(--patient-text-muted, #94a3b8);
  margin: 0 0 var(--patient-space-4, 16px);
  line-height: var(--patient-leading-relaxed, 1.7);
  max-width: 260px;
}

.empty-state__action {
  margin-top: var(--patient-space-3, 12px);
}
</style>
