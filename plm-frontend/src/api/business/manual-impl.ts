/**
 * 实施手册 API — PRD §F5.2 + 原型 implmanual.html
 */
import request from '@/utils/request'

export interface ManualImpl {
  manualImplId?: number
  manualImplNo?: string
  projectId: number
  releaseId?: number
  title: string
  deployMode?: string  // docker_compose / k8s / baremetal
  osType?: string  // centos / ubuntu / kylin
  databaseType?: string  // postgres / mysql / kingbase
  envVars?: string  // JSON
  installSteps?: string  // Markdown
  initConfigSteps?: string
  upgradeSteps?: string
  rollbackSteps?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number
}

export interface ManualImplQuery { pageNum?: number; pageSize?: number; projectId?: number; title?: string; status?: string }

export const listManualImpl = (q: ManualImplQuery): Promise<any> => request({ url: '/business/manual-impl/list', method: 'get', params: q })
export const getManualImpl = (id: number): Promise<any> => request({ url: `/business/manual-impl/${id}`, method: 'get' })
export const addManualImpl = (d: ManualImpl): Promise<any> => request({ url: '/business/manual-impl', method: 'post', data: d })
export const updateManualImpl = (d: ManualImpl): Promise<any> => request({ url: '/business/manual-impl', method: 'put', data: d })
export const delManualImpl = (ids: number | number[]): Promise<any> => request({ url: `/business/manual-impl/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGenerateManualImpl = (id: number): Promise<any> => request({ url: `/business/manual-impl/ai/generate/${id}`, method: 'post' })
export const listProjectsForSelect = (): Promise<any> => request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
