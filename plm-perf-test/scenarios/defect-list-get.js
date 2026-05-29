// PLM Defect 列表压测 — 负载场景(load)
// 50 VU × 5min,P95 < 500ms,错误率 < 0.1%
// 跑法: k6 run --env-file env/local.env scenarios/defect-list-get.js
//
// 依赖:压测前需先跑 seed-project.sql + seed-defect.sql(defect id 700-899)

import { sleep } from 'k6'
import { login } from '../lib/auth.js'
import { plmGet } from '../lib/http.js'
import { randomInt, pickOne } from '../lib/data.js'

export const options = {
  scenarios: {
    defect_load_list: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50 },
        { duration: '4m',  target: 50 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    'http_req_duration{name:defect-list}': ['p(95)<500', 'p(99)<1000'],
    'http_req_duration{name:defect-detail}': ['p(95)<300'],
    'http_req_failed': ['rate<0.001'],
    'checks': ['rate>0.999'],
  },
}

const SEVERITIES = ['P0', 'P1', 'P2', 'P3', '']
const STATUSES = ['0', '1', '2', '3', '4', '']
const PROJECT_IDS = [100, 101, 102, 103, 104, '']

export default function () {
  const token = login()

  if (Math.random() < 0.7) {
    const page = randomInt(1, 5)
    plmGet(
      token,
      '/business/defect/list',
      { pageNum: page, pageSize: 20 },
      'defect-list'
    )
  } else {
    plmGet(
      token,
      '/business/defect/list',
      {
        pageNum: 1,
        pageSize: 20,
        projectId: pickOne(PROJECT_IDS),
        severity: pickOne(SEVERITIES),
        status: pickOne(STATUSES),
      },
      'defect-list'
    )
  }

  if (Math.random() < 0.2) {
    plmGet(token, `/business/defect/${randomInt(700, 899)}`, {}, 'defect-detail')
  }

  sleep(Number(__ENV.THINK_TIME || 1))
}
