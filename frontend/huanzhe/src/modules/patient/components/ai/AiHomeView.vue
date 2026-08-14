<template>
  <div class="home-container" data-testid="ai-home-view">
    <section class="hero">
      <div class="icon-wrap">
        <AppIcon name="sparkles" :size="32" />
      </div>
      <h2>用药科普助手</h2>
      <p>可解答服药时间、漏服处理、常见不良反应和复诊提醒。</p>
    </section>

    <section class="topic-card">
      <div class="topic-header">
        <button
          v-for="tab in tabs"
          :key="tab"
          class="tab-button"
          :class="{ active: currentTab === tab }"
          @click="currentTab = tab"
        >
          {{ tab }}
          <span v-if="currentTab === tab" class="tab-indicator" />
        </button>
      </div>

      <div class="question-list">
        <div
          v-for="(question, index) in currentQuestions"
          :key="`${currentTab}-${question}`"
          class="question-item"
          :data-testid="`ai-home-question-${index}`"
          @click="$emit('start-chat', question)"
        >
          <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
          <span class="question-text">{{ question }}</span>
          <AppIcon class="arrow" name="chevronForwardOutline" :size="14" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

defineEmits<{
  'start-chat': [question: string]
}>()

const tabs = ['热门问题', '慢病随访', '用药风险']
const currentTab = ref('热门问题')

const topicQuestions: Record<string, string[]> = {
  热门问题: [
    '降压药漏服后该怎么处理？',
    '阿司匹林和氯吡格雷能否同服？',
    '长期服药为什么还要复查？',
    '出现轻微头晕是否要停药？'
  ],
  慢病随访: [
    '血压早晚波动大该如何记录？',
    '糖尿病患者如何安排服药和饮食？',
    '复诊前需要准备哪些用药信息？'
  ],
  用药风险: [
    '哪些情况需要立即线下就医？',
    '老年人多药联用要重点关注什么？',
    '出现不良反应后如何描述给医生？'
  ]
}

const currentQuestions = computed(() => topicQuestions[currentTab.value] || [])
</script>

<style scoped>
.home-container {
  padding: var(--patient-space-4);
}

/* Hero section */
.hero {
  background: var(--patient-bg-card);
  border-radius: var(--patient-radius-xl);
  padding: var(--patient-space-7) var(--patient-space-5) var(--patient-space-6);
  text-align: center;
  box-shadow: var(--patient-shadow-sm);
  margin-bottom: var(--patient-space-4);
}

.icon-wrap {
  width: 64px;
  height: 64px;
  margin: 0 auto var(--patient-space-4);
  border-radius: 50%;
  background: var(--patient-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--patient-primary);
}

.hero h2 {
  margin: 0 0 var(--patient-space-2);
  font-size: var(--patient-font-subtitle);
  font-weight: var(--patient-font-bold);
  color: var(--patient-text-primary);
}

.hero p {
  margin: 0;
  color: var(--patient-text-secondary);
  font-size: var(--patient-font-helper);
  line-height: var(--patient-leading-relaxed);
}

/* Topic card */
.topic-card {
  background: var(--patient-bg-card);
  border-radius: var(--patient-radius-lg);
  box-shadow: var(--patient-shadow-sm);
  overflow: hidden;
}

.topic-header {
  display: flex;
  padding: 0 var(--patient-space-1);
  border-bottom: 1px solid var(--patient-border-light);
}

.tab-button {
  flex: 1;
  position: relative;
  border: none;
  background: transparent;
  padding: var(--patient-space-4) var(--patient-space-2) var(--patient-space-3);
  color: var(--patient-text-muted);
  font-weight: var(--patient-font-semibold);
  font-size: var(--patient-font-helper);
  transition: color 0.2s ease;
}

.tab-button.active {
  color: var(--patient-primary);
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  border-radius: 2px;
  background: var(--patient-primary);
}

/* Question list */
.question-list {
  padding: var(--patient-space-1) 0;
}

.question-item {
  display: flex;
  align-items: center;
  padding: var(--patient-space-4);
  cursor: pointer;
  transition: background 0.15s ease;
}

.question-item:active {
  background: var(--patient-bg-page);
}

.rank {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--patient-font-caption);
  font-weight: var(--patient-font-bold);
  margin-right: var(--patient-space-3);
  flex-shrink: 0;
  background: var(--patient-border);
  color: var(--patient-text-muted);
}

.rank.top {
  background: rgba(16, 185, 129, 0.12);
  color: var(--patient-success);
}

.question-text {
  flex: 1;
  font-size: var(--patient-font-helper);
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-normal);
}

.arrow {
  color: var(--patient-text-muted);
  margin-left: var(--patient-space-2);
  flex-shrink: 0;
}
</style>
