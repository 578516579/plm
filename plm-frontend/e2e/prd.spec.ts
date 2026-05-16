/** PRD 模块 E2E — PRD §F2.2 AI PRD 生成器 + 原型 prd.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('PRD 模块 E2E (PRD §F2.2)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`prd-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`prd-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_prd', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-PRD-F001 创建 PRD (业务场景 + 目标用户)', async () => {
    const r = await api.post('/business/prd', {
      projectId,
      title: `AI 灌溉推荐引擎-${RUN_ID}`,
      description: '基于土壤墒情传感器、气象数据和作物生长模型,自动推荐灌溉时间和灌溉量。原型 prd.html L144 同款诉求。',
      sceneTemplate: 'irrigation',
      targetUser: 'farmer',
      version: 'v1.0',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
