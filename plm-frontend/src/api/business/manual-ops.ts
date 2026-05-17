/** 运维手册 API — PRD §F5.3 */
import request from '@/utils/request'

export interface ManualOps {
  manualopsId?: number
  manualopsNo?: string
  projectId: number
  title: string
  monitoringPlan?: string             // prometheus_grafana/aliyun_cms/zabbix
  alertChannels?: string              // CSV: dingtalk,feishu,wework,email
  iotDeviceTypes?: string             // CSV: soil_sensor,weather_station,drone,irrigation_controller
  content?: string
  outputFormats?: string
  aiGenerated?: 'Y' | 'N'
  generatedAt?: string
  status?: '00' | '01' | '02' | '03'
  authorUserId: number
  remark?: string
}

export interface ManualOpsQuery {
  pageNum?: number; pageSize?: number
  manualopsNo?: string; projectId?: number; title?: string
  monitoringPlan?: string; status?: string
}

export const listManualOps  = (q?: ManualOpsQuery) => request({ url: '/business/manual-ops/list', method: 'get', params: q })
export const getManualOps   = (id: number) => request({ url: `/business/manual-ops/${id}`, method: 'get' })
export const addManualOps   = (d: ManualOps) => request({ url: '/business/manual-ops', method: 'post', data: d })
export const updateManualOps= (d: ManualOps) => request({ url: '/business/manual-ops', method: 'put', data: d })
export const delManualOps   = (ids: number|number[]) => request({ url: `/business/manual-ops/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiGenerateManualOps = (id: number) => request({ url: `/business/manual-ops/ai/generate/${id}`, method: 'post' })
export const exportManualOps     = (q?: ManualOpsQuery) => request({ url: '/business/manual-ops/export', method: 'post', params: q, responseType: 'blob' })
