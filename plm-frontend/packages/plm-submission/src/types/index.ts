import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface SubmissionForm extends BaseEntity {
  submissionId?: number | string
  submissionNo?: string
  projectId?: number | string
  title?: string
  status?: string
}

export interface SubmissionQuery extends PageQuery {
  submissionNo?: string
  projectId?: number | string
  title?: string
  status?: string
}
