export interface CompetitiveForm {
  competitiveId?: number | string
  competitiveNo?: string
  projectId?: number | string
  competitorName: string
  vendor?: string
  website?: string
  pricingModel?: string
  pricingTier?: string
  featureMatrix?: string
  strengths?: string
  weaknesses?: string
  opportunities?: string
  threats?: string
  aiAnalysisReport?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  monitorEnabled?: string
  monitorKeywords?: string
  lastMonitoredAt?: string
  status?: string
  authorUserId?: number | string
}

export interface CompetitiveQuery {
  competitorName?: string
  vendor?: string
  pricingTier?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
