/**
 * 后端 API 直调 helper — 用于在 UI 测试中快速 setup / cleanup 数据,
 * 避免每个测试都点 UI 走完整流程
 */
import { APIRequestContext } from '@playwright/test'

const BACKEND = process.env.E2E_BACKEND_URL || 'http://localhost:8081'

/**
 * 创建 API 客户端,自动带上 Bearer token
 */
export class ApiClient {
  constructor(private request: APIRequestContext, private token: string) {}

  async post(path: string, body: any) {
    const r = await this.request.post(`${BACKEND}${path}`, {
      headers: {
        Authorization: `Bearer ${this.token}`,
        'Content-Type': 'application/json; charset=UTF-8'
      },
      data: body
    })
    return r.json()
  }

  async put(path: string, body: any) {
    const r = await this.request.put(`${BACKEND}${path}`, {
      headers: {
        Authorization: `Bearer ${this.token}`,
        'Content-Type': 'application/json; charset=UTF-8'
      },
      data: body
    })
    return r.json()
  }

  async get(path: string, params?: Record<string, string | number>) {
    const url = new URL(`${BACKEND}${path}`)
    if (params) Object.entries(params).forEach(([k, v]) => url.searchParams.set(k, String(v)))
    const r = await this.request.get(url.toString(), {
      headers: { Authorization: `Bearer ${this.token}` }
    })
    return r.json()
  }

  async delete(path: string) {
    const r = await this.request.delete(`${BACKEND}${path}`, {
      headers: { Authorization: `Bearer ${this.token}` }
    })
    return r.json()
  }

  // ==== 业务模块 shortcut ====
  createProject(data: any) { return this.post('/business/project', data) }
  createRequirement(data: any) { return this.post('/business/requirement', data) }
  createSprint(data: any) { return this.post('/business/sprint', data) }
  createTask(data: any) { return this.post('/business/task', data) }

  updateProject(data: any) { return this.put('/business/project', data) }
  updateRequirement(data: any) { return this.put('/business/requirement', data) }
  updateSprint(data: any) { return this.put('/business/sprint', data) }
  updateTask(data: any) { return this.put('/business/task', data) }

  deleteProject(id: number) { return this.delete(`/business/project/${id}`) }
  deleteRequirement(id: number) { return this.delete(`/business/requirement/${id}`) }
  deleteSprint(id: number) { return this.delete(`/business/sprint/${id}`) }
  deleteTask(id: number) { return this.delete(`/business/task/${id}`) }

  listProjects() { return this.get('/business/project/list', { pageSize: 100 }) }
  listRequirements() { return this.get('/business/requirement/list', { pageSize: 100 }) }
  listSprints() { return this.get('/business/sprint/list', { pageSize: 100 }) }
  listTasks() { return this.get('/business/task/list', { pageSize: 100 }) }

  sprintStats(id: number) { return this.get(`/business/sprint/${id}/stats`) }
  currentSprint(projectId: number) { return this.get('/business/sprint/current', { projectId }) }
  taskKanban(projectId: number, sprintId?: number) {
    const params: any = { projectId }
    if (sprintId) params.sprintId = sprintId
    return this.get('/business/task/kanban', params)
  }
  myTasks() { return this.get('/business/task/my') }

  // ==== 需求评审 (PRD §F2.4, 2026-05-25 新增) ====
  submitRequirementReview(reqId: number, data: any) {
    return this.post(`/business/requirement/${reqId}/review`, data)
  }
  listRequirementReviews(reqId: number) {
    return this.get(`/business/requirement/${reqId}/reviews`)
  }
  deleteRequirementReviews(ids: number | number[]) {
    const idStr = Array.isArray(ids) ? ids.join(',') : ids
    return this.delete(`/business/requirement/review/${idStr}`)
  }
}
