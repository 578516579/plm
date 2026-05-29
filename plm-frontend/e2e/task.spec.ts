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

  test('TC-Task-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    // 1. 建一个独立 task (避免与 F001 等其他 case 串)
    const data = makeTaskData(projectId, sprintId, requirementId, `del-${RUN_ID}`)
    const createResp = await api.createTask(data)
    expect(createResp.code, '创建应成功').toBe(200)

    // 2. 确认它出现在列表里,拿到 taskId
    const before = await api.listTasks()
    const created = before.rows.find((x: any) => x.title === data.title)
    expect(created, '新建 task 应能在列表里查到').toBeDefined()
    const id: number = created.taskId
    expect(typeof id, 'taskId 应是 number').toBe('number')

    // 3. 删除
    const delResp = await api.deleteTask(id)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    // 4. 再 list,该 taskId 应消失 (软删 del_flag=2,list 端默认过滤掉)
    const after = await api.listTasks()
    const stillThere = after.rows.find((x: any) => x.taskId === id)
    expect(stillThere, `taskId=${id} 删除后不该出现在 list`).toBeUndefined()
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

  test('UI 层: 任务管理 + 看板 + 我的任务 三个菜单都可访问', async ({ page, context, request }) => {
    // 用 loginAsAdmin 给 fresh context 注入完整 cookie + 触发 fresh login
    // 旧 context.addCookies 在 vue-router 动态路由场景下拿不到菜单 → /business/task 404
    await loginAsAdmin(request, context)

    await page.goto('/business/task')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 60_000 })

    await page.goto('/business/taskkanban')
    // 看板页只验证容器存在,kanban-board 内 columns 空时高度为 0 不算 visible
    await expect(page.locator('.app-container').first()).toBeVisible({ timeout: 10_000 })
    await expect(page.getByPlaceholder(/必填|项目ID/).first()).toBeVisible({ timeout: 5_000 })

    // "我的任务"菜单当前 parent_id=2930 phase-dev, 子菜单 path='/business/mytask' (绝对路径)
    // 历史: 一度 parent_id=0 走顶级 /mytask, 现已统一到 /business/<entity> 契约
    await page.goto('/business/mytask')
    await expect(page.locator('.app-container, .el-result').first()).toBeVisible({ timeout: 10_000 })
  })
})
