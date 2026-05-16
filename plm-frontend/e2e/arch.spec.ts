/** Arch 模块 E2E — PRD §F3.1 系统概要设计 HLD + 原型 archdesign.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

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

  test('TC-Arch-F001 创建架构方案 (6 技术选型枚举)', async () => {
    const r = await api.post('/business/arch', {
      projectId,
      title: `AI 灌溉决策平台架构-${RUN_ID}`,
      archMode: 'microservice',
      primaryStack: 'java_sb3',
      databaseChoice: 'pg_redis',
      aiOrchestration: 'dify_deepseek',
      deploymentType: 'k8s',
      iotProtocol: 'mqtt',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
