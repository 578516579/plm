/**
 * 立项管理 API (Inception) — PRD §F1.1 + 原型 inception.html
 * REST 路径: /business/inception
 * AI 端点: POST /business/inception/ai/generate/{id}
 *
 * 这是 PLM 业务模块的 API 模板,其余 30 个模块按这个模式批量生成。
 */
import request from '@/utils/request'

export interface Inception {
  inceptionId?: number
  inceptionNo?: string
  projectName: string
  businessLine?: string             // 字典 biz_inception_business_line
  inceptionType?: string            // 字典 biz_inception_type
  background?: string
  estimatedDurationMonths?: number
  estimatedTeam?: string
  aiGenerated?: 'Y' | 'N'
  aiProposalContent?: string
  aiRisks?: string
  aiGeneratedAt?: string
  status?: '00' | '01' | '02' | '03' | '04'  // 字典 biz_inception_status
  rejectReason?: string
  submitterUserId: number
  approverUserId?: number
  approvedAt?: string
  projectId?: number
  remark?: string
}

export interface InceptionQuery {
  pageNum?: number
  pageSize?: number
  inceptionNo?: string
  projectName?: string
  businessLine?: string
  inceptionType?: string
  status?: string
}

/** 列表 — GET /business/inception/list */
export function listInception(query?: InceptionQuery) {
  return request({ url: '/business/inception/list', method: 'get', params: query })
}

/** 详情 — GET /business/inception/{id} */
export function getInception(id: number) {
  return request({ url: `/business/inception/${id}`, method: 'get' })
}

/** 新增 — POST /business/inception */
export function addInception(data: Inception) {
  return request({ url: '/business/inception', method: 'post', data })
}

/** 修改 — PUT /business/inception */
export function updateInception(data: Inception) {
  return request({ url: '/business/inception', method: 'put', data })
}

/** 删除 — DELETE /business/inception/{ids} */
export function delInception(ids: number | number[]) {
  return request({
    url: `/business/inception/${Array.isArray(ids) ? ids.join(',') : ids}`,
    method: 'delete'
  })
}

/** AI 立项助手 — POST /business/inception/ai/generate/{id} */
export function aiGenerateInception(id: number) {
  return request({ url: `/business/inception/ai/generate/${id}`, method: 'post' })
}

/** 导出 — POST /business/inception/export */
export function exportInception(query?: InceptionQuery) {
  return request({
    url: '/business/inception/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
