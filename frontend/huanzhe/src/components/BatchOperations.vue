<template>
  <div class="batch-operations">
    <ion-button 
      fill="outline" 
      @click="toggleBatchMode"
      class="batch-toggle"
    >
      <ion-icon :icon="checkmarkCircle" slot="start"></ion-icon>
      {{ isBatchMode ? '退出批量' : '批量操作' }}
    </ion-button>
    
    <div v-if="isBatchMode" class="batch-controls">
      <div class="selected-count">
        已选择 {{ selectedRecords.length }} 条记录
      </div>
      
      <div class="batch-actions">
        <ion-button 
          fill="solid" 
          color="success"
          @click="batchMarkTaken"
          :disabled="selectedRecords.length === 0"
          class="batch-action"
        >
          <ion-icon :icon="checkmarkCircle" slot="start"></ion-icon>
          标记已服药
        </ion-button>
        
        <ion-button 
          fill="solid" 
          color="warning"
          @click="batchMarkMissed"
          :disabled="selectedRecords.length === 0"
          class="batch-action"
        >
          <ion-icon :icon="closeCircle" slot="start"></ion-icon>
          标记漏服
        </ion-button>
        
        <ion-button 
          fill="solid" 
          color="danger"
          @click="batchDelete"
          :disabled="selectedRecords.length === 0"
          class="batch-action"
        >
          <ion-icon :icon="trash" slot="start"></ion-icon>
          批量删除
        </ion-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { IonButton, IonIcon, alertController } from '@ionic/vue';
import { checkmarkCircle, closeCircle, trash } from 'ionicons/icons';

const emit = defineEmits<{
  batchModeChanged: [isBatchMode: boolean]
  batchAction: [action: string, recordIds: string[]]
}>();

const isBatchMode = ref(false);
const selectedRecords = ref<string[]>([]);

const toggleBatchMode = () => {
  isBatchMode.value = !isBatchMode.value;
  if (!isBatchMode.value) {
    selectedRecords.value = [];
  }
  emit('batchModeChanged', isBatchMode.value);
};

const selectRecord = (recordId: string) => {
  if (selectedRecords.value.includes(recordId)) {
    selectedRecords.value = selectedRecords.value.filter(id => id !== recordId);
  } else {
    selectedRecords.value.push(recordId);
  }
};

const batchMarkTaken = async () => {
  if (selectedRecords.value.length === 0) return;
  
  const alert = await alertController.create({
    header: '确认操作',
    message: `确定要将选中的 ${selectedRecords.value.length} 条记录标记为已服药吗？`,
    buttons: [
      {
        text: '取消',
        role: 'cancel'
      },
      {
        text: '确认',
        handler: () => {
          emit('batchAction', 'markTaken', selectedRecords.value);
          selectedRecords.value = [];
        }
      }
    ]
  });
  await alert.present();
};

const batchMarkMissed = async () => {
  if (selectedRecords.value.length === 0) return;
  
  const alert = await alertController.create({
    header: '确认操作',
    message: `确定要将选中的 ${selectedRecords.value.length} 条记录标记为漏服吗？`,
    buttons: [
      {
        text: '取消',
        role: 'cancel'
      },
      {
        text: '确认',
        handler: () => {
          emit('batchAction', 'markMissed', selectedRecords.value);
          selectedRecords.value = [];
        }
      }
    ]
  });
  await alert.present();
};

const batchDelete = async () => {
  if (selectedRecords.value.length === 0) return;
  
  const alert = await alertController.create({
    header: '确认删除',
    message: `确定要删除选中的 ${selectedRecords.value.length} 条记录吗？此操作不可撤销。`,
    buttons: [
      {
        text: '取消',
        role: 'cancel'
      },
      {
        text: '删除',
        role: 'destructive',
        handler: () => {
          emit('batchAction', 'delete', selectedRecords.value);
          selectedRecords.value = [];
        }
      }
    ]
  });
  await alert.present();
};

defineExpose({
  selectRecord,
  selectedRecords: selectedRecords.value
});
</script>

<style scoped>
.batch-operations {
  margin-bottom: 16px;
}

.batch-toggle {
  width: 100%;
  margin-bottom: 12px;
}

.batch-controls {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  padding: 16px;
  border: 1px solid rgba(41, 121, 255, 0.1);
}

.selected-count {
  font-size: 14px;
  color: var(--ion-color-medium);
  margin-bottom: 12px;
  text-align: center;
}

.batch-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.batch-action {
  width: 100%;
}

@media (min-width: 768px) {
  .batch-actions {
    flex-direction: row;
  }
  
  .batch-action {
    flex: 1;
  }
}
</style>
