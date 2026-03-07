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

class HealthDataService {
  private readonly STORAGE_KEY = 'patient_health_data';

  // 保存健康数据
  saveHealthData(data: Omit<HealthData, 'submitTime'>): void {
    const healthData: HealthData = {
      ...data,
      submitTime: new Date().toISOString()
    };
    
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(healthData));
      console.log('健康数据已保存:', healthData);
    } catch (error) {
      console.error('保存健康数据失败:', error);
      throw new Error('保存健康数据失败');
    }
  }

  // 获取健康数据
  getHealthData(): HealthData | null {
    try {
      const data = localStorage.getItem(this.STORAGE_KEY);
      return data ? JSON.parse(data) : null;
    } catch (error) {
      console.error('获取健康数据失败:', error);
      return null;
    }
  }

  // 清除健康数据
  clearHealthData(): void {
    try {
      localStorage.removeItem(this.STORAGE_KEY);
      console.log('健康数据已清除');
    } catch (error) {
      console.error('清除健康数据失败:', error);
    }
  }

  // 检查是否有健康数据
  hasHealthData(): boolean {
    return this.getHealthData() !== null;
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
