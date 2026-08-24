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
            <img :src="profileData.avatar || defaultAvatarImg" alt="用户头像" class="avatar" :class="{ 'editable': isEditing }">
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
                <div class="info-label">实名材料</div>
                <div class="info-value">{{ profileData.identityMaterialArchived ? '已归档' : '未归档' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">证件类型</div>
                <div class="info-value">{{ profileData.idType }}</div>
              </div>
              <div v-if="profileData.realNameVerifiedTime" class="info-item">
                <div class="info-label">认证时间</div>
                <div class="info-value">{{ formatDate(profileData.realNameVerifiedTime) }}</div>
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

              <div class="info-tip">实名材料已做后台归档，当前页面不展示也不重新收集件号。</div>
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

        <ion-card v-if="canManageFamily" class="info-card" data-testid="family-binding-card">
          <ion-card-header>
            <ion-card-title class="card-title">亲情号管理</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <p class="family-hint">最多绑定 2 位亲友。解绑前会再次确认。</p>
            <div v-if="familyLoading" class="family-empty">加载中...</div>
            <div v-else-if="familyBindings.length === 0" class="family-empty">暂未绑定亲情号</div>
            <div v-else class="family-list">
              <div v-for="binding in familyBindings" :key="binding.id" class="family-row">
                <div>
                  <div class="family-name">{{ binding.displayName || '亲友' }}</div>
                  <div class="family-meta">{{ binding.mobileMasked }} · {{ relationshipText(binding.relationshipType) }}</div>
                </div>
                <ion-button fill="clear" color="danger" size="small" @click="confirmUnbind(binding)">解绑</ion-button>
              </div>
            </div>

            <div v-if="familyBindings.length < 2" class="family-form">
              <ion-item>
                <ion-label position="stacked">亲友手机号</ion-label>
                <ion-input v-model="familyForm.familyMobile" data-testid="family-mobile" type="tel" :maxlength="11" placeholder="请输入 11 位手机号" />
              </ion-item>
              <ion-item>
                <ion-label position="stacked">称呼</ion-label>
                <ion-input v-model="familyForm.familyDisplayName" data-testid="family-display-name" :maxlength="20" placeholder="如：女儿" />
              </ion-item>
              <ion-item>
                <ion-label position="stacked">关系</ion-label>
                <ion-select v-model="familyForm.relationshipType">
                  <ion-select-option value="SPOUSE">配偶</ion-select-option>
                  <ion-select-option value="PARENT">父母</ion-select-option>
                  <ion-select-option value="CHILD">子女</ion-select-option>
                  <ion-select-option value="OTHER">其他</ion-select-option>
                </ion-select>
              </ion-item>
              <ion-button data-testid="family-submit" expand="block" :disabled="familySaving || !familyFormValid" @click="addFamilyBinding">
                {{ familySaving ? '绑定中...' : '新增亲情号' }}
              </ion-button>
            </div>
          </ion-card-content>
        </ion-card>

        <div class="session-actions">
          <ion-button
            expand="block"
            fill="outline"
            color="danger"
            class="logout-button"
            :disabled="isEditing || logoutLoading"
            @click="handleLogout"
          >
            {{ logoutLoading ? '退出中...' : '退出登录' }}
          </ion-button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle,
  IonContent,
  IonHeader,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonPage,
  IonSelect,
  IonSelectOption,
  IonTextarea,
  IonTitle,
  IonToolbar,
  alertController,
  toastController
} from '@ionic/vue'
import { cameraOutline, checkmarkCircleOutline, createOutline, documentTextOutline } from 'ionicons/icons'
import defaultAvatarImg from '@/shared/assets/images/user_avatar.png'
import { patientSession } from '@/app/session'
import {
  appAuthService,
  patientService,
  type AppFamilyBindingDTO,
  type PatientProfileDTO,
  type PatientHealthProfileDTO
} from '@/modules/patient/api/mvp'
import { healthDataService, type HealthData } from '@/services/healthDataService'
import { userDataService, type UserProfile } from '@/services/userDataService'
import { clearMedicalCache } from '@/modules/patient/services/offlineMedication'

const router = useRouter()

const isEditing = ref(false)
const logoutLoading = ref(false)
const healthData = ref<HealthData | null>(null)
const canManageFamily = ref(false)
const familyBindings = ref<AppFamilyBindingDTO[]>([])
const familyLoading = ref(false)
const familySaving = ref(false)
const familyForm = reactive({ familyMobile: '', familyDisplayName: '', relationshipType: 'OTHER' })
const familyFormValid = computed(() => /^1\d{10}$/.test(familyForm.familyMobile) && Boolean(familyForm.familyDisplayName.trim()))

const profileData = reactive<UserProfile>({
  userId: 'P2024001',
  patientId: undefined,
  name: '张患者',
  phone: '13800000000',
  age: 30,
  gender: '男',
  idCard: '',
  idType: '身份证',
  ethnicity: '汉族',
  height: 175,
  weight: 70,
  bloodType: 'A型',
  allergies: '无',
  avatar: defaultAvatarImg,
  address: '',
  patientType: 0,
  insurance: 0,
  emergencyContact: {
    name: '',
    phone: '',
    relationship: ''
  },
  isRealNameVerified: false,
  identityMaterialArchived: false
})

const editForm = reactive({
  name: '',
  phone: '',
  age: 0,
  gender: '',
  height: 0,
  weight: 0,
  bloodType: '',
  allergies: '',
  emergencyContact: {
    name: '',
    phone: '',
    relationship: ''
  }
})

const errors = reactive({
  name: '',
  phone: '',
  age: '',
  gender: ''
})

const isFormValid = computed(() => {
  return editForm.name
    && editForm.phone
    && editForm.age > 0
    && editForm.gender
    && !errors.name
    && !errors.phone
    && !errors.age
    && !errors.gender
})

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goToQuestionnaire = () => {
  router.push('/questionnaire')
}

const startEdit = () => {
  isEditing.value = true
  Object.assign(editForm, {
    name: profileData.name,
    phone: profileData.phone,
    age: profileData.age,
    gender: profileData.gender,
    height: profileData.height,
    weight: profileData.weight,
    bloodType: profileData.bloodType,
    allergies: profileData.allergies,
    emergencyContact: { ...profileData.emergencyContact }
  })
  clearErrors()
}

const cancelEdit = () => {
  isEditing.value = false
  clearErrors()
}

const toHealthData = (serverData: PatientHealthProfileDTO): HealthData => ({
  layer1: {
    allergy: serverData.layer1?.allergy || '',
    diagnosis: serverData.layer1?.diagnosis || ''
  },
  layer2: {
    medicalHistory: serverData.layer2?.medicalHistory || '',
    smokingDrinking: serverData.layer2?.smokingDrinking || '',
    liverKidney: serverData.layer2?.liverKidney || '',
    currentMedication: serverData.layer2?.currentMedication || ''
  },
  layer3: {
    detailedSymptoms: serverData.layer3?.detailedSymptoms || '',
    familyHistory: serverData.layer3?.familyHistory || '',
    surgeryHistory: serverData.layer3?.surgeryHistory || '',
    socialHistory: serverData.layer3?.socialHistory || ''
  },
  submitTime: serverData.submitTime || new Date().toISOString()
})

const applyProfileSnapshot = (serverData: PatientProfileDTO) => {
  const emergencyContact = {
    name: serverData.emergencyContact?.name || profileData.emergencyContact.name,
    phone: serverData.emergencyContact?.phone || profileData.emergencyContact.phone,
    relationship: serverData.emergencyContact?.relationship || profileData.emergencyContact.relationship
  }
  const nextProfile: Partial<UserProfile> = {
    userId: serverData.patientId ? `PAT-${serverData.patientId}` : profileData.userId,
    patientId: serverData.patientId ?? profileData.patientId,
    name: serverData.name || profileData.name,
    phone: serverData.phone || profileData.phone,
    age: serverData.age ?? profileData.age,
    gender: serverData.gender || profileData.gender,
    idCard: '',
    idType: serverData.idType || profileData.idType,
    ethnicity: serverData.ethnicity || profileData.ethnicity,
    height: serverData.height ?? profileData.height,
    weight: serverData.weight ?? profileData.weight,
    bloodType: serverData.bloodType || profileData.bloodType,
    allergies: serverData.allergies || profileData.allergies,
    avatar: serverData.avatar || profileData.avatar,
    address: serverData.address || profileData.address,
    patientType: serverData.patientType ?? profileData.patientType,
    insurance: serverData.insurance ?? profileData.insurance,
    emergencyContact,
    identityMaterialArchived: Boolean(serverData.identityMaterialArchived ?? profileData.identityMaterialArchived),
    isRealNameVerified: Boolean(serverData.realNameVerified ?? profileData.isRealNameVerified),
    realNameVerifiedTime: serverData.realNameVerifiedTime || profileData.realNameVerifiedTime
  }

  Object.assign(profileData, nextProfile)
  userDataService.updateProfile(nextProfile)
}

const applyHealthSnapshot = (serverData: PatientHealthProfileDTO | null | undefined) => {
  if (!serverData?.submitTime) {
    healthData.value = null
    healthDataService.replaceHealthData(null)
    return
  }

  const nextHealthData = toHealthData(serverData)
  healthData.value = nextHealthData
  healthDataService.replaceHealthData(nextHealthData)
}

const syncPatientProfileDTO = async () => {
  const response = await patientService.getProfile()
  if (response.data) {
    applyProfileSnapshot(response.data)
  }
}

const syncHealthProfile = async () => {
  const response = await patientService.getHealthProfile()
  applyHealthSnapshot(response.data)
}

const saveProfile = async () => {
  if (!validateForm()) {
    return
  }

  try {
    const response = await patientService.updateProfile({
      name: editForm.name,
      phone: editForm.phone,
      age: editForm.age,
      gender: editForm.gender,
      height: editForm.height,
      weight: editForm.weight,
      bloodType: editForm.bloodType,
      allergies: editForm.allergies,
      emergencyContact: { ...editForm.emergencyContact }
    })

    if (response.data) {
      applyProfileSnapshot(response.data)
    } else {
      await syncPatientProfileDTO()
    }

    isEditing.value = false

    const toast = await toastController.create({
      message: '资料保存成功',
      duration: 2000,
      position: 'top',
      color: 'success'
    })
    await toast.present()
  } catch {
    const toast = await toastController.create({
      message: '保存失败，请重试',
      duration: 2000,
      position: 'top',
      color: 'danger'
    })
    await toast.present()
  }
}

const validateForm = () => {
  clearErrors()
  let valid = true

  if (!editForm.name.trim()) {
    errors.name = '请输入姓名'
    valid = false
  }

  if (!/^1[3-9]\d{9}$/.test(editForm.phone.trim())) {
    errors.phone = '请输入正确的手机号'
    valid = false
  }

  if (!editForm.age || editForm.age < 1 || editForm.age > 150) {
    errors.age = '请输入正确的年龄'
    valid = false
  }

  if (!editForm.gender) {
    errors.gender = '请选择性别'
    valid = false
  }

  return valid
}

const clearErrors = () => {
  (Object.keys(errors) as Array<keyof typeof errors>).forEach((key) => {
    errors[key] = ''
  })
}

const selectAvatar = () => {
  // 头像上传未在本次安全收敛范围内，暂不实现。
}

const loadHealthData = () => {
  healthData.value = healthDataService.getHealthData()
}

const loadUserData = () => {
  const userProfile = userDataService.getUserProfile()
  Object.assign(profileData, userProfile)
}

const clearLocalSession = () => {
  patientSession.clearAuthState()
  patientSession.clearGateState()
  userDataService.clearUserData()
  healthDataService.clearHealthData()
}

const relationshipText = (value: string) => ({
  SPOUSE: '配偶', PARENT: '父母', CHILD: '子女', OTHER: '其他'
}[value] || value)

const loadFamilyBindings = async () => {
  familyLoading.value = true
  try {
    const account = await appAuthService.getCurrentAccount()
    canManageFamily.value = String((account.data as any)?.accountType || 'PATIENT').toUpperCase() === 'PATIENT'
    if (!canManageFamily.value) return
    const response = await patientService.getFamilyBindings()
    familyBindings.value = response.data ?? []
  } catch {
    familyBindings.value = []
  } finally {
    familyLoading.value = false
  }
}

const addFamilyBinding = async () => {
  if (!familyFormValid.value || familySaving.value) return
  familySaving.value = true
  try {
    await patientService.createFamilyBinding({ ...familyForm, familyDisplayName: familyForm.familyDisplayName.trim() })
    familyForm.familyMobile = ''
    familyForm.familyDisplayName = ''
    familyForm.relationshipType = 'OTHER'
    await loadFamilyBindings()
    const toast = await toastController.create({ message: '亲情号绑定成功', duration: 1800, color: 'success' })
    await toast.present()
  } catch (error: any) {
    const toast = await toastController.create({ message: error?.message || '亲情号绑定失败', duration: 2200, color: 'danger' })
    await toast.present()
  } finally {
    familySaving.value = false
  }
}

const confirmUnbind = async (binding: AppFamilyBindingDTO) => {
  const alert = await alertController.create({
    header: '确认解绑亲情号？',
    message: `解绑后 ${binding.displayName || '该亲友'} 将无法协助查看与打卡。`,
    buttons: [
      { text: '取消', role: 'cancel' },
      {
        text: '确认解绑',
        role: 'destructive',
        handler: () => {
          void patientService.deleteFamilyBinding(binding.id).then(async () => {
            await clearMedicalCache()
            await loadFamilyBindings()
          })
        }
      }
    ]
  })
  await alert.present()
}

const handleLogout = async () => {
  if (logoutLoading.value) {
    return
  }

  logoutLoading.value = true

  try {
    await appAuthService.logout()
  } catch {
    // ignore logout network failure and close local session
  } finally {
    await clearMedicalCache().catch(() => undefined)
    clearLocalSession()
    logoutLoading.value = false
    await router.replace('/login')
  }
}

const onUserDataChange = (newProfile: UserProfile) => {
  Object.assign(profileData, newProfile)
}

onMounted(async () => {
  if (!patientSession.getGateState().loggedIn) {
    router.push('/login')
    return
  }

  loadUserData()
  loadHealthData()
  userDataService.addListener(onUserDataChange)

  try {
    await Promise.all([syncPatientProfileDTO(), syncHealthProfile(), loadFamilyBindings()])
  } catch {
    // keep current session cache when sync fails
  }
})

onUnmounted(() => {
  userDataService.removeListener(onUserDataChange)
})
</script>

<style scoped>
.custom-content {
  --background: var(--patient-bg-gradient);
}

.container {
  padding: var(--patient-space-4);
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* Header */
.custom-header {
  --background: var(--patient-primary);
  --color: var(--patient-text-inverse);
}

.app-title {
  font-weight: var(--patient-font-bold);
  color: var(--patient-text-inverse);
}

.edit-button,
.save-button {
  --color: var(--patient-text-inverse);
}

.cancel-button {
  --color: var(--patient-danger);
}

/* Avatar section */
.avatar-section {
  text-align: center;
  padding: var(--patient-space-8) var(--patient-space-5) var(--patient-space-6);
  background: linear-gradient(135deg, var(--patient-primary), var(--patient-primary-dark));
  border-radius: 0 0 var(--patient-radius-xl) var(--patient-radius-xl);
  margin-bottom: var(--patient-space-4);
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: var(--patient-space-4);
}

.avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.4);
  /* 88px is a specific brand design decision for avatar size */
}

