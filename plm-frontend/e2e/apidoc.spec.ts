/** ApiDoc 模块 E2E — PRD §F5.4 OpenAPI 规范 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('ApiDoc 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`apidoc-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`apidoc-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_apidoc', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-API-F001 创建 API 文档 (HTTP 方法 + 路径)', async () => {
    const r = await api.post('/business/apidoc', {
      projectId,
      title: `获取项目列表-${RUN_ID}`,
      httpMethod: 'GET',
      path: `/test/api-${RUN_ID.slice(-4)}`,
      description: '分页查询项目列表',
      version: 'v1.0',
      autoExtracted: 'Y'
    })
    expect(r.code).toBe(200)
  })
})
