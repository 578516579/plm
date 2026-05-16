/** 自动化测试套件模块 E2E — PRD §F4.5 自动化测试 + 原型 autotest.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('自动化测试套件模块 E2E (PRD §F4.5)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`autotest-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`autotest-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_autotest', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-AUTOTEST-F001 创建自动化测试套件 (Playwright E2E)', async () => {
    const r = await api.post('/business/autotest', {
      projectId,
      title: `AgriPLM端到端测试套件-${RUN_ID}`,
      suiteType: 'e2e',
      framework: 'playwright',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-AUTOTEST-F002 创建自动化测试套件 (pytest API)', async () => {
    const r = await api.post('/business/autotest', {
      projectId,
      title: `农情接口自动化测试-${RUN_ID}`,
      suiteType: 'api',
      framework: 'pytest',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-AUTOTEST-F003 AI 脚本生成', async () => {
    const createRes = await api.post('/business/autotest', {
      projectId,
      title: `AI脚本套件-${RUN_ID}`,
      suiteType: 'api',
      framework: 'playwright',
      authorUserId: 1
    })
    expect(createRes.code).toBe(200)

    const list = await api.get('/business/autotest/list', { projectId })
    const at = list.rows.find((r: any) => r.title.includes(`AI脚本套件-${RUN_ID}`))
    expect(at).toBeDefined()

    const aiRes = await api.post(`/business/autotest/ai/script/${at.autotestId}`, {})
    expect(aiRes.code).toBe(200)
    expect(aiRes.data.aiGenerated).toBe('Y')
    expect(aiRes.data.scriptContent).toContain('playwright')
  })
})
