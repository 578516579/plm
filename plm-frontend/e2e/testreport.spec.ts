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
})
