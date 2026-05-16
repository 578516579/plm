import request from '@/utils/request'
import type { DocumentQuery, DocumentForm } from '../types'

export function listDocument(query: DocumentQuery) {
  return request({ url: '/business/document/list', method: 'get', params: query })
}
export function getDocument(id: number | string) {
  return request({ url: '/business/document/' + id, method: 'get' })
}
export function addDocument(data: DocumentForm) {
  return request({ url: '/business/document', method: 'post', data })
}
export function updateDocument(data: DocumentForm) {
  return request({ url: '/business/document', method: 'put', data })
}
export function delDocument(ids: (number | string)[]) {
  return request({ url: '/business/document/' + ids.join(','), method: 'delete' })
}
