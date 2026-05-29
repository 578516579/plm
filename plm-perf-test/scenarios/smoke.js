// PLM 冒烟性能测试 — 1 VU × 30s,验证脚本/服务都能跑
// 用途:CI 必跑,作为后续 load/stress 的前置门
// 跑法: k6 run --env-file env/local.env scenarios/smoke.js

import { sleep } from 'k6'
import { login } from '../lib/auth.js'
import { plmGet } from '../lib/http.js'

export const options = {
  vus: 1,
  duration: '30s',
  thresholds: {
    // 冒烟阶段:任何 HTTP 失败/业务失败都算失败
    'checks': ['rate>0.99'],
    'http_req_duration': ['p(95)<1000'], // 单用户 P95 < 1s
    'http_req_failed': ['rate<0.01'],
  },
}

const MODULES = [
  'project',
  'sprint',
  'task',
  'requirement',
  'prd',
  'testcase',
  'defect',
  'testreport',
]

export default function () {
  const token = login()

  // 1. 登录后续 ping(getInfo 用户)
  plmGet(token, '/getInfo', {}, 'getInfo')

  // 2. 遍历核心业务模块的 list 接口(单页 10 行)
  for (const m of MODULES) {
    plmGet(token, `/business/${m}/list`, { pageNum: 1, pageSize: 10 }, `${m}-list`)
  }

  sleep(Number(__ENV.THINK_TIME || 1))
}
