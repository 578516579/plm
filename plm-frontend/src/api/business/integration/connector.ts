import request from '@/utils/request'

export interface IntegrationConnector {
  id?: number
  connectorCode?: string
  connectorName?: string
  connectorType?: string
  endpoint?: string
  authType?: string
  /** 写入时明文 JSON，加密入库；返回时不出现 */
  credentialJsonPlain?: string
  webhookSecret?: string
  configJson?: string
  status?: string
  lastSyncAt?: string
  remark?: string
}

export interface ConnectorQuery {
  pageNum?: number
  pageSize?: number
  connectorCode?: string
  connectorName?: string
  connectorType?: string
  status?: string
}

export function listConnectors(query: ConnectorQuery) {
  return request({ url: '/business/integration/connector/list', method: 'get', params: query })
}
export function getConnector(id: number) {
  return request({ url: '/business/integration/connector/' + id, method: 'get' })
}
export function addConnector(data: IntegrationConnector) {
  return request({ url: '/business/integration/connector', method: 'post', data })
}
export function updateConnector(data: IntegrationConnector) {
  return request({ url: '/business/integration/connector', method: 'put', data })
}
export function delConnector(ids: number | number[]) {
  return request({ url: '/business/integration/connector/' + ids, method: 'delete' })
}
export function testConnector(id: number) {
  return request({ url: `/business/integration/connector/${id}/test`, method: 'post' })
}
export function exportConnector(query: ConnectorQuery) {
  return request({ url: '/business/integration/connector/export', method: 'post', params: query, responseType: 'blob' })
}
