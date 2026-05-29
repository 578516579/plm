/**
 * ApiDoc 模块 E2E — PRD §F5.4 OpenAPI 规范
 * 覆盖: CRUD + API-YYYY-NNNN 编号 + UK(method+path+version) + 中文 HEX 守门员
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { assertNoMojibake, execDelete } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('ApiDoc 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`apidoc-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`apidoc-suite-${RUN_ID}`))?.id
    expect(projectId).toBeDefined()
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_apidoc', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-API-F001 创建 API 文档 + API-YYYY-NNNN 编号', async () => {
    const path = `/test/api-${RUN_ID.slice(-4)}`
    const r = await api.post('/business/apidoc', {
      projectId,
      title: `获取项目列表-${RUN_ID}`,
      httpMethod: 'GET',
      path,
      description: '分页查询项目列表',
      version: 'v1.0',
      autoExtracted: 'Y'
    })
    expect(r.code).toBe(200)

    const list = await api.get('/business/apidoc/list', { pageSize: 100 })
    const doc = list.rows.find((x: any) => x.path === path)
    expect(doc).toBeDefined()
    expect(doc.apidocNo).toMatch(/^API-\d{4}-\d{4}$/)
    expect(doc.status).toBe('00')
  })

  test('TC-API-F002 编码守门员: 中文 title + description 无乱码', async () => {
    const path = `/test/enc-${RUN_ID.slice(-4)}`
    await api.post('/business/apidoc', {
      projectId,
      title: `创建项目接口-中文测试-${RUN_ID}`,
      httpMethod: 'POST',
      path,
      description: '【接口说明】病虫害诊断 αβγ 🐛 接收图像 base64',
      version: 'v1.0',
      autoExtracted: 'N'
    })

    const titleCheck = assertNoMojibake('tb_apidoc', 'title',
      `path='${path}' AND del_flag='0'`)
    expect.soft(titleCheck.ok, `title HEX=${titleCheck.hex}`).toBe(true)
    const descCheck = assertNoMojibake('tb_apidoc', 'description',
      `path='${path}' AND del_flag='0'`)
    expect.soft(descCheck.ok, `description HEX=${descCheck.hex}`).toBe(true)
  })

  test('TC-API-F003 同 method+path+version 唯一 (uk_apidoc_method_path)', async () => {
    const dupPath = `/test/dup-${RUN_ID.slice(-4)}`
    const r1 = await api.post('/business/apidoc', {
      projectId, title: `重复测试-1-${RUN_ID}`, httpMethod: 'PUT', path: dupPath, version: 'v1.0'
    })
    expect(r1.code).toBe(200)
    const r2 = await api.post('/business/apidoc', {
      projectId, title: `重复测试-2-${RUN_ID}`, httpMethod: 'PUT', path: dupPath, version: 'v1.0'
    })
    expect.soft(r2.code, '同 method+path+version 应被 UK 拒').not.toBe(200)
  })

  test('TC-API-F004 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/apidoc', {
      projectId: 999_999_999,
      title: `FK 测试-${RUN_ID}`,
      httpMethod: 'GET',
      path: `/test/fk-${RUN_ID.slice(-4)}`,
      version: 'v1.0'
    })
    expect.soft(r.code, 'FK 不存在应返 702').toBe(ERROR_CODES.FK_NOT_EXISTS)
  })

  // === 状态机覆盖 — 3 状态线性 00→01→02 终态 (Phase 04 Gate B.2) ===

  test('TC-API-F005 状态机正向 00→01 (草稿→已发布)', async () => {
    const path = `/test/sm-01-${RUN_ID.slice(-4)}`
    await api.post('/business/apidoc', {
      projectId, title: `状态机-01-${RUN_ID}`, httpMethod: 'GET', path, version: 'v1.0'
    })
    const list = await api.get('/business/apidoc/list', { pageSize: 100 })
    const doc = list.rows.find((x: any) => x.path === path)
    expect(doc.status).toBe('00')

    const r = await api.put('/business/apidoc', { apidocId: doc.apidocId, status: '01' })
    expect(r.code).toBe(200)
    const after = await api.get(`/business/apidoc/${doc.apidocId}`)
    expect(after.data.status).toBe('01')
  })

  test('TC-API-F006 状态机正向 01→02 (已发布→已废弃)', async () => {
    const path = `/test/sm-02-${RUN_ID.slice(-4)}`
    await api.post('/business/apidoc', {
      projectId, title: `状态机-02-${RUN_ID}`, httpMethod: 'GET', path, version: 'v1.0'
    })
    const list = await api.get('/business/apidoc/list', { pageSize: 100 })
    const doc = list.rows.find((x: any) => x.path === path)
    await api.put('/business/apidoc', { apidocId: doc.apidocId, status: '01' })

    const r = await api.put('/business/apidoc', { apidocId: doc.apidocId, status: '02' })
    expect(r.code).toBe(200)
    const after = await api.get(`/business/apidoc/${doc.apidocId}`)
    expect(after.data.status).toBe('02')
  })

  test('TC-API-F007 跳级 00→02 非法 → 601', async () => {
    const path = `/test/sm-skip-${RUN_ID.slice(-4)}`
    await api.post('/business/apidoc', {
      projectId, title: `跳级测试-${RUN_ID}`, httpMethod: 'GET', path, version: 'v1.0'
    })
    const list = await api.get('/business/apidoc/list', { pageSize: 100 })
    const doc = list.rows.find((x: any) => x.path === path)

    const r = await api.put('/business/apidoc', { apidocId: doc.apidocId, status: '02' })
    expect.soft(r.code, '00→02 跳级应被 601 拒').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-API-F008 终态保护 02→01 非法 → 601', async () => {
    const path = `/test/sm-final-${RUN_ID.slice(-4)}`
    await api.post('/business/apidoc', {
      projectId, title: `终态测试-${RUN_ID}`, httpMethod: 'GET', path, version: 'v1.0'
    })
    const list = await api.get('/business/apidoc/list', { pageSize: 100 })
    const doc = list.rows.find((x: any) => x.path === path)
    await api.put('/business/apidoc', { apidocId: doc.apidocId, status: '01' })
    await api.put('/business/apidoc', { apidocId: doc.apidocId, status: '02' })

    const r = await api.put('/business/apidoc', { apidocId: doc.apidocId, status: '01' })
    expect.soft(r.code, '02 终态不可回滚到 01').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-API-F009 小写 method 自动规范化 (get → GET)', async () => {
    const path = `/test/case-${RUN_ID.slice(-4)}`
    const r = await api.post('/business/apidoc', {
      projectId, title: `大小写规范-${RUN_ID}`, httpMethod: 'get', path, version: 'v1.0'
    })
    expect(r.code).toBe(200)
    const list = await api.get('/business/apidoc/list', { pageSize: 100 })
    const doc = list.rows.find((x: any) => x.path === path)
    expect(doc.httpMethod, 'method 应规范化为大写').toBe('GET')
  })

  test('TC-API-F010 非白名单 method=CONNECT → 604', async () => {
    const r = await api.post('/business/apidoc', {
      projectId,
      title: `白名单测试-${RUN_ID}`,
      httpMethod: 'CONNECT',
      path: `/test/wl-${RUN_ID.slice(-4)}`,
      version: 'v1.0'
    })
    expect.soft(r.code, 'CONNECT 不在白名单应返 604').toBe(ERROR_CODES.FIELD_FORMAT)
  })

  test('TC-API-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const path = `/test/del-${RUN_ID.slice(-4)}`
    const createResp = await api.post('/business/apidoc', {
      projectId,
      title: `删除测试-${RUN_ID}`,
      httpMethod: 'GET',
      path,
      version: 'v1.0'
    })
    expect(createResp.code, '创建应成功').toBe(200)

    const before = await api.get('/business/apidoc/list', { pageSize: 100 })
    const created = before.rows.find((x: any) => x.path === path)
    expect(created, '新建 apidoc 应能在列表里查到').toBeDefined()
    const id: number = created.apidocId
    expect(typeof id, 'apidocId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/apidoc/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/apidoc/list', { pageSize: 100 })
    const stillThere = after.rows.find((x: any) => x.apidocId === id)
    expect(stillThere, `apidocId=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
