/** AI OpenSpec 模块 E2E — PRD §F3.5 + 原型 aispec.html (11 case) */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
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

  const create = (suffix: string, extra: any = {}) =>
    api.post('/business/openspec', { specName: `${suffix}-${RUN_ID}`, specType: 'openapi', version: '1.0.0', authorUserId: 1, ...extra })
  const idByName = async (suffix: string) => {
    const list = await api.get('/business/openspec/list', { specName: `${suffix}-${RUN_ID}` })
    return list.rows[0]?.openspecId
  }

  test('TC-OPENSPEC-F001 创建 OpenAPI 3.1 规范', async () => {
    const r = await create('agriplm-soil-api', { description: '土壤墒情 OpenAPI', agriKbRef: 'agrikb://soil-sensor/v1' })
    expect(r.code).toBe(200)
  })

  test('TC-OPENSPEC-F002 创建 AsyncAPI 3.0 规范', async () => {
    const r = await create('agriplm-iot-events', { specType: 'asyncapi' })
    expect(r.code).toBe(200)
  })

  test('TC-OPENSPEC-F003 AI 生成 GraphQL 骨架', async () => {
    expect((await create('AI-Spec', { specType: 'graphql', version: '0.1.0' })).code).toBe(200)
    const id = await idByName('AI-Spec')
    const ai = await api.post(`/business/openspec/ai/generate/${id}`, {})
    expect(ai.code).toBe(200)
    expect(ai.data.aiGenerated).toBe('Y')
    expect(ai.data.specContent).toContain('type SoilReading')
  })

  test('TC-OPENSPEC-F004 同名同版本冲突 → 701', async () => {
    await create('dup', { version: '9.9.9' })
    const r = await create('dup', { version: '9.9.9' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/已存在/)
  })

  test('TC-OPENSPEC-F005 specName 必填 → 602', async () => {
    const r = await api.post('/business/openspec', { specType: 'openapi', version: '1.0.0', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/规范名称/)
  })

  test('TC-OPENSPEC-F006 specType 白名单非法 → 604', async () => {
    const r = await create('非法类型', { specType: 'protobuf' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/无效的规范类型/)
  })

  test('TC-OPENSPEC-F007 version 必填 → 602', async () => {
    const r = await api.post('/business/openspec', { specName: `缺版本-${RUN_ID}`, specType: 'openapi', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/版本号/)
  })

  test('TC-OPENSPEC-F008 状态机正向 00→01→02', async () => {
    expect((await create('状态机正向')).code).toBe(200)
    const id = await idByName('状态机正向')
    expect((await api.put('/business/openspec', { openspecId: id, status: '01' })).code).toBe(200)
    expect((await api.put('/business/openspec', { openspecId: id, status: '02' })).code).toBe(200)
    const got = await api.get(`/business/openspec/${id}`)
    expect(got.data.status).toBe('02')
  })

  test('TC-OPENSPEC-F009 状态机跳级 00→02 非法 → 601', async () => {
    expect((await create('跳级非法')).code).toBe(200)
    const id = await idByName('跳级非法')
    const r = await api.put('/business/openspec', { openspecId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/转到/)
  })

  test('TC-OPENSPEC-F010 编号格式 SPEC-YYYY-NNNN', async () => {
    expect((await create('编号格式')).code).toBe(200)
    const list = await api.get('/business/openspec/list', { specName: `编号格式-${RUN_ID}` })
    const year = new Date().getFullYear()
    expect(list.rows[0].openspecNo).toMatch(new RegExp(`^SPEC-${year}-\\d{4}$`))
  })

  test('TC-OPENSPEC-ENC001 编码 HEX — 中文 spec_name 不含 EFBFBD', async () => {
    const cn = '农情规约-中文检测'
    expect((await create(cn)).code).toBe(200)
    const hex = getFieldHex('tb_openspec', 'spec_name', `spec_name like '${cn}-${RUN_ID}%'`)
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })

  test('TC-OPENSPEC-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const createResp = await create('删除测试', { version: '8.8.8' })
    expect(createResp.code, '创建应成功').toBe(200)

    const id = await idByName('删除测试')
    expect(id, '新建 openspec 应能在列表里查到').toBeDefined()
    expect(typeof id, 'openspecId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/openspec/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/openspec/list', { specName: `删除测试-${RUN_ID}` })
    const stillThere = after.rows.find((r: any) => r.openspecId === id)
    expect(stillThere, `openspecId=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
