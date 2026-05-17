/**
 * 运维手册 API — PRD §F5.3 + 原型 opsmanual.html
 */
import request from '@/utils/request'

export interface ManualOps {
  manualOpsId?: number
  manualOpsNo?: string
  projectId: number
  title: string
  monitoringSolution?: string  // prometheus_grafana / aliyun_monitor / zabbix
  alertChannels?: string  // CSV: dingtalk,feishu,wecom,email
  iotDeviceTypes?: string  // CSV: soil_sensor,weather_station,drone,irrigation_ctrl
  monitorMetrics?: string
  alertRules?: string
  backupStrategy?: string
  troubleshootGuide?: string
  iotMaintenance?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number
}

export interface ManualOpsQuery { pageNum?: number; pageSize?: number; projectId?: number; title?: string; status?: string }

export const listManualOps = (q: ManualOpsQuery): Promise<any> => request({ url: '/business/manual-ops/list', method: 'get', params: q })
export const getManualOps = (id: number): Promise<any> => request({ url: `/business/manual-ops/${id}`, method: 'get' })
export const addManualOps = (d: ManualOps): Promise<any> => request({ url: '/business/manual-ops', method: 'post', data: d })
export const updateManualOps = (d: ManualOps): Promise<any> => request({ url: '/business/manual-ops', method: 'put', data: d })
export const delManualOps = (ids: number | number[]): Promise<any> => request({ url: `/business/manual-ops/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGenerateManualOps = (id: number): Promise<any> => request({ url: `/business/manual-ops/ai/generate/${id}`, method: 'post' })
export const listProjectsForSelect = (): Promise<any> => request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
