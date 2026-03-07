<template>
  <div class="home-container">
    <section class="hero">
      <div class="icon-wrap">
        <ion-icon :icon="sparklesOutline" />
      </div>
      <h2>AI 用药科普助手</h2>
      <p>可咨询常见用药问题、注意事项和风险提示。</p>
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
        </button>
      </div>

      <ion-list lines="none">
        <ion-item
          v-for="(question, index) in currentQuestions"
          :key="`${currentTab}-${question}`"
          button
          @click="$emit('start-chat', question)"
        >
          <div class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</div>
          <ion-label>{{ question }}</ion-label>
          <ion-icon slot="end" :icon="chevronForwardOutline" />
        </ion-item>
      </ion-list>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { IonIcon, IonItem, IonLabel, IonList } from '@ionic/vue'
import { chevronForwardOutline, sparklesOutline } from 'ionicons/icons'

defineEmits<{
  'start-chat': [question: string]
}>()

const tabs = ['热门问题', '慢病随访', '用药风险']
const currentTab = ref('热门问题')

const topicQuestions: Record<string, string[]> = {
  热门问题: [
    '漏服降压药后该怎么处理？',
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

.hero {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px 16px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.08);
  margin-bottom: 14px;
}

.icon-wrap {
  width: 56px;
  height: 56px;
  margin: 0 auto 10px;
  border-radius: 50%;
  background: #dbeafe;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrap ion-icon {
  color: #2563eb;
  font-size: 28px;
}

.hero h2 {
  margin: 0 0 8px;
  color: #0f172a;
}

.hero p {
  margin: 0;
  color: #64748b;
  line-height: 1.6;
}

.topic-card {
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.topic-header {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border-bottom: 1px solid #e2e8f0;
}

.tab-button {
  border: none;
  background: #ffffff;
  padding: 12px 8px;
  color: #64748b;
  font-weight: 600;
  font-size: 14px;
}

.tab-button.active {
  color: #1d4ed8;
  background: #eff6ff;
}

.rank {
  width: 20px;
  text-align: center;
  color: #94a3b8;
  font-weight: 700;
  margin-right: 10px;
}

.rank.top {
  color: #f59e0b;
}
</style>
