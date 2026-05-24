/**
 * Task 模块 E2E 测试 — 覆盖 6×6 状态机含反向边 + 看板 + 我的任务 + 3 FK
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import {
  RUN_ID,
  makeProjectData,
  makeRequirementData,
  makeSprintData,
  makeTaskData,
  ERROR_CODES
} from './helpers/fixtures'

let token: string
let api: ApiClient
let apiRequest: APIRequestContext
let projectId: number
let requirementId: number
let sprintId: number

test.describe('Task 模块 E2E', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)

    // 建 Project + Requirement + Sprint 作为 FK
    await api.createProject(makeProjectData(`task-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`task-suite-${RUN_ID}`))?.id

    await api.createRequirement(makeRequirementData(projectId, `task-suite-${RUN_ID}`))
    const rl = await api.listRequirements()
    requirementId = rl.rows.find((r: any) => r.title.includes(`task-suite-${RUN_ID}`))?.requirementId

    await api.createSprint(makeSprintData(projectId, `task-suite-${RUN_ID}`))
    const sl = await api.listSprints()
    sprintId = sl.rows.find((s: any) => s.name.includes(`task-suite-${RUN_ID}`))?.sprintId
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_task', `project_id=${projectId}`)
      if (sprintId) execDelete('tb_sprint', `sprint_id=${sprintId}`)
      if (requirementId) execDelete('tb_requirement', `requirement_id=${requirementId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-Task-F001 CRUD + ADR-0003 编号', async () => {
    const data = makeTaskData(projectId, sprintId, requirementId, `crud-${RUN_ID}`)
    const resp = await api.createTask(data)
    expect(resp.code).toBe(200)
    const list = await api.listTasks()
    const t = list.rows.find((x: any) => x.title === data.title)
    expect(t.taskNo).toMatch(/^TASK-\d{4}-\d{4}$/)
    expect(t.status).toBe('00')
  })

  test('TC-Task-F004 反向边 02→01 (评审打回)', async () => {
    const data = makeTaskData(projectId, sprintId, requirementId, `reverse-${RUN_ID}`)
    const create = await api.createTask(data)
    expect(create.code).toBe(200)
    const list = await api.listTasks()
    const t = list.rows.find((x: any) => x.title === data.title)
    const id = t.taskId

    // 推 00 → 01 → 02
    await api.updateTask({ taskId: id, status: '01' })
    await api.updateTask({ taskId: id, status: '02' })

    // 02 → 01 (反向边)
    const resp = await api.updateTask({ taskId: id, status: '01' })
    expect(resp.code, '02→01 反向边应允许 (评审打回)').toBe(200)
  })

  test('TC-Task-F005 反向边 03→02 (测试打回)', async () => {
    const data = makeTaskData(projectId, sprintId, requirementId, `reverse2-${RUN_ID}`)
    const create = await api.createTask(data)
    expect(create.code).toBe(200)
    const list = await api.listTasks()
    const t = list.rows.find((x: any) => x.title === data.title)
    const id = t.taskId

    // 推 00 → 01 → 02 → 03
    await api.updateTask({ taskId: id, status: '01' })
    await api.updateTask({ taskId: id, status: '02' })
    await api.updateTask({ taskId: id, status: '03' })

    // 03 → 02 (反向边)
    const resp = await api.updateTask({ taskId: id, status: '02' })
    expect(resp.code, '03→02 反向边应允许 (测试打回)').toBe(200)
  })

  test('TC-Task-F006 进入 04 必填 actualHours', async () => {
    const data = makeTaskData(projectId, sprintId, requirementId, `done-${RUN_ID}`)
    const create = await api.createTask(data)
    expect(create.code).toBe(200)
    const list = await api.listTasks()
    const t = list.rows.find((x: any) => x.title === data.title)
    const id = t.taskId

    // 推 00 → 01 → 02 → 03
    await api.updateTask({ taskId: id, status: '01' })
    await api.updateTask({ taskId: id, status: '02' })
    await api.updateTask({ taskId: id, status: '03' })

    // 03 → 04 但不带 actualHours → 应失败
    let resp = await api.updateTask({ taskId: id, status: '04' })
    expect(resp.code).toBe(ERROR_CODES.REQUIRED_FIELD)
    expect(resp.msg).toContain('实际工时')

    // 03 → 04 + actualHours → 应成功
    resp = await api.updateTask({ taskId: id, status: '04', actualHours: 5.5 })
    expect(resp.code).toBe(200)
  })

  test('TC-Task-F008 3 FK 校验 — Sprint 不存在 702', async () => {
    const resp = await api.createTask({
      projectId,
      sprintId: 99999,
      title: `FK 测试-${RUN_ID}`,
      priority: '02'
    })
    expect(resp.code).toBe(ERROR_CODES.FK_NOT_EXISTS)
    expect(resp.msg).toContain('迭代')
  })

  test('TC-Task-F008 3 FK 校验 — Requirement 不存在 702', async () => {
    const resp = await api.createTask({
      projectId,
      requirementId: 99999,
      title: `FK 测试-${RUN_ID}`,
      priority: '02'
    })
    expect(resp.code).toBe(ERROR_CODES.FK_NOT_EXISTS)
    expect(resp.msg).toContain('需求')
  })

  test('TC-Task-F009 MR URL 格式校验', async () => {
    const resp = await api.createTask({
      projectId,
      title: `MR 格式-${RUN_ID}`,
      priority: '02',
      mrUrl: 'not-a-url'
    })
    expect(resp.code).toBe(ERROR_CODES.FIELD_FORMAT)
    expect(resp.msg).toContain('MR')
  })

  test('TC-Task-F010 看板视图返回 5 列', async () => {
    const kb = await api.taskKanban(projectId, sprintId)
    expect(kb.code).toBe(200)
    expect(kb.data.columns).toHaveLength(5)
    expect(kb.data.columns[0].status).toBe('00')
    expect(kb.data.columns[0].label).toBe('待开发')
    expect(kb.data.columns[4].status).toBe('04')
    expect(kb.data.columns[4].label).toBe('已完成')
  })

  test('TC-Task-F011 我的任务端点', async () => {
    const mine = await api.myTasks()
    expect(mine.code).toBe(200)
    expect(mine.rows).toBeDefined()
    // 当前用户是 admin (userId=1),我们建的 task assigneeUserId=1
    mine.rows.forEach((t: any) => {
      if (t.assigneeUserId) expect(t.assigneeUserId).toBe(1)
    })
  })

  test('UI 层: 任务管理 + 看板 + 我的任务 三个菜单都可访问', async ({ page, context }) => {
    await context.addCookies([{ name: 'Admin-Token', value: token, url: 'http://localhost' }])

    await page.goto('/business/task')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 60_000 })

    await page.goto('/business/taskkanban')
    // 看板页只验证容器存在,kanban-board 内 columns 空时高度为 0 不算 visible
    await expect(page.locator('.app-container').first()).toBeVisible({ timeout: 10_000 })
    await expect(page.getByPlaceholder(/必填|项目ID/).first()).toBeVisible({ timeout: 5_000 })

    // "我的任务"菜单 parent_id=0 是顶级菜单,实际路径是 /mytask 不是 /business/mytask
    await page.goto('/mytask')
    await expect(page.locator('.app-container, .el-result').first()).toBeVisible({ timeout: 10_000 })
  })
})
