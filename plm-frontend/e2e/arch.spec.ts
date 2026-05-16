/** 架构设计模块 E2E — PRD §F3.1 系统架构设计 + 原型 archdesign.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('架构设计模块 E2E (PRD §F3.1)', () => {
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

  test('TC-ARCH-F001 创建架构方案 (微服务 + Java + Kubernetes)', async () => {
    const r = await api.post('/business/arch', {
      projectId,
      title: `AgriPLM微服务架构方案-${RUN_ID}`,
      archMode: 'microservice',
      techStack: 'java_springboot3',
      dbStack: 'mysql_redis',
      aiOrchestration: 'dify_deepseek',
      deployMode: 'kubernetes',
      iotProtocol: 'mqtt_emqx',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-ARCH-F002 创建架构方案 (单体 + Python)', async () => {
    const r = await api.post('/business/arch', {
      projectId,
      title: `农情监测单体架构-${RUN_ID}`,
      archMode: 'monolith',
      techStack: 'python_fastapi',
      dbStack: 'postgresql_redis',
      deployMode: 'docker_compose',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
