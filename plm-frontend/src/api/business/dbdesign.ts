/**
 * 数据库设计 API — PRD §F3.2 + 原型 dbdesign.html
 */
import request from '@/utils/request'

export interface DbDesign {
  dbdesignId?: number
  dbdesignNo?: string
  projectId: number
  archId?: number
  title: string
  dbEngine?: string  // mysql / postgres / kingbase
  domainEntities?: string
  erDiagram?: string  // Mermaid erDiagram
  dataDictionary?: string  // JSON / Markdown
  ddlScript?: string
  normalForm?: string  // 1nf / 2nf / 3nf / bcnf
  indexSuggestions?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number
}

export interface DbDesignQuery {
  pageNum?: number; pageSize?: number; projectId?: number; title?: string; status?: string
}

export const listDbDesign = (q: DbDesignQuery): Promise<any> =>
  request({ url: '/business/dbdesign/list', method: 'get', params: q })
export const getDbDesign = (id: number): Promise<any> =>
  request({ url: `/business/dbdesign/${id}`, method: 'get' })
export const addDbDesign = (d: DbDesign): Promise<any> =>
  request({ url: '/business/dbdesign', method: 'post', data: d })
export const updateDbDesign = (d: DbDesign): Promise<any> =>
  request({ url: '/business/dbdesign', method: 'put', data: d })
export const delDbDesign = (ids: number | number[]): Promise<any> =>
  request({ url: `/business/dbdesign/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGenerateDbDesign = (id: number): Promise<any> =>
  request({ url: `/business/dbdesign/ai/generate/${id}`, method: 'post' })
export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
