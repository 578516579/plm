/** 数据库设计 API — PRD §F3.2 */
import request from '@/utils/request'

export interface Dbdesign {
  dbdesignId?: number
  dbdesignNo?: string
  projectId: number
  title: string
  dbType?: string                   // mysql/postgresql/kdb/sqlite
  erContent?: string
  dictContent?: string
  ddlContent?: string
  reviewReport?: string
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02' | '03'
  authorUserId: number
  reviewerUserId?: number
  remark?: string
}

export interface DbdesignQuery {
  pageNum?: number; pageSize?: number
  dbdesignNo?: string; projectId?: number; title?: string; dbType?: string; status?: string
}

export const listDbdesign  = (q?: DbdesignQuery) => request({ url: '/business/dbdesign/list', method: 'get', params: q })
export const getDbdesign   = (id: number) => request({ url: `/business/dbdesign/${id}`, method: 'get' })
export const addDbdesign   = (d: Dbdesign) => request({ url: '/business/dbdesign', method: 'post', data: d })
export const updateDbdesign= (d: Dbdesign) => request({ url: '/business/dbdesign', method: 'put', data: d })
export const delDbdesign   = (ids: number|number[]) => request({ url: `/business/dbdesign/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiErDbdesign = (id: number) => request({ url: `/business/dbdesign/ai/er/${id}`, method: 'post' })
export const exportDbdesign   = (q?: DbdesignQuery) => request({ url: '/business/dbdesign/export', method: 'post', params: q, responseType: 'blob' })
