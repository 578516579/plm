/**
 * 文档中心 API — PRD §F5.5 + 原型 (合并 5 stub 文档管理)
 * 12 doc_type: prd / hld / lld / db / api / req / arch / test / manual / changelog / other
 */
import request from '@/utils/request'

export interface Document {
  documentId?: number
  documentNo?: string
  projectId: number
  relatedEntityType?: string
  relatedEntityId?: number
  docType?: string
  title: string
  content?: string
  version?: string
  status?: string
  authorUserId?: number
  reviewerUserId?: number
  tags?: string
}

export interface DocumentQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  docType?: string
  status?: string
  title?: string
}

export const listDocument = (q: DocumentQuery): Promise<any> =>
  request({ url: '/business/document/list', method: 'get', params: q })

export const getDocument = (id: number): Promise<any> =>
  request({ url: `/business/document/${id}`, method: 'get' })

export const addDocument = (data: Document): Promise<any> =>
  request({ url: '/business/document', method: 'post', data })

export const updateDocument = (data: Document): Promise<any> =>
  request({ url: '/business/document', method: 'put', data })

export const delDocument = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/document/${idStr}`, method: 'delete' })
}

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
