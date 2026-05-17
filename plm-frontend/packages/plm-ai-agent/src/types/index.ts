import type { BaseEntity, PageQuery } from '@/types/api/common'

/** AI Agent 表单 / 列表行 */
export interface AiAgentForm extends BaseEntity {
  id?: number | string
  agentNo?: string
  agentName?: string
  agentRole?: string
  agentType?: string
  modelName?: string
  difyFlowId?: string
  toolsJson?: string
  status?: string
  callsToday?: number
  successRate?: number | string
  avgLatency?: string
  description?: string
}

/** AI Agent 查询条件 */
export interface AiAgentQuery extends PageQuery {
  agentNo?: string
  agentName?: string
  agentRole?: string
  agentType?: string
  modelName?: string
  status?: string
}
