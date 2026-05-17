/**
 * ManualProduct 模块 E2E — PRD §F5.1 AI 一键生成产品手册
 * 覆盖: CRUD + PM-YYYY-NNNN 编号 + 中文 HEX 守门员 + FK
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { assertNoMojibake, execDelete } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('ManualProduct 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`pm-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`pm-suite-${RUN_ID}`))?.id
    expect(projectId).toBeDefined()
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_manual_product', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-PM-F001 创建产品手册 + PM-YYYY-NNNN 编号', async () => {
    const title = `PLM v0.4 产品手册-${RUN_ID}`
    const r = await api.post('/business/manual-product', {
      projectId,
      title,
      productVersion: 'v0.4.0',
      includeModules: '系统概述,快速上手,功能详细说明,常见问题FAQ',
      outputFormats: 'pdf,html',
      aiGenerated: 'Y',
      authorUserId: 1
    })
    expect(r.code).toBe(200)

    const list = await api.get('/business/manual-product/list', { pageSize: 100 })
    const pm = list.rows.find((x: any) => x.title === title)
    expect(pm).toBeDefined()
    expect(pm.manualproductNo).toMatch(/^PM-\d{4}-\d{4}$/)
    expect(pm.status).toBe('00')
  })

  test('TC-PM-F002 编码守门员: 中文 title + includeModules + content 无乱码', async () => {
    const title = `中文手册-编码守门员-${RUN_ID}`
    const r = await api.post('/business/manual-product', {
      projectId,
      title,
      productVersion: 'v1.0',
      includeModules: '系统概述,快速上手,病虫害诊断,常见问题FAQ',
      outputFormats: 'pdf',
      authorUserId: 1
    })
    expect(r.code).toBe(200)

    const list = await api.get('/business/manual-product/list', { pageSize: 100 })
    const pm = list.rows.find((x: any) => x.title === title)
    await api.put('/business/manual-product', {
      manualproductId: pm.manualproductId,
      content: '# 系统概述\n\n本产品支持 αβγ 病虫害自动识别 🌾\n\n## 快速上手\n点击「拍照识别」即可'
    })

    const titleCheck = assertNoMojibake('tb_manual_product', 'title',
      `manualproduct_id=${pm.manualproductId}`)
    expect.soft(titleCheck.ok, `title HEX=${titleCheck.hex}`).toBe(true)
    const modulesCheck = assertNoMojibake('tb_manual_product', 'include_modules',
      `manualproduct_id=${pm.manualproductId}`)
    expect.soft(modulesCheck.ok, `include_modules HEX=${modulesCheck.hex}`).toBe(true)
    const contentCheck = assertNoMojibake('tb_manual_product', 'content',
      `manualproduct_id=${pm.manualproductId}`)
    expect.soft(contentCheck.ok, `content HEX=${contentCheck.hex}`).toBe(true)
  })

  test('TC-PM-F003 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/manual-product', {
      projectId: 999_999_999,
      title: `FK 测试-${RUN_ID}`,
      productVersion: 'v0.1',
      includeModules: '概述',
      outputFormats: 'pdf',
      authorUserId: 1
    })
    expect.soft(r.code, 'FK 不存在应返 702').toBe(ERROR_CODES.FK_NOT_EXISTS)
  })
})
