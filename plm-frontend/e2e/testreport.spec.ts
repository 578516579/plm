/**
 * TestReport 模块 E2E — PRD §F4.7 测试报告
 * 覆盖: CRUD + TR-YYYY-NNNN 编号 + risk_level 白名单 + 中文 HEX 守门员 + FK
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { assertNoMojibake, execDelete } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('TestReport 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`tr-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`tr-suite-${RUN_ID}`))?.id
    expect(projectId).toBeDefined()
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_testreport', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-TR-F001 创建测试报告 + TR-YYYY-NNNN 编号 (黄灯风险)', async () => {
    const title = `Sprint 测试报告-${RUN_ID}`
    const r = await api.post('/business/testreport', {
      projectId,
      title,
      totalCases: 120,
      passedCases: 115,
      failedCases: 5,
      coverageRate: 92.5,
      p0Defects: 0,
      p1Defects: 2,
      p2Defects: 3,
      riskLevel: 'yellow',
      riskEvaluation: '2 P1 缺陷待修复',
      recommendations: '上线 24h 监控',
      aiGenerated: 'Y'
    })
    expect(r.code).toBe(200)

    const list = await api.get('/business/testreport/list', { pageSize: 100 })
    const tr = list.rows.find((x: any) => x.title === title)
    expect(tr).toBeDefined()
    expect(tr.testreportNo).toMatch(/^TR-\d{4}-\d{4}$/)
    expect(tr.status).toBe('00')
    expect(tr.riskLevel).toBe('yellow')
  })

  test('TC-TR-F002 编码守门员: 中文 riskEvaluation + recommendations 无乱码', async () => {
    const title = `编码守门员报告-${RUN_ID}`
    await api.post('/business/testreport', {
      projectId,
      title,
      totalCases: 50,
      passedCases: 50,
      failedCases: 0,
      coverageRate: 88.0,
      riskLevel: 'green',
      riskEvaluation: '【低风险】所有 P0/P1 缺陷已修复 αβγ ✓',
      recommendations: '建议:① 增强埋点 ② 完善文档 🎯',
      aiGenerated: 'Y'
    })
    const list = await api.get('/business/testreport/list', { pageSize: 100 })
    const tr = list.rows.find((x: any) => x.title === title)

    const titleCheck = assertNoMojibake('tb_testreport', 'title',
      `testreport_id=${tr.testreportId}`)
    expect.soft(titleCheck.ok, `title HEX=${titleCheck.hex}`).toBe(true)
    const evalCheck = assertNoMojibake('tb_testreport', 'risk_evaluation',
      `testreport_id=${tr.testreportId}`)
    expect.soft(evalCheck.ok, `risk_evaluation HEX=${evalCheck.hex}`).toBe(true)
    const recCheck = assertNoMojibake('tb_testreport', 'recommendations',
      `testreport_id=${tr.testreportId}`)
    expect.soft(recCheck.ok, `recommendations HEX=${recCheck.hex}`).toBe(true)
  })

  test('TC-TR-F003 风险级别字典白名单 (green/yellow/red)', async () => {
    const r = await api.post('/business/testreport', {
      projectId,
      title: `非法风险级别-${RUN_ID}`,
      totalCases: 10,
      passedCases: 10,
      coverageRate: 100,
      riskLevel: 'rainbow',
      aiGenerated: 'N'
    })
    expect.soft(r.code, '非字典值 riskLevel=rainbow 应被拒').not.toBe(200)
  })

  test('TC-TR-F004 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/testreport', {
      projectId: 999_999_999,
      title: `FK 测试-${RUN_ID}`,
      totalCases: 1,
      passedCases: 1,
      riskLevel: 'green',
      aiGenerated: 'N'
    })
    expect.soft(r.code, 'FK 不存在应返 702').toBe(ERROR_CODES.FK_NOT_EXISTS)
  })

  // === 状态机覆盖 — 3 状态 + 反向边 (Phase 04 Gate B.2) ===
  // 00 草稿 → {01 审核中}
  // 01 审核中 → {00 草稿(打回), 02 已发布}
  // 02 已发布 (终态)

  async function createReport(suffix: string, riskLevel = 'green') {
    const title = `状态机-${suffix}-${RUN_ID}`
    await api.post('/business/testreport', {
      projectId, title, totalCases: 10, passedCases: 10, riskLevel, aiGenerated: 'N'
    })
    const list = await api.get('/business/testreport/list', { pageSize: 100 })
    return list.rows.find((x: any) => x.title === title)
  }

  test('TC-TR-F005 状态机正向 00→01→02 (草稿→审核中→已发布)', async () => {
    const tr = await createReport('happy')
    expect(tr.status).toBe('00')

    expect((await api.put('/business/testreport', { testreportId: tr.testreportId, status: '01' })).code).toBe(200)
    expect((await api.put('/business/testreport', { testreportId: tr.testreportId, status: '02' })).code).toBe(200)

    const after = await api.get(`/business/testreport/${tr.testreportId}`)
    expect(after.data.status).toBe('02')
  })

  test('TC-TR-F006 反向边 01→00 合法 (审核打回)', async () => {
    const tr = await createReport('reject')
    await api.put('/business/testreport', { testreportId: tr.testreportId, status: '01' })

    const r = await api.put('/business/testreport', { testreportId: tr.testreportId, status: '00' })
    expect(r.code).toBe(200)
  })

  test('TC-TR-F007 跳级 00→02 非法 → 601', async () => {
    const tr = await createReport('skip')
    const r = await api.put('/business/testreport', { testreportId: tr.testreportId, status: '02' })
    expect.soft(r.code, '00→02 必须先经审核 01').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-TR-F008 终态保护 02→01 非法 → 601', async () => {
    const tr = await createReport('final')
    await api.put('/business/testreport', { testreportId: tr.testreportId, status: '01' })
    await api.put('/business/testreport', { testreportId: tr.testreportId, status: '02' })

    const r = await api.put('/business/testreport', { testreportId: tr.testreportId, status: '01' })
    expect.soft(r.code, '02 已发布是终态').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-TR-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const title = `删除测试-${RUN_ID}`
    const createResp = await api.post('/business/testreport', {
      projectId,
      title,
      totalCases: 10,
      passedCases: 10,
      coverageRate: 100,
      riskLevel: 'green',
      aiGenerated: 'N'
    })
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.get('/business/testreport/list', { pageSize: 100 })
    const created = before.rows.find((x: any) => x.title === title)
    expect(created, '新建 testreport 应能在列表里查到').toBeDefined()
    const id: number = created.testreportId
    expect(typeof id, 'testreportId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/testreport/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/testreport/list', { pageSize: 100 })
    const stillThere = after.rows.find((x: any) => x.testreportId === id)
    expect(stillThere, `testreportId=${id} 删除后不该出现在 list`).toBeUndefined()
  })

  test('TC-TR-F009 红灯风险 + P0 缺陷数据完整性', async () => {
    const title = `红灯报告-${RUN_ID}`
    const r = await api.post('/business/testreport', {
      projectId,
      title,
      totalCases: 200,
      passedCases: 150,
      failedCases: 50,
      coverageRate: 75.0,
      p0Defects: 3,
      p1Defects: 8,
      p2Defects: 12,
      riskLevel: 'red',
      riskEvaluation: '【高风险】P0 缺陷未清零,不建议上线',
      recommendations: '修复 3 个 P0 后重测,延后发布',
      aiGenerated: 'N'
    })
    expect(r.code).toBe(200)
    const list = await api.get('/business/testreport/list', { pageSize: 100 })
    const tr = list.rows.find((x: any) => x.title === title)
    expect(tr.riskLevel).toBe('red')
    expect(tr.p0Defects).toBe(3)
  })
})
