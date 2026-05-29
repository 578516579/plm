// PLM Project 列表压测 — 负载场景(load)
// 50 VU × 5min,P95 < 500ms,错误率 < 0.1%
// 跑法: k6 run --env-file env/local.env scenarios/project-list-get.js

import { sleep } from 'k6'
import { login } from '../lib/auth.js'
import { plmGet } from '../lib/http.js'
import { randomInt, pickOne } from '../lib/data.js'

export const options = {
  scenarios: {
    load_list: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50 },  // 爬坡到 50 VU
        { duration: '4m',  target: 50 },  // 稳态 4min
        { duration: '30s', target: 0 },   // 降回
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    // 业务基线:GET 列表 P95 < 500ms,P99 < 1000ms
    'http_req_duration{name:project-list}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:project-detail}': ['p(95)<300'],
    'http_req_failed': ['rate<0.001'],   // < 0.1%
    'checks': ['rate>0.999'],
  },
}

const PROJECT_TYPES = ['rnd', 'upgrade', 'maintenance', 'research', '']
const STATUSES = ['0', '1', '2', '3', '4', '']

export default function () {
  const token = login()

  // Scenario 70%: 翻页 list(无筛选)
  if (Math.random() < 0.7) {
    const page = randomInt(1, 5)
    plmGet(
      token,
      '/business/project/list',
      { pageNum: page, pageSize: 20 },
      'project-list'
    )
  } else {
    // Scenario 30%: 带筛选 list(type + status 组合)
    plmGet(
      token,
      '/business/project/list',
      {
        pageNum: 1,
        pageSize: 20,
        projectType: pickOne(PROJECT_TYPES),
        status: pickOne(STATUSES),
      },
      'project-list'
    )
  }

  // 偶尔抽 1 个详情看(模拟点击)— 假设有 PRJ-2026-0001 这类已存在数据
  if (Math.random() < 0.2) {
    plmGet(token, `/business/project/${randomInt(1, 50)}`, {}, 'project-detail')
  }

  sleep(Number(__ENV.THINK_TIME || 1))
}