.avatar.editable {
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.camera-icon {
  color: var(--patient-text-inverse);
  font-size: var(--patient-font-title);
}

.user-name {
  margin: 0 0 var(--patient-space-2);
  font-size: var(--patient-font-title);
  font-weight: var(--patient-font-bold);
  color: var(--patient-text-inverse);
  line-height: var(--patient-leading-tight);
}

.user-id {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  font-size: var(--patient-font-body);
  font-weight: var(--patient-font-medium);
}

.verification-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--patient-space-2);
  background: rgba(255, 255, 255, 0.2);
  color: var(--patient-text-inverse);
  padding: var(--patient-space-1) var(--patient-space-3);
  border-radius: var(--patient-radius-md);
  font-size: var(--patient-font-caption);
  font-weight: var(--patient-font-semibold);
  margin-top: var(--patient-space-2);
}

.badge-icon {
  font-size: var(--patient-font-body);
}

/* Info cards */
.info-card {
  margin-bottom: var(--patient-space-3);
}

.card-title {
  font-size: var(--patient-font-subtitle);
  font-weight: var(--patient-font-bold);
  color: var(--patient-text-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.health-data-badge {
  display: flex;
  align-items: center;
  gap: var(--patient-space-2);
  background: var(--patient-primary-light);
  color: var(--patient-text-secondary);
  padding: var(--patient-space-1) var(--patient-space-2);
  border-radius: var(--patient-radius-sm);
  font-size: var(--patient-font-caption);
  font-weight: var(--patient-font-semibold);
}

/* Info display (view mode) */
.info-display {
  padding: var(--patient-space-2) 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--patient-space-4) 0;
  border-bottom: var(--patient-border-light);
  min-height: 48px;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: var(--patient-font-semibold);
  color: var(--patient-text-secondary);
  min-width: 90px;
  font-size: var(--patient-font-body);
}

.info-value {
  color: var(--patient-text-primary);
  text-align: right;
  flex: 1;
  font-weight: var(--patient-font-medium);
  font-size: var(--patient-font-body);
  word-break: break-word;
}

/* Questionnaire section */
.questionnaire-health-section {
  margin-top: var(--patient-space-4);
  padding-top: var(--patient-space-4);
  border-top: var(--patient-border-light);
}

.section-divider {
  text-align: center;
  margin-bottom: var(--patient-space-4);
  position: relative;
}

.section-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--patient-border);
}

