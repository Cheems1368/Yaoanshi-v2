import request from '@/utils/request'

const backendV2BaseUrl = import.meta.env.VITE_BACKEND_V2_API_BASE_URL || ''

/**
 * 搜索药品目录（支持中文名称和拼音首字母，如输入 aspl 匹配 阿司匹林）
 * @param {string} q 关键词
 * @param {number} [limit=10] 最大返回条数
 */
export function searchDrugs(q, limit = 10) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/drugs/search',
    method: 'get',
    params: { q, limit }
  })
}
