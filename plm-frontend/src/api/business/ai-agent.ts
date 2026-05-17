/**
 * AI Agent 编排 API — 原型 aiagents.html
 */
import request from '@/utils/request'

export interface AiAgent {
  agentId?: number
  agentNo?: string
  agentName: string
  agentType?: string  // req_analyzer / code_reviewer / test_gen / release_reviewer / ops_inspector
  description?: string
  modelProvider?: string  // deepseek / claude / chatglm / gpt4
  systemPrompt?: string
  toolsConfig?: string  // JSON
  totalCalls?: number
  successCalls?: number
  failedCalls?: number
  successRate?: number
  avgLatencyMs?: number
  lastCallAt?: string
  status?: string  // 00 草稿 / 01 运行中 / 02 已停用
}

export interface AiAgentQuery { pageNum?: number; pageSize?: number; agentType?: string; status?: string }

export const listAiAgent = (q: AiAgentQuery): Promise<any> => request({ url: '/business/ai-agent/list', method: 'get', params: q })
export const getAiAgent = (id: number): Promise<any> => request({ url: `/business/ai-agent/${id}`, method: 'get' })
export const addAiAgent = (d: AiAgent): Promise<any> => request({ url: '/business/ai-agent', method: 'post', data: d })
export const updateAiAgent = (d: AiAgent): Promise<any> => request({ url: '/business/ai-agent', method: 'put', data: d })
export const delAiAgent = (ids: number | number[]): Promise<any> => request({ url: `/business/ai-agent/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const invokeAiAgent = (id: number, input: any): Promise<any> => request({ url: `/business/ai-agent/invoke/${id}`, method: 'post', data: input })
