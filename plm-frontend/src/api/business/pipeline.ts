/** CI/CD 流水线 API — DevOps 扩展 */
import request from '@/utils/request'

export interface Pipeline {
  pipelineId?: number
  pipelineNo?: string
  projectId?: number
  pipelineName: string
  repoName: string
  repoBranch?: string
  cicdTool?: string                   // jenkins/gitlab/github/gitea
  triggerType?: string                // manual/push/cron/tag
  cronExpr?: string
  yamlContent?: string
  lastRunStatus?: string              // success/failed/running/skipped
  lastRunAt?: string
  totalRuns?: number
  successCount?: number
  successRate?: number
  status?: '00' | '01'
  authorUserId: number
  remark?: string
}

export interface PipelineQuery {
  pageNum?: number; pageSize?: number
  pipelineNo?: string; projectId?: number; pipelineName?: string
  repoName?: string; cicdTool?: string; status?: string
}

export const listPipeline  = (q?: PipelineQuery) => request({ url: '/business/pipeline/list', method: 'get', params: q })
export const getPipeline   = (id: number) => request({ url: `/business/pipeline/${id}`, method: 'get' })
export const addPipeline   = (d: Pipeline) => request({ url: '/business/pipeline', method: 'post', data: d })
export const updatePipeline= (d: Pipeline) => request({ url: '/business/pipeline', method: 'put', data: d })
export const delPipeline   = (ids: number|number[]) => request({ url: `/business/pipeline/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const triggerPipeline = (id: number) => request({ url: `/business/pipeline/trigger/${id}`, method: 'post' })
export const exportPipeline  = (q?: PipelineQuery) => request({ url: '/business/pipeline/export', method: 'post', params: q, responseType: 'blob' })
