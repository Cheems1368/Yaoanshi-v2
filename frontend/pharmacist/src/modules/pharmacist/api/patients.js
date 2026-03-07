import request from '@/utils/request'

const backendV2BaseUrl = import.meta.env.VITE_BACKEND_V2_API_BASE_URL || ''

export function getPatients(params) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/patients',
    method: 'get',
    params
  })
}

export function getPatientDetail(id) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/patients/${id}`,
    method: 'get'
  })
}

export function updatePatient(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/patients/${id}`,
    method: 'put',
    data
  })
}
