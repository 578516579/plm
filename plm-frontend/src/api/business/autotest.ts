/** 自动化测试套件 API — PRD §F4.5 */
import request from '@/utils/request'

export interface Autotest {
  autotestId?: number
  autotestNo?: string
  projectId: number
  title: string
  suiteType?: string                  // api/e2e/unit/performance
  framework?: string                  // playwright/pytest/jest/jmeter
  scriptContent?: string
  lastRunAt?: string
  lastRunResult?: string              // passed/failed/error
  passRate?: number
  totalCases?: number
  failedCases?: number
  executionTime?: number
  scheduleCron?: string
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02'
  authorUserId: number
  remark?: string
}

export interface AutotestQuery {
  pageNum?: number; pageSize?: number
  autotestNo?: string; projectId?: number; title?: string
  suiteType?: string; framework?: string; status?: string
}

export const listAutotest  = (q?: AutotestQuery) => request({ url: '/business/autotest/list', method: 'get', params: q })
export const getAutotest   = (id: number) => request({ url: `/business/autotest/${id}`, method: 'get' })
export const addAutotest   = (d: Autotest) => request({ url: '/business/autotest', method: 'post', data: d })
export const updateAutotest= (d: Autotest) => request({ url: '/business/autotest', method: 'put', data: d })
export const delAutotest   = (ids: number|number[]) => request({ url: `/business/autotest/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiScriptAutotest = (id: number) => request({ url: `/business/autotest/ai/script/${id}`, method: 'post' })
export const exportAutotest   = (q?: AutotestQuery) => request({ url: '/business/autotest/export', method: 'post', params: q, responseType: 'blob' })
