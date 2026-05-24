/**
 * 项目立项 API — PRD §F1.1 + 原型 inception.html
 * 后端: /business/inception/*
 */
import request from '@/utils/request'

export interface Inception {
  inceptionId?: number
  inceptionNo?: string
  projectName: string
  businessLine?: string
  inceptionType?: string
  background?: string
  estimatedDurationMonths?: number
  estimatedTeam?: string
  aiGenerated?: string
  aiProposalContent?: string
  aiRisks?: string
  aiGeneratedAt?: string
  status?: string
  rejectReason?: string
  submitterUserId?: number
  approverUserId?: number
  approvedAt?: string
  projectId?: number
}

export interface InceptionQuery {
  pageNum?: number
  pageSize?: number
  projectName?: string
  businessLine?: string
  inceptionType?: string
  status?: string
}

// 列表
export function listInception(query: InceptionQuery): Promise<any> {
  return request({ url: '/business/inception/list', method: 'get', params: query })
}

// 详情
export function getInception(id: number): Promise<any> {
  return request({ url: `/business/inception/${id}`, method: 'get' })
}

// 新增
export function addInception(data: Inception): Promise<any> {
  return request({ url: '/business/inception', method: 'post', data })
}

// 修改
export function updateInception(data: Inception): Promise<any> {
  return request({ url: '/business/inception', method: 'put', data })
}

// 删除
export function delInception(ids: number | number[]): Promise<any> {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/inception/${idStr}`, method: 'delete' })
}

// AI 生成立项建议书 — PRD §F1.1 project-inception-flow
export function aiGenerateInception(id: number): Promise<any> {
  return request({ url: `/business/inception/ai/generate/${id}`, method: 'post' })
}
