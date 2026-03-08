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
                    <ion-icon :icon="chevronForwardOutline" slot="end" />
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
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonPage,
  IonTitle,
  IonToolbar
} from '@ionic/vue'
import { chevronForwardOutline } from 'ionicons/icons'
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
  --background: #2979ff;
  --color: #ffffff;
}

.page-content {
  --background: linear-gradient(180deg, #eff6ff 0%, #ffffff 35%, #f8fafc 100%);
}

.container {
  padding: 16px;
}

.summary-card {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #dbeafe;
  padding: 16px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
  margin-bottom: 14px;
}

.summary-card h2 {
  margin: 0 0 8px;
  color: #0f172a;
}

.summary-card p {
  margin: 0 0 10px;
  color: #64748b;
  line-height: 1.6;
}

ion-accordion-group {
  display: grid;
  gap: 10px;
}

ion-accordion {
  border: 1px solid #dbeafe;
  border-radius: 12px;
  overflow: hidden;
  background: #ffffff;
}

ion-item h3 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
}

ion-item p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.topic-content {
  padding: 0 6px 8px;
}

.topic-content h4 {
  margin: 0;
  font-size: 15px;
}

.topic-content p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.error-card {
  text-align: center;
  border-radius: 14px;
}

.error-card p {
  color: #64748b;
}
</style>
