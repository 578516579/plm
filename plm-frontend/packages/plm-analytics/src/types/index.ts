export interface AnalyticsForm {
  analyticsId?: number | string
  analyticsNo?: string
  projectId?: number | string
  title: string
  period?: string
  periodValue?: string
  requirementThroughput?: number | string
  iterationOnTimeRate?: number | string
  defectDensity?: number | string
  aiTimeSaved?: number | string
  projectHealthScore?: number | string
  aiSuggestions?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
}

export interface AnalyticsQuery {
  title?: string
  period?: string
  periodValue?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
