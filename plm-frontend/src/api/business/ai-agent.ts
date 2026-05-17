/** AI Agent 编排 API — PRD §F3.5 */
import request from '@/utils/request'

export interface AiAgent {
  agentId?: number
  agentNo?: string
  agentName: string
  agentType: string                   // requirement/prd/code/test/release/ops
  description?: string
  promptTemplate?: string
  difyWorkflowId?: string
  configJson?: string
  totalCalls?: number
  successRate?: number
  lastInvokedAt?: string
  status?: '00' | '01' | '02'         // 00运行中/01已停止/02错误
  authorUserId: number
  remark?: string
}

export interface AiAgentQuery {
  pageNum?: number; pageSize?: number
  agentNo?: string; agentName?: string; agentType?: string; status?: string
}

export const listAiAgent  = (q?: AiAgentQuery) => request({ url: '/business/ai-agent/list', method: 'get', params: q })
export const getAiAgent   = (id: number) => request({ url: `/business/ai-agent/${id}`, method: 'get' })
export const addAiAgent   = (d: AiAgent) => request({ url: '/business/ai-agent', method: 'post', data: d })
export const updateAiAgent= (d: AiAgent) => request({ url: '/business/ai-agent', method: 'put', data: d })
export const delAiAgent   = (ids: number|number[]) => request({ url: `/business/ai-agent/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const invokeAiAgent = (id: number) => request({ url: `/business/ai-agent/invoke/${id}`, method: 'post' })
export const exportAiAgent = (q?: AiAgentQuery) => request({ url: '/business/ai-agent/export', method: 'post', params: q, responseType: 'blob' })
