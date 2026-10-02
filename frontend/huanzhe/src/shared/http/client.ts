import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { patientSession } from '@/app/session'

export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

type AccountBoundRequestConfig = AxiosRequestConfig & {
  _retry?: boolean
  _accountSubject?: string | null
  _sessionGeneration?: number
}

class HttpService {
  private readonly instance: AxiosInstance
  private refreshPromise: Promise<string | null> | null = null
  private refreshUnavailable = false
  private loginInProgress = false

  constructor() {
    const apiBaseUrl = typeof __API_BASE_URL__ === 'string' ? __API_BASE_URL__ : 'http://localhost:8080'

    this.instance = axios.create({
      baseURL: import.meta.env.DEV ? '' : apiBaseUrl,
      timeout: 10000,
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    this.instance.interceptors.request.use(async (config) => {
      const accountBoundConfig = config as typeof config & AccountBoundRequestConfig
      const bindAccountSubject = accountBoundConfig._accountSubject === undefined
      const requestGeneration = patientSession.getSessionGeneration()
      const requestUrl = String(config.url ?? '')
      let token = patientSession.getAccessToken()
      config.headers = config.headers ?? {}

      const offlineSession = patientSession.getAuthStatus() === 'offline'
        && typeof navigator !== 'undefined' && !navigator.onLine
      if (!token && !offlineSession && patientSession.getAuthStatus() !== 'anonymous' && !this.shouldSkipRefresh(requestUrl)) {
        token = await this.refreshAccessToken()
        if (patientSession.getSessionGeneration() !== requestGeneration) {
          throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
        }
      }
      if (bindAccountSubject) accountBoundConfig._accountSubject = patientSession.getAccountSubject()
      if (accountBoundConfig._sessionGeneration === undefined) {
        accountBoundConfig._sessionGeneration = patientSession.getSessionGeneration()
      }

      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      config.headers['tenant-id'] = '1'
      return config
    })

    this.instance.interceptors.response.use(
      (async (response: any) => {
        const payload = response.data
        if (payload.code === 200 || payload.code === 0) {
          return payload
        }
        if (payload.code === 401) {
          return this.retryWithRefresh(response.config, payload.msg || 'Login expired')
        }
        return Promise.reject(new Error(payload.msg || 'Request failed'))
      }) as any,
      async (error) => {
        if (error.response?.status === 401) {
          return this.retryWithRefresh(error.config, 'Login expired')
        }
        if (error.response?.status === 403) {
          return Promise.reject(new Error('No permission'))
        }
        if (error.response?.status === 404) {
          return Promise.reject(new Error('Resource not found'))
        }
        if (error.response?.status >= 500) {
          return Promise.reject(new Error('Server error'))
        }
        if (error.response?.data) {
          const data = error.response.data
          const businessError = new Error(data.msg || data.message || 'Request failed') as any
          businessError.code = data.code
          return Promise.reject(businessError)
        }
        if (error.request) {
          return Promise.reject(new Error('Network error'))
        }
        return Promise.reject(error)
      }
    )
  }

  setToken(token: string) {
    patientSession.setAccessToken(token)
  }

  getToken() {
    return patientSession.getAccessToken()
  }

  clearToken() {
    patientSession.clearAuthState()
    patientSession.clearGateState()
  }

  async loginSession<T extends { accessToken: string }>(payload: unknown): Promise<ApiResponse<T>> {
    this.loginInProgress = true
    patientSession.advanceSessionGeneration()
    try {
      await this.refreshPromise
      const response = await this.post<T>('/app-api/auth/session/login', payload)
      patientSession.setAccessToken(response.data.accessToken)
      return response
    } finally {
      this.loginInProgress = false
    }
  }

  async restoreSession() {
    if (patientSession.getAccessToken()) {
      return true
    }
    if (patientSession.getAuthStatus() === 'anonymous') {
      return false
    }
    if (patientSession.getAuthStatus() === 'offline' && typeof navigator !== 'undefined' && !navigator.onLine) {
      return true
    }
    if (await this.refreshAccessToken()) return true
    return this.refreshUnavailable ? patientSession.restoreOfflineContext() : false
  }

  private shouldSkipRefresh(requestUrl: string) {
    return requestUrl.includes('/app-api/auth/login')
      || requestUrl.includes('/app-api/auth/send-otp')
      || requestUrl.includes('/app-api/auth/refresh-token')
      || requestUrl.includes('/app-api/auth/session/')
  }

  private async retryWithRefresh(
    config?: AccountBoundRequestConfig,
    fallbackMessage = 'Login expired'
  ) {
    const requestUrl = String(config?.url ?? '')

    if (this.loginInProgress
      || (config?._sessionGeneration !== undefined
        && patientSession.getSessionGeneration() !== config._sessionGeneration)
      || (config?._accountSubject !== undefined
        && patientSession.getAccountSubject() !== config._accountSubject)) {
      return Promise.reject(new Error('AUTHENTICATED_ACCOUNT_CHANGED'))
    }
    if (!config || config._retry || this.shouldSkipRefresh(requestUrl)) {
      await this.expireSession()
      this.redirectToLogin()
      return Promise.reject(new Error(fallbackMessage))
    }

    const requestGeneration = patientSession.getSessionGeneration()
    const nextAccessToken = await this.refreshAccessToken()
    if (this.loginInProgress || patientSession.getSessionGeneration() !== requestGeneration) {
      if (patientSession.getAuthStatus() === 'anonymous') this.redirectToLogin()
      return Promise.reject(new Error('AUTHENTICATED_ACCOUNT_CHANGED'))
    }
    if (!nextAccessToken) {
      if (this.refreshUnavailable) {
        return Promise.reject(new Error('Session temporarily unavailable'))
      }
      await this.expireSession()
      this.redirectToLogin()
      return Promise.reject(new Error(fallbackMessage))
    }
    if (config._sessionGeneration !== undefined
      && patientSession.getSessionGeneration() !== config._sessionGeneration) {
      return Promise.reject(new Error('AUTHENTICATED_ACCOUNT_CHANGED'))
    }

    config._retry = true
    config.headers = {
      ...(config.headers ?? {}),
      Authorization: `Bearer ${nextAccessToken}`
    }

    return this.instance.request(config)
  }

  private async refreshAccessToken(): Promise<string | null> {
    if (this.loginInProgress) return null
    if (!this.refreshPromise) {
      this.refreshUnavailable = false
      const refreshGeneration = patientSession.getSessionGeneration()
      const baseURL = this.instance.defaults.baseURL ?? ''
      this.refreshPromise = axios
        .post<ApiResponse<{ accessToken: string; expiresIn: number; refreshExpiresIn: number; deviceId: string }>>(
          `${baseURL}/app-api/auth/session/refresh`,
          {},
          {
            timeout: 10000,
            withCredentials: true,
            headers: {
              'Content-Type': 'application/json',
              'tenant-id': '1',
              'X-Auth-Refresh': '1'
            }
          }
        )
        .then(async (response) => {
          if (patientSession.getSessionGeneration() !== refreshGeneration) return null
          const payload = response.data
          if (payload.code !== 200 && payload.code !== 0) {
            if (payload.code === 40004) {
              await this.expireSession()
            } else {
              this.refreshUnavailable = true
            }
            return null
          }

          patientSession.setAccessToken(payload.data.accessToken, true)
          return payload.data.accessToken
        })
        .catch(async (error) => {
          if (patientSession.getSessionGeneration() !== refreshGeneration) return null
          if (error?.response?.status !== 401 && error?.response?.data?.code !== 40004) {
            this.refreshUnavailable = true
            return null
          }
          await this.expireSession()
          return null
        })
        .finally(() => {
          this.refreshPromise = null
        })
    }

    return this.refreshPromise
  }

  private async expireSession() {
    if (patientSession.getAuthStatus() === 'anonymous') return
    const generation = patientSession.getSessionGeneration()
    const { sealPendingCheckinsForSessionExpiry } = await import('@/modules/patient/services/offlineMedication')
    if (patientSession.getSessionGeneration() !== generation) return
    await sealPendingCheckinsForSessionExpiry(patientSession.getAccountSubject())
    if (patientSession.getSessionGeneration() !== generation) return
    patientSession.markSessionExpired()
    patientSession.clearGateState()
  }

  private redirectToLogin() {
    if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
      window.location.href = '/login'
    }
  }

  get<T = unknown>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig) {
    return this.instance.get<never, ApiResponse<T>>(url, { params, ...config })
  }

  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return this.instance.post<never, ApiResponse<T>>(url, data, config)
  }

  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return this.instance.put<never, ApiResponse<T>>(url, data, config)
  }

  delete<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return this.instance.delete<never, ApiResponse<T>>(url, config)
  }

  upload<T = unknown>(url: string, file: File, onProgress?: (progress: number) => void) {
    const formData = new FormData()
    formData.append('file', file)

    return this.instance.post<never, ApiResponse<T>>(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          onProgress(Math.round((progressEvent.loaded * 100) / progressEvent.total))
        }
      }
    })
  }
}

export const httpService = new HttpService()
