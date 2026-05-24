import request from '@/utils/request'
import type { CompetitiveQuery, CompetitiveForm } from '../types'

export function listCompetitive(query: CompetitiveQuery) {
  return request({ url: '/business/competitive/list', method: 'get', params: query })
}
export function getCompetitive(id: number | string) {
  return request({ url: '/business/competitive/' + id, method: 'get' })
}
export function addCompetitive(data: CompetitiveForm) {
  return request({ url: '/business/competitive', method: 'post', data })
}
export function updateCompetitive(data: CompetitiveForm) {
  return request({ url: '/business/competitive', method: 'put', data })
}
export function delCompetitive(ids: (number | string)[]) {
  return request({ url: '/business/competitive/' + ids.join(','), method: 'delete' })
}
