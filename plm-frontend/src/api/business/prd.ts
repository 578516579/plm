/** PRD 文档 API — PRD §F2.2 */
import request from '@/utils/request'

export interface Prd {
  prdId?: number
  prdNo?: string
  projectId: number
  title: string
  version?: string
  background?: string
  userPersonas?: string
  features?: string
  userStories?: string
  acceptanceCriteria?: string
  nonFunctionalReqs?: string
  content?: string
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02' | '03'
  authorUserId: number
  remark?: string
}

export interface PrdQuery {
  pageNum?: number; pageSize?: number
  prdNo?: string; projectId?: number; title?: string; version?: string; status?: string
}

export const listPrd  = (q?: PrdQuery) => request({ url: '/business/prd/list', method: 'get', params: q })
export const getPrd   = (id: number) => request({ url: `/business/prd/${id}`, method: 'get' })
export const addPrd   = (d: Prd) => request({ url: '/business/prd', method: 'post', data: d })
export const updatePrd= (d: Prd) => request({ url: '/business/prd', method: 'put', data: d })
export const delPrd   = (ids: number|number[]) => request({ url: `/business/prd/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiGeneratePrd = (id: number) => request({ url: `/business/prd/ai/generate/${id}`, method: 'post' })
export const exportPrd    = (q?: PrdQuery) => request({ url: '/business/prd/export', method: 'post', params: q, responseType: 'blob' })
