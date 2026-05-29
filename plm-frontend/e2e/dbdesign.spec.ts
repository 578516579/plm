/**
 * DbDesign 模块 E2E — PRD §F3.2 数据库设计 + 原型 dbdesign.html
 *
 * 覆盖 (占位 1 → 真实 11 case;让 DbDesign 转 🟢):
 *   - CRUD + DB-YYYY-NNNN 编号 + 默认值 (status=00 / aiGenerated=N)
 *   - 编码守门员: 中文 title DB HEX 无 EFBFBD
 *   - dbEngine 白名单 (mysql/postgresql/kingbase) → 604
 *   - FK projectId 不存在 → 702 / 必填 (title/projectId/authorUserId) → 602 / 新建非草稿 → 601
 *   - 4 状态机含反向边: 00→01→{00,02}→03 终态 (01→00 评审打回)
 *   - aiGenerate: erDiagramContent (Mermaid ER) + dataDictionary + ddlScript + normalizationCheck + aiGenerated=Y
 *   - UI 菜单可达
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, assertNoMojibake } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

/** 创建数据库设计,返回响应 + title */
async function createDbDesign(suffix: string, overrides: Record<string, any> = {}) {
  const title = `DB-${suffix}-${RUN_ID}`
  const r = await api.post('/business/dbdesign', {
    projectId,
    title,
    dbEngine: 'mysql',
    authorUserId: 1,
    ...overrides
  })
  return { r, title }
}

async function findByTitle(title: string): Promise<any> {
  const list = await api.get('/business/dbdesign/list', { pageSize: 200 })
  return list.rows.find((x: any) => x.title === title)
}

