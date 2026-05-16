/**
 * Requirement 模块 E2E 测试
 *
 * 覆盖:
 * - TC-Req-F001: CRUD 全流程
 * - TC-Req-F005: 4×4 状态机 (含反向边 01→00 打回)
 * - TC-Req-F008: FK 702 — projectId 不存在
 * - TC-Req-F009: 新建非 00 状态 → 601
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import {
  RUN_ID,
  makeProjectData,
  makeRequirementData,
  REQUIREMENT_STATUS_TRANSITIONS,
  ERROR_CODES
} from './helpers/fixtures'

let token: string
let api: ApiClient
let apiRequest: APIRequestContext
let projectId: number

test.describe('Requirement 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)

    // 先建一个父项目作为 FK
    const proj = await api.createProject(makeProjectData(`req-suite-${RUN_ID}`))
    expect(proj.code).toBe(200)
    const list = await api.listProjects()
    projectId = list.rows.find((p: any) => p.projectName.includes(`req-suite-${RUN_ID}`))?.id
    expect(projectId).toBeDefined()
  })

  test.afterAll(async () => {
    // 清理 — Suite 级 teardown
    if (projectId) {
      execDelete('tb_requirement', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Req-F001 创建 + 列表 + 删除全流程', async () => {
    const data = makeRequirementData(projectId, `crud-${RUN_ID}`)
    const create = await api.createRequirement(data)
    expect(create.code).toBe(200)

    const list = await api.listRequirements()
    const created = list.rows.find((r: any) => r.title === data.title)
    expect(created).toBeDefined()
    expect(created.requirementNo).toMatch(/^REQ-\d{4}-\d{4}$/) // ADR-0002
    expect(created.status).toBe('00')

    // 删除
    const del = await api.deleteRequirement(created.requirementId)
    expect(del.code).toBe(200)
  })

  test('TC-Req-F005 状态机合法转换 (含反向边 01→00)', async () => {
    const data = makeRequirementData(projectId, `state-${RUN_ID}`)
    const create = await api.createRequirement(data)
    expect(create.code).toBe(200)
    const list = await api.listRequirements()
    const r = list.rows.find((x: any) => x.title === data.title)
    const id = r.requirementId

    // 00 → 01
    let resp = await api.updateRequirement({ requirementId: id, status: '01' })
    expect(resp.code).toBe(200)
    // 01 → 00 (反向边: 打回评审)
    resp = await api.updateRequirement({ requirementId: id, status: '00' })
    expect(resp.code, '反向边 01→00 应允许 (打回)').toBe(200)
    // 00 → 03
    resp = await api.updateRequirement({ requirementId: id, status: '03' })
    expect(resp.code).toBe(200)
    // 03 终态保护:不能回 00
    resp = await api.updateRequirement({ requirementId: id, status: '00' })
    expect(resp.code).toBe(ERROR_CODES.STATUS_VIOLATION)

    execDelete('tb_requirement', `requirement_id=${id}`)
  })

  test('TC-Req-F005 状态机非法转换全覆盖', async () => {
    for (const tc of REQUIREMENT_STATUS_TRANSITIONS.illegal) {
      const data = makeRequirementData(projectId, `illegal-${tc.from}${tc.to}-${RUN_ID}`)
      const c = await api.createRequirement(data)
      const list = await api.listRequirements()
      const r = list.rows.find((x: any) => x.title === data.title)
      const id = r.requirementId

      // 推到 from 状态 (若 from != 00 需要中间步骤)
      if (tc.from === '02') {
        // 推 00 → 01 → 02
        await api.updateRequirement({ requirementId: id, status: '01' })
        await api.updateRequirement({ requirementId: id, status: '02' })
      } else if (tc.from === '03') {
        await api.updateRequirement({ requirementId: id, status: '03' })
      } else if (tc.from === '01') {
        await api.updateRequirement({ requirementId: id, status: '01' })
      }

      // 然后尝试非法转换
      const resp = await api.updateRequirement({ requirementId: id, status: tc.to })
      expect.soft(resp.code, `${tc.name} 应被拒绝 (601)`).toBe(ERROR_CODES.STATUS_VIOLATION)

      execDelete('tb_requirement', `requirement_id=${id}`)
    }
  })

  test('TC-Req-F008 FK projectId 不存在 → 702', async () => {
    const resp = await api.createRequirement({
      projectId: 99999,
      title: `FK 测试-${RUN_ID}`,
      source: '01',
      priority: '02'
    })
    expect(resp.code).toBe(ERROR_CODES.FK_NOT_EXISTS)
    expect(resp.msg).toContain('关联项目不存在')
  })

  test('TC-Req-F009 新建状态必须为 00', async () => {
    const resp = await api.createRequirement({
      projectId,
      title: `新建状态-${RUN_ID}`,
      source: '01',
      priority: '02',
      status: '01' // 非 00
    })
    expect(resp.code).toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('UI 层: 需求管理菜单可访问且表单可填', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
    await page.goto('/business/requirement')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })

    // 新增按钮点开
    await page.getByRole('button', { name: /新增/ }).first().click()
    const dialog = page.locator('.el-dialog')
    await expect(dialog).toBeVisible()
    await expect(dialog.getByLabel(/需求标题/)).toBeVisible()
    await dialog.getByRole('button', { name: /取\s*消/ }).click()
  })
})
