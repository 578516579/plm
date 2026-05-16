/** CI/CD 流水线模块 E2E — DevOps 扩展 + 原型 pipeline.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('Pipeline 模块 E2E (DevOps)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_pipeline', `pipeline_name like '%${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  test('TC-PIPELINE-F001 创建 Jenkins push 触发流水线', async () => {
    const r = await api.post('/business/pipeline', {
      pipelineName: `AgriPLM-Build-${RUN_ID}`,
      repoName: 'agriplm/backend',
      repoBranch: 'main',
      cicdTool: 'jenkins',
      triggerType: 'push',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-PIPELINE-F002 Cron 触发必须带 cronExpr', async () => {
    const r = await api.post('/business/pipeline', {
      pipelineName: `Cron-Bad-${RUN_ID}`,
      repoName: 'agriplm/backend',
      triggerType: 'cron',
      authorUserId: 1
    })
    expect(r.code).toBe(602)
  })

  test('TC-PIPELINE-F003 触发流水线累计 successRate', async () => {
    const c = await api.post('/business/pipeline', {
      pipelineName: `Trigger-Test-${RUN_ID}`,
      repoName: 'agriplm/iot',
      cicdTool: 'gitea',
      triggerType: 'manual',
      authorUserId: 1
    })
    expect(c.code).toBe(200)
    const list = await api.get('/business/pipeline/list', { pipelineName: `Trigger-Test-${RUN_ID}` })
    const pl = list.rows[0]
    const t1 = await api.post(`/business/pipeline/trigger/${pl.pipelineId}`, {})
    expect(t1.code).toBe(200)
    expect(t1.data.totalRuns).toBe(1)
    expect(['success', 'failed']).toContain(t1.data.lastRunStatus)
  })
})
