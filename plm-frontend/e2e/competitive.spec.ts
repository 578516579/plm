/** Competitive 模块 E2E — PRD §F1.3 竞品情报 + 原型 competitive.html
 *
 * 覆盖 (对齐 inception.spec.ts 套路, 11 case):
 *   F001 创建竞品 (含价格档 + 监控订阅)
 *   F002 competitorName 必填 → 602
 *   F003 关联项目不存在 → 702
 *   F004 pricingTier 白名单非法 → 604
 *   F005 状态机正向 00→01→02
 *   F006 状态机反向 01→00 非法 → 601
 *   F007 状态机跳级 00→02 非法 → 601
 *   F008 终态保护 02→01 非法 → 601
 *   F009 AI 分析 → SWOT 四象限 + 综合报告 + aiGenerated=Y
 *   F010 编号格式 COMP-YYYY-NNNN
 *   ENC001 编码 HEX — 中文 competitor_name 不含 EFBFBD 替换符
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('Competitive 模块 E2E (PRD §F1.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`comp-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`comp-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_competitive', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Comp-F001 创建竞品 (含价格档 + 监控订阅)', async () => {
    const r = await api.post('/business/competitive', {
      projectId,
      competitorName: `禅道-${RUN_ID}`,
      vendor: '青岛易软天创',
      website: 'https://www.zentao.net',
      pricingModel: '社区版免费,企业版 2 万/年起',
      pricingTier: 'midrange',
      monitorEnabled: 'Y',
      monitorKeywords: 'AI,RAG,Dify',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-Comp-F002 competitorName 必填 → 602', async () => {
    const r = await api.post('/business/competitive', {
      projectId,
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/竞品名称/)
  })

  test('TC-Comp-F003 关联项目不存在 → 702', async () => {
    const r = await api.post('/business/competitive', {
      projectId: 99999999,
      competitorName: `LigaAI-${RUN_ID}-非法项目`,
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/关联项目不存在/)
  })

  test('TC-Comp-F004 pricingTier 白名单非法 → 604', async () => {
    const r = await api.post('/business/competitive', {
      projectId,
      competitorName: `LigaAI-${RUN_ID}-非法价格档`,
      pricingTier: 'premium_plus',
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/价格档/)
  })

  test('TC-Comp-F005 状态机正向 00→01→02 (草稿→已发布→已归档)', async () => {
    const created = await api.post('/business/competitive', {
      projectId,
      competitorName: `Jira-${RUN_ID}-状态机正向`,
      vendor: 'Atlassian',
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/competitive/list', {
      competitorName: `Jira-${RUN_ID}-状态机正向`
    })
    const id = list.rows[0].competitiveId

    let r = await api.put('/business/competitive', { competitiveId: id, status: '01' })
    expect(r.code).toBe(200)
    r = await api.put('/business/competitive', { competitiveId: id, status: '02' })
    expect(r.code).toBe(200)
    const got = await api.get(`/business/competitive/${id}`)
    expect(got.data.status).toBe('02')
  })

  test('TC-Comp-F006 状态机反向 01→00 非法 (无反向边) → 601', async () => {
    const created = await api.post('/business/competitive', {
      projectId,
      competitorName: `飞书项目-${RUN_ID}-反向边`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/competitive/list', {
      competitorName: `飞书项目-${RUN_ID}-反向边`
    })
    const id = list.rows[0].competitiveId
    await api.put('/business/competitive', { competitiveId: id, status: '01' })

    const r = await api.put('/business/competitive', { competitiveId: id, status: '00' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-Comp-F007 状态机跳级 00→02 非法 → 601', async () => {
    const created = await api.post('/business/competitive', {
      projectId,
      competitorName: `Trello-${RUN_ID}-跳级`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/competitive/list', {
      competitorName: `Trello-${RUN_ID}-跳级`
    })
    const id = list.rows[0].competitiveId

    const r = await api.put('/business/competitive', { competitiveId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-Comp-F008 终态保护 02→01 非法 → 601', async () => {
    const created = await api.post('/business/competitive', {
      projectId,
      competitorName: `Asana-${RUN_ID}-终态保护`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/competitive/list', {
      competitorName: `Asana-${RUN_ID}-终态保护`
    })
    const id = list.rows[0].competitiveId
    await api.put('/business/competitive', { competitiveId: id, status: '01' })
    await api.put('/business/competitive', { competitiveId: id, status: '02' })

    const r = await api.put('/business/competitive', { competitiveId: id, status: '01' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-Comp-F009 AI 分析 → SWOT 四象限 + 综合报告 + aiGenerated=Y', async () => {
    const created = await api.post('/business/competitive', {
      projectId,
      competitorName: `LigaAI-${RUN_ID}-AI分析`,
      vendor: 'LigaAI',
      website: 'https://ligai.cn',
      pricingTier: 'enterprise',
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/competitive/list', {
      competitorName: `LigaAI-${RUN_ID}-AI分析`
    })
    const id = list.rows[0].competitiveId

    const r = await api.post(`/business/competitive/ai/analyze/${id}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.strengths).toBeTruthy()
    expect(r.data.weaknesses).toBeTruthy()
    expect(r.data.opportunities).toBeTruthy()
    expect(r.data.threats).toBeTruthy()
    expect(r.data.aiAnalysisReport).toContain('竞品分析报告')
    expect(r.data.aiAnalysisReport).toContain('SWOT 矩阵')
    expect(r.data.aiAnalysisReport).toContain(`LigaAI-${RUN_ID}-AI分析`)
    expect(r.data.aiGeneratedAt).toBeTruthy()
  })

  test('TC-Comp-F010 编号格式 COMP-YYYY-NNNN', async () => {
    const created = await api.post('/business/competitive', {
      projectId,
      competitorName: `Monday-${RUN_ID}-编号格式`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/competitive/list', {
      competitorName: `Monday-${RUN_ID}-编号格式`
    })
    const year = new Date().getFullYear()
    expect(list.rows[0].competitiveNo).toMatch(new RegExp(`^COMP-${year}-\\d{4}$`))
  })

  test('TC-Comp-ENC001 编码 HEX — 中文 competitor_name 不含 EFBFBD 替换符', async () => {
    const cn = '禅道开源版-中文检测'
    const r = await api.post('/business/competitive', {
      projectId,
      competitorName: `${cn}-${RUN_ID}`,
      authorUserId: 1
    })
    expect(r.code).toBe(200)
    const hex = getFieldHex(
      'tb_competitive',
      'competitor_name',
      `competitor_name like '${cn}-${RUN_ID}%'`
    )
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })
})