.divider-text {
  background: var(--patient-bg-card);
  padding: 0 var(--patient-space-4);
  color: var(--patient-text-secondary);
  font-size: var(--patient-font-helper);
  font-weight: var(--patient-font-semibold);
  position: relative;
  z-index: 1;
}

.no-questionnaire-section {
  text-align: center;
  padding: var(--patient-space-8) var(--patient-space-4);
}

.no-data-icon {
  font-size: 48px;
  color: var(--patient-text-muted);
  margin-bottom: var(--patient-space-4);
}

.no-data-text {
  margin: 0 0 var(--patient-space-4);
  font-size: var(--patient-font-body);
  color: var(--patient-text-secondary);
}

/* Edit mode */
.info-tip {
  margin: var(--patient-space-2) 0 0;
  padding: var(--patient-space-3) var(--patient-space-4);
  border-radius: var(--patient-radius-md);
  background: var(--patient-primary-light);
  color: var(--patient-text-secondary);
  font-size: var(--patient-font-caption);
  line-height: var(--patient-leading-relaxed);
}

.info-edit {
  padding: var(--patient-space-2) 0;
}

.edit-item {
  --padding-start: 0;
  --padding-end: 0;
  --inner-padding-end: 0;
  --background: transparent;
  margin-bottom: var(--patient-space-5);
}

.edit-item:last-child {
  margin-bottom: 0;
}

.error-message {
  color: var(--patient-danger);
  font-size: var(--patient-font-caption);
  margin-top: var(--patient-space-1);
  margin-left: var(--patient-space-4);
  font-weight: var(--patient-font-medium);
}

/* Session actions */
.session-actions {
  margin: var(--patient-space-2) 0 var(--patient-space-6);
}

.logout-button {
  --border-radius: var(--patient-radius-md);
  height: 48px;
  font-weight: var(--patient-font-semibold);
}

.family-hint,
.family-empty,
.family-meta {
  color: var(--patient-text-secondary);
  font-size: var(--patient-font-helper);
}

.family-list {
  display: flex;
  flex-direction: column;
  margin-bottom: var(--patient-space-3);
}

.family-row {
  min-height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: var(--patient-border-light);
}

.family-name {
  color: var(--patient-text-primary);
  font-weight: var(--patient-font-semibold);
}

.family-form {
  display: flex;
  flex-direction: column;
  gap: var(--patient-space-2);
}
</style>

