export interface UserProfile {
  userId: string;
  patientId?: number;
  name: string;
  phone: string;
  age: number;
  gender: string;
  idCard: string;
  idType: string;
  ethnicity: string;
  height: number;
  weight: number;
  bloodType: string;
  allergies: string;
  avatar: string;
  address?: string;
  patientType?: number;
  insurance?: number;
  emergencyContact: {
    name: string;
    phone: string;
    relationship: string;
  };
  isRealNameVerified: boolean;
  realNameVerifiedTime?: string;
}

class UserDataService {
  private readonly STORAGE_KEY = 'patient_user_profile';
  private readonly listeners: Array<(profile: UserProfile) => void> = [];

  private defaultProfile: UserProfile = {
    userId: 'P2024001',
    patientId: undefined,
    name: '张三',
    phone: '138****8888',
    age: 30,
    gender: '男',
    idCard: '330***********1234',
    idType: '居民身份证',
    ethnicity: '汉族',
    height: 175,
    weight: 70,
    bloodType: 'A型',
    allergies: '青霉素过敏',
    avatar: '/src/picture/user_avatar.png',
    address: '',
    patientType: undefined,
    insurance: undefined,
    emergencyContact: {
      name: '张女士',
      phone: '139****9999',
      relationship: '配偶'
    },
    isRealNameVerified: false
  };

  getUserProfile(): UserProfile {
    try {
      const data = localStorage.getItem(this.STORAGE_KEY);
      if (data) {
        const profile = JSON.parse(data);
        return { ...this.defaultProfile, ...profile };
      }
      return this.defaultProfile;
    } catch (error) {
      console.error('获取用户资料失败:', error);
      return this.defaultProfile;
    }
  }

  private saveUserProfile(profile: Partial<UserProfile>): void {
    try {
      const currentProfile = this.getUserProfile();
      const updatedProfile: UserProfile = { ...currentProfile, ...profile };
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(updatedProfile));
      this.notifyListeners(updatedProfile);
    } catch (error) {
      console.error('保存用户资料失败:', error);
      throw new Error('保存用户资料失败');
    }
  }

  updateRealNameInfo(realNameData: {
    realName: string;
    idType: string;
    idNumber: string;
    ethnicity: string;
    mobile: string;
    patientId?: number;
  }): void {
    const updatedProfile: Partial<UserProfile> = {
      patientId: realNameData.patientId ?? this.getUserProfile().patientId,
      name: realNameData.realName,
      idType: realNameData.idType,
      idCard: this.maskIdCard(realNameData.idNumber),
      ethnicity: realNameData.ethnicity,
      phone: this.maskPhone(realNameData.mobile),
      isRealNameVerified: true,
      realNameVerifiedTime: new Date().toISOString()
    };
    this.saveUserProfile(updatedProfile);
  }

  updateProfile(profileData: Partial<UserProfile>): void {
    this.saveUserProfile(profileData);
  }

  addListener(listener: (profile: UserProfile) => void): void {
    this.listeners.push(listener);
  }

  removeListener(listener: (profile: UserProfile) => void): void {
    const index = this.listeners.indexOf(listener);
    if (index > -1) {
      this.listeners.splice(index, 1);
    }
  }

  private notifyListeners(profile: UserProfile): void {
    this.listeners.forEach(listener => {
      try {
        listener(profile);
      } catch (error) {
        console.error('通知监听器失败:', error);
      }
    });
  }

  private maskIdCard(idCard: string): string {
    if (idCard.length < 8) return idCard;
    return idCard.substring(0, 3) + '*'.repeat(idCard.length - 7) + idCard.substring(idCard.length - 4);
  }

  private maskPhone(phone: string): string {
    if (phone.length < 7) return phone;
    return phone.substring(0, 3) + '****' + phone.substring(phone.length - 4);
  }

  clearUserData(): void {
    try {
      localStorage.removeItem(this.STORAGE_KEY);
    } catch (error) {
      console.error('清除用户资料失败:', error);
    }
  }

  isRealNameVerified(): boolean {
    return this.getUserProfile().isRealNameVerified;
  }

  getRealNameStatus(): {
    isVerified: boolean;
    verifiedTime?: string;
    name?: string;
    patientId?: number;
  } {
    const profile = this.getUserProfile();
    return {
      isVerified: profile.isRealNameVerified,
      verifiedTime: profile.realNameVerifiedTime,
      name: profile.name,
      patientId: profile.patientId
    };
  }
}

export const userDataService = new UserDataService();
