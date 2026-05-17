/**
 * AI PRD 生成 API — PRD §F2.2 + 原型 prd.html
 */
import request from '@/utils/request'

export interface Prd {
  prdId?: number
  prdNo?: string
  projectId?: number
  title: string
  description?: string
  sceneTemplate?: string
  targetUser?: string
  version?: string
  content?: string
  completenessScore?: number
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number
  reviewerUserId?: number
}

export interface PrdQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  title?: string
  status?: string
}

export const listPrd = (q: PrdQuery): Promise<any> =>
  request({ url: '/business/prd/list', method: 'get', params: q })

export const getPrd = (id: number): Promise<any> =>
  request({ url: `/business/prd/${id}`, method: 'get' })

export const addPrd = (data: Prd): Promise<any> =>
  request({ url: '/business/prd', method: 'post', data })

export const updatePrd = (data: Prd): Promise<any> =>
  request({ url: '/business/prd', method: 'put', data })

export const delPrd = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/prd/${idStr}`, method: 'delete' })
}

// PRD §F2.2 AI 生成完整 PRD (prd-generation-flow)
export const aiGeneratePrd = (id: number): Promise<any> =>
  request({ url: `/business/prd/ai/generate/${id}`, method: 'post' })

// 复用 project 列表 (用于关联项目下拉)
export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
