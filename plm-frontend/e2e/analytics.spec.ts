/** 效能分析模块 E2E — PRD §F6 + 原型 analytics.html / devops.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('效能分析模块 E2E (PRD §F6)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_analytics_snapshot', `title like '%${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  test('TC-ANALYTICS-F001 创建月度快照 (全局)', async () => {
    const r = await api.post('/business/analytics', {
      title: `2026-05 月度效能快照-${RUN_ID}`,
      periodType: 'month',
      snapshotDate: '2026-05-01',
      requirementThroughput: 34,
      sprintOnTimeRate: 87.0,
      defectDensity: 1.8,
      autoTestCoverage: 76.0,
      deploymentFrequency: 2.3,
      leadTimeHours: 38.5,
      mttrHours: 2.1,
      changeFailureRate: 8.5,
      aiHoursSaved: 284.0,
      activeProjects: 7,
      projectsAtRisk: 2,
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-ANALYTICS-F002 创建季度快照 + 状态机', async () => {
    const r = await api.post('/business/analytics', {
      title: `2026-Q2 季度效能-${RUN_ID}`,
      periodType: 'quarter',
      snapshotDate: '2026-04-01',
      requirementThroughput: 102,
      sprintOnTimeRate: 89.5,
      authorUserId: 1
    })
    expect(r.code).toBe(200)
    const list = await api.get('/business/analytics/list', { title: `2026-Q2 季度效能-${RUN_ID}` })
    const snap = list.rows[0]
    const upd = await api.put('/business/analytics', { snapshotId: snap.snapshotId, status: '01' })
    expect(upd.code).toBe(200)
  })

  test('TC-ANALYTICS-F003 AI 复盘建议', async () => {
    const createRes = await api.post('/business/analytics', {
      title: `AI 复盘快照-${RUN_ID}`,
      periodType: 'month',
      snapshotDate: '2026-05-01',
      sprintOnTimeRate: 75.0,
      defectDensity: 4.2,
      autoTestCoverage: 55.0,
      changeFailureRate: 18.0,
      aiHoursSaved: 250.0,
      authorUserId: 1
    })
    expect(createRes.code).toBe(200)

    const list = await api.get('/business/analytics/list', { title: `AI 复盘快照-${RUN_ID}` })
    const snap = list.rows[0]

    const aiRes = await api.post(`/business/analytics/ai/recommend/${snap.snapshotId}`, {})
    expect(aiRes.code).toBe(200)
    expect(aiRes.data.aiGenerated).toBe('Y')
    expect(aiRes.data.aiRecommendations).toContain('AI 复盘改进建议')
    expect(aiRes.data.aiRecommendations).toContain('迭代准时率偏低')
    expect(aiRes.data.aiRecommendations).toContain('DORA')
  })
})
