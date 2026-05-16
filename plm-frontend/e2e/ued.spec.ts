/** Ued 模块 E2E — PRD §F2.3 UED 设计协同 + 原型 ued.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

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

  test('TC-Ued-F001 创建 UED 设计 (Figma + 农业组件标签)', async () => {
    const r = await api.post('/business/ued', {
      projectId,
      title: `灌溉控制台 v2.1 设计稿-${RUN_ID}`,
      figmaUrl: 'https://www.figma.com/file/abc123/Irrigation-v2.1',
      figmaFileKey: 'abc123',
      versionLabel: 'v2.1',
      agriComponentTags: '农情大屏组件,IoT数据看板,地块地图组件',
      designerUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
