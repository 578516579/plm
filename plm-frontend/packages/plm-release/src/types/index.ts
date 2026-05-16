import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface ReleaseForm extends BaseEntity {
  releaseId?: number | string
  releaseNo?: string
  projectId?: number | string
  title?: string
  status?: string
}

export interface ReleaseQuery extends PageQuery {
  releaseNo?: string
  projectId?: number | string
  title?: string
  status?: string
}
