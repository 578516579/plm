import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface CompetitiveForm extends BaseEntity {
  competitiveId?: number | string
  competitiveNo?: string
  projectId?: number | string
  competitorName?: string
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

export interface CompetitiveQuery extends PageQuery {
  competitiveNo?: string
  projectId?: number | string
  competitorName?: string
  vendor?: string
  pricingTier?: string
  aiGenerated?: string
  monitorEnabled?: string
  status?: string
  authorUserId?: number | string
}
