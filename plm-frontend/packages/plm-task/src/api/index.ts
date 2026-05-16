import request from '@/utils/request'
import type { TaskQuery, TaskForm } from '../types'

/** 查询任务列表 */
export function listTask(query: TaskQuery) {
  return request({
    url: '/business/task/list',
    method: 'get',
    params: query
  })
}

/** 查询任务详情 */
export function getTask(taskId: number | string) {
  return request({
    url: '/business/task/' + taskId,
    method: 'get'
  })
}

/** 新增任务 */
export function addTask(data: TaskForm) {
  return request({
    url: '/business/task',
    method: 'post',
    data
  })
}

/** 修改任务 */
export function updateTask(data: TaskForm) {
  return request({
    url: '/business/task',
    method: 'put',
    data
  })
}

/** 删除任务 */
export function delTask(ids: (number | string)[]) {
  return request({
    url: '/business/task/' + ids.join(','),
    method: 'delete'
  })
}

/** 我的任务 */
export function myTasks(query: TaskQuery) {
  return request({
    url: '/business/task/my',
    method: 'get',
    params: query
  })
}

/** 看板视图 */
export function kanbanTasks(projectId: number | string, sprintId?: number | string) {
  return request({
    url: '/business/task/kanban',
    method: 'get',
    params: { projectId, sprintId }
  })
}
