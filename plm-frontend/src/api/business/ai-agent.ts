/**
 * AI Agent 编排 API — PRD §F3.5 + 原型 aiagents.html
 *
 * V2 多 Provider 集成 (2026-05-18):
 *   - provider: mock / dify / openai / anthropic
 *   - modelName: gpt-4o-mini / deepseek-chat / claude-sonnet-4-5 (provider=dify 时为空,用 difyWorkflowId)
 *   - difyWorkflowId: 仅 provider=dify 时使用
 *   - GET /ai/health 列出所有 provider 装配状态
 */
import request from '@/utils/request'

/** 与后端 cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent 严格对齐 */
export interface AiAgent {
  agentId?: number
  agentNo?: string
  agentName: string
  /** 字典 biz_aiagent_type — requirement / prd / code / test / release / ops */
  agentType?: string
  description?: string
  promptTemplate?: string
  /** Dify 工作流 ID (仅 provider=dify 时使用) */
  difyWorkflowId?: string
  /** 字典 biz_ai_provider — mock / dify / openai / anthropic (默认 mock) */
  provider?: string
  /** 模型名 — provider=openai 时如 gpt-4o-mini / deepseek-chat;provider=anthropic 时如 claude-sonnet-4-5 */
  modelName?: string
  configJson?: string
  totalCalls?: number
  successRate?: number
  lastInvokedAt?: string
  /** 字典 biz_aiagent_status — 00 运行中 / 01 已停止 / 02 错误 */
  status?: string
  authorUserId?: number
  remark?: string
}

export interface AiAgentQuery { pageNum?: number; pageSize?: number; agentName?: string; agentType?: string; status?: string }

/** AI 集成总览健康响应 */
export interface AiHealthInfo {
  defaultProvider: string
  providers: Record<string, boolean>  // {"mock":true,"openai":false,"anthropic":false,"dify":false}
  openaiEnabled: boolean
  openaiBaseUrl: string
  openaiModel: string
  anthropicEnabled: boolean
  anthropicBaseUrl: string
  anthropicModel: string
  difyUsable: boolean
}

export const listAiAgent   = (q: AiAgentQuery): Promise<any> => request({ url: '/business/ai-agent/list', method: 'get', params: q })
export const getAiAgent    = (id: number): Promise<any> => request({ url: `/business/ai-agent/${id}`, method: 'get' })
export const addAiAgent    = (d: AiAgent): Promise<any> => request({ url: '/business/ai-agent', method: 'post', data: d })
export const updateAiAgent = (d: AiAgent): Promise<any> => request({ url: '/business/ai-agent', method: 'put', data: d })
export const delAiAgent    = (ids: number | number[]): Promise<any> => request({ url: `/business/ai-agent/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const invokeAiAgent = (id: number, input?: any): Promise<any> => request({ url: `/business/ai-agent/invoke/${id}`, method: 'post', data: input ?? {} })

/** AI 集成总览 (V2) */
export const getAiHealth   = (): Promise<any> => request({ url: '/business/ai-agent/ai/health', method: 'get' })
/** Dify 单独健康 (V1,保留兼容) */
export const getDifyHealth = (): Promise<any> => request({ url: '/business/ai-agent/dify/health', method: 'get' })
