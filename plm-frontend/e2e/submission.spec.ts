/**
 * Submission 模块 E2E — PRD §F4.4 + 原型 submit.html 对齐版
 * AI 质量门禁: 单测覆盖率 ≥60% + 代码扫描通过 + PRD 完整 + API 文档更新
 * 5×5 状态机: 00 草稿 → 01 已提交 → 02 质量门禁中 → 03 已通过 / 04 已退回 (反向边 04→00)
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

test.describe('Submission 模块 E2E (PRD §F4.4)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`sub-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`sub-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_submission', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Sub-F001 CRUD + 提测单创建', async () => {
    const r = await api.post('/business/submission', {
      projectId,
      title: `Sprint 26W21 提测-${RUN_ID}`,
      scope: '需求 REQ-001/REQ-002,任务 TASK-001~005',
      environment: 'staging',
      expectedTestDays: 7,
      riskNotes: '依赖 Redis 升级,需要先回归缓存模块',
      submitterUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-Sub-F002 AI 质量门禁 4 项全 Y 才通过', async () => {
    const r = await api.post('/business/submission', {
      projectId,
      title: `质量门禁测试-${RUN_ID}`,
      submitterUserId: 1
    })
    expect(r.code).toBe(200)
    const list = await api.get('/business/submission/list', { pageSize: 100 })
    const s = list.rows.find((x: any) => x.title === `质量门禁测试-${RUN_ID}`)

    // 只填 3 项 → quality_gate_passed=N
    await api.put('/business/submission', {
      submissionId: s.submissionId,
      unitTestCoverage: 65.5, codeScanPassed: 'Y', prdCompleted: 'Y', apiDocUpdated: 'N'
    })
    let detail = await api.get(`/business/submission/${s.submissionId}`)
    expect(detail.data.qualityGatePassed).toBe('N')

    // 补齐第 4 项 → 通过
    await api.put('/business/submission', {
      submissionId: s.submissionId, apiDocUpdated: 'Y'
    })
    detail = await api.get(`/business/submission/${s.submissionId}`)
    expect(detail.data.qualityGatePassed).toBe('Y')
  })

  test('TC-Sub-F003 单测覆盖率 < 60% 不通过门禁', async () => {
    const r = await api.post('/business/submission', {
      projectId, title: `低覆盖率-${RUN_ID}`, submitterUserId: 1
    })
    const list = await api.get('/business/submission/list', { pageSize: 100 })
    const s = list.rows.find((x: any) => x.title === `低覆盖率-${RUN_ID}`)

    await api.put('/business/submission', {
      submissionId: s.submissionId,
      unitTestCoverage: 55.0, codeScanPassed: 'Y', prdCompleted: 'Y', apiDocUpdated: 'Y'
    })
    const detail = await api.get(`/business/submission/${s.submissionId}`)
    expect(detail.data.qualityGatePassed).toBe('N')
  })

  test('TC-Sub-F004 状态机 + 708 (进入 03 必须门禁通过)', async () => {
    const r = await api.post('/business/submission', {
      projectId, title: `状态机-${RUN_ID}`, submitterUserId: 1
    })
    const list = await api.get('/business/submission/list', { pageSize: 100 })
    const id = list.rows.find((x: any) => x.title === `状态机-${RUN_ID}`).submissionId

    let u = await api.put('/business/submission', { submissionId: id, status: '01' })
    expect(u.code).toBe(200)
    let detail = await api.get(`/business/submission/${id}`)
    expect(detail.data.submittedAt).toBeTruthy()

    await api.put('/business/submission', { submissionId: id, status: '02' })

    // 02 → 03 但门禁未通过 → 708
    u = await api.put('/business/submission', { submissionId: id, status: '03' })
    expect(u.code).toBe(708)

    // 填齐门禁后再标 → OK
    await api.put('/business/submission', {
      submissionId: id, unitTestCoverage: 75.0, codeScanPassed: 'Y', prdCompleted: 'Y', apiDocUpdated: 'Y'
    })
    u = await api.put('/business/submission', { submissionId: id, status: '03' })
    expect(u.code).toBe(200)
  })

  test('TC-Sub-F005 退回必填原因 + 反向边 04→00', async () => {
    const r = await api.post('/business/submission', {
      projectId, title: `退回-${RUN_ID}`, submitterUserId: 1
    })
    const list = await api.get('/business/submission/list', { pageSize: 100 })
    const id = list.rows.find((x: any) => x.title === `退回-${RUN_ID}`).submissionId

    await api.put('/business/submission', { submissionId: id, status: '01' })

    // 不带原因 → 602
    let u = await api.put('/business/submission', { submissionId: id, status: '04' })
    expect(u.code).toBe(602)

    // 带原因 → OK
    u = await api.put('/business/submission', {
      submissionId: id, status: '04', rejectReason: '单测不达标'
    })
    expect(u.code).toBe(200)

    // 反向边 04→00 → OK
    u = await api.put('/business/submission', { submissionId: id, status: '00' })
    expect(u.code).toBe(200)
  })

  test('TC-Sub-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const title = `删除测试-${RUN_ID}`
    const createResp = await api.post('/business/submission', {
      projectId,
      title,
      submitterUserId: 1
    })
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.get('/business/submission/list', { pageSize: 100 })
    const created = before.rows.find((x: any) => x.title === title)
    expect(created, '新建 submission 应能在列表里查到').toBeDefined()
    const id: number = created.submissionId
    expect(typeof id, 'submissionId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/submission/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/submission/list', { pageSize: 100 })
    const stillThere = after.rows.find((x: any) => x.submissionId === id)
    expect(stillThere, `submissionId=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
