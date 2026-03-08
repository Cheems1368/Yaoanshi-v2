// 健康数据服务
export interface HealthData {
  layer1: {
    allergy: string;
    diagnosis: string;
  };
  layer2: {
    medicalHistory: string;
    smokingDrinking: string;
    liverKidney: string;
    currentMedication: string;
  };
  layer3: {
    detailedSymptoms: string;
    familyHistory: string;
    surgeryHistory: string;
    socialHistory: string;
  };
  submitTime: string;
}

const cloneHealthData = (data: HealthData | null): HealthData | null => {
  if (!data) {
    return null;
  }
  return JSON.parse(JSON.stringify(data)) as HealthData;
};

class HealthDataService {
  private currentHealthData: HealthData | null = null;

  // 保存健康数据，仅保留当前运行时内存态，避免落盘到浏览器存储。
  saveHealthData(data: Omit<HealthData, 'submitTime'>): void {
    const healthData: HealthData = {
      ...data,
      submitTime: new Date().toISOString()
    };

    this.currentHealthData = cloneHealthData(healthData);
  }

  // 获取健康数据
  getHealthData(): HealthData | null {
    return cloneHealthData(this.currentHealthData);
  }

  replaceHealthData(data: HealthData | null): void {
    this.currentHealthData = cloneHealthData(data);
  }

  // 清除健康数据
  clearHealthData(): void {
    this.currentHealthData = null;
  }

  // 检查是否有健康数据
  hasHealthData(): boolean {
    return this.currentHealthData !== null;
  }

  // 获取格式化的健康信息摘要
  getHealthSummary(): string {
    const data = this.getHealthData();
    if (!data) return '暂无健康信息';

    const summary = [];

    if (data.layer1.allergy) summary.push(`过敏史: ${data.layer1.allergy}`);
    if (data.layer1.diagnosis) summary.push(`主要诊断: ${data.layer1.diagnosis}`);
    if (data.layer2.medicalHistory) summary.push(`既往病史: ${data.layer2.medicalHistory}`);
    if (data.layer2.currentMedication) summary.push(`当前用药: ${data.layer2.currentMedication}`);

    return summary.length > 0 ? summary.join('; ') : '暂无健康信息';
  }
}

export const healthDataService = new HealthDataService();
