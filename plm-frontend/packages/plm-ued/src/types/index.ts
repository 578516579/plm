export interface UedForm {
  uedId?: number | string
  uedNo?: string
  projectId?: number | string
  requirementId?: number | string
  title: string
  figmaUrl?: string
  figmaFileKey?: string
  versionLabel?: string
  previewUrl?: string
  annotationContent?: string
  aiReviewReport?: string
  aiReviewScore?: number
  complianceCheck?: string
  usabilityIssues?: string
  agriComponentTags?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  designerUserId?: number | string
  reviewerUserId?: number | string
}

export interface UedQuery {
  title?: string
  versionLabel?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
