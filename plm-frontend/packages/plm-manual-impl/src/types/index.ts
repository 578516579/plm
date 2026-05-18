export interface ManualImplForm {
  manualImplId?: number | string
  manualImplNo?: string
  projectId?: number | string
  title: string
  deploymentMode?: string
  os?: string
  database?: string
  envVars?: string
  content?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
}

export interface ManualImplQuery {
  title?: string
  deploymentMode?: string
  os?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
