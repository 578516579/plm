/** AI OpenSpec 模块 E2E — PRD §F3.5 + 原型 aispec.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('OpenSpec 模块 E2E (PRD §F3.5)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_openspec', `spec_name like '%${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  test('TC-OPENSPEC-F001 创建 OpenAPI 3.1 规范', async () => {
    const r = await api.post('/business/openspec', {
      specName: `agriplm-soil-api-${RUN_ID}`,
      specType: 'openapi',
      version: '1.0.0',
      description: '土壤墒情 OpenAPI',
      agriKbRef: 'agrikb://soil-sensor/v1',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-OPENSPEC-F002 创建 AsyncAPI 3.0 规范', async () => {
    const r = await api.post('/business/openspec', {
      specName: `agriplm-iot-events-${RUN_ID}`,
      specType: 'asyncapi',
      version: '1.0.0',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-OPENSPEC-F003 AI 生成 GraphQL 骨架', async () => {
    const c = await api.post('/business/openspec', {
      specName: `AI-Spec-${RUN_ID}`,
      specType: 'graphql',
      version: '0.1.0',
      authorUserId: 1
    })
    expect(c.code).toBe(200)
    const list = await api.get('/business/openspec/list', { specName: `AI-Spec-${RUN_ID}` })
    const sp = list.rows[0]
    const ai = await api.post(`/business/openspec/ai/generate/${sp.openspecId}`, {})
    expect(ai.code).toBe(200)
    expect(ai.data.aiGenerated).toBe('Y')
    expect(ai.data.specContent).toContain('type SoilReading')
  })

  test('TC-OPENSPEC-F004 同名同版本冲突 701', async () => {
    await api.post('/business/openspec', {
      specName: `dup-${RUN_ID}`,
      specType: 'openapi',
      version: '1.0.0',
      authorUserId: 1
    })
    const r = await api.post('/business/openspec', {
      specName: `dup-${RUN_ID}`,
      specType: 'openapi',
      version: '1.0.0',
      authorUserId: 1
    })
    expect(r.code).toBe(701)
  })
})
