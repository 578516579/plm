import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface ApiDocForm extends BaseEntity {
  apidocId?: number | string
  apidocNo?: string
  projectId?: number | string
  title?: string
  status?: string
}

export interface ApiDocQuery extends PageQuery {
  apidocNo?: string
  projectId?: number | string
  title?: string
  status?: string
}
