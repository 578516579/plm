export interface PipelineForm {
  pipelineId?: number | string
  pipelineNo?: string
  projectId?: number | string
  pipelineName: string
  repository?: string
  branch?: string
  triggerType?: string
  stages?: string
  lastRunStatus?: string
  lastRunAt?: string
  lastRunDuration?: string
  successCount?: number
  failedCount?: number
  successRate?: number | string
  status?: string
  authorUserId?: number | string
}

export interface PipelineQuery {
  pipelineName?: string
  repository?: string
  triggerType?: string
  lastRunStatus?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
