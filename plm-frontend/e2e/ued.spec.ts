/**
 * Ued 模块 E2E — PRD §F2.3 UED 设计协同 + 原型 ued.html
 *
 * 覆盖 (占位 1 → 真实 10 case;让 UED 转 🟢):
 *   - CRUD + UED-YYYY-NNNN 编号 + 默认值 (status=00 / aiGenerated=N)
 *   - 编码守门员: 中文 title + 农业组件标签 DB HEX 无 EFBFBD
 *   - 必填 (title / projectId / designerUserId) → 602 / FK → 702 / 新建非草稿 → 601
 *   - 4 状态机含反向边: 00→01→{00,02}→03 终态 (01→00 评审打回)
 *   - aiReview: aiReviewReport + complianceCheck + aiReviewScore + aiGenerated=Y
 *   - UI 菜单可达
 *   (UED 无 ENUM 白名单,故无 604 用例)
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, assertNoMojibake } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

/** 创建 UED 设计稿,返回响应 + title */
async function createUed(suffix: string, overrides: Record<string, any> = {}) {
  const title = `UED-${suffix}-${RUN_ID}`
  const r = await api.post('/business/ued', {
    projectId,
    title,
    figmaUrl: 'https://www.figma.com/file/abc123/Irrigation',
    versionLabel: 'v1.0',
    agriComponentTags: '农情大屏组件,IoT数据看板',
    designerUserId: 1,
    ...overrides
  })
  return { r, title }
}

async function findByTitle(title: string): Promise<any> {
  const list = await api.get('/business/ued/list', { pageSize: 200 })
  return list.rows.find((x: any) => x.title === title)
}

test.describe('Ued 模块 E2E (PRD §F2.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`ued-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`ued-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_ued', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Ued-F001 创建 UED 设计 + UED-YYYY-NNNN 编号 + 默认值', async () => {
    const { r, title } = await createUed('crud')
    expect(r.code).toBe(200)
    const t = await findByTitle(title)
    expect(t.uedNo, '编号格式 UED-YYYY-NNNN').toMatch(/^UED-\d{4}-\d{4}$/)
    expect(t.status, '新建默认草稿').toBe('00')
    expect(t.aiGenerated, '未评审前 aiGenerated=N').toBe('N')
    expect(t.versionLabel).toBe('v1.0')
  })

  test('TC-Ued-F002 编码守门员: 中文 title + 农业组件标签 DB HEX 无 EFBFBD', async () => {
    const title = `编码自检-灌溉控制台设计稿-${RUN_ID}`
    const { r } = await createUed('enc', { title, agriComponentTags: '农情大屏组件,地块地图组件,病虫害预警卡' })
    expect(r.code).toBe(200)
    const t = await findByTitle(title)
    const encTitle = assertNoMojibake('tb_ued', 'title', `ued_id=${t.uedId}`)
    expect(encTitle.ok, encTitle.reason).toBe(true)
    expect(encTitle.hex, 'title 应以「编码」UTF-8 字节开头').toContain('E7BC96E7A081')
    const encTags = assertNoMojibake('tb_ued', 'agri_component_tags', `ued_id=${t.uedId}`)
    expect(encTags.ok, encTags.reason).toBe(true)
  })

  test('TC-Ued-F003 必填校验 (title / projectId / designerUserId) → 602', async () => {
    expect((await api.post('/business/ued', { projectId, designerUserId: 1 })).code).toBe(ERROR_CODES.REQUIRED_FIELD)
    expect((await api.post('/business/ued', { title: `no-proj-${RUN_ID}`, designerUserId: 1 })).code).toBe(ERROR_CODES.REQUIRED_FIELD)
    const r = await api.post('/business/ued', { projectId, title: `no-designer-${RUN_ID}` })
    expect(r.code).toBe(ERROR_CODES.REQUIRED_FIELD)
    expect(r.msg).toContain('设计师')
  })

  test('TC-Ued-F005 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/ued', {
      projectId: 99999999, title: `fk-${RUN_ID}`, designerUserId: 1
    })
    expect(r.code).toBe(ERROR_CODES.FK_NOT_EXISTS)
    expect(r.msg).toContain('关联项目不存在')
  })

  test('TC-Ued-F006 新建状态非 00 → 601', async () => {
    const { r } = await createUed('non-draft', { status: '01' })
    expect(r.code).toBe(ERROR_CODES.STATUS_VIOLATION)
    expect(r.msg).toContain('草稿')
  })

  test('TC-Ued-F007 状态机合法 00→01→02→03 + 反向边 01→00', async () => {
    const a = await createUed('sm-fwd')
    const idA = (await findByTitle(a.title)).uedId
    expect((await api.put('/business/ued', { uedId: idA, status: '01' })).code, '00→01').toBe(200)
    expect((await api.put('/business/ued', { uedId: idA, status: '02' })).code, '01→02').toBe(200)
    expect((await api.put('/business/ued', { uedId: idA, status: '03' })).code, '02→03').toBe(200)
    const b = await createUed('sm-reverse')
    const idB = (await findByTitle(b.title)).uedId
    await api.put('/business/ued', { uedId: idB, status: '01' })
    expect((await api.put('/business/ued', { uedId: idB, status: '00' })).code, '01→00 评审打回').toBe(200)
  })

  test('TC-Ued-F008 状态机非法 (跨级/反向/终态) 全 601', async () => {
    const a = await createUed('il-jump')
    const idA = (await findByTitle(a.title)).uedId
    expect.soft((await api.put('/business/ued', { uedId: idA, status: '02' })).code, '00→02 跨级').toBe(ERROR_CODES.STATUS_VIOLATION)

    const b = await createUed('il-rev')
    const idB = (await findByTitle(b.title)).uedId
    await api.put('/business/ued', { uedId: idB, status: '01' })
    await api.put('/business/ued', { uedId: idB, status: '02' })
    expect.soft((await api.put('/business/ued', { uedId: idB, status: '01' })).code, '02→01 反向').toBe(ERROR_CODES.STATUS_VIOLATION)

    const c = await createUed('il-term')
    const idC = (await findByTitle(c.title)).uedId
    await api.put('/business/ued', { uedId: idC, status: '01' })
    await api.put('/business/ued', { uedId: idC, status: '02' })
    await api.put('/business/ued', { uedId: idC, status: '03' })
    expect.soft((await api.put('/business/ued', { uedId: idC, status: '02' })).code, '03→02 终态').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-Ued-F009 aiReview → 评审报告 + 规范检查 + 评分 + aiGenerated=Y', async () => {
    const { title } = await createUed('ai')
    const t = await findByTitle(title)
    const r = await api.post(`/business/ued/ai/review/${t.uedId}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.aiGeneratedAt).toBeTruthy()
    expect(r.data.aiReviewReport, '评审报告非空').toBeTruthy()
    expect(r.data.complianceCheck, '规范检查 JSON').toContain('accessibility')
    expect(Number(r.data.aiReviewScore), '评审评分 > 0').toBeGreaterThan(0)
  })

  test('TC-Ued-UI UED 设计协同菜单可访问', async ({ page, context, request }) => {
    await loginAsAdmin(request, context)
    await page.goto('/business/ued')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })
  })
})
