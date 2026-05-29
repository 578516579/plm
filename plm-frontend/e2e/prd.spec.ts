/** PRD 模块 E2E — PRD §F2.2 AI PRD 生成器 + 原型 prd.html
 *
 * 覆盖 (对齐 inception/competitive.spec.ts 套路, 11 case):
 *   F001 创建 PRD (含业务场景 + 目标用户)
 *   F002 title 必填 → 602
 *   F003 关联项目不存在 → 702
 *   F004 sceneTemplate 白名单非法 → 604
 *   F005 状态机正向 00→01→02→03
 *   F006 反向边 01→00 (评审打回重写,合法)
 *   F007 状态机跳级 00→02 非法 → 601
 *   F008 终态保护 03→任意 非法 → 601
 *   F009 AI 生成 → 7 段 + completenessScore ≥ 80% (PRD §F2.2 验收红线)
 *   F010 编号格式 PRD-YYYY-NNNN
 *   ENC001 编码 HEX — 中文 title 不含 EFBFBD 替换符
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('PRD 模块 E2E (PRD §F2.2)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`prd-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`prd-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_prd', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-PRD-F001 创建 PRD (业务场景 + 目标用户)', async () => {
    const r = await api.post('/business/prd', {
      projectId,
      title: `AI 灌溉推荐引擎-${RUN_ID}`,
      description: '基于土壤墒情传感器、气象数据和作物生长模型,自动推荐灌溉时间和灌溉量。原型 prd.html L144 同款诉求。',
      sceneTemplate: 'irrigation',
      targetUser: 'farmer',
      version: 'v1.0',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-PRD-F002 title 必填 → 602', async () => {
    const r = await api.post('/business/prd', {
      projectId,
      description: '没有 title',
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/功能名称/)
  })

  test('TC-PRD-F003 关联项目不存在 → 702', async () => {
    const r = await api.post('/business/prd', {
      projectId: 99999999,
      title: `PRD-${RUN_ID}-非法项目`,
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/关联项目不存在/)
  })

  test('TC-PRD-F004 sceneTemplate 白名单非法 → 604', async () => {
    const r = await api.post('/business/prd', {
      projectId,
      title: `PRD-${RUN_ID}-非法场景`,
      sceneTemplate: 'organic_farming',
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/业务场景/)
  })

  test('TC-PRD-F005 状态机正向 00→01→02→03 (草稿→评审→确认→废弃)', async () => {
    const created = await api.post('/business/prd', {
      projectId,
      title: `PRD-${RUN_ID}-状态机正向`,
      sceneTemplate: 'agri_sales',
      targetUser: 'admin',
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/prd/list', {
      title: `PRD-${RUN_ID}-状态机正向`
    })
    const id = list.rows[0].prdId

    let r = await api.put('/business/prd', { prdId: id, status: '01' })
    expect(r.code).toBe(200)
    r = await api.put('/business/prd', { prdId: id, status: '02' })
    expect(r.code).toBe(200)
    r = await api.put('/business/prd', { prdId: id, status: '03' })
    expect(r.code).toBe(200)
    const got = await api.get(`/business/prd/${id}`)
    expect(got.data.status).toBe('03')
  })

  test('TC-PRD-F006 反向边 01→00 (评审打回重写,合法)', async () => {
    const created = await api.post('/business/prd', {
      projectId,
      title: `PRD-${RUN_ID}-反向边`,
      sceneTemplate: 'pest_control',
      targetUser: 'agronomist',
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/prd/list', {
      title: `PRD-${RUN_ID}-反向边`
    })
    const id = list.rows[0].prdId
    await api.put('/business/prd', { prdId: id, status: '01' })

    const r = await api.put('/business/prd', { prdId: id, status: '00' })
    expect(r.code).toBe(200)
    const got = await api.get(`/business/prd/${id}`)
    expect(got.data.status).toBe('00')
  })

  test('TC-PRD-F007 状态机跳级 00→02 非法 → 601', async () => {
    const created = await api.post('/business/prd', {
      projectId,
      title: `PRD-${RUN_ID}-跳级`,
      sceneTemplate: 'traceability',
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/prd/list', {
      title: `PRD-${RUN_ID}-跳级`
    })
    const id = list.rows[0].prdId

    const r = await api.put('/business/prd', { prdId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-PRD-F008 终态保护 03→任意 非法 → 601', async () => {
    const created = await api.post('/business/prd', {
      projectId,
      title: `PRD-${RUN_ID}-终态保护`,
      sceneTemplate: 'irrigation',
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/prd/list', {
      title: `PRD-${RUN_ID}-终态保护`
    })
    const id = list.rows[0].prdId
    await api.put('/business/prd', { prdId: id, status: '01' })
    await api.put('/business/prd', { prdId: id, status: '02' })
    await api.put('/business/prd', { prdId: id, status: '03' })

    const r = await api.put('/business/prd', { prdId: id, status: '00' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-PRD-F009 AI 生成 → 7 段 + completenessScore ≥ 80% (PRD §F2.2 验收红线)', async () => {
    const created = await api.post('/business/prd', {
      projectId,
      title: `PRD-${RUN_ID}-AI 生成`,
      description: '农产品扫码溯源 H5 — 消费者扫码可见种植档案与流转记录',
      sceneTemplate: 'traceability',
      targetUser: 'farmer',
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/prd/list', {
      title: `PRD-${RUN_ID}-AI 生成`
    })
    const id = list.rows[0].prdId
    const r = await api.post(`/business/prd/ai/generate/${id}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    // 7 段标题命中(场景化模板覆盖)
    expect(r.data.content).toContain('背景与目标')
    expect(r.data.content).toContain('用户故事')
    expect(r.data.content).toContain('功能描述')
    expect(r.data.content).toContain('非功能需求')
    expect(r.data.content).toContain('验收标准')
    expect(r.data.content).toContain('原型说明')
    expect(r.data.content).toContain('版本说明')
    // 农业场景措辞断言(traceability 场景应包含溯源相关关键词)
    expect(r.data.content).toMatch(/溯源|区块链|扫码/)
    // PRD §F2.2 红线
    expect(Number(r.data.completenessScore)).toBeGreaterThanOrEqual(80)
    expect(r.data.aiGeneratedAt).toBeTruthy()
  })

  test('TC-PRD-F010 编号格式 PRD-YYYY-NNNN', async () => {
    const created = await api.post('/business/prd', {
      projectId,
      title: `PRD-${RUN_ID}-编号格式`,
      sceneTemplate: 'agri_sales',
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/prd/list', {
      title: `PRD-${RUN_ID}-编号格式`
    })
    const year = new Date().getFullYear()
    expect(list.rows[0].prdNo).toMatch(new RegExp(`^PRD-${year}-\\d{4}$`))
  })

  test('TC-PRD-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const title = `PRD-${RUN_ID}-删除测试`
    const createResp = await api.post('/business/prd', {
      projectId,
      title,
      sceneTemplate: 'irrigation',
      targetUser: 'farmer',
      authorUserId: 1
    })
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.get('/business/prd/list', { title })
    const created = before.rows.find((x: any) => x.title === title)
    expect(created, '新建 prd 应能在列表里查到').toBeDefined()
    const id: number = created.prdId
    expect(typeof id, 'prdId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/prd/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/prd/list', { title })
    const stillThere = after.rows.find((x: any) => x.prdId === id)
    expect(stillThere, `prdId=${id} 删除后不该出现在 list`).toBeUndefined()
  })

  test('TC-PRD-ENC001 编码 HEX — 中文 title 不含 EFBFBD 替换符', async () => {
    const cn = '智慧灌溉决策引擎-中文检测'
    const r = await api.post('/business/prd', {
      projectId,
      title: `${cn}-${RUN_ID}`,
      sceneTemplate: 'irrigation',
      targetUser: 'farmer',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
    const hex = getFieldHex(
      'tb_prd',
      'title',
      `title like '${cn}-${RUN_ID}%'`
    )
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })
})
