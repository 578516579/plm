import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 发布表单/列表行（5 态机 + DORA 4 指标 + AI 评审） */
export interface ReleaseForm extends BaseEntity {
  releaseId?: number | string
  releaseNo?: string
  version?: string
  projectId?: number | string
  sprintId?: number | string
  strategy?: string
  environment?: string
  releaseNotes?: string
  plannedAt?: string
  releasedAt?: string
  rollbackAt?: string
  rollbackReason?: string
  status?: string
  aiReviewScore?: number | string
  aiReviewNotes?: string
  // DORA 4 指标
  deploymentFrequency?: number | string
  leadTimeHours?: number | string
  mttrMinutes?: number | string
  changeFailureRate?: number | string
  releasedByUserId?: number | string
}

/** 发布查询条件 */
export interface ReleaseQuery extends PageQuery {
  releaseNo?: string
  version?: string
  projectId?: number | string
  strategy?: string
  environment?: string
  status?: string
}
