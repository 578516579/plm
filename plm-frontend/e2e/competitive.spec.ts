/** Competitive 模块 E2E — PRD §F1.3 竞品情报 + 原型 competitive.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('Competitive 模块 E2E (PRD §F1.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`comp-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`comp-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_competitive', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Comp-F001 创建竞品 (含价格档 + 监控订阅)', async () => {
    const r = await api.post('/business/competitive', {
      projectId,
      competitorName: `禅道-${RUN_ID}`,
      vendor: '青岛易软天创',
      website: 'https://www.zentao.net',
      pricingModel: '社区版免费,企业版 2 万/年起',
      pricingTier: 'midrange',
      monitorEnabled: 'Y',
      monitorKeywords: 'AI,RAG,Dify',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
