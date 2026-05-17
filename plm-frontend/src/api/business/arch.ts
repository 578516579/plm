/** 架构设计 API — PRD §F3.1 + 原型 archdesign.html (genArchDesign 4 步 timeline + 4 NFR) */
import request from '@/utils/request'

export interface ArchTimelineStep {
  step: number
  name: string
  status: 'pass' | 'warning' | 'fail'
  description: string
}

export interface Arch {
  archId?: number
  archNo?: string
  projectId: number
  title: string
  archMode?: string                 // microservice/monolith/serverless/layered
  techStack?: string
  dbStack?: string
  aiOrchestration?: string
  deployMode?: string
  iotProtocol?: string
  archContent?: string
  c4Diagram?: string
  nfrContent?: string
  // 4 NFR 子项 + AI timeline (drift 修复后)
  nfrPerformance?: string
  nfrAvailability?: string
  nfrSecurity?: string
  nfrScalability?: string
  aiTimelineJson?: string           // JSON → ArchTimelineStep[]
  reviewReport?: string
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02' | '03'
  authorUserId: number
  reviewerUserId?: number
  remark?: string
}

export interface ArchQuery {
  pageNum?: number; pageSize?: number
  archNo?: string; projectId?: number; title?: string
  archMode?: string; status?: string
}

export const listArch = (q?: ArchQuery) => request({ url: '/business/arch/list', method: 'get', params: q })
export const getArch = (id: number) => request({ url: `/business/arch/${id}`, method: 'get' })
export const addArch = (d: Arch) => request({ url: '/business/arch', method: 'post', data: d })
export const updateArch = (d: Arch) => request({ url: '/business/arch', method: 'put', data: d })
export const delArch = (ids: number | number[]) =>
  request({ url: `/business/arch/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiRecommendArch = (id: number) => request({ url: `/business/arch/ai/recommend/${id}`, method: 'post' })

export function parseTimeline(a: Arch): ArchTimelineStep[] {
  try { return a.aiTimelineJson ? JSON.parse(a.aiTimelineJson) : [] } catch { return [] }
}
