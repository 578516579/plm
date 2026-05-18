export interface DoraForm {
  doraId?: number | string
  doraNo?: string
  projectId?: number | string
  period: string
  deployFrequency?: number | string
  leadTimeHours?: number | string
  changeFailureRate?: number | string
  mttrHours?: number | string
  doraLevel?: string
  aiSuggestions?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
}

export interface DoraQuery {
  period?: string
  doraLevel?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
