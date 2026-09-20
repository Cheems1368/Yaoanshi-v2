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
}

class HttpService {
  private readonly instance: AxiosInstance
  private refreshPromise: Promise<string | null> | null = null
  private refreshUnavailable = false

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
      const requestUrl = String(config.url ?? '')
      let token = patientSession.getAccessToken()
      config.headers = config.headers ?? {}

      const offlineSession = patientSession.getAuthStatus() === 'offline'
        && typeof navigator !== 'undefined' && !navigator.onLine
      if (!token && !offlineSession && patientSession.getAuthStatus() !== 'anonymous' && !this.shouldSkipRefresh(requestUrl)) {
        token = await this.refreshAccessToken()
      }
      if (bindAccountSubject) accountBoundConfig._accountSubject = patientSession.getAccountSubject()

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

    if (!config || config._retry || this.shouldSkipRefresh(requestUrl)) {
      this.clearToken()
      this.redirectToLogin()
      return Promise.reject(new Error(fallbackMessage))
    }

    if (config._accountSubject !== undefined
      && patientSession.getAccountSubject() !== config._accountSubject) {
      return Promise.reject(new Error('AUTHENTICATED_ACCOUNT_CHANGED'))
    }

    const nextAccessToken = await this.refreshAccessToken()
    if (!nextAccessToken) {
      if (this.refreshUnavailable) {
        return Promise.reject(new Error('Session temporarily unavailable'))
      }
      this.clearToken()
      this.redirectToLogin()
      return Promise.reject(new Error(fallbackMessage))
    }
    if (config._accountSubject !== undefined
      && patientSession.getAccountSubject() !== config._accountSubject) {
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
    if (!this.refreshPromise) {
      this.refreshUnavailable = false
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
        .then((response) => {
          const payload = response.data
          if (payload.code !== 200 && payload.code !== 0) {
            if (payload.code === 40004) {
              patientSession.markAnonymous()
            } else {
              this.refreshUnavailable = true
            }
            return null
          }

          patientSession.setAccessToken(payload.data.accessToken)
          return payload.data.accessToken
        })
        .catch((error) => {
          if (error?.response?.status !== 401 && error?.response?.data?.code !== 40004) {
            this.refreshUnavailable = true
            return null
          }
          patientSession.markAnonymous()
          return null
        })
        .finally(() => {
          this.refreshPromise = null
        })
    }

    return this.refreshPromise
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
