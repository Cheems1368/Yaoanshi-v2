<template>
  <div class="home-container" data-testid="ai-home-view">
    <section class="hero">
      <div class="icon-wrap">
        <ion-icon :icon="sparklesOutline" />
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
          <ion-icon class="arrow" :icon="chevronForwardOutline" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { IonIcon } from '@ionic/vue'
import { chevronForwardOutline, sparklesOutline } from 'ionicons/icons'

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
  padding: 16px;
}

/* Hero section */
.hero {
  background: #ffffff;
  border-radius: 20px;
  padding: 28px 20px 24px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.icon-wrap {
  width: 64px;
  height: 64px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, #EBF4FF 0%, #DBEAFE 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrap ion-icon {
  color: #3B82F6;
  font-size: 32px;
}

.hero h2 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: #1E293B;
}

.hero p {
  margin: 0;
  color: #64748B;
  font-size: 14px;
  line-height: 1.6;
}

/* Topic card */
.topic-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.topic-header {
  display: flex;
  padding: 0 4px;
  border-bottom: 1px solid #F1F5F9;
}

.tab-button {
  flex: 1;
  position: relative;
  border: none;
  background: transparent;
  padding: 14px 8px 12px;
  color: #94A3B8;
  font-weight: 600;
  font-size: 14px;
  transition: color 0.2s ease;
}

.tab-button.active {
  color: #3B82F6;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  border-radius: 2px;
  background: #3B82F6;
}

/* Question list */
.question-list {
  padding: 4px 0;
}

.question-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.question-item:active {
  background: #F8FAFC;
}

.rank {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  margin-right: 12px;
  flex-shrink: 0;
  background: #F1F5F9;
  color: #94A3B8;
}

.rank.top {
  background: #ECFDF5;
  color: #10B981;
}

.question-text {
  flex: 1;
  font-size: 14px;
  color: #334155;
  line-height: 1.5;
}

.arrow {
  color: #CBD5E1;
  font-size: 14px;
  margin-left: 8px;
  flex-shrink: 0;
}
</style>
