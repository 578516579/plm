/** Inception 模块 E2E — PRD §F1.1 立项 AI 助手 + 原型 inception.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('Inception 模块 E2E (PRD §F1.1)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_inception', `project_name like 'E2E 立项-${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  test('TC-Inc-F001 创建立项 (含业务线 + 类型 + 背景诉求)', async () => {
    const r = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-病虫害识别`,
      businessLine: 'plant_protection',
      inceptionType: 'new_product',
      background: '基于 AI 视觉识别技术,实现拍照识别病虫害种类。原型 inception.html L147 同款诉求。',
      estimatedDurationMonths: 6,
      estimatedTeam: '产品×1 前端×2 后端×3 测试×2 AI×2',
      submitterUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-Inc-F002 projectName 必填 → 602', async () => {
    const r = await api.post('/business/inception', {
      businessLine: 'plant_protection',
      inceptionType: 'new_product',
      submitterUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/项目名称/)
  })

  test('TC-Inc-F003 businessLine 白名单非法 → 604', async () => {
    const r = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-非法业务线`,
      businessLine: 'invalid_xx',
      inceptionType: 'new_product',
      submitterUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/业务线/)
  })

  test('TC-Inc-F004 inceptionType 白名单非法 → 604', async () => {
    const r = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-非法类型`,
      businessLine: 'precision_farming',
      inceptionType: 'invalid_xx',
      submitterUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/项目类型/)
  })

  test('TC-Inc-F005 状态机正向 00→01→02→03 (含 approvedAt 自动填)', async () => {
    const created = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-状态机正向`,
      businessLine: 'precision_farming',
      inceptionType: 'iteration',
      submitterUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/inception/list', {
      projectName: `E2E 立项-${RUN_ID}-状态机正向`
    })
    const id = list.rows[0].inceptionId

    let r = await api.put('/business/inception', { inceptionId: id, status: '01' })
    expect(r.code).toBe(200)
    r = await api.put('/business/inception', { inceptionId: id, status: '02' })
    expect(r.code).toBe(200)
    r = await api.put('/business/inception', { inceptionId: id, status: '03' })
    expect(r.code).toBe(200)
    const got = await api.get(`/business/inception/${id}`)
    expect(got.data.status).toBe('03')
    expect(got.data.approvedAt).toBeTruthy()
  })

  test('TC-Inc-F006 状态机非法 00→03 跳级 → 601', async () => {
    const created = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-非法跳级`,
      businessLine: 'agri_supply',
      inceptionType: 'platform',
      submitterUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/inception/list', {
      projectName: `E2E 立项-${RUN_ID}-非法跳级`
    })
    const id = list.rows[0].inceptionId

    const r = await api.put('/business/inception', { inceptionId: id, status: '03' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-Inc-F007 →04 缺 rejectReason → 602', async () => {
    const created = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-驳回缺原因`,
      businessLine: 'traceability',
      inceptionType: 'refactor',
      submitterUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/inception/list', {
      projectName: `E2E 立项-${RUN_ID}-驳回缺原因`
    })
    const id = list.rows[0].inceptionId
    await api.put('/business/inception', { inceptionId: id, status: '01' })
    const r = await api.put('/business/inception', { inceptionId: id, status: '04' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/驳回/)
  })

  test('TC-Inc-F008 反向边 04→00 (打回重写)', async () => {
    const created = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-反向边`,
      businessLine: 'plant_protection',
      inceptionType: 'iteration',
      submitterUserId: 1
    })
    const list = await api.get('/business/inception/list', {
      projectName: `E2E 立项-${RUN_ID}-反向边`
    })
    const id = list.rows[0].inceptionId
    await api.put('/business/inception', { inceptionId: id, status: '01' })
    await api.put('/business/inception', {
      inceptionId: id, status: '04', rejectReason: '本期预算不足,缓推'
    })
    const r = await api.put('/business/inception', { inceptionId: id, status: '00' })
    expect(r.code).toBe(200)
    const got = await api.get(`/business/inception/${id}`)
    expect(got.data.status).toBe('00')
  })

  test('TC-Inc-F009 AI 生成 → aiGenerated=Y / 内容非空 / 风险非空', async () => {
    const created = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-AI 生成`,
      businessLine: 'precision_farming',
      inceptionType: 'new_product',
      background: '基于 IoT 的精准灌溉决策系统',
      estimatedDurationMonths: 8,
      estimatedTeam: '产品×1 后端×3 IoT×2',
      submitterUserId: 1
    })
    const list = await api.get('/business/inception/list', {
      projectName: `E2E 立项-${RUN_ID}-AI 生成`
    })
    const id = list.rows[0].inceptionId
    const r = await api.post(`/business/inception/ai/generate/${id}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.aiProposalContent).toContain('立项建议书')
    expect(r.data.aiProposalContent).toContain('精准灌溉决策系统')
    expect(r.data.aiRisks).toBeTruthy()
    expect(r.data.aiGeneratedAt).toBeTruthy()
  })

  test('TC-Inc-F010 编号格式 INC-YYYY-NNNN', async () => {
    const created = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-编号格式`,
      businessLine: 'plant_protection',
      inceptionType: 'platform',
      submitterUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/inception/list', {
      projectName: `E2E 立项-${RUN_ID}-编号格式`
    })
    const year = new Date().getFullYear()
    expect(list.rows[0].inceptionNo).toMatch(new RegExp(`^INC-${year}-\\d{4}$`))
  })

  test('TC-Inc-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const projectName = `E2E 立项-${RUN_ID}-删除测试`
    const createResp = await api.post('/business/inception', {
      projectName,
      businessLine: 'plant_protection',
      inceptionType: 'new_product',
      submitterUserId: 1
    })
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.get('/business/inception/list', { projectName })
    const created = before.rows.find((x: any) => x.projectName === projectName)
    expect(created, '新建 inception 应能在列表里查到').toBeDefined()
    const id: number = created.inceptionId
    expect(typeof id, 'inceptionId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/inception/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/inception/list', { projectName })
    const stillThere = after.rows.find((x: any) => x.inceptionId === id)
    expect(stillThere, `inceptionId=${id} 删除后不该出现在 list`).toBeUndefined()
  })

  test('TC-Inc-ENC001 编码 HEX — 中文项目名不含 EFBFBD 替换符', async () => {
    const cn = '小麦病害精准防治-中文检测'
    const r = await api.post('/business/inception', {
      projectName: `E2E 立项-${RUN_ID}-${cn}`,
      businessLine: 'plant_protection',
      inceptionType: 'iteration',
      submitterUserId: 1
    })
    expect(r.code).toBe(200)
    const hex = getFieldHex(
      'tb_inception',
      'project_name',
      `project_name like 'E2E 立项-${RUN_ID}-${cn}%'`
    )
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })
})
