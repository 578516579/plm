/** 效能分析模块 E2E — PRD §F6 + 原型 analytics.html / devops.html (10 case) */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
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

  const create = (suffix: string, extra: any = {}) =>
    api.post('/business/analytics', { title: `${suffix}-${RUN_ID}`, periodType: 'month', snapshotDate: '2026-05-01', authorUserId: 1, ...extra })
  const idByTitle = async (suffix: string) => {
    const list = await api.get('/business/analytics/list', { title: `${suffix}-${RUN_ID}` })
    return list.rows[0]?.snapshotId
  }

  test('TC-ANALYTICS-F001 创建月度快照 (全局)', async () => {
    const r = await create('2026-05 月度效能快照', {
      requirementThroughput: 34, sprintOnTimeRate: 87.0, defectDensity: 1.8, autoTestCoverage: 76.0,
      deploymentFrequency: 2.3, leadTimeHours: 38.5, mttrHours: 2.1, changeFailureRate: 8.5, aiHoursSaved: 284.0
    })
    expect(r.code).toBe(200)
  })

  test('TC-ANALYTICS-F002 创建季度快照 + 状态机正向 00→01→02', async () => {
    expect((await create('季度效能', { periodType: 'quarter', snapshotDate: '2026-04-01' })).code).toBe(200)
    const id = await idByTitle('季度效能')
    expect((await api.put('/business/analytics', { snapshotId: id, status: '01' })).code).toBe(200)
    expect((await api.put('/business/analytics', { snapshotId: id, status: '02' })).code).toBe(200)
    const got = await api.get(`/business/analytics/${id}`)
    expect(got.data.status).toBe('02')
  })

  test('TC-ANALYTICS-F003 AI 复盘建议 → aiRecommendations + aiGenerated=Y', async () => {
    expect((await create('AI 复盘快照', {
      sprintOnTimeRate: 75.0, defectDensity: 4.2, autoTestCoverage: 55.0, changeFailureRate: 18.0, aiHoursSaved: 250.0
    })).code).toBe(200)
    const id = await idByTitle('AI 复盘快照')
    const r = await api.post(`/business/analytics/ai/recommend/${id}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.aiRecommendations).toContain('AI 复盘改进建议')
    expect(r.data.aiRecommendations).toContain('DORA')
  })

  test('TC-ANALYTICS-F004 title 必填 → 602', async () => {
    const r = await api.post('/business/analytics', { periodType: 'month', snapshotDate: '2026-05-01', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/快照标题/)
  })

  test('TC-ANALYTICS-F005 periodType 白名单非法 → 604', async () => {
    const r = await create('非法周期', { periodType: 'week' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/无效的周期/)
  })

  test('TC-ANALYTICS-F006 snapshotDate 必填 → 602', async () => {
    const r = await api.post('/business/analytics', { title: `缺日期-${RUN_ID}`, periodType: 'month', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/快照日期/)
  })

  test('TC-ANALYTICS-F007 状态机跳级 00→02 非法 → 601', async () => {
    expect((await create('跳级非法')).code).toBe(200)
    const id = await idByTitle('跳级非法')
    const r = await api.put('/business/analytics', { snapshotId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-ANALYTICS-F008 终态保护 02→01 非法 → 601', async () => {
    expect((await create('终态保护')).code).toBe(200)
    const id = await idByTitle('终态保护')
    await api.put('/business/analytics', { snapshotId: id, status: '01' })
    await api.put('/business/analytics', { snapshotId: id, status: '02' })
    const r = await api.put('/business/analytics', { snapshotId: id, status: '01' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-ANALYTICS-F009 编号格式 AS-YYYY-NNNN', async () => {
    expect((await create('编号格式')).code).toBe(200)
    const list = await api.get('/business/analytics/list', { title: `编号格式-${RUN_ID}` })
    const year = new Date().getFullYear()
    expect(list.rows[0].snapshotNo).toMatch(new RegExp(`^AS-${year}-\\d{4}$`))
  })

  test('TC-ANALYTICS-ENC001 编码 HEX — 中文 title 不含 EFBFBD', async () => {
    const cn = '农情效能快照-中文检测'
    expect((await create(cn)).code).toBe(200)
    const hex = getFieldHex('tb_analytics_snapshot', 'title', `title like '${cn}-${RUN_ID}%'`)
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })
})
