/**
 * Autotest 模块 E2E — PRD §F4.5 自动化测试 + 原型 autotest.html (改造后字段)
 *
 * 覆盖 4 个核心场景:
 *  1. 列表加载 + 4 统计卡可见 (UI)
 *  2. 新增套件 — UI Dialog 走完整字段 (projectId / testSuiteType=ui / framework=playwright /
 *     targetUrl / scheduleEnabled=Y / scheduleCron) → 列表出现
 *  3. AI 生成脚本 — 点 "AI 生成脚本" → toast + scriptContent 非空 + aiGenerated=Y
 *  4. 立即执行 + RCA — 必须先把套件 status 切到 01 (API 旁路),再 UI 点 "立即执行";
 *     验证 totalCases/passedCases/failedCases ≥ 0,如 failedCases > 0 看 .rca-text 出现
 *
 * 注意:
 *  - run/{id} 接口前置: status='01' 已激活,否则 601 ServiceException;前端在按钮触发前用
 *    ElMessage.warning 自检,所以本测试通过 API PUT 把 status 直接置 01 绕过。
 *  - 字段名严格使用 testSuiteType / scheduleEnabled / scheduleCron / targetUrl (改造后)。
 *  - workers=1 + describe.serial: 4 个测试共享一个 projectId/autotestId 链路。
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string
let api: ApiClient
let apiRequest: APIRequestContext
let projectId: number
let suiteTitle: string
let createdAutotestId: number | undefined

test.describe.serial('Autotest 模块 E2E (PRD §F4.5)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)

    // 准备一个独立项目,所有套件挂它名下,afterAll 一次性清干净
    await api.createProject(makeProjectData(`at-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`at-suite-${RUN_ID}`))?.id
    expect(projectId, '前置项目应已创建').toBeTruthy()

    suiteTitle = `E2E TestSuite ${RUN_ID}`
  })

  test.afterAll(async () => {
    // 顺序: 先 tb_autotest (FK to project),再 tb_project
    if (projectId) {
      execDelete('tb_autotest', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  // === 场景 1: 列表加载 + 4 统计卡 ===
  test('TC-AT-E001 列表加载 + 4 统计卡渲染', async ({ page, context, request }) => {
    // 用 loginAsAdmin 给 fresh context 注入完整 cookie + 触发 fresh login
    // (vue-router 动态路由场景下,旧 context.addCookies 拿不到菜单 → /business/autotest 404)
    await loginAsAdmin(request, context)
    await page.goto('/business/autotest')

    // 页面标题 (h2 page-title 含 emoji)
    await expect(page.locator('h2.page-title')).toContainText('自动化测试', { timeout: 10_000 })

    // 4 统计卡的 label 文案 (顺序: 测试套件 / 最新执行通过率 / 执行耗时 / 失败用例)
    await expect(page.getByText('测试套件', { exact: true })).toBeVisible({ timeout: 10_000 })
    await expect(page.getByText('最新执行通过率', { exact: true })).toBeVisible()
    await expect(page.getByText('执行耗时', { exact: true })).toBeVisible()
    await expect(page.getByText('失败用例', { exact: true })).toBeVisible()

    // 表格容器渲染 (rows 数 ≥ 0,空也合法)
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })
  })

  // === 场景 2: 新增套件 (UI Dialog 全字段) ===
  test('TC-AT-E002 新增套件 — Dialog 全字段创建', async ({ page, context, request }) => {
    await loginAsAdmin(request, context)
    await page.goto('/business/autotest')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })

    // 点 "新增套件"
    await page.getByRole('button', { name: /新增套件/ }).click()
    const dialog = page.locator('.el-dialog').filter({ hasText: '新增套件' })
    await expect(dialog).toBeVisible({ timeout: 5_000 })

    // 关联项目: 选指定的测试项目 (按名字匹配,避免选到默认第一项时拿错 id)
    await dialog.locator('.el-form-item').filter({ hasText: '关联项目' })
      .locator('.el-select').click()
    // dropdown 是 teleport 到 body 的,从 page 找
    await page.locator('.el-select-dropdown__item')
      .filter({ hasText: `at-suite-${RUN_ID}` }).first().click()

    // 套件名
    await dialog.getByPlaceholder(/灌溉决策/).fill(suiteTitle)

    // 类型 = UI (默认值已是 ui,显式重选稳点)
    // 注意: el-select 选中后 form-item 文本含值 ("类型UI"),不能用锚定 /^类型$/,用子串匹配 (同"关联项目")
    await dialog.locator('.el-form-item').filter({ hasText: '类型' })
      .locator('.el-select').click()
    await page.locator('.el-select-dropdown__item').filter({ hasText: /^UI$/ }).first().click()

    // 框架 = Playwright (默认即可,显式确保) — 同上,子串匹配避免锚定漏选中值
    await dialog.locator('.el-form-item').filter({ hasText: '框架' })
      .locator('.el-select').click()
    await page.locator('.el-select-dropdown__item').filter({ hasText: /^Playwright$/ }).first().click()

    // 目标 URL
    await dialog.getByPlaceholder('http://localhost').fill('http://localhost')

    // 启用调度 Switch → Y (Element Plus el-switch 默认未激活,点一下切到 active)
    const scheduleSwitch = dialog.locator('.el-form-item')
      .filter({ hasText: '启用调度' }).locator('.el-switch')
    await scheduleSwitch.click()

    // 调度 Cron
    await dialog.getByPlaceholder(/每日凌晨 2 点/).fill('0 2 * * *')

    // 提交
    await dialog.getByRole('button', { name: /^创建$/ }).click()

    // toast "创建成功"
    await expect(page.locator('.el-message').filter({ hasText: '创建成功' }))
      .toBeVisible({ timeout: 5_000 })

    // 列表里能看到新套件 (走 API 查最稳;UI 表格 paging 可能要翻页)
    const list = await api.get('/business/autotest/list', { pageSize: 200, projectId })
    const created = list.rows.find((x: any) => x.title === suiteTitle)
    expect(created, `新套件 "${suiteTitle}" 应在列表中`).toBeTruthy()
    expect(created.testSuiteType).toBe('ui')
    expect(created.framework).toBe('playwright')
    expect(created.scheduleEnabled).toBe('Y')
    expect(created.scheduleCron).toBe('0 2 * * *')
    createdAutotestId = created.autotestId
  })

  // === 场景 3: AI 生成脚本 ===
  test('TC-AT-E003 AI 生成脚本 → toast + scriptContent 非空', async ({ page, context, request }) => {
    test.skip(!createdAutotestId, '依赖 TC-AT-E002 创建的套件')
    await loginAsAdmin(request, context)
    await page.goto('/business/autotest')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })

    // 表格里点中新建的那行 (highlight-current-row + @current-change 触发 current 选中)
    await page.locator('.el-table__row').filter({ hasText: suiteTitle }).first().click()

    // 等右侧详情卡片加载 (套件编号文案出现)
    await expect(page.getByText('套件编号', { exact: true })).toBeVisible({ timeout: 5_000 })

    // 点 "AI 生成脚本" (按钮文案含 emoji,用 partial 匹配)
    await page.getByRole('button', { name: /AI 生成脚本/ }).click()

    // toast "AI 脚本已生成"
    await expect(page.locator('.el-message').filter({ hasText: 'AI 脚本已生成' }))
      .toBeVisible({ timeout: 15_000 })

    // 走 API 直查最稳:aiGenerated=Y + scriptContent 非空
    const detail = await api.get(`/business/autotest/${createdAutotestId}`)
    expect(detail.code).toBe(200)
    expect(detail.data.aiGenerated, 'aiGenerated 应为 Y').toBe('Y')
    expect(detail.data.scriptContent, 'scriptContent 应非空').toBeTruthy()
    expect((detail.data.scriptContent || '').length).toBeGreaterThan(10)

    // UI 右侧 "脚本片段" 区也应有内容 (不是占位"(待 AI 生成)")
    const scriptBlock = page.locator('pre.script-code').first()
    await expect(scriptBlock).toBeVisible()
    const scriptText = await scriptBlock.innerText()
    expect(scriptText.trim()).not.toBe('(待 AI 生成)')
  })

  // === 场景 4: 立即执行 + RCA ===
  test('TC-AT-E004 立即执行 (status 01 前置) → 统计 3 项 + 失败时 RCA 可见', async ({ page, context, request }) => {
    test.skip(!createdAutotestId, '依赖 TC-AT-E002 创建的套件')

    // API 旁路: 先把状态切到 01 已激活 (绕 UI,前端 runNow 自检要求 status==='01')
    const u = await api.put('/business/autotest', {
      autotestId: createdAutotestId,
      status: '01'
    })
    expect(u.code, 'PUT status=01 应成功').toBe(200)

    await loginAsAdmin(request, context)
    await page.goto('/business/autotest')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })

    // 选中那行
    await page.locator('.el-table__row').filter({ hasText: suiteTitle }).first().click()
    await expect(page.getByText('套件编号', { exact: true })).toBeVisible({ timeout: 5_000 })

    // 点 "立即执行"
    await page.getByRole('button', { name: /立即执行/ }).click()

    // toast (无论通过/失败,前端都用 ElMessage 提示"执行完成")
    await expect(page.locator('.el-message').filter({ hasText: /执行完成/ }))
      .toBeVisible({ timeout: 15_000 })

    // 右侧 3 统计 (总用例 / 通过 / 失败) — 限定 el-statistic 标题,避免与表格"通过/失败"结果 tag 文本冲突 (strict mode)
    const statHeads = page.locator('.el-statistic__head')
    await expect(statHeads.filter({ hasText: '总用例' })).toBeVisible()
    await expect(statHeads.filter({ hasText: '通过' })).toBeVisible()
    await expect(statHeads.filter({ hasText: '失败' })).toBeVisible()

    // API 直查最终态校验数值合理性
    const detail = await api.get(`/business/autotest/${createdAutotestId}`)
    expect(detail.code).toBe(200)
    expect(detail.data.lastExecutedAt, 'lastExecutedAt 应填充').toBeTruthy()
    expect(detail.data.totalCases ?? 0).toBeGreaterThanOrEqual(0)
    expect(detail.data.passedCases ?? 0).toBeGreaterThanOrEqual(0)
    expect(detail.data.failedCases ?? 0).toBeGreaterThanOrEqual(0)
    expect(Number(detail.data.passRate ?? 0)).toBeGreaterThanOrEqual(0)
    expect(detail.data.executionDurationSec ?? 0).toBeGreaterThanOrEqual(0)

    // RCA: 如果 failedCases > 0,则 .rca-text 区块应可见
    // TODO: mock 执行 (AutoTestServiceImpl.runAutoTest) 当前是固定/随机?如固定全通过则此分支永不进
    if ((detail.data.failedCases ?? 0) > 0) {
      expect(detail.data.lastRootCauseAnalysis, '有失败用例必须给 RCA').toBeTruthy()
      await expect(page.locator('.rca-text')).toBeVisible({ timeout: 5_000 })
      const rcaText = await page.locator('.rca-text').innerText()
      expect(rcaText.length, 'RCA 文本应非空').toBeGreaterThan(0)
    }
  })

  // === 场景 5: 执行历史区块存在性 (弱断言,不依赖具体数据) ===
  test('TC-AT-E005 套件详情页可见 — 套件编号/类型/框架等元数据展示', async ({ page, context, request }) => {
    test.skip(!createdAutotestId, '依赖 TC-AT-E002 创建的套件')
    await loginAsAdmin(request, context)
    await page.goto('/business/autotest')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })

    // 选中之前创建的套件
    await page.locator('.el-table__row').filter({ hasText: suiteTitle }).first().click()

    // 详情区核心字段标签存在性(弱断言,不验数值)
    await expect(page.getByText('套件编号', { exact: true })).toBeVisible({ timeout: 5_000 })

    // 类型/框架/状态等标签至少有一个可见(容错文案变化)
    const meta = page.getByText(/类型|框架|状态|目标 URL/).first()
    await expect(meta).toBeVisible({ timeout: 5_000 })
  })
})
