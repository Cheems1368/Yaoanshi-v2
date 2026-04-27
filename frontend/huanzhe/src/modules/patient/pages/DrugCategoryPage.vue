<template>
  <ion-page>
    <ion-header class="page-header">
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/rational-medication" />
        </ion-buttons>
        <ion-title>{{ category?.name || '药品分类' }}</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <div class="container">
        <template v-if="category">
          <section class="summary-card">
            <h2>{{ category.name }}</h2>
            <p>{{ category.description }}</p>
            <ion-badge>{{ topicCount }} 个主题 · {{ itemCount }} 个常见药品</ion-badge>
          </section>

          <ion-accordion-group>
            <ion-accordion v-for="topic in category.topics" :key="topic.id" :value="topic.id">
              <ion-item slot="header">
                <ion-label>
                  <h3>{{ topic.name }}</h3>
                  <p>{{ topic.description }}</p>
                </ion-label>
                <ion-badge slot="end">{{ topic.items.length }}</ion-badge>
              </ion-item>
              <div slot="content" class="topic-content">
                <ion-list>
                  <ion-item
                    v-for="item in topic.items"
                    :key="item.id"
                    button
                    :data-testid="`rational-item-${item.id}`"
                    @click="openDetail(item.id)"
                  >
                    <ion-label>
                      <h4>{{ item.name }}</h4>
                      <p>{{ item.indications }}</p>
                    </ion-label>
                    <AppIcon name="chevronForwardOutline" :size="16" slot="end" />
                  </ion-item>
                </ion-list>
              </div>
            </ion-accordion>
          </ion-accordion-group>
        </template>

        <ion-card v-else class="error-card">
          <ion-card-content>
            <h3>未找到分类内容</h3>
            <p>该分类可能已下线或链接失效。</p>
            <ion-button fill="outline" @click="goBack">返回合理用药</ion-button>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  IonAccordion,
  IonAccordionGroup,
  IonBackButton,
  IonBadge,
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonPage,
  IonTitle,
  IonToolbar
} from '@ionic/vue'
import { rationalMedicationData } from '@/modules/patient/data/rationalMedication'

const route = useRoute()
const router = useRouter()

const categoryId = computed(() => String(route.params.id || ''))
const category = computed(() => rationalMedicationData.getCategoryById(categoryId.value))
const topicCount = computed(() => category.value?.topics.length || 0)
const itemCount = computed(() => {
  return category.value?.topics.reduce((total, topic) => total + topic.items.length, 0) || 0
})

const openDetail = (itemId: string) => {
  router.push({
    path: `/drug-detail/${itemId}`,
    query: {
      categoryId: categoryId.value
    }
  })
}

const goBack = () => {
  router.push('/rational-medication')
}
</script>

<style scoped>
.page-header {
  --background: var(--patient-primary);
  --color: var(--patient-text-inverse);
}

.page-content {
  --background: linear-gradient(180deg, var(--patient-primary-light) 0%, var(--patient-bg-card) 35%, var(--patient-bg-page) 100%);
}

.container {
  padding: var(--patient-space-4);
}

.summary-card {
  background: var(--patient-bg-card);
  border-radius: var(--patient-radius-lg);
  border: 1px solid var(--patient-border-light);
  padding: var(--patient-space-4);
  box-shadow: var(--patient-shadow-sm);
  margin-bottom: var(--patient-space-4);
}

.summary-card h2 {
  margin: 0 0 var(--patient-space-2);
  color: var(--patient-text-primary);
}

.summary-card p {
  margin: 0 0 var(--patient-space-3);
  color: var(--patient-text-muted);
  line-height: var(--patient-leading-relaxed);
}

ion-accordion-group {
  display: grid;
  gap: var(--patient-space-3);
}

ion-accordion {
  border: 1px solid var(--patient-border-light);
  border-radius: var(--patient-radius-md);
  overflow: hidden;
  background: var(--patient-bg-card);
}

ion-item h3 {
  margin: 0;
  font-size: var(--patient-font-body);
  color: var(--patient-text-primary);
}

ion-item p {
  margin: var(--patient-space-1) 0 0;
  color: var(--patient-text-muted);
  font-size: var(--patient-font-caption);
}

.topic-content {
  padding: 0 var(--patient-space-2) var(--patient-space-2);
}

.topic-content h4 {
  margin: 0;
  font-size: var(--patient-font-helper);
}

.topic-content p {
  margin: var(--patient-space-1) 0 0;
  color: var(--patient-text-muted);
  font-size: var(--patient-font-caption);
}

.error-card {
  text-align: center;
  border-radius: var(--patient-radius-lg);
}

.error-card p {
  color: var(--patient-text-muted);
}
</style>