test.describe('DbDesign 模块 E2E (PRD §F3.2)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`db-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`db-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_dbdesign', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-DB-F001 创建数据库设计 + DB-YYYY-NNNN 编号 + 默认值', async () => {
    const { r, title } = await createDbDesign('crud')
    expect(r.code).toBe(200)
    const t = await findByTitle(title)
    expect(t.dbdesignNo, '编号格式 DB-YYYY-NNNN').toMatch(/^DB-\d{4}-\d{4}$/)
    expect(t.status, '新建默认草稿').toBe('00')
    expect(t.aiGenerated, '未生成前 aiGenerated=N').toBe('N')
    expect(t.dbEngine).toBe('mysql')
  })

  test('TC-DB-F002 编码守门员: 中文 title DB HEX 无 EFBFBD', async () => {
    const title = `编码自检-业务库设计-${RUN_ID}`
    const { r } = await createDbDesign('enc', { title, remark: '需求标题：测试，结束。αβγ 🎯' })
    expect(r.code).toBe(200)
    const t = await findByTitle(title)
    const enc = assertNoMojibake('tb_dbdesign', 'title', `dbdesign_id=${t.dbdesignId}`)
    expect(enc.ok, enc.reason).toBe(true)
    expect(enc.hex, 'title 应以「编码」UTF-8 字节开头').toContain('E7BC96E7A081')
  })

  test('TC-DB-F003 必填校验 (title / projectId / authorUserId) → 602', async () => {
    expect((await api.post('/business/dbdesign', { projectId, authorUserId: 1 })).code).toBe(ERROR_CODES.REQUIRED_FIELD)
    expect((await api.post('/business/dbdesign', { title: `no-proj-${RUN_ID}`, authorUserId: 1 })).code).toBe(ERROR_CODES.REQUIRED_FIELD)
    const r = await api.post('/business/dbdesign', { projectId, title: `no-dba-${RUN_ID}` })
    expect(r.code).toBe(ERROR_CODES.REQUIRED_FIELD)
    expect(r.msg).toContain('DBA')
  })

  test('TC-DB-F004 dbEngine 非白名单 → 604', async () => {
    const { r } = await createDbDesign('bad-engine', { dbEngine: 'oracle' })
    expect(r.code).toBe(ERROR_CODES.FIELD_FORMAT)
    expect(r.msg).toContain('引擎')
  })

  test('TC-DB-F005 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/dbdesign', {
      projectId: 99999999, title: `fk-${RUN_ID}`, dbEngine: 'mysql', authorUserId: 1
    })
    expect(r.code).toBe(ERROR_CODES.FK_NOT_EXISTS)
    expect(r.msg).toContain('关联项目不存在')
  })

  test('TC-DB-F006 新建状态非 00 → 601', async () => {
    const { r } = await createDbDesign('non-draft', { status: '01' })
    expect(r.code).toBe(ERROR_CODES.STATUS_VIOLATION)
    expect(r.msg).toContain('草稿')
  })

  test('TC-DB-F007 状态机合法 00→01→02→03 + 反向边 01→00', async () => {
    const a = await createDbDesign('sm-fwd')
    const idA = (await findByTitle(a.title)).dbdesignId
    expect((await api.put('/business/dbdesign', { dbdesignId: idA, status: '01' })).code, '00→01').toBe(200)
    expect((await api.put('/business/dbdesign', { dbdesignId: idA, status: '02' })).code, '01→02').toBe(200)
    expect((await api.put('/business/dbdesign', { dbdesignId: idA, status: '03' })).code, '02→03').toBe(200)
    const b = await createDbDesign('sm-reverse')
    const idB = (await findByTitle(b.title)).dbdesignId
    await api.put('/business/dbdesign', { dbdesignId: idB, status: '01' })
    expect((await api.put('/business/dbdesign', { dbdesignId: idB, status: '00' })).code, '01→00 评审打回').toBe(200)
  })

  test('TC-DB-F008 状态机非法 (跨级/反向/终态) 全 601', async () => {
    const a = await createDbDesign('il-jump')
    const idA = (await findByTitle(a.title)).dbdesignId
    expect.soft((await api.put('/business/dbdesign', { dbdesignId: idA, status: '02' })).code, '00→02 跨级').toBe(ERROR_CODES.STATUS_VIOLATION)

    const b = await createDbDesign('il-rev')
    const idB = (await findByTitle(b.title)).dbdesignId
    await api.put('/business/dbdesign', { dbdesignId: idB, status: '01' })
    await api.put('/business/dbdesign', { dbdesignId: idB, status: '02' })
    expect.soft((await api.put('/business/dbdesign', { dbdesignId: idB, status: '01' })).code, '02→01 反向').toBe(ERROR_CODES.STATUS_VIOLATION)

    const c = await createDbDesign('il-term')
    const idC = (await findByTitle(c.title)).dbdesignId
    await api.put('/business/dbdesign', { dbdesignId: idC, status: '01' })
    await api.put('/business/dbdesign', { dbdesignId: idC, status: '02' })
    await api.put('/business/dbdesign', { dbdesignId: idC, status: '03' })
    expect.soft((await api.put('/business/dbdesign', { dbdesignId: idC, status: '02' })).code, '03→02 终态').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-DB-F009 aiGenerate → ER 图 + 数据字典 + DDL + 规范检查 + aiGenerated=Y', async () => {
    const { title } = await createDbDesign('ai')
    const t = await findByTitle(title)
    const r = await api.post(`/business/dbdesign/ai/generate/${t.dbdesignId}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.aiGeneratedAt).toBeTruthy()
    expect(r.data.erDiagramContent, 'Mermaid ER 图').toContain('erDiagram')
    expect(r.data.dataDictionary, '数据字典非空').toBeTruthy()
    expect(r.data.ddlScript, 'DDL 含 CREATE TABLE').toContain('CREATE TABLE')
    expect(r.data.normalizationCheck, '规范检查 JSON').toContain('normalization')
  })

  test('TC-DB-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const { r, title } = await createDbDesign('del')
    expect(r.code, '创建应成功').toBe(200)

    const before = await api.get('/business/dbdesign/list', { pageSize: 200 })
    const created = before.rows.find((x: any) => x.title === title)
    expect(created, '新建 dbdesign 应能在列表里查到').toBeDefined()
    const id: number = created.dbdesignId
    expect(typeof id, 'dbdesignId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/dbdesign/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/dbdesign/list', { pageSize: 200 })
    const stillThere = after.rows.find((x: any) => x.dbdesignId === id)
    expect(stillThere, `dbdesignId=${id} 删除后不该出现在 list`).toBeUndefined()
  })

  test('TC-DB-UI 数据库设计管理菜单可访问', async ({ page, context, request }) => {
    await loginAsAdmin(request, context)
    await page.goto('/business/dbdesign')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })
  })
})
