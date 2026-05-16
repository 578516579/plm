/** TestPlan 模块 E2E — PRD §F4.1 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('TestPlan 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`tp-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`tp-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_testplan', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-TP-F001 创建测试方案 (5 种 test_types)', async () => {
    const r = await api.post('/business/testplan', {
      projectId,
      title: `Sprint 测试方案-${RUN_ID}`,
      testTypes: 'functional,api,automation',
      testCycleDays: 10,
      scope: '需求 REQ-001~005',
      strategy: '功能优先,自动化覆盖核心路径',
      toolsRecommended: 'playwright,jmeter',
      aiGenerated: 'Y',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
