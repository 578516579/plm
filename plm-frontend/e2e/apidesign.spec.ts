/** ApiDesign 模块 E2E — PRD §F3.3 LLD 接口详细设计 + 原型 apidesign.html
 *
 * 覆盖 (对齐 competitive.spec.ts 11 case 套路, apidesign 14 case + 1 编码 = 15 case):
 *   F001 创建接口设计 (HTTP方法 + 路径 + Mock开关)
 *   F002 title 必填 → 602
 *   F003 httpMethod 必填 → 602
 *   F004 httpMethod=CONNECT 白名单外 → 604
 *   F005 关联项目不存在 → 702
 *   F006 状态机正向 00→01→02→03 (草稿→评审中→已确认→已废弃)
 *   F007 反向边 01→00 合法 ⭐ apidesign 独有 (评审打回)
 *   F008 跳级 00→02 非法 → 601
 *   F009 反向 02→00 非法 → 601 (确认后不可回打回)
 *   F010 终态保护 03→02 非法 → 601
 *   F011 小写 method 规范化 (post → POST 接受)
 *   F012 AI 生成 OpenAPI 3.0 YAML + JSON Schema + Mock + aiGenerated=Y
 *   F013 唯一键 (project_id, http_method, path) 冲突 → 701 ⭐ apidesign 独有
 *   F014 编号格式 APID-YYYY-NNNN
 *   ENC001 编码 HEX — 中文 title 不含 EFBFBD 替换符
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('ApiDesign 模块 E2E (PRD §F3.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`apidesign-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`apidesign-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_apidesign', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-APID-F001 创建接口设计 (HTTP方法 + 路径 + Mock开关)', async () => {
    const r = await api.post('/business/apidesign', {
      projectId,
      title: `灌溉推荐接口-${RUN_ID}`,
      httpMethod: 'POST',
      path: `/api/v1/irrigation-${RUN_ID.slice(-4)}/recommend`,
      description: '根据土壤墒情 + 气象数据生成灌溉建议',
      mockEnabled: 'Y',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-APID-F002 title 必填 → 602', async () => {
    const r = await api.post('/business/apidesign', {
      projectId,
      httpMethod: 'GET',
      path: `/api/v1/null-title-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/接口设计标题/)
  })

  test('TC-APID-F003 httpMethod 必填 → 602', async () => {
    const r = await api.post('/business/apidesign', {
      projectId,
      title: `无 method-${RUN_ID}`,
      path: `/api/v1/no-method-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/HTTP 方法/)
  })

  test('TC-APID-F004 httpMethod=CONNECT 白名单外 → 604', async () => {
    const r = await api.post('/business/apidesign', {
      projectId,
      title: `非法 method-${RUN_ID}`,
      httpMethod: 'CONNECT',
      path: `/api/v1/non-whitelist-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/HTTP 方法仅支持/)
  })

  test('TC-APID-F005 关联项目不存在 → 702', async () => {
    const r = await api.post('/business/apidesign', {
      projectId: 99999999,
      title: `非法项目-${RUN_ID}`,
      httpMethod: 'GET',
      path: `/api/v1/invalid-project-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/关联项目不存在/)
  })

  test('TC-APID-F006 状态机正向 00→01→02→03 (草稿→评审中→已确认→已废弃)', async () => {
    const created = await api.post('/business/apidesign', {
      projectId,
      title: `状态机正向-${RUN_ID}`,
      httpMethod: 'GET',
      path: `/api/v1/state-machine-fwd-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/apidesign/list', {
      title: `状态机正向-${RUN_ID}`
    })
    const id = list.rows[0].apidesignId

    let r = await api.put('/business/apidesign', { apidesignId: id, status: '01' })
    expect(r.code).toBe(200)
    r = await api.put('/business/apidesign', { apidesignId: id, status: '02' })
    expect(r.code).toBe(200)
    r = await api.put('/business/apidesign', { apidesignId: id, status: '03' })
    expect(r.code).toBe(200)
    const got = await api.get(`/business/apidesign/${id}`)
    expect(got.data.status).toBe('03')
  })

  test('TC-APID-F007 反向边 01→00 合法 ⭐ apidesign 独有 (评审打回)', async () => {
    const created = await api.post('/business/apidesign', {
      projectId,
      title: `反向打回-${RUN_ID}`,
      httpMethod: 'POST',
      path: `/api/v1/reverse-edge-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/apidesign/list', {
      title: `反向打回-${RUN_ID}`
    })
    const id = list.rows[0].apidesignId
    await api.put('/business/apidesign', { apidesignId: id, status: '01' })

    // 反向边 01 → 00 合法(apidesign 独有的评审打回机制)
    const r = await api.put('/business/apidesign', { apidesignId: id, status: '00' })
    expect(r.code).toBe(200)
    const got = await api.get(`/business/apidesign/${id}`)
    expect(got.data.status).toBe('00')
  })

  test('TC-APID-F008 跳级 00→02 非法 → 601', async () => {
    const created = await api.post('/business/apidesign', {
      projectId,
      title: `跳级-${RUN_ID}`,
      httpMethod: 'PUT',
      path: `/api/v1/jump-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/apidesign/list', {
      title: `跳级-${RUN_ID}`
    })
    const id = list.rows[0].apidesignId

    const r = await api.put('/business/apidesign', { apidesignId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-APID-F009 反向 02→00 非法 → 601 (确认后不可回打回)', async () => {
    const created = await api.post('/business/apidesign', {
      projectId,
      title: `已确认不可回-${RUN_ID}`,
      httpMethod: 'DELETE',
      path: `/api/v1/confirmed-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/apidesign/list', {
      title: `已确认不可回-${RUN_ID}`
    })
    const id = list.rows[0].apidesignId
    await api.put('/business/apidesign', { apidesignId: id, status: '01' })
    await api.put('/business/apidesign', { apidesignId: id, status: '02' })

    const r = await api.put('/business/apidesign', { apidesignId: id, status: '00' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-APID-F010 终态保护 03→02 非法 → 601', async () => {
    const created = await api.post('/business/apidesign', {
      projectId,
      title: `终态-${RUN_ID}`,
      httpMethod: 'PATCH',
      path: `/api/v1/terminal-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/apidesign/list', {
      title: `终态-${RUN_ID}`
    })
    const id = list.rows[0].apidesignId
    await api.put('/business/apidesign', { apidesignId: id, status: '01' })
    await api.put('/business/apidesign', { apidesignId: id, status: '02' })
    await api.put('/business/apidesign', { apidesignId: id, status: '03' })

    const r = await api.put('/business/apidesign', { apidesignId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-APID-F011 小写 method 规范化 (post → POST 接受)', async () => {
    const r = await api.post('/business/apidesign', {
      projectId,
      title: `小写规范化-${RUN_ID}`,
      httpMethod: 'post',
      path: `/api/v1/lowercase-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(r.code).toBe(200)
    const list = await api.get('/business/apidesign/list', {
      title: `小写规范化-${RUN_ID}`
    })
    expect(list.rows[0].httpMethod).toBe('POST')
  })

  test('TC-APID-F012 AI 生成 OpenAPI 3.0 YAML + JSON Schema + Mock + aiGenerated=Y', async () => {
    const created = await api.post('/business/apidesign', {
      projectId,
      title: `AI 生成-${RUN_ID}`,
      httpMethod: 'GET',
      path: `/api/v1/ai-gen-${RUN_ID.slice(-4)}`,
      description: '用户列表查询接口',
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/apidesign/list', {
      title: `AI 生成-${RUN_ID}`
    })
    const id = list.rows[0].apidesignId

    const r = await api.post(`/business/apidesign/ai/generate/${id}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.openapiSpec).toContain('openapi: 3.0.3')
    expect(r.data.openapiSpec).toContain(`AI 生成-${RUN_ID}`)
    expect(r.data.openapiSpec).toContain('get:')
    expect(r.data.requestSchema).toContain('"type":"object"')
    expect(r.data.responseSchema).toContain('"code"')
    expect(r.data.mockResponse).toContain('"code":200')
    expect(r.data.aiGeneratedAt).toBeTruthy()
  })

  test('TC-APID-F013 唯一键 (project_id, http_method, path) 冲突 → 701 ⭐ apidesign 独有', async () => {
    const samePath = `/api/v1/duplicate-key-${RUN_ID.slice(-4)}`
    // 第 1 次创建
    const first = await api.post('/business/apidesign', {
      projectId,
      title: `唯一键首发-${RUN_ID}`,
      httpMethod: 'POST',
      path: samePath,
      authorUserId: 1
    })
    expect(first.code).toBe(200)

    // 第 2 次相同 (project_id, method, path) → 抛 701
    const dup = await api.post('/business/apidesign', {
      projectId,
      title: `唯一键撞-${RUN_ID}`,
      httpMethod: 'POST',
      path: samePath,
      authorUserId: 1
    })
    expect(dup.code).not.toBe(200)
    expect(JSON.stringify(dup)).toMatch(/已存在相同 method\+path/)
  })

  test('TC-APID-F014 编号格式 APID-YYYY-NNNN', async () => {
    const created = await api.post('/business/apidesign', {
      projectId,
      title: `编号格式-${RUN_ID}`,
      httpMethod: 'HEAD',
      path: `/api/v1/no-format-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(created.code).toBe(200)
    const list = await api.get('/business/apidesign/list', {
      title: `编号格式-${RUN_ID}`
    })
    const year = new Date().getFullYear()
    expect(list.rows[0].apidesignNo).toMatch(new RegExp(`^APID-${year}-\\d{4}$`))
  })

  test('TC-APID-ENC001 编码 HEX — 中文 title 不含 EFBFBD 替换符', async () => {
    const cn = '接口设计中文检测'
    const r = await api.post('/business/apidesign', {
      projectId,
      title: `${cn}-${RUN_ID}`,
      httpMethod: 'OPTIONS',
      path: `/api/v1/encoding-${RUN_ID.slice(-4)}`,
      authorUserId: 1
    })
    expect(r.code).toBe(200)
    const hex = getFieldHex(
      'tb_apidesign',
      'title',
      `title like '${cn}-${RUN_ID}%'`
    )
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })
})
