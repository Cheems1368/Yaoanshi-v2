<template>
  <div class="export-records">
    <ion-button 
      fill="outline" 
      @click="showExportModal = true"
      class="export-button"
    >
      <ion-icon :icon="download" slot="start"></ion-icon>
      导出记录
    </ion-button>
    
    <ion-modal :is-open="showExportModal" @did-dismiss="closeExportModal">
      <ion-header>
        <ion-toolbar>
          <ion-title>导出服药记录</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="closeExportModal">取消</ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      
      <ion-content>
        <div class="export-content">
          <ion-item>
            <ion-label position="stacked">导出格式</ion-label>
            <ion-select v-model="exportFormat">
              <ion-select-option value="csv">CSV文件</ion-select-option>
              <ion-select-option value="pdf">PDF报告</ion-select-option>
              <ion-select-option value="json">JSON数据</ion-select-option>
            </ion-select>
          </ion-item>
          
          <ion-item>
            <ion-label position="stacked">时间范围</ion-label>
            <ion-select v-model="dateRange">
              <ion-select-option value="week">最近一周</ion-select-option>
              <ion-select-option value="month">最近一月</ion-select-option>
              <ion-select-option value="quarter">最近三月</ion-select-option>
              <ion-select-option value="all">全部记录</ion-select-option>
            </ion-select>
          </ion-item>
          
          <ion-item>
            <ion-label position="stacked">包含内容</ion-label>
          </ion-item>
          <div class="checkbox-list">
            <ion-item>
              <ion-checkbox
                slot="start"
                :checked="includeContent.includes('basic')"
                @ion-change="toggleInclude('basic', $event.detail.checked)"
              ></ion-checkbox>
              <ion-label>基本信息</ion-label>
            </ion-item>
            <ion-item>
              <ion-checkbox
                slot="start"
                :checked="includeContent.includes('photos')"
                @ion-change="toggleInclude('photos', $event.detail.checked)"
              ></ion-checkbox>
              <ion-label>照片记录</ion-label>
            </ion-item>
            <ion-item>
              <ion-checkbox
                slot="start"
                :checked="includeContent.includes('statistics')"
                @ion-change="toggleInclude('statistics', $event.detail.checked)"
              ></ion-checkbox>
              <ion-label>统计数据</ion-label>
            </ion-item>
          </div>
          
          <div class="export-actions">
            <ion-button expand="block" @click="exportRecords" :disabled="isExporting">
              <ion-icon :icon="isExporting ? hourglass : download" slot="start"></ion-icon>
              {{ isExporting ? '导出中...' : '开始导出' }}
            </ion-button>
          </div>
        </div>
      </ion-content>
    </ion-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { 
  IonButton, 
  IonIcon, 
  IonModal, 
  IonHeader, 
  IonToolbar, 
  IonTitle, 
  IonContent,
  IonItem,
  IonLabel,
  IonSelect,
  IonSelectOption,
  IonCheckbox,
  IonButtons
} from '@ionic/vue';
import { download, hourglass } from 'ionicons/icons';

interface Props {
  records: any[];
}

const props = defineProps<Props>();

const showExportModal = ref(false);
const exportFormat = ref('csv');
const dateRange = ref('month');
const includeContent = ref(['basic', 'statistics']);
const isExporting = ref(false);

const closeExportModal = () => {
  showExportModal.value = false;
};

const toggleInclude = (field: string, checked: boolean) => {
  if (checked) {
    if (!includeContent.value.includes(field)) {
      includeContent.value = [...includeContent.value, field];
    }
    return;
  }

  includeContent.value = includeContent.value.filter(item => item !== field);
};

const exportRecords = async () => {
  isExporting.value = true;
  
  try {
    const filteredRecords = getFilteredRecords();
    
    switch (exportFormat.value) {
      case 'csv':
        await exportToCSV(filteredRecords);
        break;
      case 'pdf':
        await exportToPDF(filteredRecords);
        break;
      case 'json':
        await exportToJSON(filteredRecords);
        break;
    }
    
    closeExportModal();
  } catch (error) {
    console.error('导出失败:', error);
    alert('导出失败，请重试');
  } finally {
    isExporting.value = false;
  }
};

const getFilteredRecords = () => {
  const now = new Date();
  let startDate: Date;
  
  switch (dateRange.value) {
    case 'week':
      startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      break;
    case 'month':
      startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
      break;
    case 'quarter':
      startDate = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000);
      break;
    default:
      return props.records;
  }
  
  return props.records.filter(record => 
    new Date(record.date) >= startDate
  );
};

const exportToCSV = async (records: any[]) => {
  const headers = ['日期', '时间', '药品名称', '剂量', '状态', '备注'];
  const csvContent = [
    headers.join(','),
    ...records.map(record => [
      record.date,
      record.time,
      record.medicationName,
      record.dose,
      getStatusText(record.status),
      record.notes || ''
    ].join(','))
  ].join('\n');
  
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `服药记录_${new Date().toISOString().split('T')[0]}.csv`;
  link.click();
};

const exportToJSON = async (records: any[]) => {
  const exportData = {
    exportDate: new Date().toISOString(),
    totalRecords: records.length,
    records: records
  };
  
  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `服药记录_${new Date().toISOString().split('T')[0]}.json`;
  link.click();
};

const exportToPDF = async (records: any[]) => {
  // 简单的PDF导出实现
  const htmlContent = generatePDFHTML(records);
  const blob = new Blob([htmlContent], { type: 'text/html' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `服药记录_${new Date().toISOString().split('T')[0]}.html`;
  link.click();
};

const generatePDFHTML = (records: any[]) => {
  return `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>服药记录报告</title>
      <style>
        body { font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Noto Sans SC", sans-serif; margin: 20px; }
        .header { text-align: center; margin-bottom: 30px; }
        .record { margin-bottom: 15px; padding: 10px; border: 1px solid #ddd; }
        .status-taken { color: green; }
        .status-missed { color: red; }
        .status-partial { color: orange; }
      </style>
    </head>
    <body>
      <div class="header">
        <h1>服药记录报告</h1>
        <p>生成时间: ${new Date().toLocaleString('zh-CN')}</p>
        <p>总记录数: ${records.length}</p>
      </div>
      ${records.map(record => `
        <div class="record">
          <h3>${record.medicationName}</h3>
          <p><strong>日期:</strong> ${record.date}</p>
          <p><strong>时间:</strong> ${record.time}</p>
          <p><strong>剂量:</strong> ${record.dose}</p>
          <p><strong>状态:</strong> <span class="status-${record.status}">${getStatusText(record.status)}</span></p>
          ${record.notes ? `<p><strong>备注:</strong> ${record.notes}</p>` : ''}
        </div>
      `).join('')}
    </body>
    </html>
  `;
};

const getStatusText = (status: string) => {
  const textMap = {
    taken: '已服药',
    missed: '漏服',
    partial: '部分服用'
  };
  return textMap[status as keyof typeof textMap] || '未知';
};
</script>

<style scoped>
.export-records {
  margin-bottom: 16px;
}

.export-button {
  width: 100%;
}

.export-content {
  padding: 16px;
}

.export-actions {
  margin-top: 24px;
}
</style>
