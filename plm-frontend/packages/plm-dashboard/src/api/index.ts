import request from '@/utils/request'
import type { DashboardForm, DashboardQuery, DashboardAggregate } from '../types'

/** 工作台聚合查询(首屏 6 类 widget) — UI §4.2 */
export function aggregateDashboard(ownerUserId?: number | string) {
  return request<{ code: number; msg: string; data: DashboardAggregate }>({
    url: '/business/dashboard/aggregate',
    method: 'get',
    params: ownerUserId ? { ownerUserId } : {}
  })
}

/** 查询工作台预设列表 */
export function listDashboard(query: DashboardQuery) {
  return request({
    url: '/business/dashboard/list',
    method: 'get',
    params: query
  })
}

/** 查询工作台预设详细 */
export function getDashboard(id: number | string) {
  return request({
    url: '/business/dashboard/' + id,
    method: 'get'
  })
}

/** 新增工作台预设 */
export function addDashboard(data: DashboardForm) {
  return request({
    url: '/business/dashboard',
    method: 'post',
    data
  })
}

/** 修改工作台预设 */
export function updateDashboard(data: DashboardForm) {
  return request({
    url: '/business/dashboard',
    method: 'put',
    data
  })
}

/** 删除工作台预设 */
export function delDashboard(ids: (number | string)[]) {
  return request({
    url: '/business/dashboard/' + ids.join(','),
    method: 'delete'
  })
}
