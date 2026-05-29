# PLM 性能 / 压力测试 (k6)

PLM 项目性能与压力测试目录,基于 [k6](https://k6.io)(JS 脚本,JVM 系外测试工具)。

## 为什么用 k6

| 候选 | 取舍 |
|---|---|
| **k6** ✅ | JS 脚本,与前端栈一致;CLI + HTML 报告;指标可推 Prometheus/InfluxDB;无 GUI 包袱;开源 + Grafana 出品 |
| JMeter | XML/.jmx 编辑麻烦,生态成熟但不适合 GitOps |
| Locust | Python 栈,项目本身无 Python,引入额外维护成本 |
| Gatling | JVM 系最强但需 Scala/Kotlin DSL,学习曲线陡 |

决策来源:2026-05-28 测试范围对齐会(本次任务)。

## 安装

```powershell
# Windows (Chocolatey)
choco install k6

# macOS (Homebrew)
brew install k6

# 验证
k6 version    # >= 0.49
```

## 目录布局

```
plm-perf-test/
├── env/                       # 环境变量(本地/staging/prod 三套,base + override)
│   └── local.env.example
├── lib/                       # 共用库(可被 scenarios 复用)
│   ├── auth.js               # captcha + login → 拿 JWT token
│   ├── data.js               # 数据 fake(项目名/编号/日期)
│   └── http.js               # 通用 client(baseURL + headers + 状态码断言)
└── scenarios/                 # 具体场景(每文件一个独立可跑场景)
    ├── smoke.js              # 冒烟:登录 + GET 列表(所有业务模块)
    ├── project-list-get.js   # Project 列表分页压测
    ├── project-create-post.js   # Project 新增并发(测 ADR-0001 撞号重试)
    └── project-state-transition.js  # Project 状态机并发(测 PRD §3.3 冲突)
```

## 快速跑

```powershell
# 1. 复制 env 并填密码
Copy-Item env\local.env.example env\local.env
notepad env\local.env

# 2. 启后端(默认 8081)/前端代理 + MySQL/Redis
#    见 CLAUDE.md "Running locally"

# 3. 跑冒烟
k6 run --env-file env\local.env scenarios\smoke.js

# 4. 跑某个场景
k6 run --env-file env\local.env scenarios\project-list-get.js

# 5. 出 HTML 报告
k6 run --env-file env\local.env --out json=run.json scenarios\project-create-post.js
# 再用 https://k6.io/docs/results-output/web-dashboard/ 查看
```

## 场景设计原则

| 场景类型 | VU(并发用户)| 持续时间 | 目标 |
|---|---|---|---|
| **smoke** | 1 | 30s | 验脚本不爆;CI 必跑 |
| **load**(负载)| 50 → 100 | 5min | 模拟正常业务;P95 < 500ms |
| **stress**(压力)| 200 → 500 | 10min | 找拐点;系统不崩 |
| **spike**(脉冲)| 0 → 1000 in 30s | 2min | 突发流量;自动扩容验证 |
| **soak**(浸泡)| 50 | 1h+ | 内存泄漏;连接池泄漏 |

每个 scenarios/*.js 都需在脚本顶部 `export const options = { ... }` 明确属于哪类。

## 性能基线(从 5×5 状态机 + ADR-0001 测出)

| 接口 | 期望 P95 | 期望 P99 | 错误率上限 |
|---|---|---|---|
| GET /business/project/list | < 200ms | < 500ms | 0% |
| GET /business/project/{id} | < 100ms | < 300ms | 0% |
| POST /business/project | < 300ms | < 800ms | < 0.1%(撞号重试)|
| PUT /business/project (状态变更)| < 200ms | < 500ms | < 5%(并发同 ID 状态变更冲突)|

随业务扩展,基线在每个 scenarios/*.js 顶部 `thresholds:` 中以代码形式锁定 — failed thresholds → k6 exit 非 0,CI 阻断。

## 关联

- 测试规范: [../03-开发/测试规范.md](../03-开发/测试规范.md)
- 测试金字塔层级: 单测(50) → 组件(25) → 契约(本项目新增) → E2E(36) → **性能(本目录)**
- 在途任务台账: [../99-跨阶段/在途任务.md](../99-跨阶段/在途任务.md)

## 排查

| 现象 | 处理 |
|---|---|
| 401 Unauthorized 大量 | auth.js token 缓存过期;`--env REFRESH_TOKEN=1` 重新登录 |
| 5xx 大量 | 后端日志看是否 DB 锁;先 cleanup seed 再跑 |
| RPS 极低(< 10) | Druid 连接池满;`application-druid.yml` 调 `initialSize/maxActive` |
| k6 报"too many open files" | Windows 改 ulimit / WSL 跑 |
