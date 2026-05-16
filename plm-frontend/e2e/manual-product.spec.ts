/** ManualProduct 模块 E2E — PRD §F5.1 AI 一键生成 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

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
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_manual_product', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-PM-F001 创建产品手册 (含模块 + 多格式)', async () => {
    const r = await api.post('/business/manual-product', {
      projectId,
      title: `PLM v0.4 产品手册-${RUN_ID}`,
      productVersion: 'v0.4.0',
      includeModules: '系统概述,快速上手,功能详细说明,常见问题FAQ',
      outputFormats: 'pdf,html',
      aiGenerated: 'Y',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
