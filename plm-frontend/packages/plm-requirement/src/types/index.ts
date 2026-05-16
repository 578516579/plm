import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 需求表单/列表行类型 */
export interface RequirementForm extends BaseEntity {
  requirementId?: number | string
  requirementNo?: string
  projectId?: number | string
  title?: string
  description?: string
  source?: string
  priority?: string
  status?: string
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
  assigneeUserId?: number | string
}
