import request from '@/utils/request'

export interface McpToolAudit {
  id?: number
  serverId?: number
  toolName?: string
  callerType?: string
  callerId?: string
  paramsJson?: string
  resultStatus?: string
  resultBrief?: string
  latencyMs?: number
  callTime?: string
}

export interface McpAuditQuery {
  pageNum?: number
  pageSize?: number
  serverId?: number
  toolName?: string
  callerType?: string
  resultStatus?: string
  params?: { beginTime?: string; endTime?: string }
}

export function listAudits(query: McpAuditQuery) {
  return request({ url: '/business/mcp/audit/list', method: 'get', params: query })
}
export function getAudit(id: number) {
  return request({ url: '/business/mcp/audit/' + id, method: 'get' })
}
export function exportAudit(query: McpAuditQuery) {
  return request({ url: '/business/mcp/audit/export', method: 'post', params: query, responseType: 'blob' })
}
