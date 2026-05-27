/**
 * TestCase 模块 E2E — 5×5 状态机含反向边 03/04→01 (重测) + /execute 端点 + 706
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'
import { makeTestCaseData, TESTCASE_STATUS_TRANSITIONS } from './helpers/fixtures-testcase'

let token: string
let api: ApiClient
let apiRequest: APIRequestContext
let projectId: number

test.describe('TestCase 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)

    await api.createProject(makeProjectData(`tc-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`tc-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_testcase', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-TestCase-F001 CRUD + ADR-0006 TC-YYYY-NNNN', async () => {
    const data = makeTestCaseData(projectId, undefined, `crud-${RUN_ID}`)
    const c = await api.post('/business/testcase', data)
    expect(c.code).toBe(200)
    const list = await api.get('/business/testcase/list', { pageSize: 100 })
    const t = list.rows.find((x: any) => x.title === data.title)
    expect(t.testcaseNo).toMatch(/^TC-\d{4}-\d{4}$/)
    expect(t.status).toBe('00')
    expect(t.isAutomated).toBe('Y')
    expect(t.executionCount).toBe(0)
  })

  test('TC-TestCase-F002 706 自动化用例必填脚本路径', async () => {
    const r = await api.post('/business/testcase', {
      ...makeTestCaseData(projectId, undefined, `706-${RUN_ID}`),
      isAutomated: 'Y',
      automationScriptPath: undefined
    })
    expect(r.code).toBe(706)
    expect(r.msg).toContain('脚本')
  })

  test('TC-TestCase-F003 反向边 03→01 + 04→01 重测', async () => {
    const data = makeTestCaseData(projectId, undefined, `reverse-${RUN_ID}`)
    const c = await api.post('/business/testcase', data)
    const list = await api.get('/business/testcase/list', { pageSize: 100 })
    const t = list.rows.find((x: any) => x.title === data.title)
    const id = t.testcaseId

    // 00 → 01 → 02 → 03
    await api.put('/business/testcase', { testcaseId: id, status: '01' })
    await api.put('/business/testcase', { testcaseId: id, status: '02' })
    await api.put('/business/testcase', { testcaseId: id, status: '03' })

    // 03 → 01 (反向边·重测)
    let r = await api.put('/business/testcase', { testcaseId: id, status: '01' })
    expect(r.code, '反向边 03→01 (重测) 应允许').toBe(200)

    // 再推 01 → 02 → 04
    await api.put('/business/testcase', { testcaseId: id, status: '02' })
    await api.put('/business/testcase', { testcaseId: id, status: '04' })

    // 04 → 01 (反向边·重测)
    r = await api.put('/business/testcase', { testcaseId: id, status: '01' })
    expect(r.code, '反向边 04→01 (重测) 应允许').toBe(200)
  })

  test('TC-TestCase-F004 /execute 端点 + execution_count + last_executed_at', async () => {
    const data = makeTestCaseData(projectId, undefined, `exec-${RUN_ID}`)
    await api.post('/business/testcase', data)
    const list = await api.get('/business/testcase/list', { pageSize: 100 })
    const t = list.rows.find((x: any) => x.title === data.title)
    const id = t.testcaseId

    // 必须先推到 02 才能 execute
    let r = await api.post(`/business/testcase/${id}/execute`, { status: '03', actualResult: '通过' })
    expect(r.code, '当前 status=00,不应允许 execute').toBe(601)

    // 推到 02
    await api.put('/business/testcase', { testcaseId: id, status: '01' })
    await api.put('/business/testcase', { testcaseId: id, status: '02' })

    // 现在 execute → 通过
    r = await api.post(`/business/testcase/${id}/execute`, { status: '03', actualResult: '执行 1 通过' })
    expect(r.code).toBe(200)

    // 校验 execution_count=1 + last_executed_at 已填
    const detail1 = await api.get(`/business/testcase/${id}`)
    expect(detail1.data.executionCount).toBe(1)
    expect(detail1.data.lastExecutedAt).toBeTruthy()
    expect(detail1.data.actualResult).toContain('执行 1')
    expect(detail1.data.status).toBe('03')

    // 重测: 03 → 01 → 02 → execute(04 失败)
    await api.put('/business/testcase', { testcaseId: id, status: '01' })
    await api.put('/business/testcase', { testcaseId: id, status: '02' })
    r = await api.post(`/business/testcase/${id}/execute`, { status: '04', actualResult: '执行 2 失败' })
    expect(r.code).toBe(200)
    const detail2 = await api.get(`/business/testcase/${id}`)
    expect(detail2.data.executionCount, '第二次执行后 count = 2').toBe(2)
    expect(detail2.data.status).toBe('04')
    expect(detail2.data.actualResult).toContain('执行 2')
  })

  test('TC-TestCase-F005 /execute 不能直接传非 03/04 状态', async () => {
    const data = makeTestCaseData(projectId, undefined, `bad-exec-${RUN_ID}`)
    await api.post('/business/testcase', data)
    const list = await api.get('/business/testcase/list', { pageSize: 100 })
    const t = list.rows.find((x: any) => x.title === data.title)

    await api.put('/business/testcase', { testcaseId: t.testcaseId, status: '01' })
    await api.put('/business/testcase', { testcaseId: t.testcaseId, status: '02' })

    const r = await api.post(`/business/testcase/${t.testcaseId}/execute`, { status: '01' })
    expect(r.code).toBe(604)
  })

  test('TC-TestCase-F006 非法转换全覆盖', async () => {
    for (const tc of TESTCASE_STATUS_TRANSITIONS.illegal) {
      const data = makeTestCaseData(projectId, undefined, `illegal-${tc.from}${tc.to}-${RUN_ID}`)
      await api.post('/business/testcase', data)
      const list = await api.get('/business/testcase/list', { pageSize: 100 })
      const t = list.rows.find((x: any) => x.title === data.title)
      const id = t.testcaseId

      // 推到 from 状态
      if (tc.from === '01') {
        await api.put('/business/testcase', { testcaseId: id, status: '01' })
      }
      // 非法转换
      const r = await api.put('/business/testcase', { testcaseId: id, status: tc.to })
      expect.soft(r.code, `${tc.name} 应被拒 (601)`).toBe(ERROR_CODES.STATUS_VIOLATION)
    }
  })

  test('TC-TestCase-F007 必填字段校验', async () => {
    // steps 缺
    let r = await api.post('/business/testcase', {
      projectId, title: `no-steps-${RUN_ID}`, expectedResult: 'OK'
    })
    expect(r.code).toBe(602)
    // expectedResult 缺
    r = await api.post('/business/testcase', {
      projectId, title: `no-expected-${RUN_ID}`, steps: '1. 步骤'
    })
    expect(r.code).toBe(602)
  })

  test('UI 层: 测试用例管理菜单可访问', async ({ page, context, request }) => {
    // 用 loginAsAdmin 给 fresh context 注入完整 cookie + 触发 fresh login
    // (vue-router 动态路由场景下,旧 context.addCookies 拿不到菜单 → /business/testcase 404)
    await loginAsAdmin(request, context)
    await page.goto('/business/testcase')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })
    // 验证表格列名含"是否自动化"(用例独有),用更精确的 selector 避开侧边菜单同名项
    await expect(page.locator('.el-table .el-table__header').getByText(/自动化/).first()).toBeVisible()
  })
})
