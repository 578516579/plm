import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface PrdForm extends BaseEntity {
  prdId?: number | string
  prdNo?: string
  projectId?: number | string
  title?: string
  description?: string
  sceneTemplate?: string
  targetUser?: string
  content?: string
  completenessScore?: number | string
  version?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
}

export interface PrdQuery extends PageQuery {
  prdNo?: string
  projectId?: number | string
  title?: string
  sceneTemplate?: string
  targetUser?: string
  version?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number | string
}
