/**
 * Arch 模块 E2E — PRD §F3.1 系统概要设计 HLD + 原型 archdesign.html
 *
 * 覆盖 (占位 1 → 真实 11 case;让 Arch 转 🟢):
 *   - CRUD + ARCH-YYYY-NNNN 编号 + 默认值 (status=00 / aiGenerated=N)
 *   - 编码守门员: 中文 title DB HEX 无 EFBFBD
 *   - 6 ENUM 白名单 (archMode / iotProtocol ...) → 604
 *   - FK projectId 不存在 → 702 / 必填 → 602 / 新建非草稿 → 601
 *   - 4 状态机含反向边: 00→01→{00,02}→03 终态 (01→00 评审打回)
 *   - aiGenerate: designContent + c4DiagramContent (Mermaid C4) + nfrMapping + aiGenerated=Y
 *   - UI 菜单可达
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, assertNoMojibake } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

/** 创建架构方案 (6 枚举默认全填合法值),返回响应 + title */
async function createArch(suffix: string, overrides: Record<string, any> = {}) {
  const title = `ARCH-${suffix}-${RUN_ID}`
  const r = await api.post('/business/arch', {
    projectId,
    title,
    archMode: 'microservice',
    primaryStack: 'java_sb3',
    databaseChoice: 'pg_redis',
    aiOrchestration: 'dify_deepseek',
    deploymentType: 'k8s',
    iotProtocol: 'mqtt',
    authorUserId: 1,
    ...overrides
  })
  return { r, title }
}

async function findByTitle(title: string): Promise<any> {
  const list = await api.get('/business/arch/list', { pageSize: 200 })
  return list.rows.find((x: any) => x.title === title)
}

test.describe('Arch 模块 E2E (PRD §F3.1)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`arch-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`arch-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_arch', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Arch-F001 创建架构方案 + ARCH-YYYY-NNNN 编号 + 默认值', async () => {
    const { r, title } = await createArch('crud')
    expect(r.code).toBe(200)
    const t = await findByTitle(title)
    expect(t.archNo, '编号格式 ARCH-YYYY-NNNN').toMatch(/^ARCH-\d{4}-\d{4}$/)
    expect(t.status, '新建默认草稿').toBe('00')
    expect(t.aiGenerated, '未生成前 aiGenerated=N').toBe('N')
    expect(t.archMode).toBe('microservice')
  })

  test('TC-Arch-F002 编码守门员: 中文 title DB HEX 无 EFBFBD', async () => {
    const title = `编码自检-架构方案-${RUN_ID}`
    const r = await api.post('/business/arch', {
      projectId, title, authorUserId: 1, remark: '需求标题：测试，结束。αβγ 🎯'
    })
    expect(r.code).toBe(200)
    const t = await findByTitle(title)
    const enc = assertNoMojibake('tb_arch', 'title', `arch_id=${t.archId}`)
    expect(enc.ok, enc.reason).toBe(true)
    expect(enc.hex, 'title 应以「编码」UTF-8 字节开头').toContain('E7BC96E7A081')
  })

  test('TC-Arch-F003 必填校验 (title / projectId / authorUserId) → 602', async () => {
    expect((await api.post('/business/arch', { projectId, authorUserId: 1 })).code).toBe(ERROR_CODES.REQUIRED_FIELD)
    expect((await api.post('/business/arch', { title: `no-proj-${RUN_ID}`, authorUserId: 1 })).code).toBe(ERROR_CODES.REQUIRED_FIELD)
    expect((await api.post('/business/arch', { projectId, title: `no-author-${RUN_ID}` })).code).toBe(ERROR_CODES.REQUIRED_FIELD)
  })

  test('TC-Arch-F004 ENUM 非白名单 (archMode / iotProtocol) → 604', async () => {
    const a = await createArch('bad-mode', { archMode: 'mesh_xyz' })
    expect(a.r.code, 'archMode 非法').toBe(ERROR_CODES.FIELD_FORMAT)
    const b = await createArch('bad-iot', { iotProtocol: 'coap' })
    expect(b.r.code, 'iotProtocol 非法').toBe(ERROR_CODES.FIELD_FORMAT)
  })

  test('TC-Arch-F005 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/arch', {
      projectId: 99999999, title: `fk-${RUN_ID}`, authorUserId: 1
    })
    expect(r.code).toBe(ERROR_CODES.FK_NOT_EXISTS)
    expect(r.msg).toContain('关联项目不存在')
  })

  test('TC-Arch-F006 新建状态非 00 → 601', async () => {
    const { r } = await createArch('non-draft', { status: '01' })
    expect(r.code).toBe(ERROR_CODES.STATUS_VIOLATION)
    expect(r.msg).toContain('草稿')
  })

  test('TC-Arch-F007 状态机合法 00→01→02→03 + 反向边 01→00', async () => {
    // 正向链
    const a = await createArch('sm-fwd')
    const idA = (await findByTitle(a.title)).archId
    expect((await api.put('/business/arch', { archId: idA, status: '01' })).code, '00→01').toBe(200)
    expect((await api.put('/business/arch', { archId: idA, status: '02' })).code, '01→02').toBe(200)
    expect((await api.put('/business/arch', { archId: idA, status: '03' })).code, '02→03').toBe(200)
    // 反向打回
    const b = await createArch('sm-reverse')
    const idB = (await findByTitle(b.title)).archId
    await api.put('/business/arch', { archId: idB, status: '01' })
    expect((await api.put('/business/arch', { archId: idB, status: '00' })).code, '01→00 评审打回').toBe(200)
  })

  test('TC-Arch-F008 状态机非法 (跨级/反向/终态) 全 601', async () => {
    const a = await createArch('il-jump')
    const idA = (await findByTitle(a.title)).archId
    expect.soft((await api.put('/business/arch', { archId: idA, status: '02' })).code, '00→02 跨级').toBe(ERROR_CODES.STATUS_VIOLATION)

    const b = await createArch('il-rev')
    const idB = (await findByTitle(b.title)).archId
    await api.put('/business/arch', { archId: idB, status: '01' })
    await api.put('/business/arch', { archId: idB, status: '02' })
    expect.soft((await api.put('/business/arch', { archId: idB, status: '01' })).code, '02→01 反向').toBe(ERROR_CODES.STATUS_VIOLATION)

    const c = await createArch('il-term')
    const idC = (await findByTitle(c.title)).archId
    await api.put('/business/arch', { archId: idC, status: '01' })
    await api.put('/business/arch', { archId: idC, status: '02' })
    await api.put('/business/arch', { archId: idC, status: '03' })
    expect.soft((await api.put('/business/arch', { archId: idC, status: '02' })).code, '03→02 终态').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-Arch-F009 aiGenerate → designContent + C4 容器图 + nfrMapping + aiGenerated=Y', async () => {
    const { title } = await createArch('ai')
    const t = await findByTitle(title)
    const r = await api.post(`/business/arch/ai/generate/${t.archId}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.aiGeneratedAt).toBeTruthy()
    expect(r.data.designContent, '设计正文非空').toBeTruthy()
    expect(r.data.c4DiagramContent, 'C4 容器图 Mermaid').toContain('C4Container')
    expect(r.data.nfrMapping, 'NFR 映射含性能').toContain('性能')
  })

  test('TC-Arch-UI 概要设计管理菜单可访问', async ({ page, context, request }) => {
    await loginAsAdmin(request, context)
    await page.goto('/business/arch')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })
  })
})
