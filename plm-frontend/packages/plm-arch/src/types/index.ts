import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface ArchForm extends BaseEntity {
  archId?: number | string
  archNo?: string
  projectId?: number | string
  prdId?: number | string
  title?: string
  archMode?: string
  primaryStack?: string
  databaseChoice?: string
  aiOrchestration?: string
  deploymentType?: string
  iotProtocol?: string
  designContent?: string
  c4DiagramContent?: string
  nfrMapping?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
}

export interface ArchQuery extends PageQuery {
  archNo?: string
  projectId?: number | string
  prdId?: number | string
  title?: string
  archMode?: string
  primaryStack?: string
  databaseChoice?: string
  deploymentType?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number | string
}
