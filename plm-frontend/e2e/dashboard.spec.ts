/** 工作台模块 E2E — UI §4.2 + 原型 dashboard.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('工作台模块 E2E (UI §4.2)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_dashboard', `title like '%${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  test('TC-DASHBOARD-F001 创建默认工作台预设', async () => {
    const r = await api.post('/business/dashboard', {
      title: `张总默认工作台-${RUN_ID}`,
      ownerUserId: 1,
      widgetTypes: 'stats,active_projects,my_todos,quality_snapshot',
      refreshInterval: 60,
      isDefault: 'Y'
    })
    expect(r.code).toBe(200)
  })

  test('TC-DASHBOARD-F002 切换默认 → 自动取消旧默认', async () => {
    const a = await api.post('/business/dashboard', {
      title: `测试经理工作台 A-${RUN_ID}`,
      ownerUserId: 2,
      widgetTypes: 'stats,my_todos',
      isDefault: 'Y'
    })
    expect(a.code).toBe(200)

    const b = await api.post('/business/dashboard', {
      title: `测试经理工作台 B-${RUN_ID}`,
      ownerUserId: 2,
      widgetTypes: 'stats,quality_snapshot,ai_metrics',
      isDefault: 'Y'
    })
    expect(b.code).toBe(200)

    const list = await api.get('/business/dashboard/list', { ownerUserId: 2 })
    const defaultRows = list.rows.filter((r: any) => r.isDefault === 'Y' && r.title.includes(RUN_ID))
    expect(defaultRows.length).toBe(1)
    expect(defaultRows[0].title).toContain('工作台 B')
  })

  test('TC-DASHBOARD-F003 聚合查询返回 6 类 widget', async () => {
    const r = await api.get('/business/dashboard/aggregate', { ownerUserId: 1 })
    expect(r.code).toBe(200)
    expect(r.data.stats).toBeDefined()
    expect(r.data.stats.activeProjects).toBe(7)
    expect(Array.isArray(r.data.activeProjects)).toBe(true)
    expect(Array.isArray(r.data.myTodos)).toBe(true)
    expect(r.data.qualitySnapshot.testPassRate).toBeGreaterThan(0)
    expect(r.data.aiMetrics.hoursSaved).toBeGreaterThan(0)
    expect(Array.isArray(r.data.lifecycle)).toBe(true)
    expect(r.data.lifecycle.length).toBe(17)
  })

  test('TC-DASHBOARD-F004 工作台页面 UI 可达 — body 渲染 + 无 JS 错误', async ({ page, context, request }) => {
    await loginAsAdmin(request, context)
    const errors: string[] = []
    page.on('pageerror', err => errors.push(err.message))
    await page.goto('/business/dashboard', { waitUntil: 'domcontentloaded' })
    await expect(page.locator('body')).toBeVisible({ timeout: 10_000 })
    await page.waitForTimeout(1500)
    expect.soft(errors, `工作台 JS 错误: ${errors.join(' | ')}`).toHaveLength(0)
  })

  test('TC-DASHBOARD-F005 列表接口分页可控 (pageNum/pageSize 参数生效)', async () => {
    // 默认 pageSize 10
    const p1 = await api.get('/business/dashboard/list', { pageNum: 1, pageSize: 5 })
    expect(p1.code).toBe(200)
    expect(Array.isArray(p1.rows)).toBe(true)
    expect(p1.rows.length).toBeLessThanOrEqual(5)

    // page 2 (即使为空数组也合法,只要 code=200 + rows is Array)
    const p2 = await api.get('/business/dashboard/list', { pageNum: 2, pageSize: 5 })
    expect(p2.code).toBe(200)
    expect(Array.isArray(p2.rows)).toBe(true)
  })
})
