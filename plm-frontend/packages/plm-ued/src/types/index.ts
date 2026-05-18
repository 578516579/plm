import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface UedForm extends BaseEntity {
  uedId?: number | string
  uedNo?: string
  projectId?: number | string
  requirementId?: number | string
  title?: string
  figmaUrl?: string
  figmaFileKey?: string
  versionLabel?: string
  previewUrl?: string
  annotationContent?: string
  aiReviewReport?: string
  aiReviewScore?: number | string
  complianceCheck?: string
  usabilityIssues?: string
  agriComponentTags?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  designerUserId?: number | string
  reviewerUserId?: number | string
}

export interface UedQuery extends PageQuery {
  uedNo?: string
  projectId?: number | string
  requirementId?: number | string
  title?: string
  versionLabel?: string
  aiGenerated?: string
  status?: string
  designerUserId?: number | string
}
