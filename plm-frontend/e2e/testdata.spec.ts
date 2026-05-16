/** TestData 模块 E2E — PRD §F4.3 测试数据工厂 + 原型 testdata.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('TestData 模块 E2E (PRD §F4.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`td-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`td-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_testdata', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-TD-F001 创建测试数据集 (土壤传感器 + 4 规则开关)', async () => {
    const r = await api.post('/business/testdata', {
      projectId,
      title: `土壤湿度生产数据-${RUN_ID}`,
      targetTable: 'soil_sensor',
      generateCount: 5000,
      outputFormat: 'json',
      ruleChinaCoord: 'Y',
      ruleTimeContinuity: 'Y',
      ruleSensorRange: 'Y',
      ruleIncludeOutliers: 'N',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
