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
  source?: string  // biz_req_source: 01 客户反馈 / 02 内部提案 / 03 运营数据 / 04 竞品分析
  priority?: string  // biz_req_priority: 00 P0紧急 / 01 P1重要 / 02 P2一般
  status?: string  // biz_req_status: 00 待评审 / 01 开发中 / 02 已完成 / 03 已取消
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

// ─────────────────────────────────────────────────────────────────────
// 需求评审管理 — PRD §F2.4 (2026-05-25 新增)
// 状态机 00→01 前置: 必须存在至少 1 条 review_result=00 通过的评审
// ─────────────────────────────────────────────────────────────────────

export interface RequirementReview {
  reviewId?: number
  requirementId?: number
  reviewerUserId?: number
  reviewResult: string  // 00=通过 01=打回
  reviewComment?: string
  reviewAt?: string
  createBy?: string
  createTime?: string
  remark?: string
}

/** 评审历史:列出某需求的全部评审记录(倒序) */
export const listRequirementReviews = (requirementId: number): Promise<any> =>
  request({ url: `/business/requirement/${requirementId}/reviews`, method: 'get' })

/** 单条评审详情 */
export const getRequirementReview = (reviewId: number): Promise<any> =>
  request({ url: `/business/requirement/review/${reviewId}`, method: 'get' })

/** 提交评审 — 状态机 00→01 的前置 */
export const submitRequirementReview = (requirementId: number, data: RequirementReview): Promise<any> =>
  request({ url: `/business/requirement/${requirementId}/review`, method: 'post', data })

/** 撤回评审(逻辑删除) */
export const deleteRequirementReviews = (reviewIds: number | number[]): Promise<any> => {
  const idStr = Array.isArray(reviewIds) ? reviewIds.join(',') : reviewIds
  return request({ url: `/business/requirement/review/${idStr}`, method: 'delete' })
}

// ─────────────────────────────────────────────────────────────────────
// 关联资源查询 — 需求→PRD / UED / 任务 的反向追溯 (2026-05-25 新增)
// ─────────────────────────────────────────────────────────────────────

/** 查关联的 PRD 列表(by requirementId) */
export const listPrdByRequirementId = (requirementId: number): Promise<any> =>
  request({ url: '/business/prd/list', method: 'get', params: { requirementId, pageSize: 100 } })

/** 查关联的 UED 列表(by requirementId) */
export const listUedByRequirementId = (requirementId: number): Promise<any> =>
  request({ url: '/business/ued/list', method: 'get', params: { requirementId, pageSize: 100 } })

/** 查关联的任务列表(by requirementId) — 复用 task 模块的 list */
export const listTasksByRequirementId = (requirementId: number): Promise<any> =>
  request({ url: '/business/task/list', method: 'get', params: { requirementId, pageSize: 100 } })
