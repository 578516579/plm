export interface PrdForm {
  prdId?: number | string
  prdNo?: string
  projectId?: number | string
  title: string
  description?: string
  sceneTemplate?: string
  targetUser?: string
  content?: string
  completenessScore?: number
  version?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
}

export interface PrdQuery {
  title?: string
  sceneTemplate?: string
  targetUser?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
