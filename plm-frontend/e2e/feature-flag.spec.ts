/** Feature Flag 模块 E2E — DevOps 扩展 + 原型 featureflag.html (10 case) */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext
const RT = RUN_ID.toLowerCase().replace(/-/g, '_') // snake_case-safe token for flagKey

test.describe('Feature Flag 模块 E2E (DevOps)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_feature_flag', `flag_key like '%${RT}%'`)
    await apiRequest?.dispose()
  })

  const flag = (key: string, extra: any = {}) =>
    api.post('/business/feature-flag', { flagKey: `${key}_${RT}`, title: `开关-${RUN_ID}`, environment: 'test', authorUserId: 1, ...extra })

  test('TC-FF-F001 创建 canary 灰度 Flag', async () => {
    const r = await flag('new_dashboard', { title: '新版工作台', environment: 'prod', rolloutStrategy: 'canary', rolloutPercentage: 20, status: '00' })
    expect(r.code).toBe(200)
  })

  test('TC-FF-F002 canary 百分比必须 1-99 → 604', async () => {
    const r = await flag('bad_canary', { rolloutStrategy: 'canary', rolloutPercentage: 100 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/canary/)
  })

  test('TC-FF-F003 flagKey 必须 snake_case → 604', async () => {
    const r = await api.post('/business/feature-flag', { flagKey: `BadCamelCase-${RUN_ID}`, title: '非法key', environment: 'test', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/snake_case/)
  })

  test('TC-FF-F004 check 端点 — all_on 返回 true', async () => {
    expect((await flag('always_on', { rolloutStrategy: 'all_on', rolloutPercentage: 100, status: '00' })).code).toBe(200)
    const r = await api.get('/business/feature-flag/check', { flagKey: `always_on_${RT}`, environment: 'test', userId: 99 })
    expect(r.code).toBe(200)
    expect(r.data).toBe(true)
  })

  test('TC-FF-F005 flagKey 必填 → 602', async () => {
    const r = await api.post('/business/feature-flag', { title: '缺key', environment: 'test', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/Flag Key/)
  })

  test('TC-FF-F006 environment 白名单非法 → 604', async () => {
    const r = await flag('bad_env', { environment: 'uat' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/无效的环境/)
  })

  test('TC-FF-F007 all_on 策略百分比必须 100 → 604', async () => {
    const r = await flag('bad_allon', { rolloutStrategy: 'all_on', rolloutPercentage: 50 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/all_on/)
  })

  test('TC-FF-F008 创建关闭态再开启 all_on (update toggle)', async () => {
    expect((await flag('toggle', { rolloutStrategy: 'all_off', rolloutPercentage: 0, status: '01' })).code).toBe(200)
    const list = await api.get('/business/feature-flag/list', { flagKey: `toggle_${RT}` })
    const id = list.rows[0].flagId
    const r = await api.put('/business/feature-flag', { flagId: id, status: '00', rolloutStrategy: 'all_on', rolloutPercentage: 100 })
    expect(r.code).toBe(200)
  })

  test('TC-FF-F009 编号格式 FF-YYYY-NNNN', async () => {
    expect((await flag('numfmt', { rolloutStrategy: 'all_off', rolloutPercentage: 0 })).code).toBe(200)
    const list = await api.get('/business/feature-flag/list', { flagKey: `numfmt_${RT}` })
    const year = new Date().getFullYear()
    expect(list.rows[0].flagNo).toMatch(new RegExp(`^FF-${year}-\\d{4}$`))
  })

  test('TC-FF-ENC001 编码 HEX — 中文 title 不含 EFBFBD', async () => {
    expect((await flag('enc', { title: `农情灰度开关-${RUN_ID}`, rolloutStrategy: 'all_off', rolloutPercentage: 0 })).code).toBe(200)
    const hex = getFieldHex('tb_feature_flag', 'title', `flag_key = 'enc_${RT}'`)
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })

  test('TC-FF-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const flagKey = `delete_test_${RT}`
    const createResp = await flag('delete_test', { rolloutStrategy: 'all_off', rolloutPercentage: 0 })
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.get('/business/feature-flag/list', { flagKey })
    const created = before.rows.find((x: any) => x.flagKey === flagKey)
    expect(created, '新建 feature-flag 应能在列表里查到').toBeDefined()
    const id: number = created.flagId
    expect(typeof id, 'flagId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/feature-flag/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/feature-flag/list', { flagKey })
    const stillThere = after.rows.find((x: any) => x.flagId === id)
    expect(stillThere, `flagId=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
