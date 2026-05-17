/**
 * 接口详细设计 LLD API — PRD §F3.3 + 原型 apidesign.html
 */
import request from '@/utils/request'

export interface ApiDesign {
  apidesignId?: number
  apidesignNo?: string
  projectId: number
  prdId?: number
  archId?: number
  httpMethod: string  // GET / POST / PUT / DELETE / PATCH
  path: string
  description?: string
  authRequired?: string  // Y / N
  requestSchema?: string  // JSON Schema
  responseSchema?: string  // JSON Schema
  exampleRequest?: string
  exampleResponse?: string
  errorCodes?: string  // 400/401/403/404/500 列表
  mockEnabled?: string  // Y / N
  mockResponse?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number
}

export interface ApiDesignQuery {
  pageNum?: number; pageSize?: number; projectId?: number; httpMethod?: string; path?: string; status?: string
}

export const listApiDesign = (q: ApiDesignQuery): Promise<any> =>
  request({ url: '/business/apidesign/list', method: 'get', params: q })
export const getApiDesign = (id: number): Promise<any> =>
  request({ url: `/business/apidesign/${id}`, method: 'get' })
export const addApiDesign = (d: ApiDesign): Promise<any> =>
  request({ url: '/business/apidesign', method: 'post', data: d })
export const updateApiDesign = (d: ApiDesign): Promise<any> =>
  request({ url: '/business/apidesign', method: 'put', data: d })
export const delApiDesign = (ids: number | number[]): Promise<any> =>
  request({ url: `/business/apidesign/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGenerateApiDesign = (id: number): Promise<any> =>
  request({ url: `/business/apidesign/ai/generate/${id}`, method: 'post' })
export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
