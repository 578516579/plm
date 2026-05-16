/** AutoTest 模块 E2E — PRD §F4.5 自动化测试 + 原型 autotest.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('AutoTest 模块 E2E (PRD §F4.5)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`at-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`at-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_autotest', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-AT-F001 创建自动化套件 (Playwright UI + 定时执行)', async () => {
    const r = await api.post('/business/autotest', {
      projectId,
      title: `灌溉控制台冒烟套件-${RUN_ID}`,
      testSuiteType: 'ui',
      framework: 'playwright',
      targetUrl: 'http://localhost:8080',
      scheduleEnabled: 'Y',
      scheduleCron: '0 0 2 * * ?',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
