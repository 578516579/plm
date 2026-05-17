/**
 * 产品手册 API — PRD §F5.1 + 原型 productmanual.html
 * 字段对齐后端 Domain (manualproductId 小写 + CSV includeModules + outputFormats)
 */
import request from '@/utils/request'

export interface ManualProduct {
  manualproductId?: number
  manualproductNo?: string
  projectId?: number
  title: string
  productVersion?: string
  includeModules?: string  // CSV: overview,quickstart,feature_detail,faq,video
  content?: string  // Markdown
  screenshotsUrls?: string  // CSV
  screenshotsCount?: number
  outputFormats?: string  // CSV: word,pdf,html,markdown
  aiGenerated?: string
  generatedAt?: string
  status?: string
  authorUserId?: number
}

export interface ManualProductQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  title?: string
  status?: string
}

export const listManualProduct = (q: ManualProductQuery): Promise<any> =>
  request({ url: '/business/manual-product/list', method: 'get', params: q })

export const getManualProduct = (id: number): Promise<any> =>
  request({ url: `/business/manual-product/${id}`, method: 'get' })

export const addManualProduct = (data: ManualProduct): Promise<any> =>
  request({ url: '/business/manual-product', method: 'post', data })

export const updateManualProduct = (data: ManualProduct): Promise<any> =>
  request({ url: '/business/manual-product', method: 'put', data })

export const delManualProduct = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/manual-product/${idStr}`, method: 'delete' })
}

// PRD §F5.1 AI 生成 — product-manual-flow
export const aiGenerateManualProduct = (id: number): Promise<any> =>
  request({ url: `/business/manual-product/ai/generate/${id}`, method: 'post' })

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
