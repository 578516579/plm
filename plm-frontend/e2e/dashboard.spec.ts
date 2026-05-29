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

  test('TC-DASHBOARD-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const title = `删除测试-${RUN_ID}`
    const createResp = await api.post('/business/dashboard', {
      title,
      ownerUserId: 99,
      widgetTypes: 'stats',
      isDefault: 'N'
    })
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.get('/business/dashboard/list', { ownerUserId: 99 })
    const created = before.rows.find((x: any) => x.title === title)
    expect(created, '新建 dashboard 应能在列表里查到').toBeDefined()
    const id: number = created.dashboardId
    expect(typeof id, 'dashboardId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/dashboard/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/dashboard/list', { ownerUserId: 99 })
    const stillThere = after.rows.find((x: any) => x.dashboardId === id)
    expect(stillThere, `dashboardId=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
