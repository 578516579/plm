import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 提测表单/列表行（PRD F4.4 + 5×5 状态机） */
export interface SubmissionForm extends BaseEntity {
  submissionId?: number | string
  submissionNo?: string
  projectId?: number | string
  sprintId?: number | string
  title?: string
  scope?: string
  environment?: string
  expectedTestDays?: number | string
  riskNotes?: string
  // AI 质量门禁 4 项
  unitTestCoverage?: number | string
  codeScanPassed?: string
  prdCompleted?: string
  apiDocUpdated?: string
  qualityGatePassed?: string
  status?: string
  rejectReason?: string
  submitterUserId?: number | string
  reviewerUserId?: number | string
  submittedAt?: string
  approvedAt?: string
}

/** 提测查询条件 */
export interface SubmissionQuery extends PageQuery {
  submissionNo?: string
  projectId?: number | string
  sprintId?: number | string
  title?: string
  environment?: string
  status?: string
  qualityGatePassed?: string
  submitterUserId?: number | string
}
