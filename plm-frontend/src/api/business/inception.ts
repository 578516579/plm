/**
 * 立项管理 API (Inception) — PRD §F1.1 + 原型 inception.html (含 JS runInceptionAI line 696-729)
 *
 * 字段已对齐原型 — AI 立项建议书拆 4 段输出 + 8 个 ROI 数值 + 风险 JSON 数组。
 */
import request from '@/utils/request'

/** AI 风险一项 (对应原型 incRisks innerHTML 的一条 <div>) */
export interface InceptionRisk {
  level: 'warning' | 'critical'
  title: string
  description: string
}

export interface Inception {
  inceptionId?: number
  inceptionNo?: string
  projectName: string
  businessLine?: string             // dict biz_inception_biz_line: plant_protection/precision_farming/agri_supply/traceability
  inceptionType?: string            // dict biz_inception_type: new_product/iteration/refactor/platform
  background?: string
  estimatedDurationMonths?: number
  estimatedTeam?: string

  // === AI 立项建议书 4 段 (原型 runInceptionAI 的 4 个 <h4>) ===
  aiBackground?: string              // 一、项目背景
  aiMarketOpportunity?: string       // 二、市场机会
  aiRoiEstimate?: string             // 三、ROI预估 (含具体数字的散文)
  aiRecommendDecision?: string       // 四、建议决策

  // === 8 个 ROI 结构化数值 (原型 ROI 段中硬编码的数) ===
  marketSize?: number                // 580 (亿元)
  digitalPenetration?: number        // 8 (%)
  devCostEstimate?: number           // 180 (万元)
  firstYearRevenue?: number          // 3000 (万元)
  roiMultiple?: number               // 16.7
  recommendedPriority?: 'P0' | 'P1' | 'P2'
  recommendedStartQuarter?: string   // Q3-2026
  deliveryPhases?: number            // 3

  // === 风险数组 JSON (原型 incRisks innerHTML 渲染) ===
  /** JSON 字符串 — 反序列化得 InceptionRisk[] */
  aiRisksJson?: string

  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02' | '03' | '04'
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

export function listInception(query?: InceptionQuery) {
  return request({ url: '/business/inception/list', method: 'get', params: query })
}
export function getInception(id: number) {
  return request({ url: `/business/inception/${id}`, method: 'get' })
}
export function addInception(data: Inception) {
  return request({ url: '/business/inception', method: 'post', data })
}
export function updateInception(data: Inception) {
  return request({ url: '/business/inception', method: 'put', data })
}
export function delInception(ids: number | number[]) {
  return request({
    url: `/business/inception/${Array.isArray(ids) ? ids.join(',') : ids}`,
    method: 'delete'
  })
}
export function aiGenerateInception(id: number) {
  return request({ url: `/business/inception/ai/generate/${id}`, method: 'post' })
}
export function exportInception(query?: InceptionQuery) {
  return request({
    url: '/business/inception/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

/** Helper: 反序列化 aiRisksJson 为数组 */
export function parseRisks(rec: Inception): InceptionRisk[] {
  if (!rec.aiRisksJson) return []
  try { return JSON.parse(rec.aiRisksJson) } catch { return [] }
}
