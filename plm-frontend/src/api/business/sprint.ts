/**
 * 迭代 Sprint API — PRD §F3.4 + 原型 kanban.html (modal-sprint)
 * 4 态: 00 草稿 → 01 进行中 → 02 已完成 / 03 已取消
 */
import request from '@/utils/request'

export interface Sprint {
  sprintId?: number
  sprintNo?: string
  projectId: number
  name: string
  goal?: string
  status?: string
  plannedStartDate?: string
  plannedEndDate?: string
  actualStartDate?: string
  actualEndDate?: string
  durationDays?: number
}

export interface SprintQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  name?: string
  status?: string
}

export const listSprint = (q: SprintQuery): Promise<any> =>
  request({ url: '/business/sprint/list', method: 'get', params: q })

export const getSprint = (id: number): Promise<any> =>
  request({ url: `/business/sprint/${id}`, method: 'get' })

export const addSprint = (data: Sprint): Promise<any> =>
  request({ url: '/business/sprint', method: 'post', data })

export const updateSprint = (data: Sprint): Promise<any> =>
  request({ url: '/business/sprint', method: 'put', data })

export const delSprint = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/sprint/${idStr}`, method: 'delete' })
}

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
