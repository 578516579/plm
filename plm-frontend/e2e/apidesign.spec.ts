/** ApiDesign 模块 E2E — PRD §F3.3 LLD 接口详细设计 + 原型 apidesign.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('ApiDesign 模块 E2E (PRD §F3.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`apidesign-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`apidesign-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_apidesign', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-APID-F001 创建接口设计 (HTTP方法 + 路径 + Mock开关)', async () => {
    const r = await api.post('/business/apidesign', {
      projectId,
      title: `灌溉推荐接口-${RUN_ID}`,
      httpMethod: 'POST',
      path: `/api/v1/irrigation-${RUN_ID.slice(-4)}/recommend`,
      description: '根据土壤墒情 + 气象数据生成灌溉建议',
      mockEnabled: 'Y',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
