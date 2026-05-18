export interface AutotestForm {
  autotestId?: number | string
  autotestNo?: string
  projectId?: number | string
  suiteName: string
  description?: string
  framework?: string
  targetModule?: string
  scriptContent?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  lastRunAt?: string
  lastRunPassRate?: number | string
  lastRunDuration?: string
  failedCaseCount?: number
  status?: string
  authorUserId?: number | string
}

export interface AutotestQuery {
  suiteName?: string
  framework?: string
  targetModule?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
