/** DORA 效能指标模块 E2E — DevOps 扩展 + 原型 devops.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('DORA 模块 E2E (DevOps)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_dora_metric', `metric_name like '%${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  test('TC-DORA-F001 记录部署频率 (Elite 等级)', async () => {
    const r = await api.post('/business/dora', {
      metricName: `部署频率-${RUN_ID}`,
      metricType: 'deploy_freq',
      metricValue: 2.3,
      metricUnit: '次/天',
      periodType: 'month',
      snapshotDate: '2026-05-01',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-DORA-F002 记录 MTTR + AI 建议', async () => {
    const c = await api.post('/business/dora', {
      metricName: `MTTR-${RUN_ID}`,
      metricType: 'mttr',
      metricValue: 5.5,
      metricUnit: '小时',
      periodType: 'month',
      snapshotDate: '2026-05-01',
      authorUserId: 1
    })
    expect(c.code).toBe(200)
    const list = await api.get('/business/dora/list', { metricName: `MTTR-${RUN_ID}` })
    const m = list.rows[0]
    const ai = await api.post(`/business/dora/ai/suggest/${m.doraId}`, {})
    expect(ai.code).toBe(200)
    expect(ai.data.aiGenerated).toBe('Y')
    expect(ai.data.aiSuggestions).toContain('MTTR')
    expect(ai.data.aiSuggestions).toContain('农情专项')
  })

  test('TC-DORA-F003 无效 metricType 触发 604', async () => {
    const r = await api.post('/business/dora', {
      metricName: `Bad-${RUN_ID}`,
      metricType: 'invalid_metric',
      metricValue: 1.0,
      periodType: 'month',
      snapshotDate: '2026-05-01',
      authorUserId: 1
    })
    expect(r.code).toBe(604)
  })
})
