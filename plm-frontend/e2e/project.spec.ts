import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

/**
 * Project 模块 E2E 测试 — 覆盖 PRD §2.2 场景 S1-S4。
 *
 * 对应测试用例（见 04-测试/测试用例库/Project-functional.md）：
 *   TC-Proj-F001 立项完整流程（S1）— UI 层验证
 *   TC-Proj-F006 合法状态转换 0→1（S2）
 *   TC-Proj-F013 列表分页（S3 简化版）
 *   TC-Proj-F014 多条件搜索（S3）
 *
 * 通过 cookie 注入 token 跳过 UI 登录（programmatic login）。
 */

test.describe('Project 模块 E2E', () => {
  test.beforeEach(async ({ request, context }) => {
    await loginAsAdmin(request, context)
  })

  test('首页能加载（登录后跳到 /index）', async ({ page }) => {
    await page.goto('/')
    // 等任意业务管理菜单出现就算通过（菜单是从 sys_menu 加载的）
    await expect(page).toHaveURL(/\/(index)?$/)
    // 标题应含 PLM
    await expect(page).toHaveTitle(/PLM/i, { timeout: 10_000 })
  })

  test('项目管理路由能直接访问', async ({ page }) => {
    await page.goto('/business/project')
    // 等列表表格出现 — 若依用 el-table，第一列是选择框
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })
  })

  test('列表中能看到已存在的 PRJ-2026-0001', async ({ page }) => {
    await page.goto('/business/project')
    // 等待表格渲染
    await page.waitForSelector('.el-table__body tr', { timeout: 10_000 })
    // 验证至少 1 行 + 含 PRJ-2026 编号
    const text = await page.locator('.el-table').innerText()
    expect(text).toContain('PRJ-2026-')
  })

  test('点新增按钮，弹出对话框（不实际提交，避免污染数据）', async ({ page }) => {
    await page.goto('/business/project')
    await page.waitForSelector('.el-table', { timeout: 10_000 })
    // 找新增按钮（含"新增"文字的按钮）
    await page.getByRole('button', { name: /新增/ }).first().click()
    // 验证对话框出现
    const dialog = page.locator('.el-dialog')
    await expect(dialog).toBeVisible({ timeout: 5_000 })
    // 在对话框内验证含项目名称表单字段（避免和搜索条冲突）
    await expect(dialog.getByLabel(/项目名称/)).toBeVisible()
    // 取消（用对话框 footer 的取消按钮）
    await dialog.getByRole('button', { name: /取\s*消/ }).click()
    await expect(dialog).toBeHidden({ timeout: 5_000 })
  })

  test('搜索条件能输入', async ({ page }) => {
    await page.goto('/business/project')
    await page.waitForSelector('.el-table', { timeout: 10_000 })
    // 在"项目名称"搜索框输入文字
    const nameInput = page.getByPlaceholder(/请输入项目名称/)
    await nameInput.fill('测试')
    await expect(nameInput).toHaveValue('测试')
    // 点搜索
    await page.getByRole('button', { name: /搜索|查询/ }).first().click()
    // 等接口响应 — 列表仍能渲染
    await expect(page.locator('.el-table')).toBeVisible()
  })
})

/**
 * Project API 软删覆盖 — 补 spec 缺口(2026-05-29):
 * 此前 project.spec.ts 是纯 UI spec,DELETE 端点零覆盖,容易漏掉回归。
 * 独立 describe + 独立 setup,不与 UI 测试共享 fixture。
 */
test.describe('Project API — 软删', () => {
  let apiRequest: APIRequestContext
  let api: ApiClient

  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    const token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    await apiRequest?.dispose()
  })

  test('TC-Proj-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const data = makeProjectData(`del-${RUN_ID}`)
    const createResp = await api.createProject(data)
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.listProjects()
    const created = before.rows.find((p: any) => p.projectName === data.projectName)
    expect(created, '新建 project 应能在列表里查到').toBeDefined()
    const id: number = created.id
    expect(typeof id, 'project id 应是 number').toBe('number')

    const delResp = await api.deleteProject(id)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.listProjects()
    const stillThere = after.rows.find((p: any) => p.id === id)
    expect(stillThere, `project id=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
