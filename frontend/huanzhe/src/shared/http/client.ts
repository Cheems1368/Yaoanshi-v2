import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { patientSession } from '@/app/session'

export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

class HttpService {
  private readonly instance: AxiosInstance
  private refreshPromise: Promise<string | null> | null = null

  constructor() {
    const apiBaseUrl = typeof __API_BASE_URL__ === 'string' ? __API_BASE_URL__ : 'http://127.0.0.1:8080'

    this.instance = axios.create({
      baseURL: import.meta.env.DEV ? '' : apiBaseUrl,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    this.instance.interceptors.request.use(async (config) => {
      const requestUrl = String(config.url ?? '')
      let token = patientSession.getAccessToken()
      config.headers = config.headers ?? {}

      if (!token && patientSession.getRefreshToken() && !this.shouldSkipRefresh(requestUrl)) {
        token = await this.refreshAccessToken()
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

  private shouldSkipRefresh(requestUrl: string) {
    return requestUrl.includes('/app-api/auth/login')
      || requestUrl.includes('/app-api/auth/send-otp')
      || requestUrl.includes('/app-api/auth/refresh-token')
  }

  private async retryWithRefresh(
    config?: AxiosRequestConfig & { _retry?: boolean },
    fallbackMessage = 'Login expired'
  ) {
    const requestUrl = String(config?.url ?? '')

    if (!config || config._retry || this.shouldSkipRefresh(requestUrl)) {
      this.clearToken()
      this.redirectToLogin()
      return Promise.reject(new Error(fallbackMessage))
    }

    const nextAccessToken = await this.refreshAccessToken()
    if (!nextAccessToken) {
      this.clearToken()
      this.redirectToLogin()
      return Promise.reject(new Error(fallbackMessage))
    }

    config._retry = true
    config.headers = {
      ...(config.headers ?? {}),
      Authorization: `Bearer ${nextAccessToken}`
    }

    return this.instance.request(config)
  }

  private async refreshAccessToken(): Promise<string | null> {
    const refreshToken = patientSession.getRefreshToken()
    const deviceId = patientSession.getDeviceId()

    if (!refreshToken || !deviceId) {
      return null
    }

    if (!this.refreshPromise) {
      const baseURL = this.instance.defaults.baseURL ?? ''
      this.refreshPromise = axios
        .post<ApiResponse<{ accessToken: string; refreshToken: string; expiresIn: number; refreshExpiresIn: number; deviceId: string }>>(
          `${baseURL}/app-api/auth/refresh-token`,
          {
            refreshToken,
            deviceId
          },
          {
            timeout: 10000,
            headers: {
              'Content-Type': 'application/json',
              'tenant-id': '1'
            }
          }
        )
        .then((response) => {
          const payload = response.data
          if (payload.code !== 200 && payload.code !== 0) {
            throw new Error(payload.msg || 'Refresh token invalid')
          }

          patientSession.setAccessToken(payload.data.accessToken)
          patientSession.setRefreshToken(payload.data.refreshToken)
          return payload.data.accessToken
        })
        .catch(() => {
          this.clearToken()
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
