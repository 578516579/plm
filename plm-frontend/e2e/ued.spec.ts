/** UED 模块 E2E — PRD §F2.3 UED 设计协同 + 原型 ued.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('UED 模块 E2E (PRD §F2.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`ued-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`ued-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_ued', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-UED-F001 创建设计稿 (Figma URL + 版本号)', async () => {
    const r = await api.post('/business/ued', {
      projectId,
      title: `农情大屏 v2 交互稿-${RUN_ID}`,
      version: 'v1.0',
      figmaUrl: 'https://www.figma.com/file/abc123/agri-dashboard',
      designerUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
