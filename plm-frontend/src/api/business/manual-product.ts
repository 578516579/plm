/**
 * 产品手册 API — PRD §F5.1 + 原型 productmanual.html
 */
import request from '@/utils/request'

export interface ManualProduct {
  manualProductId?: number
  manualProductNo?: string
  projectId: number
  releaseId?: number
  title: string
  productVersion?: string
  includeOverview?: string  // Y/N
  includeQuickStart?: string
  includeFeatureDetail?: string
  includeFaq?: string
  includeVideoTutorial?: string
  screenshotUrls?: string  // CSV
  generatedContent?: string  // Markdown
  exportFormats?: string  // CSV: pdf,docx,html
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number
}

export interface ManualProductQuery { pageNum?: number; pageSize?: number; projectId?: number; title?: string; status?: string }

export const listManualProduct = (q: ManualProductQuery): Promise<any> => request({ url: '/business/manual-product/list', method: 'get', params: q })
export const getManualProduct = (id: number): Promise<any> => request({ url: `/business/manual-product/${id}`, method: 'get' })
export const addManualProduct = (d: ManualProduct): Promise<any> => request({ url: '/business/manual-product', method: 'post', data: d })
export const updateManualProduct = (d: ManualProduct): Promise<any> => request({ url: '/business/manual-product', method: 'put', data: d })
export const delManualProduct = (ids: number | number[]): Promise<any> => request({ url: `/business/manual-product/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGenerateManualProduct = (id: number): Promise<any> => request({ url: `/business/manual-product/ai/generate/${id}`, method: 'post' })
export const listProjectsForSelect = (): Promise<any> => request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
