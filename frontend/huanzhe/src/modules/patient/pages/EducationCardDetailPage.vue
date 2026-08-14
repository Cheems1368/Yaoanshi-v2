<template>
  <ion-page>
    <ion-header class="page-header">
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/rational-medication" />
        </ion-buttons>
        <ion-title>用药教育</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <div class="container">
        <ion-card v-if="loading" class="state-card">
          <ion-card-content>
            <ion-spinner name="crescent" />
            <p>正在加载内容</p>
          </ion-card-content>
        </ion-card>

        <ion-card v-else-if="error" class="state-card">
          <ion-card-content>
            <h2>内容暂不可用</h2>
            <p>{{ error }}</p>
            <ion-button fill="outline" @click="loadDetail">重试</ion-button>
          </ion-card-content>
        </ion-card>

        <article v-else-if="card" class="detail-card">
          <div class="detail-head">
            <ion-badge v-if="card.categoryName || card.category">{{ card.categoryName || card.category }}</ion-badge>
            <h1>{{ card.title }}</h1>
            <p v-if="card.summary">{{ card.summary }}</p>
          </div>

          <div class="tag-row">
            <ion-chip v-for="tag in mergedTags(card)" :key="tag" size="small">{{ tag }}</ion-chip>
          </div>

          <div class="body-text">{{ card.body }}</div>

          <footer class="review-meta">
            <span v-if="card.reviewedAt">审核时间：{{ formatDate(card.reviewedAt) }}</span>
            <span v-if="card.source">来源：{{ card.source }}</span>
          </footer>
        </article>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  IonBackButton,
  IonBadge,
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonChip,
  IonContent,
  IonHeader,
  IonPage,
  IonSpinner,
  IonTitle,
  IonToolbar
} from '@ionic/vue'
import { educationService, type EducationContentCardDTO } from '@/modules/patient/api/mvp'

const route = useRoute()
const loading = ref(false)
const error = ref('')
const card = ref<EducationContentCardDTO | null>(null)

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!Number.isFinite(id)) {
    error.value = '内容编号无效'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const response = await educationService.detail(id)
    card.value = response.data
  } catch (err) {
    card.value = null
    error.value = err instanceof Error ? err.message : '内容加载失败'
  } finally {
    loading.value = false
  }
}

const mergedTags = (value: EducationContentCardDTO) => {
  return [...(value.diagnosisTags || []), ...(value.drugTags || []), ...(value.populationTags || [])]
}

const formatDate = (value: string) => {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

loadDetail()
</script>

<style scoped>
.page-header {
  --background: var(--patient-primary);
  --color: var(--patient-text-inverse);
}

.page-content {
  --background: var(--patient-bg-page);
}

.container {
  padding: var(--patient-space-4);
}

.detail-card,
.state-card {
  border-radius: var(--patient-radius-lg);
  background: var(--patient-bg-card);
  box-shadow: var(--patient-shadow-md);
}

.detail-card {
  padding: var(--patient-space-5);
}

.detail-head h1 {
  margin: var(--patient-space-3) 0 var(--patient-space-2);
  color: var(--patient-text-primary);
  font-size: 22px;
  line-height: 1.3;
}

.detail-head p,
.state-card p {
  margin: 0;
  color: var(--patient-text-secondary);
  line-height: var(--patient-leading-relaxed);
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--patient-space-2);
  margin: var(--patient-space-4) 0;
}

ion-chip {
  --background: var(--patient-primary-light);
  --color: var(--patient-primary);
  height: 28px;
}

.body-text {
  white-space: pre-wrap;
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-relaxed);
  font-size: var(--patient-font-body);
}

.review-meta {
  display: grid;
  gap: var(--patient-space-2);
  margin-top: var(--patient-space-5);
  color: var(--patient-text-muted);
  font-size: var(--patient-font-caption);
}

.state-card {
  text-align: center;
}
</style>
