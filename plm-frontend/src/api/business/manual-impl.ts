/** 实施手册 API — PRD §F5.2 */
import request from '@/utils/request'

export interface ManualImpl {
  manualimplId?: number
  manualimplNo?: string
  projectId: number
  title: string
  deployMode?: string                 // docker_compose/kubernetes/baremetal
  osType?: string                     // centos7/ubuntu20/kylin
  dbType?: string                     // postgresql14/mysql8/kdb
  envConfig?: string
  content?: string
  outputFormats?: string
  aiGenerated?: 'Y' | 'N'
  generatedAt?: string
  status?: '00' | '01' | '02' | '03'
  authorUserId: number
  remark?: string
}

export interface ManualImplQuery {
  pageNum?: number; pageSize?: number
  manualimplNo?: string; projectId?: number; title?: string
  deployMode?: string; osType?: string; dbType?: string; status?: string
}

export const listManualImpl  = (q?: ManualImplQuery) => request({ url: '/business/manual-impl/list', method: 'get', params: q })
export const getManualImpl   = (id: number) => request({ url: `/business/manual-impl/${id}`, method: 'get' })
export const addManualImpl   = (d: ManualImpl) => request({ url: '/business/manual-impl', method: 'post', data: d })
export const updateManualImpl= (d: ManualImpl) => request({ url: '/business/manual-impl', method: 'put', data: d })
export const delManualImpl   = (ids: number|number[]) => request({ url: `/business/manual-impl/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiGenerateManualImpl = (id: number) => request({ url: `/business/manual-impl/ai/generate/${id}`, method: 'post' })
export const exportManualImpl     = (q?: ManualImplQuery) => request({ url: '/business/manual-impl/export', method: 'post', params: q, responseType: 'blob' })
