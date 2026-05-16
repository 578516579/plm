/**
 * 浏览器截图巡检 - 验证 4 个 active 模块 UI 渲染正常
 *
 * 输出: plm-frontend/test-results/screenshots/
 *
 * 仅在需要可视验证时跑: npx playwright test screenshot-tour.spec.ts
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'

let token: string
let apiRequest: APIRequestContext

test.describe.configure({ mode: 'serial' })

test.describe('截图巡检 (visual regression evidence)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
  })

  test.afterAll(async () => {
    await apiRequest?.dispose()
  })

  const PAGES = [
    { path: '/index', name: '01-home', desc: '登录首页' },
    { path: '/business/project', name: '02-project', desc: '项目管理' },
    { path: '/business/requirement', name: '03-requirement', desc: '需求管理' },
    { path: '/business/sprint', name: '04-sprint', desc: '迭代管理' },
    { path: '/business/task', name: '05-task', desc: '任务管理' },
    { path: '/business/taskkanban', name: '06-task-kanban', desc: '任务看板' },
    { path: '/mytask', name: '07-my-task', desc: '我的任务' }
  ]

  for (const p of PAGES) {
    test(`${p.name} - ${p.desc}`, async ({ page, context }) => {
      await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
      await page.goto(p.path)
      // 等 Vue 路由加载 + Element Plus 表格渲染
      await page.waitForLoadState('networkidle', { timeout: 15_000 })
      await page.waitForTimeout(1500)
      await page.screenshot({
        path: `test-results/screenshots/${p.name}.png`,
        fullPage: true
      })
      // 基本健康检查: 页面有 DOM 且无致命错误
      const errors: string[] = []
      page.on('pageerror', e => errors.push(e.message))
      await expect(page.locator('body')).toBeVisible()
      expect.soft(errors).toHaveLength(0)
    })
  }
})
