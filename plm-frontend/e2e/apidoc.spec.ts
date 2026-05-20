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
})
