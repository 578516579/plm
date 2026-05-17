/** 竞品情报 API — PRD §F1.3 + 原型 competitive.html (3 视图: 矩阵/监控/SWOT) */
import request from '@/utils/request'

export interface CompetitiveDimension { name: string; order: number }
export interface CompetitiveVendor { name: string; isOurProduct: boolean }
export interface CompetitiveMatrix {
  dimensions: CompetitiveDimension[]
  vendors: CompetitiveVendor[]
  scores: number[][]            // 二维数组,每行=dimension,每列=vendor,值 0/0.5/1
}
export interface CompetitiveMonitor {
  vendor: string
  news: string
  threatLevel: 'low' | 'mid' | 'high'
  date: string
}
export interface CompetitiveOurSwot {
  strengths: string[]
  weaknesses: string[]
  opportunities: string[]
  threats: string[]
}

export interface Competitive {
  competitiveId?: number
  competitiveNo?: string
  projectId: number
  competitorName?: string
  vendor?: string
  // 原有 per-competitor SWOT (单条竞品视角)
  strengths?: string
  weaknesses?: string
  opportunities?: string
  threats?: string
  aiAnalysisReport?: string
  // 项目级 3 视图 JSON (drift 修复后,跟原型 renderCompetitive 1:1)
  matrixJson?: string           // JSON → CompetitiveMatrix
  monitorsJson?: string         // JSON → CompetitiveMonitor[]
  ourSwotJson?: string          // JSON → CompetitiveOurSwot
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02'
  authorUserId: number
  remark?: string
}

export interface CompetitiveQuery {
  pageNum?: number; pageSize?: number
  competitiveNo?: string; projectId?: number; competitorName?: string; status?: string
}

export const listCompetitive = (q?: CompetitiveQuery) =>
  request({ url: '/business/competitive/list', method: 'get', params: q })
export const getCompetitive = (id: number) => request({ url: `/business/competitive/${id}`, method: 'get' })
export const addCompetitive = (d: Competitive) => request({ url: '/business/competitive', method: 'post', data: d })
export const updateCompetitive = (d: Competitive) =>
  request({ url: '/business/competitive', method: 'put', data: d })
export const delCompetitive = (ids: number | number[]) =>
  request({ url: `/business/competitive/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiAnalyzeCompetitive = (id: number) =>
  request({ url: `/business/competitive/ai/analyze/${id}`, method: 'post' })

export function parseMatrix(c: Competitive): CompetitiveMatrix | null {
  try { return c.matrixJson ? JSON.parse(c.matrixJson) : null } catch { return null }
}
export function parseMonitors(c: Competitive): CompetitiveMonitor[] {
  try { return c.monitorsJson ? JSON.parse(c.monitorsJson) : [] } catch { return [] }
}
export function parseOurSwot(c: Competitive): CompetitiveOurSwot | null {
  try { return c.ourSwotJson ? JSON.parse(c.ourSwotJson) : null } catch { return null }
}
