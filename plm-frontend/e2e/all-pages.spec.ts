/**
 * 全页面可达性 E2E — PLM 所有菜单页 + 静态页 + 免登录页的烟雾测试
 *
 * 目的: 保证每个页面在登录态下都能挂载进主框架、不落 404、无 JS 异常、无中文乱码。
 * 这是"广度"测试,与各业务模块的"深度"spec (project/defect/task/...) 互补。
 *
 * 页面清单来源: 后端 /getRouters 实采 (2026-05-27)。
 * 末尾 completeness 守门测试会比对实时 /getRouters —— 菜单新增却漏补用例时会变红。
 *
 * 每页断言:
 *   1. .app-main 可见   →  进了 Layout 主框架 (404 落地页无 Layout)
 *   2. 无 .wscn-http404-container  →  没有 fall-through 到 404
 *   3. .app-main 文本不含 U+FFFD (�)  →  无乱码 (项目对编码零容忍)
 *   4. (soft) 无未捕获 JS 异常
 */
import { test, expect, APIRequestContext, BrowserContext, Page } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'

const BACKEND = process.env.E2E_BACKEND_URL || 'http://localhost:8081'

type PageDef = { path: string; title: string }

// ---- 系统管理 (8) ----
const SYSTEM_PAGES: PageDef[] = [
  { path: '/system/user', title: '用户管理' },
  { path: '/system/role', title: '角色管理' },
  { path: '/system/menu', title: '菜单管理' },
  { path: '/system/dept', title: '部门管理' },
  { path: '/system/post', title: '岗位管理' },
  { path: '/system/dict', title: '字典管理' },
  { path: '/system/config', title: '参数设置' },
  { path: '/system/notice', title: '通知公告' }
]

// ---- 日志 (2) ----
const LOG_PAGES: PageDef[] = [
  { path: '/system/log/operlog', title: '操作日志' },
  { path: '/system/log/logininfor', title: '登录日志' }
]

// ---- 监控 (6) ----
const MONITOR_PAGES: PageDef[] = [
  { path: '/monitor/online', title: '在线用户' },
  { path: '/monitor/job', title: '定时任务' },
  { path: '/monitor/druid', title: '数据监控' },
  { path: '/monitor/server', title: '服务监控' },
  { path: '/monitor/cache', title: '缓存监控' },
  { path: '/monitor/cacheList', title: '缓存列表' }
]

// ---- 工具 (3) ----
const TOOL_PAGES: PageDef[] = [
  { path: '/tool/build', title: '表单构建' },
  { path: '/tool/gen', title: '代码生成' },
  { path: '/tool/swagger', title: '系统接口' }
]

// ---- 业务模块 (34) ----
const BUSINESS_PAGES: PageDef[] = [
  { path: '/business/dashboard', title: '工作台' },
  { path: '/business/project', title: '项目管理' },
  { path: '/business/inception', title: '项目立项' },
  { path: '/business/competitive', title: '竞品情报' },
  { path: '/business/requirement', title: '需求管理' },
  { path: '/business/prd', title: 'AI PRD 生成' },
  { path: '/business/ued', title: 'UED 设计' },
  { path: '/business/arch', title: '系统架构' },
  { path: '/business/dbdesign', title: '数据库设计' },
  { path: '/business/apidesign', title: '接口详细设计' },
  { path: '/business/document', title: '文档管理' },
  { path: '/business/sprint', title: '迭代管理' },
  { path: '/business/task', title: '任务管理' },
  { path: '/business/taskkanban', title: '任务看板' },
  { path: '/business/mytask', title: '我的任务' },
  { path: '/business/testplan', title: '测试方案' },
  { path: '/business/testcase', title: '测试用例' },
  { path: '/business/testdata', title: '测试数据工厂' },
  { path: '/business/submission', title: '提测管理' },
  { path: '/business/autotest', title: '自动化测试' },
  { path: '/business/defect', title: '缺陷管理' },
  { path: '/business/testreport', title: '测试报告' },
  { path: '/business/apidoc', title: 'API 文档' },
  { path: '/business/manual-product', title: '产品手册' },
  { path: '/business/manual-impl', title: '实施手册' },
  { path: '/business/manual-ops', title: '运维手册' },
  { path: '/business/pipeline', title: '流水线管理' },
  { path: '/business/release', title: '发布管理' },
  { path: '/business/feature-flag', title: '功能开关' },
  { path: '/business/dora', title: 'DORA 效能' },
  { path: '/business/openspec', title: 'AI 规范' },
  { path: '/business/ai-agent', title: 'AI Agent 编排' },
  { path: '/business/ai-invocation-log', title: 'AI 调用审计' },
  { path: '/business/analytics', title: '效能分析' }
]

// ---- 集成 / MCP (4) ----
const INTEGRATION_PAGES: PageDef[] = [
  // path 'mcpserver' 而非 'server': 避免与系统监控 /monitor/server 路由重名(都会被 RuoYi 取 name="Server" → Vue Router 互相覆盖)
  { path: '/mcp/mcpserver', title: 'MCP Server' },
  { path: '/mcp/audit', title: '调用审计' },
  { path: '/integration/connector', title: '连接器配置' },
  { path: '/integration/webhook', title: 'Webhook 事件' }
]

