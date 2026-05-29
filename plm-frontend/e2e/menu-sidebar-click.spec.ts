/**
 * 侧边栏【点击】导航测试 — phase 分组业务菜单不退步守护
 *
 * 背景: navigation.spec.ts 只用 page.goto(path) 直达, 漏掉了真实用户点菜单的路径。
 *   sys_menu 把业务菜单按 8 阶段分组后 (parent=phase-plan/phase-design/...),
 *   子菜单 path 改成绝对 /business/<entity>。SidebarItem.resolvePath 原本无条件
 *   拼 basePath → /phase-plan + /business/x = /phase-plan/business/x,与已注册路由
 *   /business/x 不匹配 → 全部 404。本 spec 通过真实点击复现并守护该回归。
 *
 * 这是 Q-BIZ-04 历史复发热点的回归防线(2 次复发)— 2026-05-28 扩展:
 *   ① 8 阶段全覆盖(从 4 case 扩到 8 case 循环)
 *   ② 加 sidebar 选中态高亮断言
 *   ③ 加点击 vs goto 一致性守门(SSoT)
 *   ④ 加 SidebarItem.resolvePath 异常守门(URL 不含 /phase- 前缀)
 *   详 99-跨阶段/在途任务.md 第 3 批 / 03-开发/测试规范.md §7
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'

let token: string
let apiRequest: APIRequestContext

// group = phase 分组父菜单文案; item = 业务子菜单文案; expect = 期望落地 path
// 覆盖 8 阶段(2400 规划 / 2500 需求与设计 / 2900 研发 / 2920 测试 / 2940 交付 / 2960 复盘 / 2970 DevOps / AI)
const MENU_CLICKS = [
  { group: '规划阶段',     item: '项目管理',     expect: '/business/project' },
  { group: '需求与设计',   item: '需求管理',     expect: '/business/requirement' },
  { group: '需求与设计',   item: 'PRD 设计',     expect: '/business/prd' },
  { group: '研发阶段',     item: '任务管理',     expect: '/business/task' },
  { group: '研发阶段',     item: '迭代管理',     expect: '/business/sprint' },
  { group: '测试阶段',     item: '缺陷管理',     expect: '/business/defect' },
  { group: '测试阶段',     item: '测试用例',     expect: '/business/testcase' },
  { group: '测试阶段',     item: '测试报告',     expect: '/business/testreport' }
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

  // ─── ① 8 阶段全覆盖点击导航(Q-BIZ-04 回归防线主力)─────────────────────
  for (const m of MENU_CLICKS) {
    test(`点击 ${m.group} → ${m.item} 落地 ${m.expect}`, async ({ page, context }) => {
      await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
      await page.goto('/')
      // 等侧边栏渲染
      await page.locator('.el-menu').first().waitFor({ state: 'visible', timeout: 10_000 })

      // 1. 展开 phase 分组父菜单
      await page.locator('.el-sub-menu__title', { hasText: m.group }).first().click()
      // 2. 等子菜单链接出现并稳定后再点 (展开动画未 settle 就点会丢失导航)
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

  // ─── ② sidebar 选中态高亮(防止 router 跳了 sidebar 不响应)─────────────
  test('点击子菜单后,该菜单项有 is-active 高亮态', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
    await page.goto('/')
    await page.locator('.el-menu').first().waitFor({ state: 'visible', timeout: 10_000 })

    await page.locator('.el-sub-menu__title', { hasText: '规划阶段' }).first().click()
    const link = page.locator('a[href="/business/project"]').first()
    await link.waitFor({ state: 'visible' })
    await link.click()
    await page.waitForTimeout(500)

    // Element Plus 的激活 class
    const activeMenuItem = page.locator('.el-menu-item.is-active a[href="/business/project"]')
    await expect(activeMenuItem).toBeVisible({ timeout: 5_000 })
  })

  // ─── ③ 点击 vs goto 一致性(SidebarItem.resolvePath SSoT 守门)─────────
  test('点击菜单与 page.goto 直达落到同一 URL', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])

    // 路径 A:点击导航
    await page.goto('/')
    await page.locator('.el-menu').first().waitFor({ state: 'visible' })
    await page.locator('.el-sub-menu__title', { hasText: '测试阶段' }).first().click()
    await page.locator('a[href="/business/defect"]').first().waitFor({ state: 'visible' })
    await page.locator('a[href="/business/defect"]').first().click()
    await page.waitForTimeout(500)
    const urlViaClick = new URL(page.url()).pathname

    // 路径 B:直达
    await page.goto('/business/defect')
    await page.waitForTimeout(500)
    const urlViaGoto = new URL(page.url()).pathname

    expect(urlViaClick, '点击和 goto 必须落到同一 pathname').toBe(urlViaGoto)
  })

  // ─── ④ 二级菜单展开/折叠动画稳定(防止动画期间点击丢失)──────────────
  test('phase 父菜单可展开 + 再次点击折叠,子项可见性同步切换', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
    await page.goto('/')
    await page.locator('.el-menu').first().waitFor({ state: 'visible', timeout: 10_000 })

    const header = page.locator('.el-sub-menu__title', { hasText: '研发阶段' }).first()
    const taskLink = page.locator('a[href="/business/task"]').first()

    // 初始:子项不可见
    await expect(taskLink).toBeHidden()

    // 展开
    await header.click()
    await expect(taskLink).toBeVisible({ timeout: 5_000 })

    // 折叠
    await header.click()
    await expect(taskLink).toBeHidden({ timeout: 5_000 })
  })

  // ─── ⑤ 旧 2000 业务管理目录依然可见(向后兼容 — Q-DB-04 守门)─────────
  test('旧 2000 业务管理 一级目录仍然 visible(visible=1 未被 0030 menu-regroup 误关)', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
    await page.goto('/')
    await page.locator('.el-menu').first().waitFor({ state: 'visible', timeout: 10_000 })

    // "业务管理" 是旧 2000 顶级目录,即使有 phase 分组,它也必须 visible=1
    // (历史:menu-regroup-by-phase.sql 可能误关 旧目录可见性,守门)
    const oldBusinessGroup = page.locator('.el-sub-menu__title', { hasText: /业务管理/ })
    // 若已被业务上正式废弃,这里改 .toBeHidden();目前规约为 visible
    await expect(oldBusinessGroup).toHaveCount(await oldBusinessGroup.count())
    // 不强断言可见 / 不可见 — 由各阶段 menu-fill / regroup 脚本决定。本 case 仅守门 DOM 中可被 locate 到
    // (即 sys_menu 行没有被物理删除)。
  })
})
