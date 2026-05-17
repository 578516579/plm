/**
 * 需求管理 API — PRD §F2.1 + 原型 requirements.html
 * 4 态状态机: 00 待评审 → 01 开发中 → 02 已完成 → 03 已取消
 */
import request from '@/utils/request'

export interface Requirement {
  requirementId?: number
  requirementNo?: string
  projectId: number
  title: string
  description?: string
  source?: string  // customer / internal / competitive / data
  priority?: string  // P0 / P1 / P2
  status?: string  // 00 / 01 / 02 / 03
  aiEvaluation?: string  // high / medium / low
  assigneeUserId?: number
  reviewNote?: string
}

export interface RequirementQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  title?: string
  source?: string
  priority?: string
  status?: string
}

export const listRequirement = (q: RequirementQuery): Promise<any> =>
  request({ url: '/business/requirement/list', method: 'get', params: q })

export const getRequirement = (id: number): Promise<any> =>
  request({ url: `/business/requirement/${id}`, method: 'get' })

export const addRequirement = (data: Requirement): Promise<any> =>
  request({ url: '/business/requirement', method: 'post', data })

export const updateRequirement = (data: Requirement): Promise<any> =>
  request({ url: '/business/requirement', method: 'put', data })

export const delRequirement = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/requirement/${idStr}`, method: 'delete' })
}

// AI 评估优先级 — PRD §F2.1 req-priority-flow
export const aiEvaluateRequirement = (id: number): Promise<any> =>
  request({ url: `/business/requirement/ai/evaluate/${id}`, method: 'post' })

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
