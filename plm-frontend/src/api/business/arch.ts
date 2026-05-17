/**
 * 系统概要设计 HLD API — PRD §F3.1 + 原型 archdesign.html
 */
import request from '@/utils/request'

export interface Arch {
  archId?: number
  archNo?: string
  projectId: number
  prdId?: number
  title: string
  archMode?: string         // microservice / monolith / serverless / layered
  primaryStack?: string     // java_sb3 / go_gin / python_fastapi / nodejs
  databaseChoice?: string   // pg_redis / mysql_redis / kingbase
  aiOrchestration?: string  // dify_deepseek / dify_chatglm / self_langchain
  deploymentType?: string   // k8s / docker_compose / baremetal
  iotProtocol?: string      // mqtt / http_longpoll / websocket
  designContent?: string
  c4DiagramContent?: string
  nfrMapping?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number
  reviewerUserId?: number
}

export interface ArchQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  title?: string
  archMode?: string
  status?: string
}

export function listArch(q: ArchQuery): Promise<any> {
  return request({ url: '/business/arch/list', method: 'get', params: q })
}
export function getArch(id: number): Promise<any> {
  return request({ url: `/business/arch/${id}`, method: 'get' })
}
export function addArch(d: Arch): Promise<any> {
  return request({ url: '/business/arch', method: 'post', data: d })
}
export function updateArch(d: Arch): Promise<any> {
  return request({ url: '/business/arch', method: 'put', data: d })
}
export function delArch(ids: number | number[]): Promise<any> {
  const s = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/arch/${s}`, method: 'delete' })
}
export function aiGenerateArch(id: number): Promise<any> {
  return request({ url: `/business/arch/ai/generate/${id}`, method: 'post' })
}
export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
