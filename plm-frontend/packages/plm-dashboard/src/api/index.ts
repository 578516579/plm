import request from '@/utils/request'
import type { DashboardQuery, DashboardForm } from '../types'

export function listDashboard(query: DashboardQuery) {
  return request({ url: '/business/dashboard/list', method: 'get', params: query })
}

export function getDashboard(id: number | string) {
  return request({ url: '/business/dashboard/' + id, method: 'get' })
}

export function addDashboard(data: DashboardForm) {
  return request({ url: '/business/dashboard', method: 'post', data })
}

export function updateDashboard(data: DashboardForm) {
  return request({ url: '/business/dashboard', method: 'put', data })
}

export function delDashboard(ids: (number | string)[]) {
  return request({ url: '/business/dashboard/' + ids.join(','), method: 'delete' })
}

export function exportDashboard(query: DashboardQuery) {
  return request({ url: '/business/dashboard/export', method: 'post', params: query, responseType: 'blob' })
}
