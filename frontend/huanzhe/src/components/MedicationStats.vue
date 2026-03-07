<template>
  <ion-card class="stats-card">
    <ion-card-header>
      <ion-card-title>服药统计</ion-card-title>
      <ion-card-subtitle>最近7天服药情况</ion-card-subtitle>
    </ion-card-header>
    <ion-card-content>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-number">{{ totalRecords }}</div>
          <div class="stat-label">总记录</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ adherenceRate }}%</div>
          <div class="stat-label">依从性</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ missedCount }}</div>
          <div class="stat-label">漏服次数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ streakDays }}</div>
          <div class="stat-label">连续天数</div>
        </div>
      </div>
      
      <!-- 服药趋势图 -->
      <div class="trend-chart">
        <div class="chart-title">服药趋势</div>
        <div class="chart-container">
          <div v-for="(day, index) in weeklyData" :key="index" class="chart-bar">
            <div class="bar" :style="{ height: day.adherenceRate + '%' }"></div>
            <div class="bar-label">{{ day.day }}</div>
          </div>
        </div>
      </div>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle, IonCardContent } from '@ionic/vue';

interface Props {
  records: any[];
}

const props = defineProps<Props>();

const totalRecords = computed(() => props.records.length);

const adherenceRate = computed(() => {
  if (props.records.length === 0) return 0;
  const takenCount = props.records.filter(r => r.status === 'taken').length;
  return Math.round((takenCount / props.records.length) * 100);
});

const missedCount = computed(() => {
  return props.records.filter(r => r.status === 'missed').length;
});

const streakDays = computed(() => {
  // 计算连续服药天数
  let streak = 0;
  const sortedRecords = [...props.records].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  
  for (const record of sortedRecords) {
    if (record.status === 'taken') {
      streak++;
    } else {
      break;
    }
  }
  
  return streak;
});

const weeklyData = computed(() => {
  const last7Days = [];
  const today = new Date();
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const dateStr = date.toISOString().split('T')[0];
    
    const dayRecords = props.records.filter(r => r.date === dateStr);
    const takenCount = dayRecords.filter(r => r.status === 'taken').length;
    const adherenceRate = dayRecords.length > 0 ? (takenCount / dayRecords.length) * 100 : 0;
    
    last7Days.push({
      day: date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }),
      adherenceRate: Math.round(adherenceRate)
    });
  }
  
  return last7Days;
});
</script>

<style scoped>
.stats-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: var(--ion-color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--ion-color-medium);
}

.trend-chart {
  margin-top: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--ion-color-dark);
  margin-bottom: 12px;
}

.chart-container {
  display: flex;
  justify-content: space-between;
  align-items: end;
  height: 120px;
  padding: 0 8px;
}

.chart-bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 4px;
}

.bar {
  width: 100%;
  background: linear-gradient(to top, var(--ion-color-primary), #1e88e5);
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: all 0.3s ease;
}

.bar-label {
  font-size: 10px;
  color: var(--ion-color-medium);
  margin-top: 8px;
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-number {
    font-size: 18px;
  }
}
</style>
