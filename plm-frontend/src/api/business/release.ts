/**
 * 发布管理 API — 原型 release.html + DORA 4 指标
 * 5 态状态机: 00 计划中 → 01 发布中 → 02 已发布 → 03 已回滚 / 04 已废弃
 */
import request from '@/utils/request'

export interface Release {
  releaseId?: number
  releaseNo?: string
  projectId?: number
  sprintId?: number
  version: string
  strategy?: string
  environment?: string
  releaseNotes?: string
  plannedAt?: string
  releasedAt?: string
  rollbackAt?: string
  rollbackReason?: string
  aiReviewScore?: number
  aiReviewNotes?: string
  deploymentFrequency?: number
  leadTimeHours?: number
  mttrMinutes?: number
  changeFailureRate?: number
  releasedByUserId?: number
  status?: string
}

export interface ReleaseQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  version?: string
  strategy?: string
  environment?: string
  status?: string
}

export const listRelease = (q: ReleaseQuery): Promise<any> =>
  request({ url: '/business/release/list', method: 'get', params: q })

export const getRelease = (id: number): Promise<any> =>
  request({ url: `/business/release/${id}`, method: 'get' })

export const addRelease = (data: Release): Promise<any> =>
  request({ url: '/business/release', method: 'post', data })

export const updateRelease = (data: Release): Promise<any> =>
  request({ url: '/business/release', method: 'put', data })

export const delRelease = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/release/${idStr}`, method: 'delete' })
}

// PRD §F5 — AI 发布评审。后端真实端点(plm-release P0-1b):
// 文本走 LLM(AiTexts),评分由 DORA 4 指标确定性计算,不让 LLM 幻觉数字
export const aiReviewRelease = (id: number): Promise<any> =>
  request({ url: `/business/release/ai/review/${id}`, method: 'post' })

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
