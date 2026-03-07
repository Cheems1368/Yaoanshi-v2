<template>
  <ion-page>
    <ion-header class="custom-header">
      <ion-toolbar>
        <ion-title class="app-title">个人资料</ion-title>
        <ion-buttons slot="end">
          <ion-button v-if="!isEditing" @click="startEdit" fill="clear" class="edit-button">
            <ion-icon :icon="createOutline" slot="icon-only"></ion-icon>
          </ion-button>
          <ion-button v-if="isEditing" @click="cancelEdit" fill="clear" class="cancel-button">
            取消
          </ion-button>
          <ion-button v-if="isEditing" @click="saveProfile" fill="clear" class="save-button" :disabled="!isFormValid">
            保存
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="custom-content">
      <div class="container">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-container" @click="isEditing ? selectAvatar() : null">
            <img :src="profileData.avatar || '/src/picture/user_avatar.png'" alt="用户头像" class="avatar" :class="{ 'editable': isEditing }">
            <div v-if="isEditing" class="avatar-overlay">
              <ion-icon :icon="cameraOutline" class="camera-icon"></ion-icon>
            </div>
          </div>
          <h2 class="user-name">{{ profileData.name }}</h2>
          <p class="user-id">ID: {{ profileData.userId }}</p>
          <!-- 实名认证状态 -->
          <div v-if="profileData.isRealNameVerified" class="verification-badge">
            <ion-icon :icon="checkmarkCircleOutline" class="badge-icon"></ion-icon>
            <span class="badge-text">已实名认证</span>
          </div>
        </div>

        <!-- 基本信息卡片 -->
        <ion-card class="info-card">
          <ion-card-header>
            <ion-card-title class="card-title">基本信息</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <!-- 查看模式 -->
            <div v-if="!isEditing" class="info-display">
              <div class="info-item">
                <div class="info-label">姓名</div>
                <div class="info-value">{{ profileData.name }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">手机号</div>
                <div class="info-value">{{ profileData.phone }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">年龄</div>
                <div class="info-value">{{ profileData.age }}岁</div>
              </div>
              <div class="info-item">
                <div class="info-label">性别</div>
                <div class="info-value">{{ profileData.gender }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">身份证号</div>
                <div class="info-value">{{ profileData.idCard }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">证件类型</div>
                <div class="info-value">{{ profileData.idType }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">民族</div>
                <div class="info-value">{{ profileData.ethnicity }}</div>
              </div>
            </div>

            <!-- 编辑模式 -->
            <div v-if="isEditing" class="info-edit">
              <ion-item class="edit-item">
                <ion-label position="stacked">姓名 *</ion-label>
                <ion-input v-model="editForm.name" placeholder="请输入姓名" :class="{ 'ion-invalid': errors.name }"></ion-input>
                <div v-if="errors.name" class="error-message">{{ errors.name }}</div>
              </ion-item>

              <ion-item class="edit-item">
                <ion-label position="stacked">手机号 *</ion-label>
                <ion-input v-model="editForm.phone" type="tel" placeholder="请输入手机号" :class="{ 'ion-invalid': errors.phone }"></ion-input>
                <div v-if="errors.phone" class="error-message">{{ errors.phone }}</div>
              </ion-item>

              <ion-item class="edit-item">
                <ion-label position="stacked">年龄 *</ion-label>
                <ion-input v-model="editForm.age" type="number" placeholder="请输入年龄" :class="{ 'ion-invalid': errors.age }"></ion-input>
                <div v-if="errors.age" class="error-message">{{ errors.age }}</div>
              </ion-item>

              <ion-item class="edit-item">
                <ion-label position="stacked">性别 *</ion-label>
                <ion-select v-model="editForm.gender" placeholder="请选择性别" :class="{ 'ion-invalid': errors.gender }">
                  <ion-select-option value="男">男</ion-select-option>
                  <ion-select-option value="女">女</ion-select-option>
                </ion-select>
                <div v-if="errors.gender" class="error-message">{{ errors.gender }}</div>
              </ion-item>

              <ion-item class="edit-item">
                <ion-label position="stacked">身份证号 *</ion-label>
                <ion-input v-model="editForm.idCard" placeholder="请输入身份证号" :class="{ 'ion-invalid': errors.idCard }"></ion-input>
                <div v-if="errors.idCard" class="error-message">{{ errors.idCard }}</div>
              </ion-item>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- 健康信息卡片 -->
        <ion-card class="info-card">
          <ion-card-header>
            <ion-card-title class="card-title">健康信息</ion-card-title>
            <div v-if="healthData" class="health-data-badge">
              <ion-icon :icon="checkmarkCircleOutline" class="badge-icon"></ion-icon>
              <span class="badge-text">已填写问卷</span>
            </div>
          </ion-card-header>
          <ion-card-content>
            <!-- 查看模式 -->
            <div v-if="!isEditing" class="info-display">
              <div class="info-item">
                <div class="info-label">身高</div>
                <div class="info-value">{{ profileData.height }}cm</div>
              </div>
              <div class="info-item">
                <div class="info-label">体重</div>
                <div class="info-value">{{ profileData.weight }}kg</div>
              </div>
              <div class="info-item">
                <div class="info-label">血型</div>
                <div class="info-value">{{ profileData.bloodType }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">过敏史</div>
                <div class="info-value">{{ healthData?.layer1?.allergy || profileData.allergies || '无' }}</div>
              </div>
              
              <!-- 问卷健康信息 -->
              <div v-if="healthData" class="questionnaire-health-section">
                <div class="section-divider">
                  <span class="divider-text">问卷健康信息</span>
                </div>
                
                <div v-if="healthData.layer1.diagnosis" class="info-item">
                  <div class="info-label">主要诊断</div>
                  <div class="info-value">{{ healthData.layer1.diagnosis }}</div>
                </div>
                
                <div v-if="healthData.layer2.medicalHistory" class="info-item">
                  <div class="info-label">既往病史</div>
                  <div class="info-value">{{ healthData.layer2.medicalHistory }}</div>
                </div>
                
                <div v-if="healthData.layer2.smokingDrinking" class="info-item">
                  <div class="info-label">烟酒史</div>
                  <div class="info-value">{{ healthData.layer2.smokingDrinking }}</div>
                </div>
                
                <div v-if="healthData.layer2.liverKidney" class="info-item">
                  <div class="info-label">肝肾功能</div>
                  <div class="info-value">{{ healthData.layer2.liverKidney }}</div>
                </div>
                
                <div v-if="healthData.layer2.currentMedication" class="info-item">
                  <div class="info-label">当前用药</div>
                  <div class="info-value">{{ healthData.layer2.currentMedication }}</div>
                </div>
                
                <div v-if="healthData.layer3.detailedSymptoms" class="info-item">
                  <div class="info-label">详细症状</div>
                  <div class="info-value">{{ healthData.layer3.detailedSymptoms }}</div>
                </div>
                
                <div v-if="healthData.layer3.familyHistory" class="info-item">
                  <div class="info-label">家族史</div>
                  <div class="info-value">{{ healthData.layer3.familyHistory }}</div>
                </div>
                
                <div v-if="healthData.layer3.surgeryHistory" class="info-item">
                  <div class="info-label">手术史</div>
                  <div class="info-value">{{ healthData.layer3.surgeryHistory }}</div>
                </div>
                
                <div v-if="healthData.layer3.socialHistory" class="info-item">
                  <div class="info-label">社会史</div>
                  <div class="info-value">{{ healthData.layer3.socialHistory }}</div>
                </div>
                
                <div class="info-item">
                  <div class="info-label">问卷填写时间</div>
                  <div class="info-value">{{ formatDate(healthData.submitTime) }}</div>
                </div>
              </div>
              
              <!-- 未填写问卷提示 -->
              <div v-else class="no-questionnaire-section">
                <div class="no-data-icon">
                  <ion-icon :icon="documentTextOutline"></ion-icon>
                </div>
                <p class="no-data-text">尚未填写健康问卷</p>
                <ion-button fill="outline" size="small" @click="goToQuestionnaire" class="questionnaire-button">
                  去填写问卷
                </ion-button>
              </div>
            </div>

            <!-- 编辑模式 -->
            <div v-if="isEditing" class="info-edit">
              <ion-item class="edit-item">
                <ion-label position="stacked">身高 (cm)</ion-label>
                <ion-input v-model="editForm.height" type="number" placeholder="请输入身高"></ion-input>
              </ion-item>

              <ion-item class="edit-item">
                <ion-label position="stacked">体重 (kg)</ion-label>
                <ion-input v-model="editForm.weight" type="number" placeholder="请输入体重"></ion-input>
              </ion-item>

              <ion-item class="edit-item">
                <ion-label position="stacked">血型</ion-label>
                <ion-select v-model="editForm.bloodType" placeholder="请选择血型">
                  <ion-select-option value="A型">A型</ion-select-option>
                  <ion-select-option value="B型">B型</ion-select-option>
                  <ion-select-option value="AB型">AB型</ion-select-option>
                  <ion-select-option value="O型">O型</ion-select-option>
                  <ion-select-option value="未知">未知</ion-select-option>
                </ion-select>
              </ion-item>

              <ion-item class="edit-item">
                <ion-label position="stacked">过敏史</ion-label>
                <ion-textarea v-model="editForm.allergies" placeholder="请输入过敏史，如无请填写'无'" :rows="3"></ion-textarea>
              </ion-item>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- 紧急联系人卡片 -->
        <ion-card class="info-card">
          <ion-card-header>
            <ion-card-title class="card-title">紧急联系人</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <!-- 查看模式 -->
            <div v-if="!isEditing" class="info-display">
              <div class="info-item">
                <div class="info-label">联系人姓名</div>
                <div class="info-value">{{ profileData.emergencyContact.name || '未设置' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">联系电话</div>
                <div class="info-value">{{ profileData.emergencyContact.phone || '未设置' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">关系</div>
                <div class="info-value">{{ profileData.emergencyContact.relationship || '未设置' }}</div>
              </div>
            </div>

            <!-- 编辑模式 -->
            <div v-if="isEditing" class="info-edit">
              <ion-item class="edit-item">
                <ion-label position="stacked">联系人姓名</ion-label>
                <ion-input v-model="editForm.emergencyContact.name" placeholder="请输入紧急联系人姓名"></ion-input>
              </ion-item>

              <ion-item class="edit-item">
                <ion-label position="stacked">联系电话</ion-label>
                <ion-input v-model="editForm.emergencyContact.phone" type="tel" placeholder="请输入紧急联系人电话"></ion-input>
              </ion-item>

              <ion-item class="edit-item">
                <ion-label position="stacked">关系</ion-label>
                <ion-select v-model="editForm.emergencyContact.relationship" placeholder="请选择关系">
                  <ion-select-option value="配偶">配偶</ion-select-option>
                  <ion-select-option value="父母">父母</ion-select-option>
                  <ion-select-option value="子女">子女</ion-select-option>
                  <ion-select-option value="兄弟姐妹">兄弟姐妹</ion-select-option>
                  <ion-select-option value="朋友">朋友</ion-select-option>
                  <ion-select-option value="其他">其他</ion-select-option>
                </ion-select>
              </ion-item>
            </div>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { 
  IonPage, 
  IonHeader, 
  IonToolbar, 
  IonTitle, 
  IonContent, 
  IonCard, 
  IonCardHeader, 
  IonCardTitle, 
  IonCardContent, 
  IonItem, 
  IonLabel,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonTextarea,
  IonButton,
  IonButtons,
  IonIcon,
  toastController
} from '@ionic/vue';
import { createOutline, cameraOutline, checkmarkCircleOutline, documentTextOutline } from 'ionicons/icons';
import { healthDataService, type HealthData } from '@/services/healthDataService';
import { userDataService, type UserProfile } from '@/services/userDataService';
import { patientApiService } from '@/modules/patient/api/patientApi';
import { httpService } from '@/shared/http/client';

const router = useRouter();

// 响应式数据
const isEditing = ref(false);
const healthData = ref<HealthData | null>(null);

// 用户资料数据 - 从用户数据服务获取
const profileData = reactive<UserProfile>({
  userId: 'P2024001',
  patientId: undefined,
  name: '张患者',
  phone: '138****8888',
  age: 30,
  gender: '男',
  idCard: '330***********1234',
  idType: '身份证',
  ethnicity: '汉族',
  height: 175,
  weight: 70,
  bloodType: 'A型',
  allergies: '青霉素过敏',
  avatar: '/src/picture/user_avatar.png',
  address: '',
  patientType: 0,
  insurance: 0,
  emergencyContact: {
    name: '李女士',
    phone: '139****9999',
    relationship: '配偶'
  },
  isRealNameVerified: false
});

// 编辑表单数据
const editForm = reactive({
  name: '',
  phone: '',
  age: 0,
  gender: '',
  idCard: '',
  height: 0,
  weight: 0,
  bloodType: '',
  allergies: '',
  emergencyContact: {
    name: '',
    phone: '',
    relationship: ''
  }
});

// 表单验证错误
const errors = reactive({
  name: '',
  phone: '',
  age: '',
  gender: '',
  idCard: ''
});

// 表单验证
const isFormValid = computed(() => {
  return editForm.name && 
         editForm.phone && 
         editForm.age > 0 && 
         editForm.gender && 
         editForm.idCard &&
         !errors.name &&
         !errors.phone &&
         !errors.age &&
         !errors.gender &&
         !errors.idCard;
});

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 跳转到问卷页面
const goToQuestionnaire = () => {
  router.push('/questionnaire');
};

// 开始编辑
const startEdit = () => {
  isEditing.value = true;
  // 复制当前数据到编辑表单
  Object.assign(editForm, {
    name: profileData.name,
    phone: profileData.phone,
    age: profileData.age,
    gender: profileData.gender,
    idCard: profileData.idCard,
    height: profileData.height,
    weight: profileData.weight,
    bloodType: profileData.bloodType,
    allergies: profileData.allergies,
    emergencyContact: { ...profileData.emergencyContact }
  });
  clearErrors();
};

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false;
  clearErrors();
};

// 同步患者信息
const syncPatientInfo = async () => {
  const token = httpService.getToken();
  if (!token) {
    console.warn('同步患者信息失败: 未找到 Token');
    return;
  }
  
  try {
    console.log('开始同步患者信息，Token 存在:', !!token);
    const response = await patientApiService.getPatientInfo(profileData.patientId ?? undefined);
    if (response.code === 0 && response.data) {
      const serverData: any = response.data;
      const emergencyContact = {
        name: serverData.emergencyContact?.name ?? serverData.emergencyContact ?? profileData.emergencyContact.name,
        phone: serverData.emergencyContact?.phone ?? serverData.emergencyContactPhone ?? profileData.emergencyContact.phone,
        relationship: serverData.emergencyContact?.relationship ?? serverData.emergencyContactRelationship ?? profileData.emergencyContact.relationship
      };
      Object.assign(profileData, {
        patientId: serverData.patientId ?? profileData.patientId,
        name: serverData.name || profileData.name,
        phone: serverData.phone || serverData.mobile || profileData.phone,
        age: serverData.age ?? profileData.age,
        gender: serverData.gender || profileData.gender,
        idCard: serverData.idCard || profileData.idCard,
        idType: serverData.idType || profileData.idType,
        ethnicity: serverData.ethnicity || profileData.ethnicity,
        height: serverData.height ?? profileData.height,
        weight: serverData.weight ?? profileData.weight,
        bloodType: serverData.bloodType || profileData.bloodType,
        allergies: serverData.allergies || profileData.allergies,
        avatar: serverData.avatar || profileData.avatar,
        emergencyContact,
        address: serverData.address || profileData.address,
        patientType: serverData.patientType ?? profileData.patientType,
        insurance: serverData.insurance ?? profileData.insurance,
        isRealNameVerified: (serverData.realNameVerified ?? serverData.isRealNameVerified) ?? profileData.isRealNameVerified,
        realNameVerifiedTime: serverData.realNameVerifiedTime || profileData.realNameVerifiedTime
      });
      console.log('患者信息同步成功');
    }
  } catch (error: any) {
    console.error('同步患者信息失败:', error);
    if (error.message?.includes('权限') || error.message?.includes('登录') || error.message?.includes('未授权')) {
      console.warn('权限错误，可能需要重新登录');
      // 不在这里跳转，让用户知道问题即可
    }
  }
};

// 保存资料
const saveProfile = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    // 更新本地数据
    userDataService.updateProfile({
      name: editForm.name,
      phone: editForm.phone,
      age: editForm.age,
      gender: editForm.gender,
      idCard: editForm.idCard,
      height: editForm.height,
      weight: editForm.weight,
      bloodType: editForm.bloodType,
      allergies: editForm.allergies,
      emergencyContact: { ...editForm.emergencyContact }
    });

    // 同步到服务器
    try {
      await patientApiService.updatePatientInfo({
        name: editForm.name,
        phone: editForm.phone,
        age: editForm.age,
        gender: editForm.gender,
        idCard: editForm.idCard,
        height: editForm.height,
        weight: editForm.weight,
        bloodType: editForm.bloodType,
        allergies: editForm.allergies,
        emergencyContact: { ...editForm.emergencyContact }
      });
    } catch (apiError) {
      console.error('同步到服务器失败:', apiError);
      // 即使服务器同步失败，本地保存仍然成功
    }

    isEditing.value = false;
    
    const toast = await toastController.create({
      message: '资料保存成功！',
      duration: 2000,
      position: 'top',
      color: 'success'
    });
    await toast.present();
  } catch (error) {
    const toast = await toastController.create({
      message: '保存失败，请重试',
      duration: 2000,
      position: 'top',
      color: 'danger'
    });
    await toast.present();
  }
};

// 表单验证
const validateForm = () => {
  clearErrors();
  let isValid = true;

  // 姓名验证
  if (!editForm.name.trim()) {
    errors.name = '请输入姓名';
    isValid = false;
  }

  // 手机号验证
  const phoneRegex = /^1[3-9]\d{9}$/;
  if (!editForm.phone.trim()) {
    errors.phone = '请输入手机号';
    isValid = false;
  } else if (!phoneRegex.test(editForm.phone)) {
    errors.phone = '请输入正确的手机号';
    isValid = false;
  }

  // 年龄验证
  if (!editForm.age || editForm.age < 1 || editForm.age > 150) {
    errors.age = '请输入正确的年龄';
    isValid = false;
  }

  // 性别验证
  if (!editForm.gender) {
    errors.gender = '请选择性别';
    isValid = false;
  }

  // 身份证验证
  const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;
  if (!editForm.idCard.trim()) {
    errors.idCard = '请输入身份证号';
    isValid = false;
  } else if (!idCardRegex.test(editForm.idCard)) {
    errors.idCard = '请输入正确的身份证号';
    isValid = false;
  }

  return isValid;
};

// 清除错误信息
const clearErrors = () => {
  (Object.keys(errors) as Array<keyof typeof errors>).forEach(key => {
    errors[key] = '';
  });
};

// 选择头像
const selectAvatar = () => {
  // 这里可以实现头像选择功能
  console.log('选择头像');
};

// 加载健康数据
const loadHealthData = () => {
  healthData.value = healthDataService.getHealthData();
};

// 加载用户数据
const loadUserData = () => {
  const userProfile = userDataService.getUserProfile();
  Object.assign(profileData, userProfile);
};

// 用户数据变化监听器
const onUserDataChange = (newProfile: UserProfile) => {
  Object.assign(profileData, newProfile);
};

// 检查登录状态
onMounted(async () => {
  const token = httpService.getToken();
  if (!token) {
    router.push('/login');
    return;
  }
  
  loadUserData();
  loadHealthData();
  
  // 添加用户数据变化监听器
  userDataService.addListener(onUserDataChange);
  
  // 尝试从服务器同步患者信息
  await syncPatientInfo();
});

// 组件卸载时清理
onUnmounted(() => {
  userDataService.removeListener(onUserDataChange);
});
</script>

<style scoped>
/* 强制覆盖深色模式，确保浅色主题显示 */
:root {
  --primary-blue: #2979FF !important;
  --light-blue: #DBEAFF !important;
  --main-text: #333333 !important;
  --secondary-text: #556C8A !important;
  --tip-bg: rgba(207, 231, 255, 0.57) !important;
  --tip-text: #556C8A !important;
}

/* 强制设置浅色主题 */
ion-content {
  --background: linear-gradient(180deg, #DBEAFF 0%, #FFFFFF 100%) !important;
  --color: #333333 !important;
}

.container {
  padding: 16px;
  background: linear-gradient(180deg, #DBEAFF 0%, #FFFFFF 100%) !important;
  min-height: 100vh;
  color: #333333 !important;
}

.custom-header {
  --background: #2979FF !important;
  --color: white !important;
  box-shadow: 0 2px 8px rgba(41, 121, 255, 0.15);
}

.app-title {
  font-weight: 600;
  font-size: 1.2rem;
  color: white !important;
}

.edit-button, .save-button {
  --color: white !important;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 8px;
  margin: 0 4px;
  transition: all 0.3s ease;
}

.edit-button:hover, .save-button:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  transform: translateY(-1px);
}

.cancel-button {
  --color: #ff6b6b !important;
  font-weight: 600;
  background: rgba(255, 107, 107, 0.1) !important;
  border-radius: 8px;
  margin: 0 4px;
  transition: all 0.3s ease;
}

.cancel-button:hover {
  background: rgba(255, 107, 107, 0.2) !important;
  transform: translateY(-1px);
}

.avatar-section {
  text-align: center;
  margin-bottom: 32px;
  padding: 32px 20px;
  background: linear-gradient(135deg, #2979FF 0%, #1e5bcc 100%) !important;
  border-radius: 0 0 24px 24px;
  box-shadow: 0 4px 20px rgba(41, 121, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.avatar-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
  z-index: 2;
}

.avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.avatar.editable {
  cursor: pointer;
  border-color: rgba(255, 255, 255, 0.6);
}

.avatar.editable:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
  border-color: white;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.camera-icon {
  color: white !important;
  font-size: 28px;
}

.user-name {
  margin: 12px 0 8px 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: white !important;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 2;
  position: relative;
}

.user-id {
  margin: 0;
  color: rgba(255, 255, 255, 0.8) !important;
  font-size: 1rem;
  font-weight: 500;
  z-index: 2;
  position: relative;
}

.verification-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2) !important;
  color: white !important;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.85rem;
  font-weight: 600;
  margin-top: 8px;
  z-index: 2;
  position: relative;
}

.badge-icon {
  font-size: 16px;
}

.badge-text {
  font-size: 0.8rem;
}

.info-card {
  margin-bottom: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(41, 121, 255, 0.08);
  border: 1px solid rgba(41, 121, 255, 0.1);
  background: white !important;
  overflow: hidden;
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(41, 121, 255, 0.12);
}

.card-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333333 !important;
  margin-bottom: 12px;
  padding: 0 4px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #2979FF;
  border-radius: 2px;
}

.health-data-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(207, 231, 255, 0.57) !important;
  color: #556C8A !important;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.badge-icon {
  font-size: 16px;
}

.badge-text {
  font-size: 0.75rem;
}

.info-display {
  padding: 8px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(41, 121, 255, 0.08);
  transition: background-color 0.2s ease;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item:hover {
  background-color: rgba(41, 121, 255, 0.02);
  border-radius: 8px;
  margin: 0 -8px;
  padding: 16px 8px;
}

.info-label {
  font-weight: 600;
  color: #556C8A !important;
  min-width: 90px;
  font-size: 0.95rem;
}

.info-value {
  color: #333333 !important;
  text-align: right;
  flex: 1;
  font-weight: 500;
  font-size: 0.95rem;
  word-break: break-word;
}

.questionnaire-health-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 2px solid rgba(41, 121, 255, 0.1);
}

.section-divider {
  text-align: center;
  margin-bottom: 16px;
  position: relative;
}

.section-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(41, 121, 255, 0.2);
}

.divider-text {
  background: white !important;
  padding: 0 16px;
  color: #556C8A !important;
  font-size: 0.9rem;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

.no-questionnaire-section {
  text-align: center;
  padding: 32px 16px;
  color: #556C8A !important;
}

.no-data-icon {
  font-size: 48px;
  color: rgba(41, 121, 255, 0.3);
  margin-bottom: 16px;
}

.no-data-text {
  margin: 0 0 16px 0;
  font-size: 1rem;
  color: #556C8A !important;
}

.questionnaire-button {
  --color: #2979FF !important;
  --border-color: #2979FF !important;
  --background: transparent !important;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.questionnaire-button:hover {
  --background: #2979FF !important;
  --color: white !important;
  transform: translateY(-1px);
}

.info-edit {
  padding: 8px 0;
}

.edit-item {
  --padding-start: 0;
  --padding-end: 0;
  --inner-padding-end: 0;
  margin-bottom: 20px;
  --background: transparent !important;
  --border-radius: 12px;
  --border-width: 2px;
  --border-color: rgba(41, 121, 255, 0.1);
  --border-style: solid;
  transition: all 0.3s ease;
}

.edit-item:hover {
  --border-color: rgba(41, 121, 255, 0.3);
  transform: translateY(-1px);
}

.edit-item:focus-within {
  --border-color: #2979FF !important;
  box-shadow: 0 0 0 3px rgba(41, 121, 255, 0.1);
}

.edit-item:last-child {
  margin-bottom: 0;
}

.error-message {
  color: #ff6b6b !important;
  font-size: 0.85rem;
  margin-top: 6px;
  margin-left: 16px;
  font-weight: 500;
}

.ion-invalid {
  --border-color: #ff6b6b !important;
}

/* 输入框和选择器样式优化 */
ion-input, ion-select, ion-textarea {
  --color: #333333 !important;
  --placeholder-color: #556C8A !important;
  --border-radius: 8px;
  font-size: 0.95rem;
}

ion-label {
  --color: #556C8A !important;
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 8px;
}

/* 提示框样式 */
ion-toast {
  --background: rgba(207, 231, 255, 0.57) !important;
  --color: #556C8A !important;
  --border-radius: 12px;
  font-weight: 500;
}

/* 强制覆盖深色模式 */
@media (prefers-color-scheme: dark) {
  .container {
    background: linear-gradient(180deg, #DBEAFF 0%, #FFFFFF 100%) !important;
  }
  
  .info-card {
    background: white !important;
    border-color: rgba(41, 121, 255, 0.1) !important;
  }
  
  .user-name {
    color: white !important;
  }
  
  .user-id {
    color: rgba(255, 255, 255, 0.8) !important;
  }
  
  .info-label {
    color: #556C8A !important;
  }
  
  .info-value {
    color: #333333 !important;
  }
  
  .card-title {
    color: #333333 !important;
  }
  
  .info-item:hover {
    background-color: rgba(41, 121, 255, 0.02) !important;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 12px;
  }
  
  .avatar {
    width: 76px;
    height: 76px;
  }
  
  .user-name {
    font-size: 1.6rem;
  }
  
  .avatar-section {
    padding: 24px 16px;
  }
  
  .info-card {
    margin-bottom: 16px;
  }
}

@media (max-width: 480px) {
  .avatar {
    width: 68px;
    height: 68px;
  }
  
  .user-name {
    font-size: 1.4rem;
  }
  
  .card-title {
    font-size: 1.1rem;
  }
}

/* 加载动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.info-card {
  animation: fadeInUp 0.6s ease-out;
}

.info-card:nth-child(2) { animation-delay: 0.1s; }
.info-card:nth-child(3) { animation-delay: 0.2s; }
.info-card:nth-child(4) { animation-delay: 0.3s; }
</style>

