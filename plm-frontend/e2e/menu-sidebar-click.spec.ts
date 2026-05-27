/**
 * 侧边栏【点击】导航测试 — phase 分组业务菜单不退步守护
 *
 * 背景: navigation.spec.ts 只用 page.goto(path) 直达, 漏掉了真实用户点菜单的路径。
 *   sys_menu 把业务菜单按 8 阶段分组后 (parent=phase-plan/phase-design/...),
 *   子菜单 path 改成绝对 /business/<entity>。SidebarItem.resolvePath 原本无条件
 *   拼 basePath → /phase-plan + /business/x = /phase-plan/business/x,与已注册路由
 *   /business/x 不匹配 → 全部 404。本 spec 通过真实点击复现并守护该回归。
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'

let token: string
let apiRequest: APIRequestContext

// group = phase 分组父菜单文案; item = 业务子菜单文案; expect = 期望落地 path
const MENU_CLICKS = [
  { group: '规划阶段', item: '项目管理', expect: '/business/project' },
  { group: '需求与设计', item: '需求管理', expect: '/business/requirement' },
  { group: '研发阶段', item: '任务管理', expect: '/business/task' },
  { group: '测试阶段', item: '缺陷管理', expect: '/business/defect' }
]

test.describe('侧边栏点击导航 (phase 分组业务菜单)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
  })

  test.afterAll(async () => {
    await apiRequest?.dispose()
  })

  for (const m of MENU_CLICKS) {
    test(`点击 ${m.group} → ${m.item} 落地 ${m.expect}`, async ({ page, context }) => {
      await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
      await page.goto('/')
      // 等侧边栏渲染
      await page.locator('.el-menu').first().waitFor({ state: 'visible', timeout: 10_000 })

      // 1. 展开 phase 分组父菜单
      await page.locator('.el-sub-menu__title', { hasText: m.group }).first().click()
      // 2. 等子菜单链接出现并稳定后再点 (展开动画未settle就点会丢失导航)
      const link = page.locator(`a[href="${m.expect}"]`).first()
      await link.waitFor({ state: 'visible', timeout: 5_000 })
      await link.click()
      await page.waitForTimeout(500)

      // 关键: URL 落在 /business/<entity>, 不是 /phase-xxx/business/<entity> 也不是 /404
      const url = page.url()
      expect(url, `点击后 URL 应含 ${m.expect}`).toContain(m.expect)
      expect(url, '不应拼出 /phase-xxx/ 前缀').not.toContain('/phase-')
      expect(url, '不应跳 404').not.toContain('/404')
    })
  }
})
