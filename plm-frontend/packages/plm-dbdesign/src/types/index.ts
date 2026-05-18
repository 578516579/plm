import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface DbDesignForm extends BaseEntity {
  dbdesignId?: number | string
  dbdesignNo?: string
  projectId?: number | string
  archId?: number | string
  title?: string
  dbEngine?: string
  erDiagramContent?: string
  dataDictionary?: string
  ddlScript?: string
  normalizationCheck?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
}

export interface DbDesignQuery extends PageQuery {
  dbdesignNo?: string
  projectId?: number | string
  archId?: number | string
  title?: string
  dbEngine?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number | string
}
