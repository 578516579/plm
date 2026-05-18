export interface OpenspecForm {
  openspecId?: number | string
  openspecNo?: string
  projectId?: number | string
  specName: string
  specType?: string
  version?: string
  content?: string
  aiEnhanced?: string
  agrikbRef?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
}

export interface OpenspecQuery {
  specName?: string
  specType?: string
  aiEnhanced?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
