import request from '@/utils/request'
import type { DefectQuery, DefectForm } from '../types'

/** 查询缺陷列表 */
export function listDefect(query: DefectQuery) {
  return request({
    url: '/business/defect/list',
    method: 'get',
    params: query
  })
}

/** 查询缺陷详细 */
export function getDefect(defectId: number | string) {
  return request({
    url: '/business/defect/' + defectId,
    method: 'get'
  })
}

/** 新增缺陷 */
export function addDefect(data: DefectForm) {
  return request({
    url: '/business/defect',
    method: 'post',
    data
  })
}

/** 修改缺陷 */
export function updateDefect(data: DefectForm) {
  return request({
    url: '/business/defect',
    method: 'put',
    data
  })
}

/** 删除缺陷 */
export function delDefect(ids: (number | string)[]) {
  return request({
    url: '/business/defect/' + ids.join(','),
    method: 'delete'
  })
}
