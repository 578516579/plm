import request from '@/utils/request'
import type { SprintQuery, SprintForm } from '@/types/api/business/sprint'

/** 查询迭代列表 */
export function listSprint(query: SprintQuery) {
  return request({
    url: '/business/sprint/list',
    method: 'get',
    params: query
  })
}

/** 查询迭代详情 */
export function getSprint(sprintId: number | string) {
  return request({
    url: '/business/sprint/' + sprintId,
    method: 'get'
  })
}

/** 新增迭代 */
export function addSprint(data: SprintForm) {
  return request({
    url: '/business/sprint',
    method: 'post',
    data
  })
}

/** 修改迭代 */
export function updateSprint(data: SprintForm) {
  return request({
    url: '/business/sprint',
    method: 'put',
    data
  })
}

/** 删除迭代 */
export function delSprint(ids: (number | string)[]) {
  return request({
    url: '/business/sprint/' + ids.join(','),
    method: 'delete'
  })
}

/** 当前活跃迭代 */
export function currentSprint(projectId: number | string) {
  return request({
    url: '/business/sprint/current',
    method: 'get',
    params: { projectId }
  })
}

/** 健康度统计 */
export function sprintStats(sprintId: number | string) {
  return request({
    url: '/business/sprint/' + sprintId + '/stats',
    method: 'get'
  })
}
