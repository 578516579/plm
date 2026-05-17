/**
 * 项目管理 API — PRD §F1.2 + 原型 projects.html
 * 5 态状态机: 0 未启动 → 1 进行中 ↔ 2 暂停 → 3 已完成 / 4 已取消
 */
import request from '@/utils/request'

export interface Project {
  id?: number
  projectNo?: string
  projectName: string
  projectType?: string
  managerUserId?: number
  startDate?: string
  endDate?: string
  budget?: number
  description?: string
  status?: string
}

export interface ProjectQuery {
  pageNum?: number
  pageSize?: number
  projectName?: string
  projectNo?: string
  projectType?: string
  status?: string
}

export const listProject = (q: ProjectQuery): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: q })

export const getProject = (id: number): Promise<any> =>
  request({ url: `/business/project/${id}`, method: 'get' })

export const addProject = (data: Project): Promise<any> =>
  request({ url: '/business/project', method: 'post', data })

export const updateProject = (data: Project): Promise<any> =>
  request({ url: '/business/project', method: 'put', data })

export const delProject = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/project/${idStr}`, method: 'delete' })
}
