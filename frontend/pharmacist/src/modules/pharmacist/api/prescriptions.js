import request from '@/utils/request'

const backendV2BaseUrl = import.meta.env.VITE_BACKEND_V2_API_BASE_URL || ''

export function getPrescriptions(params) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/prescriptions',
    method: 'get',
    params
  })
}

export function getPrescriptionDetail(id) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/prescriptions/${id}`,
    method: 'get'
  })
}

export function confirmPrescription(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/prescriptions/${id}/confirm`,
    method: 'post',
    data
  })
}

export function rejectPrescription(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/prescriptions/${id}/reject`,
    method: 'post',
    data
  })
}

export function confirmDdiScreening(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/ddi/screenings/${id}/confirm`,
    method: 'post',
    data
  })
}
