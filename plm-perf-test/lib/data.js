// PLM 测试数据生成器 — 项目/Sprint/Task/Defect 等
// 用法: import { fakeProject, randomInt } from '../lib/data.js'

import { randomString } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js'

/**
 * 随机整数 [min, max]
 */
export function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

/**
 * 随机字典 value 选一个
 */
export function pickOne(arr) {
  return arr[randomInt(0, arr.length - 1)]
}

/**
 * 生成一个 Project payload(不带 projectNo,让 ADR-0001 自动生成测撞号)
 * @returns {Object}
 */
export function fakeProject() {
  const projectTypes = ['rnd', 'upgrade', 'maintenance', 'research']
  const today = new Date()
  const endDate = new Date(today.getTime() + 90 * 86400_000) // +90 天

  return {
    projectName: `压测-${randomString(8)}-${Date.now()}`,
    projectType: pickOne(projectTypes),
    // status 不传 → 默认 "0"(未启动)
    managerUserId: 1, // admin
    startDate: today.toISOString().slice(0, 10),
    endDate: endDate.toISOString().slice(0, 10),
    budget: randomInt(10, 1000),
    description: `性能测试生成的项目 ${randomString(16)}`,
  }
}

/**
 * 生成一个 Sprint payload
 */
export function fakeSprint(projectId) {
  const today = new Date()
  const endDate = new Date(today.getTime() + 14 * 86400_000) // +14 天

  return {
    projectId,
    sprintName: `Sprint-压测-${randomString(8)}`,
    startDate: today.toISOString().slice(0, 10),
    endDate: endDate.toISOString().slice(0, 10),
    goal: '性能测试生成的 Sprint',
  }
}

/**
 * 生成一个 Task payload
 */
export function fakeTask(projectId, sprintId) {
  const priorities = ['1', '2', '3', '4']
  return {
    projectId,
    sprintId,
    title: `Task-压测-${randomString(10)}`,
    priority: pickOne(priorities),
    assigneeUserId: 1,
    estimateHours: randomInt(1, 40),
    description: `性能测试生成的 Task ${randomString(20)}`,
  }
}

/**
 * 生成一个 Defect payload
 */
export function fakeDefect(projectId, testcaseId) {
  const severities = ['P0', 'P1', 'P2', 'P3']
  return {
    projectId,
    testcaseId,
    title: `Bug-压测-${randomString(10)}`,
    severity: pickOne(severities),
    reproducer: `1. 步骤A\n2. 步骤B\n3. 期望:X 实际:Y`,
    environment: `Chrome 120 / Win11 / 性能测试`,
  }
}
