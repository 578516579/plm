// PLM Defect 新增并发 — 压力场景(stress)
// 100 VU × 2min,P95 < 800ms,错误率 < 1%
// 跑法: k6 run --env-file env/local.env scenarios/defect-create-post.js
//
// 依赖:压测前需先跑 seed-project.sql(项目 id 100-149)+ seed-testcase.sql(testcase id 500-699)

import { sleep } from 'k6'
import { Counter } from 'k6/metrics'
import { login } from '../lib/auth.js'
import { plmPost } from '../lib/http.js'
import { fakeDefect, randomInt } from '../lib/data.js'

const createSuccessCounter = new Counter('defect_create_success')

export const options = {
  scenarios: {
    defect_stress_create: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100 },
        { duration: '90s', target: 100 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    'http_req_duration{name:defect-create}': ['p(95)<800', 'p(99)<2000'],
    'http_req_failed': ['rate<0.01'],
    'checks': ['rate>0.99'],
  },
}

export default function () {
  const token = login()

  const projectId = randomInt(100, 149)
  const testcaseId = randomInt(500, 699)
  const payload = fakeDefect(projectId, testcaseId)
  const res = plmPost(token, '/business/defect', payload, 'defect-create')

  const code = res.json('code')
  if (code === 200) {
    createSuccessCounter.add(1)
  }

  sleep(Number(__ENV.THINK_TIME || 0.5))
}

// 清理:压测会插入大量数据,跑完后清
// DELETE FROM tb_defect WHERE create_by='admin' AND title LIKE 'Bug-压测-%';
