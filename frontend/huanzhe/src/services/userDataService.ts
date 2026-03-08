export interface UserProfile {
  userId: string
  patientId?: number
  name: string
  phone: string
  age: number
  gender: string
  idCard: string
  idType: string
  ethnicity: string
  height: number
  weight: number
  bloodType: string
  allergies: string
  avatar: string
  address?: string
  patientType?: number
  insurance?: number
  emergencyContact: {
    name: string
    phone: string
    relationship: string
  }
  isRealNameVerified: boolean
  identityMaterialArchived?: boolean
  realNameVerifiedTime?: string
}

const cloneUserProfile = (profile: UserProfile): UserProfile => JSON.parse(JSON.stringify(profile)) as UserProfile

class UserDataService {
  private readonly listeners: Array<(profile: UserProfile) => void> = []

  private readonly defaultProfile: UserProfile = {
    userId: 'P2024001',
    patientId: undefined,
    name: '患者',
    phone: '13800000000',
    age: 30,
    gender: '男',
    idCard: '',
    idType: '',
    ethnicity: '汉族',
    height: 175,
    weight: 70,
    bloodType: 'A型',
    allergies: '无',
    avatar: '/src/picture/user_avatar.png',
    address: '',
    patientType: undefined,
    insurance: undefined,
    emergencyContact: {
      name: '',
      phone: '',
      relationship: ''
    },
    isRealNameVerified: false,
    identityMaterialArchived: false
  }

  private currentProfile: UserProfile = cloneUserProfile(this.defaultProfile)

  getUserProfile(): UserProfile {
    return cloneUserProfile(this.currentProfile)
  }

  private saveUserProfile(profile: Partial<UserProfile>): void {
    const updatedProfile: UserProfile = {
      ...this.currentProfile,
      ...profile,
      emergencyContact: {
        ...this.currentProfile.emergencyContact,
        ...(profile.emergencyContact || {})
      }
    }
    this.currentProfile = cloneUserProfile(updatedProfile)
    this.notifyListeners(this.currentProfile)
  }

  updateRealNameInfo(realNameData: {
    realName: string
    idType: string
    idNumber?: string
    ethnicity: string
    mobile: string
    patientId?: number
    identityMaterialArchived?: boolean
    realNameVerifiedTime?: string
  }): void {
    const updatedProfile: Partial<UserProfile> = {
      patientId: realNameData.patientId ?? this.currentProfile.patientId,
      name: realNameData.realName,
      idType: realNameData.idType,
      idCard: '',
      ethnicity: realNameData.ethnicity,
      phone: realNameData.mobile,
      isRealNameVerified: true,
      identityMaterialArchived: realNameData.identityMaterialArchived ?? true,
      realNameVerifiedTime: realNameData.realNameVerifiedTime ?? new Date().toISOString()
    }
    this.saveUserProfile(updatedProfile)
  }

  updateProfile(profileData: Partial<UserProfile>): void {
    this.saveUserProfile(profileData)
  }

  addListener(listener: (profile: UserProfile) => void): void {
    this.listeners.push(listener)
  }

  removeListener(listener: (profile: UserProfile) => void): void {
    const index = this.listeners.indexOf(listener)
    if (index > -1) {
      this.listeners.splice(index, 1)
    }
  }

  private notifyListeners(profile: UserProfile): void {
    this.listeners.forEach((listener) => {
      try {
        listener(cloneUserProfile(profile))
      } catch {
        // ignore listener failures so session state stays usable
      }
    })
  }

  clearUserData(): void {
    this.currentProfile = cloneUserProfile(this.defaultProfile)
    this.notifyListeners(this.currentProfile)
  }

  isRealNameVerified(): boolean {
    return this.currentProfile.isRealNameVerified
  }

  getRealNameStatus(): {
    isVerified: boolean
    verifiedTime?: string
    name?: string
    patientId?: number
    identityMaterialArchived?: boolean
  } {
    return {
      isVerified: this.currentProfile.isRealNameVerified,
      verifiedTime: this.currentProfile.realNameVerifiedTime,
      name: this.currentProfile.name,
      patientId: this.currentProfile.patientId,
      identityMaterialArchived: this.currentProfile.identityMaterialArchived
    }
  }
}

export const userDataService = new UserDataService()