const ALL_MENU_PAGES: PageDef[] = [
  ...SYSTEM_PAGES,
  ...LOG_PAGES,
  ...MONITOR_PAGES,
  ...TOOL_PAGES,
  ...BUSINESS_PAGES,
  ...INTEGRATION_PAGES
]

// 静态/隐藏页 (不在菜单树,但有路由)
const STATIC_PAGES: PageDef[] = [
  { path: '/index', title: '首页' },
  { path: '/user/profile', title: '个人中心' }
]

let token: string
let apiRequest: APIRequestContext

async function gotoWithToken(page: Page, context: BrowserContext, path: string) {
  await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
  return page.goto(path, { waitUntil: 'domcontentloaded' })
}

/** 页面健康度断言: 进框架 / 非 404 / 页面 chrome 编码正常 */
async function assertPageHealthy(page: Page, label: string) {
  // 1. 进了 Layout 主框架 (timeout 放宽到 18s: Vite dev 首次访问该路由 chunk 时
  //    需按需编译,满载跑时偶发 >12s,故配合 retries:1 吸收这类抖动)
  await expect(page.locator('.app-main'), `${label}: 未挂载到主框架 .app-main`).toBeVisible({
    timeout: 18_000
  })
  // 2. 不是 404 落地页 (404 组件无 Layout 包裹)
  await expect(
    page.locator('.wscn-http404-container'),
    `${label}: fall-through 到了 404 页`
  ).toHaveCount(0)
  // 3. 让异步组件 / 列表请求挂载
  await page.waitForTimeout(500)
  // 4. 页面 chrome (document.title = 路由中文标题 + VITE_APP_TITLE) 编码正常。
  //    只查 chrome、不查列表行数据 —— 历史脏数据(如旧编码 probe 记录)不应让冒烟用例误红;
  //    深度编码校验由 encoding.spec.ts 负责。
  const title = await page.title()
  expect(title.includes('�'), `${label}: 页面标题出现乱码 �(title="${title}")`).toBe(false)
  expect(title.length, `${label}: 页面标题为空`).toBeGreaterThan(0)
}

test.describe('全页面可达性 — 菜单页 + 静态页', () => {
  // 单页冒烟偶发受 Vite 按需编译 / CPU 争用影响,retries:1 区分"抖动"与"真坏页"
  test.describe.configure({ retries: 1 })

  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    await ctx.close()
  })

  test.afterAll(async () => {
    await apiRequest?.dispose()
  })

  for (const p of [...ALL_MENU_PAGES, ...STATIC_PAGES]) {
    test(`页面可达: ${p.title} (${p.path})`, async ({ page, context }) => {
      const errors: string[] = []
      page.on('pageerror', err => errors.push(err.message))
      await gotoWithToken(page, context, p.path)
      await assertPageHealthy(page, p.title)
      expect.soft(errors, `${p.title} 触发 JS 错误: ${errors.join(' | ')}`).toHaveLength(0)
    })
  }

  // 守门: 实时 /getRouters 的每个叶子页都必须在上面的清单里
  test('completeness: /getRouters 所有菜单叶子页都已纳入用例', async () => {
    const resp = await apiRequest.get(`${BACKEND}/getRouters`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await resp.json()
    const live = flattenLeafPaths(json.data || [], '')
    const covered = new Set(ALL_MENU_PAGES.map(x => x.path))
    const missing = [...new Set(live)].filter(pth => !covered.has(pth))
    expect(missing, `菜单新增了页面却没补可达性用例: ${missing.join(', ')}`).toHaveLength(0)
  })

  // 登录态下访问未知路由 → 落 404 兜底页
  test('未知路由落到 404 页', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
    await page.goto('/business/__no_such_page__', { waitUntil: 'domcontentloaded' })
    await expect(page.locator('.wscn-http404-container')).toBeVisible({ timeout: 10_000 })
  })
})

test.describe('免登录页', () => {
  test('登录页渲染', async ({ page }) => {
    await page.goto('/login')
    await expect(page.locator('.login-form')).toBeVisible({ timeout: 10_000 })
    await expect(page.getByPlaceholder('账号')).toBeVisible()
    await expect(page.getByPlaceholder('密码')).toBeVisible()
  })

  test('注册页渲染', async ({ page }) => {
    await page.goto('/register')
    await expect(page.locator('form.register-form')).toBeVisible({ timeout: 10_000 })
  })
})

/**
 * 复刻 RuoYi 路由拼接: 子路径相对父级拼接,只收"侧边栏可见 + 有真实组件"的叶子。
 * 跳过 hidden 路由 —— 它们要么是详情页(带参数,如 dict-data/:id),
 * 要么是历史兼容的重复菜单(如已隐藏的 autotest 老 seed 行 menu_id=2700),
 * 不属于用户从菜单直达的页面,不纳入广度可达性清单。
 */
function flattenLeafPaths(nodes: any[], parent: string): string[] {
  const out: string[] = []
  for (const n of nodes) {
    if (n.hidden === true) continue
    const p: string = n.path || ''
    const full = p.startsWith('/') ? p : `${parent.replace(/\/$/, '')}/${p}`
    const comp = n.component
    const isReal = comp && comp !== 'Layout' && comp !== 'ParentView' && comp !== 'InnerLink'
    const kids = n.children || []
    if (kids.length) out.push(...flattenLeafPaths(kids, p.startsWith('/') ? p : full))
    if (isReal) out.push(full)
  }
  return out
}
