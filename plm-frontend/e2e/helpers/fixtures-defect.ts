/**
 * Defect 模块测试数据 + 状态机 (附加到 fixtures.ts 主集)
 */
import { RUN_ID } from './fixtures'

export function makeDefectData(projectId: number, suffix?: string) {
  const tag = suffix || RUN_ID
  return {
    projectId,
    title: `E2E 缺陷-${tag}`,
    description: `自动测试发现的 bug ${tag}`,
    severity: '01',          // P1 严重
    category: '01',          // 功能
    reporterUserId: 1,
    reproduceSteps: '1. 登录\n2. 进入项目管理\n3. 点击新增',
    expectedResult: '能正常新增项目',
    actualResult: '提交后报 500 错误',
    tags: 'regression,automation'
  }
}

export const DEFECT_STATUS_TRANSITIONS = {
  legal: [
    { from: '00', to: '01', name: '新建→已确认' },
    { from: '01', to: '02', name: '已确认→处理中' },
    { from: '01', to: '04', name: '已确认→已关闭 (无效)' },
    { from: '02', to: '01', name: '处理中→已确认 (重新分析)' },
    { from: '02', to: '03', name: '处理中→已解决' }, // 需带 resolution
    { from: '03', to: '01', name: '已解决→已确认 (反向边·回归打回)' },
    { from: '03', to: '04', name: '已解决→已关闭' }
  ],
  illegal: [
    { from: '00', to: '02', name: '新建→处理中 (跨级)' },
    { from: '00', to: '03', name: '新建→已解决 (跨级)' },
    { from: '00', to: '04', name: '新建→已关闭 (跨级)' },
    { from: '01', to: '03', name: '已确认→已解决 (跨级)' },
    { from: '04', to: '01', name: '已关闭→已确认 (终态保护)' }
  ]
}
