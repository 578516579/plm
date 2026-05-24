/**
 * AI 调用审计日志 API — V3 (2026-05-18)
 *
 * 后端在每次 AiService.chat() 后自动写一条审计记录(由 AiInvocationLogServiceImpl 实现),
 * 本页只读 + 删除 + 汇总,不暴露写接口。
 */
import request from '@/utils/request'

/** 与后端 cn.com.bosssfot.dv.plm.aiagent.invocationlog.domain.AiInvocationLog 对齐 */
export interface AiInvocationLog {
  logId?: number
  /** 调用方标识 e.g. "ai-agent#AGT-2026-0001" / "inception#42" */
  callerTag?: string
  /** mock / dify / openai / anthropic */
  provider?: string
  model?: string
  /** 0=失败 / 1=成功 */
  success?: number
  finishReason?: string
  promptTokens?: number
  completionTokens?: number
  totalTokens?: number
  elapsedMs?: number
  requestId?: string
  errorMsg?: string
  invokedAt?: string
}

export interface AiInvocationLogQuery {
  pageNum?: number
  pageSize?: number
  callerTag?: string
  provider?: string
  /** 0/1/null=全部 */
  success?: number | null
}

/** Provider 维度汇总(/summary 端点返回的 row 结构) */
export interface ProviderSummaryRow {
  provider: string
  total: number
  success_count: number
  total_tokens: number
  avg_elapsed_ms: number
  /** 成功率 (0-100) */
  success_rate: number
}

export const listAiInvocationLog = (q: AiInvocationLogQuery): Promise<any> =>
  request({ url: '/business/ai-invocation-log/list', method: 'get', params: q })

export const getAiInvocationLog = (id: number): Promise<any> =>
  request({ url: `/business/ai-invocation-log/${id}`, method: 'get' })

export const delAiInvocationLog = (ids: number | number[]): Promise<any> =>
  request({ url: `/business/ai-invocation-log/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })

export const getProviderSummary = (): Promise<any> =>
  request({ url: '/business/ai-invocation-log/summary', method: 'get' })
