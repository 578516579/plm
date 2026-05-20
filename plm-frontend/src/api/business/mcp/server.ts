import request from '@/utils/request'

export interface McpServer {
  id?: number
  serverCode?: string
  serverName?: string
  protocol?: string
  endpoint?: string
  authType?: string
  oauthClientId?: string
  /** 仅写入用，明文 secret；返回时不出现 */
  oauthClientSecretPlain?: string
  toolsJson?: string
  status?: string
  lastHealthAt?: string
  description?: string
  remark?: string
}

export interface McpServerQuery {
  pageNum?: number
  pageSize?: number
  serverCode?: string
  serverName?: string
  protocol?: string
  status?: string
}

export function listServers(query: McpServerQuery) {
  return request({ url: '/business/mcp/server/list', method: 'get', params: query })
}
export function getServer(id: number) {
  return request({ url: '/business/mcp/server/' + id, method: 'get' })
}
export function addServer(data: McpServer) {
  return request({ url: '/business/mcp/server', method: 'post', data })
}
export function updateServer(data: McpServer) {
  return request({ url: '/business/mcp/server', method: 'put', data })
}
export function delServer(ids: number | number[]) {
  return request({ url: '/business/mcp/server/' + ids, method: 'delete' })
}
export function exportServer(query: McpServerQuery) {
  return request({ url: '/business/mcp/server/export', method: 'post', params: query, responseType: 'blob' })
}
