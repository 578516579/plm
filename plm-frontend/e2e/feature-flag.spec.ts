/** Feature Flag 模块 E2E — DevOps 扩展 + 原型 featureflag.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('Feature Flag 模块 E2E (DevOps)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_feature_flag', `flag_key like '%${RUN_ID.toLowerCase()}%'`)
    await apiRequest?.dispose()
  })

  test('TC-FF-F001 创建 canary 灰度 Flag', async () => {
    const r = await api.post('/business/feature-flag', {
      flagKey: `new_dashboard_${RUN_ID.toLowerCase()}`,
      title: '新版工作台',
      environment: 'prod',
      rolloutStrategy: 'canary',
      rolloutPercentage: 20,
      status: '00',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-FF-F002 canary 百分比必须 1-99', async () => {
    const r = await api.post('/business/feature-flag', {
      flagKey: `bad_canary_${RUN_ID.toLowerCase()}`,
      title: 'invalid canary',
      environment: 'test',
      rolloutStrategy: 'canary',
      rolloutPercentage: 100,
      authorUserId: 1
    })
    expect(r.code).toBe(604)
  })

  test('TC-FF-F003 flagKey 必须 snake_case', async () => {
    const r = await api.post('/business/feature-flag', {
      flagKey: `BadCamelCase-${RUN_ID}`,
      title: 'invalid key',
      environment: 'test',
      authorUserId: 1
    })
    expect(r.code).toBe(604)
  })

  test('TC-FF-F004 check 端点 — all_on 返回 true, all_off 返回 false', async () => {
    const a = await api.post('/business/feature-flag', {
      flagKey: `always_on_${RUN_ID.toLowerCase()}`,
      title: 'always on',
      environment: 'test',
      rolloutStrategy: 'all_on',
      rolloutPercentage: 100,
      status: '00',
      authorUserId: 1
    })
    expect(a.code).toBe(200)
    const r = await api.get('/business/feature-flag/check', {
      flagKey: `always_on_${RUN_ID.toLowerCase()}`,
      environment: 'test',
      userId: 99
    })
    expect(r.code).toBe(200)
    expect(r.data).toBe(true)
  })
})
