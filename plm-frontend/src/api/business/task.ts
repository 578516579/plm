/**
 * 任务管理 API — PRD §F3.4 + 原型 kanban.html
 * 5 态: 00 待开发 → 01 开发中 → 02 代码评审 → 03 测试中 → 04 已完成
 */
import request from '@/utils/request'

export interface Task {
  taskId?: number
  taskNo?: string
  projectId: number
  requirementId?: number
  sprintId?: number
  title: string
  description?: string
  status?: string
  priority?: string  // P0 / P1 / P2
  assigneeUserId?: number
  estimatedHours?: number
  actualHours?: number
  mrUrl?: string
  mrBranch?: string
}

export interface TaskQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  sprintId?: number
  requirementId?: number
  title?: string
  status?: string
  priority?: string
}

export const listTask = (q: TaskQuery): Promise<any> =>
  request({ url: '/business/task/list', method: 'get', params: q })

export const getTask = (id: number): Promise<any> =>
  request({ url: `/business/task/${id}`, method: 'get' })

export const addTask = (data: Task): Promise<any> =>
  request({ url: '/business/task', method: 'post', data })

export const updateTask = (data: Task): Promise<any> =>
  request({ url: '/business/task', method: 'put', data })

export const delTask = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/task/${idStr}`, method: 'delete' })
}

// AI 自动拆分任务 — PRD §F3.4 task-split-flow
export const aiSplitTasks = (params: { requirementId: number }): Promise<any> =>
  request({ url: `/business/task/ai/split`, method: 'post', data: params })

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })

export const listSprintsForSelect = (): Promise<any> =>
  request({ url: '/business/sprint/list', method: 'get', params: { pageSize: 200 } })

export const listRequirementsForSelect = (): Promise<any> =>
  request({ url: '/business/requirement/list', method: 'get', params: { pageSize: 200 } })
