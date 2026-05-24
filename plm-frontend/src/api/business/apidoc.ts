/**
 * API 文档 API — PRD §F5.4 + 原型 apidoc.html
 */
import request from '@/utils/request'

export interface ApiDoc {
  apidocId?: number
  apidocNo?: string
  projectId: number
  apidesignId?: number
  title: string
  version?: string
  httpMethod?: string
  path?: string
  description?: string
  requestSchema?: string
  responseSchema?: string
  requestExample?: string
  responseExample?: string
  errorCodes?: string
  aiGenerated?: string
  syncedAt?: string
  status?: string
  authorUserId?: number
}

export interface ApiDocQuery { pageNum?: number; pageSize?: number; projectId?: number; title?: string; httpMethod?: string; status?: string }

export const listApiDoc = (q: ApiDocQuery): Promise<any> => request({ url: '/business/apidoc/list', method: 'get', params: q })
export const getApiDoc = (id: number): Promise<any> => request({ url: `/business/apidoc/${id}`, method: 'get' })
export const addApiDoc = (d: ApiDoc): Promise<any> => request({ url: '/business/apidoc', method: 'post', data: d })
export const updateApiDoc = (d: ApiDoc): Promise<any> => request({ url: '/business/apidoc', method: 'put', data: d })
export const delApiDoc = (ids: number | number[]): Promise<any> => request({ url: `/business/apidoc/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const syncFromCode = (id: number): Promise<any> => request({ url: `/business/apidoc/sync/${id}`, method: 'post' })
export const debugApi = (id: number, data: any): Promise<any> => request({ url: `/business/apidoc/debug/${id}`, method: 'post', data })
export const listProjectsForSelect = (): Promise<any> => request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
