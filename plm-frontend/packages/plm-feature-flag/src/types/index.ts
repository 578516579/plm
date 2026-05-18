export interface FeatureFlagForm {
  flagId?: number | string
  flagKey: string
  flagName: string
  description?: string
  environment?: string
  rolloutStrategy?: string
  rolloutPercentage?: number
  userWhitelist?: string
  enabled?: string
  projectId?: number | string
  authorUserId?: number | string
}

export interface FeatureFlagQuery {
  flagKey?: string
  flagName?: string
  environment?: string
  rolloutStrategy?: string
  enabled?: string
  pageNum?: number
  pageSize?: number
}
