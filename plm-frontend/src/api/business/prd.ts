/**
 * AI PRD 生成 API — PRD §F2.2 + 原型 prd.html
 */
import request from '@/utils/request'

export interface Prd {
  prdId?: number
  prdNo?: string
  projectId?: number
  /** 反向关联需求 ID (FK→tb_requirement.requirement_id, 可空) — 2026-05-25 新增 */
  requirementId?: number
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
  requirementId?: number   // 2026-05-25 新增 — 按关联需求过滤
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

// 2026-05-25 新增 — 用于关联需求下拉(按项目过滤,简化关联场景)
export const listRequirementsForSelect = (projectId?: number): Promise<any> =>
  request({ url: '/business/requirement/list', method: 'get', params: { projectId, pageSize: 200 } })
