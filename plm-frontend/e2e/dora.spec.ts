/** DORA 效能指标模块 E2E — DevOps 扩展 + 原型 devops.html (10 case) */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
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

  const create = (suffix: string, extra: any = {}) =>
    api.post('/business/dora', { metricName: `${suffix}-${RUN_ID}`, metricType: 'deploy_freq', metricValue: 2.3, metricUnit: '次/天', periodType: 'month', snapshotDate: '2026-05-01', authorUserId: 1, ...extra })
  const idByName = async (suffix: string) => {
    const list = await api.get('/business/dora/list', { metricName: `${suffix}-${RUN_ID}` })
    return list.rows[0]?.doraId
  }

  test('TC-DORA-F001 记录部署频率 (Elite 等级)', async () => {
    const r = await create('部署频率')
    expect(r.code).toBe(200)
  })

  test('TC-DORA-F002 记录 MTTR + AI 建议', async () => {
    expect((await create('MTTR', { metricType: 'mttr', metricValue: 5.5, metricUnit: '小时' })).code).toBe(200)
    const id = await idByName('MTTR')
    const ai = await api.post(`/business/dora/ai/suggest/${id}`, {})
    expect(ai.code).toBe(200)
    expect(ai.data.aiGenerated).toBe('Y')
    expect(ai.data.aiSuggestions).toContain('MTTR')
    expect(ai.data.aiSuggestions).toContain('农情专项')
  })

  test('TC-DORA-F003 metricType 白名单非法 → 604', async () => {
    const r = await create('非法类型', { metricType: 'invalid_metric' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/无效的指标类型/)
  })

  test('TC-DORA-F004 metricName 必填 → 602', async () => {
    const r = await api.post('/business/dora', { metricType: 'deploy_freq', metricValue: 1.0, periodType: 'month', snapshotDate: '2026-05-01', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/指标名称/)
  })

  test('TC-DORA-F005 metricValue 必填 → 602', async () => {
    const r = await api.post('/business/dora', { metricName: `缺值-${RUN_ID}`, metricType: 'deploy_freq', periodType: 'month', snapshotDate: '2026-05-01', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/指标值/)
  })

  test('TC-DORA-F006 periodType 白名单非法 → 604', async () => {
    const r = await create('非法周期', { periodType: 'year' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/无效的周期/)
  })

  test('TC-DORA-F007 状态机正向 00→01→02', async () => {
    expect((await create('状态机正向')).code).toBe(200)
    const id = await idByName('状态机正向')
    expect((await api.put('/business/dora', { doraId: id, status: '01' })).code).toBe(200)
    expect((await api.put('/business/dora', { doraId: id, status: '02' })).code).toBe(200)
    const got = await api.get(`/business/dora/${id}`)
    expect(got.data.status).toBe('02')
  })

  test('TC-DORA-F008 状态机跳级 00→02 非法 → 601', async () => {
    expect((await create('跳级非法')).code).toBe(200)
    const id = await idByName('跳级非法')
    const r = await api.put('/business/dora', { doraId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/转到/)
  })

  test('TC-DORA-F009 编号格式 DORA-YYYY-NNNN', async () => {
    expect((await create('编号格式')).code).toBe(200)
    const list = await api.get('/business/dora/list', { metricName: `编号格式-${RUN_ID}` })
    const year = new Date().getFullYear()
    expect(list.rows[0].doraNo).toMatch(new RegExp(`^DORA-${year}-\\d{4}$`))
  })

  test('TC-DORA-ENC001 编码 HEX — 中文 metric_name 不含 EFBFBD', async () => {
    const cn = '农情部署频率-中文检测'
    expect((await create(cn)).code).toBe(200)
    const hex = getFieldHex('tb_dora_metric', 'metric_name', `metric_name like '${cn}-${RUN_ID}%'`)
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })

  test('TC-DORA-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const createResp = await create('删除测试')
    expect(createResp.code, '创建应成功').toBe(200)

    const id = await idByName('删除测试')
    expect(id, '新建 dora 应能在列表里查到').toBeDefined()
    expect(typeof id, 'doraId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/dora/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/dora/list', { metricName: `删除测试-${RUN_ID}` })
    const stillThere = after.rows.find((r: any) => r.doraId === id)
    expect(stillThere, `doraId=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
