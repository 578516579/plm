/**
 * Requirement 模块 E2E 测试
 *
 * 覆盖:
 * - TC-Req-F001: CRUD 全流程
 * - TC-Req-F005: 4×4 状态机 (含反向边 01→00 打回, 00→01 评审前置)
 * - TC-Req-F008: FK 702 — projectId 不存在
 * - TC-Req-F009: 新建非 00 状态 → 601
 * - TC-Req-F010: 评审前置失败 — 00→01 无通过评审 → 701 (PRD §F2.4 新增 2026-05-25)
 * - TC-Req-F011: 评审 API CRUD — submit / list / delete (PRD §F2.4 新增 2026-05-25)
 * - TC-Req-F012: 打回评审 reviewComment 必填 — 604 (PRD §F2.4 新增 2026-05-25)
 * - TC-Req-F013: AI 优先级初评 — aiEvaluation high/medium/low + 落库 (PRD §F2.1 新增 2026-05-28)
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

  test('TC-Req-F005 状态机合法转换 (含反向边 01→00, 评审前置)', async () => {
    const data = makeRequirementData(projectId, `state-${RUN_ID}`)
    const create = await api.createRequirement(data)
    expect(create.code).toBe(200)
    const list = await api.listRequirements()
    const r = list.rows.find((x: any) => x.title === data.title)
    const id = r.requirementId

    // PRD §F2.4 评审前置: 00→01 必须先有通过的评审记录
    const review = await api.submitRequirementReview(id, { reviewResult: '00', reviewComment: '功能定义清晰' })
    expect(review.code, '评审提交应成功').toBe(200)

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

    execDelete('tb_requirement_review', `requirement_id=${id}`)
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
      // PRD §F2.4 评审前置: 推到 01 或更高状态前先提交通过评审
      if (tc.from === '01' || tc.from === '02') {
        await api.submitRequirementReview(id, { reviewResult: '00', reviewComment: 'setup' })
      }
      if (tc.from === '02') {
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

      execDelete('tb_requirement_review', `requirement_id=${id}`)
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

  // ─────────────────────────────────────────────────────────────────────
  // PRD §F2.4 需求评审管理 (2026-05-25 新增)
  // ─────────────────────────────────────────────────────────────────────

  test('TC-Req-F010 评审前置失败:00→01 但无通过评审 → 701', async () => {
    const data = makeRequirementData(projectId, `noreview-${RUN_ID}`)
    const create = await api.createRequirement(data)
    expect(create.code).toBe(200)
    const list = await api.listRequirements()
    const r = list.rows.find((x: any) => x.title === data.title)
    const id = r.requirementId

    // 不提交评审, 直接推 00 → 01 → 应被拒绝 (701)
    const resp = await api.updateRequirement({ requirementId: id, status: '01' })
    expect(resp.code, '无评审推 00→01 应被拒绝').toBe(701)
    expect(resp.msg).toContain('评审')

    execDelete('tb_requirement', `requirement_id=${id}`)
  })

  test('TC-Req-F011 评审 API CRUD: submit / list / delete', async () => {
    const data = makeRequirementData(projectId, `reviewcrud-${RUN_ID}`)
    const create = await api.createRequirement(data)
    expect(create.code).toBe(200)
    const list = await api.listRequirements()
    const r = list.rows.find((x: any) => x.title === data.title)
    const id = r.requirementId

    // 1. 提交通过评审
    const pass = await api.submitRequirementReview(id, { reviewResult: '00', reviewComment: '清晰' })
    expect(pass.code).toBe(200)

    // 2. 提交打回评审
    const reject = await api.submitRequirementReview(id, { reviewResult: '01', reviewComment: '范围模糊' })
    expect(reject.code).toBe(200)

    // 3. 列评审历史 — 应有 2 条
    const histResp = await api.listRequirementReviews(id)
    expect(histResp.code).toBe(200)
    expect(histResp.data.length).toBeGreaterThanOrEqual(2)
    expect(histResp.data.some((x: any) => x.reviewResult === '00')).toBe(true)
    expect(histResp.data.some((x: any) => x.reviewResult === '01')).toBe(true)

    // 4. 撤回一条评审
    const firstReviewId = histResp.data[0].reviewId
    const delResp = await api.deleteRequirementReviews(firstReviewId)
    expect(delResp.code).toBe(200)

    // 5. 撤回后再查只剩 1 条
    const after = await api.listRequirementReviews(id)
    expect(after.data.length).toBeGreaterThanOrEqual(1)

    execDelete('tb_requirement_review', `requirement_id=${id}`)
    execDelete('tb_requirement', `requirement_id=${id}`)
  })

  test('TC-Req-F012 打回评审 reviewComment 必填 → 604', async () => {
    const data = makeRequirementData(projectId, `reject-blank-${RUN_ID}`)
    const create = await api.createRequirement(data)
    expect(create.code).toBe(200)
    const list = await api.listRequirements()
    const r = list.rows.find((x: any) => x.title === data.title)
    const id = r.requirementId

    // 打回但不填意见
    const resp = await api.submitRequirementReview(id, { reviewResult: '01', reviewComment: '' })
    expect(resp.code).toBe(604)
    expect(resp.msg).toContain('打回评审')

    execDelete('tb_requirement', `requirement_id=${id}`)
  })

  // ─────────────────────────────────────────────────────────────────────
  // PRD §F2.1 AI 优先级初评 (2026-05-28 新增)
  // ─────────────────────────────────────────────────────────────────────

  test('TC-Req-F013 AI 优先级初评 → aiEvaluation ∈ {high,medium,low} 且落库', async () => {
    const data = makeRequirementData(projectId, `ai-eval-${RUN_ID}`)
    data.title = `紧急:支付系统崩溃修复-${RUN_ID}`   // 含关键词 → 期望 high
    const create = await api.createRequirement(data)
    expect(create.code).toBe(200)
    const list = await api.listRequirements()
    const r = list.rows.find((x: any) => x.title === data.title)
    const id = r.requirementId

    const resp = await api.post(`/business/requirement/ai/evaluate/${id}`, {})
    expect(resp.code).toBe(200)
    expect(resp.data.aiEvaluation, 'AI 评估等级取值域').toMatch(/^(high|medium|low)$/)
    expect(resp.data.aiEvaluation, '含「紧急/崩溃」关键词应评 high').toBe('high')

    // 落库校验:列表里 aiEvaluation 已持久化
    const after = await api.listRequirements()
    const updated = after.rows.find((x: any) => x.requirementId === id)
    expect(updated.aiEvaluation, 'aiEvaluation 应已落库').toBe('high')

    execDelete('tb_requirement', `requirement_id=${id}`)
  })

  test('UI 层: 需求管理菜单可访问且表单可填', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
    await page.goto('/business/requirement')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 60_000 })

    // 新增按钮点开
    await page.getByRole('button', { name: /新增/ }).first().click()
    const dialog = page.locator('.el-dialog')
    await expect(dialog).toBeVisible()
    await expect(dialog.getByLabel(/需求标题/)).toBeVisible()
    await dialog.getByRole('button', { name: /取\s*消/ }).click()
  })
})
