export interface ArchForm {
  archId?: number | string
  archNo?: string
  projectId?: number | string
  prdId?: number | string
  title: string
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

export interface ArchQuery {
  title?: string
  archMode?: string
  primaryStack?: string
  deploymentType?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
