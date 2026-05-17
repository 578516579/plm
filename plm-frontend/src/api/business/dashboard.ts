/**
 * 工作台 API — UI §4.2 + 原型 dashboard.html
 * 聚合调用多模块 API 拼装首页数据
 */
import request from '@/utils/request'

// 聚合接口 (后续可改为后端 /business/dashboard/aggregate 单接口)
export const fetchProjects = (params: any = {}) =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 20, ...params } })

export const fetchMyTasks = (params: any = {}) =>
  request({ url: '/business/task/list', method: 'get', params: { pageSize: 10, ...params } })

export const fetchDefects = (params: any = {}) =>
  request({ url: '/business/defect/list', method: 'get', params: { pageSize: 1, ...params } })

export const fetchPrds = (params: any = {}) =>
  request({ url: '/business/prd/list', method: 'get', params: { pageSize: 1, aiGenerated: 'Y', ...params } })

export const fetchInceptions = (params: any = {}) =>
  request({ url: '/business/inception/list', method: 'get', params: { pageSize: 1, aiGenerated: 'Y', ...params } })

export const fetchAutotests = (params: any = {}) =>
  request({ url: '/business/autotest/list', method: 'get', params: { pageSize: 100, ...params } })

export const fetchReleases = (params: any = {}) =>
  request({ url: '/business/release/list', method: 'get', params: { pageSize: 100, ...params } })

export const fetchTestReports = (params: any = {}) =>
  request({ url: '/business/testreport/list', method: 'get', params: { pageSize: 1, ...params } })

export const fetchManualProducts = (params: any = {}) =>
  request({ url: '/business/manual-product/list', method: 'get', params: { pageSize: 1, aiGenerated: 'Y', ...params } })
