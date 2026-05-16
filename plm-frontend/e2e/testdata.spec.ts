/** 测试数据集模块 E2E — PRD §F4.3 测试数据工厂 + 原型 testdata.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('测试数据集模块 E2E (PRD §F4.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`testdata-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`testdata-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_testdata', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-TESTDATA-F001 创建测试数据集 (土壤传感器 JSON)', async () => {
    const r = await api.post('/business/testdata', {
      projectId,
      title: `土壤传感器测试数据集-${RUN_ID}`,
      targetTable: 't_soil_sensor_data',
      generateCount: 2000,
      outputFormat: 'json',
      ruleCoordinate: 'Y',
      ruleTimeSeries: 'Y',
      ruleSensorRange: 'Y',
      ruleIncludeAbnormal: 'N',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-TESTDATA-F002 创建测试数据集 (气象数据 SQL INSERT)', async () => {
    const r = await api.post('/business/testdata', {
      projectId,
      title: `气象数据集-${RUN_ID}`,
      targetTable: 't_weather_record',
      generateCount: 500,
      outputFormat: 'sql_insert',
      ruleCoordinate: 'Y',
      ruleTimeSeries: 'Y',
      ruleSensorRange: 'N',
      ruleIncludeAbnormal: 'Y',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-TESTDATA-F003 AI 数据生成', async () => {
    const createRes = await api.post('/business/testdata', {
      projectId,
      title: `AI生成数据集-${RUN_ID}`,
      targetTable: 't_crop_info',
      generateCount: 100,
      outputFormat: 'json',
      ruleCoordinate: 'Y',
      ruleTimeSeries: 'Y',
      ruleSensorRange: 'Y',
      ruleIncludeAbnormal: 'N',
      authorUserId: 1
    })
    expect(createRes.code).toBe(200)

    const list = await api.get('/business/testdata/list', { projectId })
    const td = list.rows.find((r: any) => r.title.includes(`AI生成数据集-${RUN_ID}`))
    expect(td).toBeDefined()

    const aiRes = await api.post(`/business/testdata/ai/generate/${td.testdataId}`, {})
    expect(aiRes.code).toBe(200)
    expect(aiRes.data.aiGenerated).toBe('Y')
    expect(aiRes.data.generatedData).toContain('AgriPLM AI Data Factory')
  })
})
