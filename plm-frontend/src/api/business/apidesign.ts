/** 接口设计 API — PRD §F3.3 */
import request from '@/utils/request'

export interface Apidesign {
  apidesignId?: number
  apidesignNo?: string
  projectId: number
  title: string
  httpMethod?: string                  // get/post/put/delete/patch
  apiPath?: string
  description?: string
  requestSchema?: string
  responseSchema?: string
  errorCodes?: string
  openapiContent?: string
  mockEnabled?: 'Y' | 'N'
  version?: string
  reviewReport?: string
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02' | '03'
  authorUserId: number
  reviewerUserId?: number
  remark?: string
}

export interface ApidesignQuery {
  pageNum?: number; pageSize?: number
  apidesignNo?: string; projectId?: number; title?: string
  httpMethod?: string; apiPath?: string; status?: string
}

export const listApidesign  = (q?: ApidesignQuery) => request({ url: '/business/apidesign/list', method: 'get', params: q })
export const getApidesign   = (id: number) => request({ url: `/business/apidesign/${id}`, method: 'get' })
export const addApidesign   = (d: Apidesign) => request({ url: '/business/apidesign', method: 'post', data: d })
export const updateApidesign= (d: Apidesign) => request({ url: '/business/apidesign', method: 'put', data: d })
export const delApidesign   = (ids: number|number[]) => request({ url: `/business/apidesign/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiOpenapiApidesign = (id: number) => request({ url: `/business/apidesign/ai/openapi/${id}`, method: 'post' })
export const exportApidesign    = (q?: ApidesignQuery) => request({ url: '/business/apidesign/export', method: 'post', params: q, responseType: 'blob' })
