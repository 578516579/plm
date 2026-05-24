import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface DocumentForm extends BaseEntity {
  documentId?: number | string
  documentNo?: string
  projectId?: number | string
  relatedEntityType?: string
  relatedEntityId?: number | string
  docType?: string
  title?: string
  content?: string
  version?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
  tags?: string
}

export interface DocumentQuery extends PageQuery {
  documentNo?: string
  projectId?: number | string
  relatedEntityType?: string
  relatedEntityId?: number | string
  docType?: string
  title?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
  tags?: string
}
