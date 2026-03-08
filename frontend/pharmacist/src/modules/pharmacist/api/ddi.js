import request from '@/utils/request'

const backendV2BaseUrl = import.meta.env.VITE_BACKEND_V2_API_BASE_URL || ''

export function getDdiRules(params) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/ddi/rules',
    method: 'get',
    params
  })
}

export function createDdiRule(data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/ddi/rules',
    method: 'post',
    data
  })
}

export function updateDdiRule(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/ddi/rules/${id}`,
    method: 'put',
    data
  })
}

export function getContraindications(params) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/ddi/contraindications',
    method: 'get',
    params
  })
}

export function createContraindication(data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/ddi/contraindications',
    method: 'post',
    data
  })
}

export function updateContraindication(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/ddi/contraindications/${id}`,
    method: 'put',
    data
  })
}
