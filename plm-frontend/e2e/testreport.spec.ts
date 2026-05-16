/** TestReport 模块 E2E — PRD §F4.7 上线风险评级 绿/黄/红 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('TestReport 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`tr-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`tr-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_testreport', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-TR-F001 创建测试报告 (黄灯风险)', async () => {
    const r = await api.post('/business/testreport', {
      projectId,
      title: `Sprint 测试报告-${RUN_ID}`,
      totalCases: 120,
      passedCases: 115,
      failedCases: 5,
      coverageRate: 92.5,
      p0Defects: 0,
      p1Defects: 2,
      p2Defects: 3,
      riskLevel: 'yellow',
      riskEvaluation: '2 P1 缺陷待修复',
      recommendations: '上线 24h 监控',
      aiGenerated: 'Y'
    })
    expect(r.code).toBe(200)
  })
})
