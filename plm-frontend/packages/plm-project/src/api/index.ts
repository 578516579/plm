import request from '@/utils/request'
import type { ProjectQuery, ProjectForm } from '@/types/api/business/project'

/** 查询项目列表 */
export function listProject(query: ProjectQuery) {
  return request({
    url: '/business/project/list',
    method: 'get',
    params: query
  })
}

/** 查询项目详细 */
export function getProject(id: number | string) {
  return request({
    url: '/business/project/' + id,
    method: 'get'
  })
}

/** 新增项目 */
export function addProject(data: ProjectForm) {
  return request({
    url: '/business/project',
    method: 'post',
    data
  })
}

/** 修改项目 */
export function updateProject(data: ProjectForm) {
  return request({
    url: '/business/project',
    method: 'put',
    data
  })
}

/** 删除项目 */
export function delProject(ids: (number | string)[]) {
  return request({
    url: '/business/project/' + ids.join(','),
    method: 'delete'
  })
}
