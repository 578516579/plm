import request from '@/utils/request'
import type { FeatureFlagQuery, FeatureFlagForm } from '../types'

export function listFeatureFlag(query: FeatureFlagQuery) {
  return request({ url: '/business/feature-flag/list', method: 'get', params: query })
}

export function getFeatureFlag(id: number | string) {
  return request({ url: '/business/feature-flag/' + id, method: 'get' })
}

export function addFeatureFlag(data: FeatureFlagForm) {
  return request({ url: '/business/feature-flag', method: 'post', data })
}

export function updateFeatureFlag(data: FeatureFlagForm) {
  return request({ url: '/business/feature-flag', method: 'put', data })
}

export function delFeatureFlag(ids: (number | string)[]) {
  return request({ url: '/business/feature-flag/' + ids.join(','), method: 'delete' })
}

export function exportFeatureFlag(query: FeatureFlagQuery) {
  return request({ url: '/business/feature-flag/export', method: 'post', params: query, responseType: 'blob' })
}

export function toggleFeatureFlag(id: number | string) {
  return request({ url: `/business/feature-flag/toggle/${id}`, method: 'put' })
}
