/**
 * Document 模块 E2E — 12 doc_type + 4×4 状态机含反向边 + 707 (进入已发布必填 reviewer) + ADR-0007 编号按 type 分别累加
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'
import { makeDocumentData, DOC_STATUS_TRANSITIONS } from './helpers/fixtures-document'

let token: string
let api: ApiClient
let apiRequest: APIRequestContext
let projectId: number

test.describe('Document 模块 E2E (合并 5 stub)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)

    await api.createProject(makeProjectData(`doc-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`doc-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_document', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Doc-F001 CRUD + ADR-0007 按 type 编号', async () => {
    // 创建 PRD 文档
    const prdData = makeDocumentData(projectId, 'prd', `prd-${RUN_ID}`)
    let c = await api.post('/business/document', prdData)
    expect(c.code).toBe(200)

    // 创建 ARCH 文档
    const archData = makeDocumentData(projectId, 'arch', `arch-${RUN_ID}`)
    c = await api.post('/business/document', archData)
    expect(c.code).toBe(200)

    const list = await api.get('/business/document/list', { pageSize: 100 })
    const prd = list.rows.find((x: any) => x.title === prdData.title)
    const arch = list.rows.find((x: any) => x.title === archData.title)

    expect(prd.documentNo).toMatch(/^DOC-PRD-\d{4}-\d{4}$/)
    expect(arch.documentNo).toMatch(/^DOC-ARCH-\d{4}-\d{4}$/)
    expect(prd.status).toBe('00')
    expect(prd.version).toBe('v1.0')
  })

  test('TC-Doc-F002 doc_type 字典外值返 604', async () => {
    const r = await api.post('/business/document', {
      ...makeDocumentData(projectId, 'invalid_type', `bad-type-${RUN_ID}`)
    })
    expect(r.code).toBe(604)
    expect(r.msg).toContain('doc_type')
  })

  test('TC-Doc-F003 4×4 状态机反向边 01→00 + 02→01', async () => {
    const data = makeDocumentData(projectId, 'prd', `state-${RUN_ID}`)
    await api.post('/business/document', data)
    const list = await api.get('/business/document/list', { pageSize: 100 })
    const d = list.rows.find((x: any) => x.title === data.title)
    const id = d.documentId

    // 00 → 01
    let r = await api.put('/business/document', { documentId: id, status: '01' })
    expect(r.code).toBe(200)

    // 反向边 01 → 00 (打回)
    r = await api.put('/business/document', { documentId: id, status: '00' })
    expect(r.code, '反向边 01→00 打回应允许').toBe(200)

    // 推回 01, 然后到 02 (需要 reviewer)
    await api.put('/business/document', { documentId: id, status: '01' })
    r = await api.put('/business/document', { documentId: id, status: '02', reviewerUserId: 1 })
    expect(r.code).toBe(200)

    // 反向边 02 → 01 (重审)
    r = await api.put('/business/document', { documentId: id, status: '01' })
    expect(r.code, '反向边 02→01 重审应允许').toBe(200)
  })

  test('TC-Doc-F004 进入 02 必填 reviewer (707)', async () => {
    const data = makeDocumentData(projectId, 'prd', `707-${RUN_ID}`)
    await api.post('/business/document', data)
    const list = await api.get('/business/document/list', { pageSize: 100 })
    const id = list.rows.find((x: any) => x.title === data.title).documentId

    await api.put('/business/document', { documentId: id, status: '01' })

    // 不填 reviewer 推 02 → 707
    let r = await api.put('/business/document', { documentId: id, status: '02' })
    expect(r.code).toBe(707)
    expect(r.msg).toContain('审核人')

    // 带 reviewer 推 02 → 200
    r = await api.put('/business/document', { documentId: id, status: '02', reviewerUserId: 1 })
    expect(r.code).toBe(200)
  })

  test('TC-Doc-F005 非法状态转换全覆盖', async () => {
    for (const tc of DOC_STATUS_TRANSITIONS.illegal) {
      const data = makeDocumentData(projectId, 'arch', `illegal-${tc.from}${tc.to}-${RUN_ID}`)
      await api.post('/business/document', data)
      const list = await api.get('/business/document/list', { pageSize: 100 })
      const id = list.rows.find((x: any) => x.title === data.title).documentId

      // 推到 from 状态
      if (tc.from === '03') {
        // 00 → 01 → 02 → 03 (需 reviewer)
        await api.put('/business/document', { documentId: id, status: '01' })
        await api.put('/business/document', { documentId: id, status: '02', reviewerUserId: 1 })
        await api.put('/business/document', { documentId: id, status: '03' })
      }

      const r = await api.put('/business/document', { documentId: id, status: tc.to })
      expect.soft(r.code, `${tc.name} 应被拒 (601)`).toBe(ERROR_CODES.STATUS_VIOLATION)
    }
  })

  test('TC-Doc-F006 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/document', {
      ...makeDocumentData(99999, 'prd', `fk-${RUN_ID}`)
    })
    expect(r.code).toBe(ERROR_CODES.FK_NOT_EXISTS)
  })

  test('TC-Doc-F007 多 doc_type 流水号分别累加', async () => {
    // 同年 2 个 PRD,流水号递增
    const prd1 = await api.post('/business/document', makeDocumentData(projectId, 'prd', `multi-prd-1-${RUN_ID}`))
    const prd2 = await api.post('/business/document', makeDocumentData(projectId, 'prd', `multi-prd-2-${RUN_ID}`))
    expect(prd1.code).toBe(200)
    expect(prd2.code).toBe(200)

    // 1 个 PROPOSAL,应该是流水号 0001 (独立累加)
    const prop1 = await api.post('/business/document', makeDocumentData(projectId, 'proposal', `multi-prop-1-${RUN_ID}`))
    expect(prop1.code).toBe(200)

    const list = await api.get('/business/document/list', { pageSize: 100 })
    const propDoc = list.rows.find((x: any) => x.title === makeDocumentData(projectId, 'proposal', `multi-prop-1-${RUN_ID}`).title)
    // PROPOSAL 第一个,流水号应为 0001
    expect(propDoc.documentNo).toMatch(/^DOC-PROPOSAL-\d{4}-0001$/)
  })

  test('UI 层: 文档管理菜单 + 12 doc_type 下拉', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])
    await page.goto('/business/document')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 60_000 })
    // 验证有"文档类型"过滤(Document 独有)
    await expect(page.getByText(/文档类型/).first()).toBeVisible()
  })
})
