/**
 * 缺陷管理 API — PRD §F4.6 + 原型 defects.html
 * 5 态: 00 新建 → 01 已确认 → 02 处理中 → 03 已解决 → 04 已关闭 (含反向边; 对齐 biz_defect_status 字典)
 */
import request from '@/utils/request'

export interface Defect {
  defectId?: number
  defectNo?: string
  projectId: number
  sprintId?: number
  taskId?: number
  title: string
  description?: string
  severity?: string  // P0 / P1 / P2 / P3
  category?: string  // functional / performance / ui / security
  status?: string
  assigneeUserId?: number
  reporterUserId?: number
  reproduceSteps?: string
  expectedResult?: string
  actualResult?: string
  resolution?: string
  tags?: string
}

export interface DefectQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  sprintId?: number
  severity?: string
  status?: string
  title?: string
}

export const listDefect = (q: DefectQuery): Promise<any> =>
  request({ url: '/business/defect/list', method: 'get', params: q })

export const getDefect = (id: number): Promise<any> =>
  request({ url: `/business/defect/${id}`, method: 'get' })

export const addDefect = (data: Defect): Promise<any> =>
  request({ url: '/business/defect', method: 'post', data })

export const updateDefect = (data: Defect): Promise<any> =>
  request({ url: '/business/defect', method: 'put', data })

export const delDefect = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/defect/${idStr}`, method: 'delete' })
}

// AI 相似缺陷检测 — PRD §F4.6 defect-match-flow
export const aiMatchDefect = (data: { title: string; description?: string }): Promise<any> =>
  request({ url: `/business/defect/ai/match`, method: 'post', data })

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
