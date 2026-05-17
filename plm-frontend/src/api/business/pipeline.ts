/**
 * CI/CD Pipeline API — 原型 pipeline.html
 */
import request from '@/utils/request'

export interface Pipeline {
  pipelineId?: number
  pipelineNo?: string
  projectId: number
  pipelineName: string
  repoUrl?: string
  triggerType?: string  // push / pr / cron / manual / tag
  cronExpr?: string
  ciTool?: string  // jenkins / gitlab_ci / github_actions / drone
  stagesConfig?: string  // JSON
  lastRunStatus?: string  // success / running / failed / never
  lastRunAt?: string
  totalRuns?: number
  successRuns?: number
  successRate?: number
  status?: string
  authorUserId?: number
}

export interface PipelineQuery { pageNum?: number; pageSize?: number; projectId?: number; pipelineName?: string; status?: string }

export const listPipeline = (q: PipelineQuery): Promise<any> => request({ url: '/business/pipeline/list', method: 'get', params: q })
export const getPipeline = (id: number): Promise<any> => request({ url: `/business/pipeline/${id}`, method: 'get' })
export const addPipeline = (d: Pipeline): Promise<any> => request({ url: '/business/pipeline', method: 'post', data: d })
export const updatePipeline = (d: Pipeline): Promise<any> => request({ url: '/business/pipeline', method: 'put', data: d })
export const delPipeline = (ids: number | number[]): Promise<any> => request({ url: `/business/pipeline/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const triggerPipeline = (id: number): Promise<any> => request({ url: `/business/pipeline/trigger/${id}`, method: 'post' })
export const listProjectsForSelect = (): Promise<any> => request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
