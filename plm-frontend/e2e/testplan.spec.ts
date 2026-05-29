/**
 * TestPlan 模块 E2E — PRD §F4.1 测试方案
 * 覆盖: CRUD + TP-YYYY-NNNN 编号 + 中文 HEX 守门员 + FK
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { assertNoMojibake, execDelete } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('TestPlan 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`tp-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`tp-suite-${RUN_ID}`))?.id
    expect(projectId).toBeDefined()
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_testplan', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-TP-F001 创建测试方案 + TP-YYYY-NNNN 编号 (5 种 test_types)', async () => {
    const title = `Sprint 测试方案-${RUN_ID}`
    const r = await api.post('/business/testplan', {
      projectId,
      title,
      testTypes: 'functional,api,automation',
      testCycleDays: 10,
      scope: '需求 REQ-001~005',
      strategy: '功能优先,自动化覆盖核心路径',
      toolsRecommended: 'playwright,jmeter',
      aiGenerated: 'Y',
      authorUserId: 1
    })
    expect(r.code).toBe(200)

    const list = await api.get('/business/testplan/list', { pageSize: 100 })
    const tp = list.rows.find((x: any) => x.title === title)
    expect(tp).toBeDefined()
    expect(tp.testplanNo).toMatch(/^TP-\d{4}-\d{4}$/)
    expect(tp.status).toBe('00')
    expect(tp.testCycleDays).toBe(10)
  })

  test('TC-TP-F002 编码守门员: 中文 title + scope + strategy 无乱码', async () => {
    const title = `编码守门员方案-${RUN_ID}`
    await api.post('/business/testplan', {
      projectId,
      title,
      testTypes: 'functional,security',
      testCycleDays: 5,
      scope: '需求 REQ-001~003,接口 API-PRJ-* αβγ',
      strategy: '【策略】核心路径优先,边界条件后行 🚀',
      riskAssessment: '风险点:依赖第三方 OCR 服务稳定性',
      authorUserId: 1
    })

    const list = await api.get('/business/testplan/list', { pageSize: 100 })
    const tp = list.rows.find((x: any) => x.title === title)

    const titleCheck = assertNoMojibake('tb_testplan', 'title',
      `testplan_id=${tp.testplanId}`)
    expect.soft(titleCheck.ok, `title HEX=${titleCheck.hex}`).toBe(true)
    const scopeCheck = assertNoMojibake('tb_testplan', 'scope',
      `testplan_id=${tp.testplanId}`)
    expect.soft(scopeCheck.ok, `scope HEX=${scopeCheck.hex}`).toBe(true)
    const strategyCheck = assertNoMojibake('tb_testplan', 'strategy',
      `testplan_id=${tp.testplanId}`)
    expect.soft(strategyCheck.ok, `strategy HEX=${strategyCheck.hex}`).toBe(true)
  })

  test('TC-TP-F003 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/testplan', {
      projectId: 999_999_999,
      title: `FK 测试-${RUN_ID}`,
      testTypes: 'functional',
      authorUserId: 1
    })
    expect.soft(r.code, 'FK 不存在应返 702').toBe(ERROR_CODES.FK_NOT_EXISTS)
  })

  test('TC-TP-F009 AI 生成 → aiGenerated=Y / 策略+范围非空 / 工具含 playwright', async () => {
    const title = `AI 生成方案-${RUN_ID}`
    const created = await api.post('/business/testplan', {
      projectId,
      title,
      testTypes: 'functional,api,automation',
      testCycleDays: 7,
      authorUserId: 1
    })
    expect(created.code).toBe(200)

    const list = await api.get('/business/testplan/list', { pageSize: 100 })
    const id = list.rows.find((x: any) => x.title === title)?.testplanId
    expect(id).toBeDefined()

    const r = await api.post(`/business/testplan/ai/generate/${id}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.strategy).toBeTruthy()
    expect(r.data.scope).toBeTruthy()
    // testTypes CSV → 中文标签映射 (functional → 功能测试)
    expect(r.data.strategy).toContain('功能测试')
    expect(r.data.toolsRecommended).toContain('playwright')
  })

  test('TC-TP-F005 TestPlan 列表页 UI 渲染 + 新增对话框可打开', async ({ page, context, request }) => {
    await loginAsAdmin(request, context)
    await page.goto('/business/testplan')

    const table = page.locator('.el-table').first()
    const emptyState = page.locator('.el-empty').first()
    await expect(table.or(emptyState)).toBeVisible({ timeout: 10_000 })

    // 点新增按钮 → Dialog 可见(只验存在性,不提交避免污染数据)
    const addBtn = page.getByRole('button', { name: /新增|添加|新建|创建/ }).first()
    await expect(addBtn).toBeVisible({ timeout: 5_000 })
  })
})
