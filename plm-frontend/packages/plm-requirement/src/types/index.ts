import type { BaseEntity, PageQuery } from '@/types/api/common'

/**
 * 需求表单/列表行类型
 * 字段对照表:PRD-MAPPING.md §2 "Requirement (F2.1)" (commit 1afe0ba)
 */
export interface RequirementForm extends BaseEntity {
  requirementId?: number | string
  requirementNo?: string
  projectId?: number | string
  title?: string
  description?: string
  source?: string
  priority?: string
  status?: string
  /** AI 价值评估:biz_req_ai_value — H 高 / M 中 / L 低 */
  aiValue?: string
  assigneeUserId?: number | string
  reviewNote?: string
}

/** 需求查询条件 */
export interface RequirementQuery extends PageQuery {
  requirementNo?: string
  projectId?: number | string
  title?: string
  source?: string
  priority?: string
  status?: string
  aiValue?: string
  assigneeUserId?: number | string
}
