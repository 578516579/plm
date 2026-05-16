import request from '@/utils/request'
import type { RequirementQuery, RequirementForm } from '../types'

/** 查询需求列表 */
export function listRequirement(query: RequirementQuery) {
  return request({
    url: '/business/requirement/list',
    method: 'get',
    params: query
  })
}

/** 查询需求详细 */
export function getRequirement(requirementId: number | string) {
  return request({
    url: '/business/requirement/' + requirementId,
    method: 'get'
  })
}

/** 新增需求 */
export function addRequirement(data: RequirementForm) {
  return request({
    url: '/business/requirement',
    method: 'post',
    data
  })
}

/** 修改需求 */
export function updateRequirement(data: RequirementForm) {
  return request({
    url: '/business/requirement',
    method: 'put',
    data
  })
}

/** 删除需求 */
export function delRequirement(ids: (number | string)[]) {
  return request({
    url: '/business/requirement/' + ids.join(','),
    method: 'delete'
  })
}
