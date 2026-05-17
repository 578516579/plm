import request from '@/utils/request'
import type { AiAgentQuery, AiAgentForm } from '../types'

/** 查询 AI Agent 列表 */
export function listAiAgent(query: AiAgentQuery) {
  return request({ url: '/business/ai-agent/list', method: 'get', params: query })
}

/** 查询 AI Agent 详细 */
export function getAiAgent(id: number | string) {
  return request({ url: '/business/ai-agent/' + id, method: 'get' })
}

/** 新增 AI Agent */
export function addAiAgent(data: AiAgentForm) {
  return request({ url: '/business/ai-agent', method: 'post', data })
}

/** 修改 AI Agent */
export function updateAiAgent(data: AiAgentForm) {
  return request({ url: '/business/ai-agent', method: 'put', data })
}

/** 删除 AI Agent */
export function delAiAgent(ids: (number | string)[]) {
  return request({ url: '/business/ai-agent/' + ids.join(','), method: 'delete' })
}

/** 切换 Agent 状态（启动/暂停） */
export function changeAgentStatus(id: number | string, status: string) {
  return request({ url: `/business/ai-agent/${id}/status`, method: 'put', data: { status } })
}
