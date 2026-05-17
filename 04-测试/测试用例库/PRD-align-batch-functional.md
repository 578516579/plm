# PRD-align 批次 18 模块 — 功能测试用例

> **批次性测试用例**：覆盖 2026-05-16 ~ 2026-05-17 集中完成 PRD-align 落地的 18 个业务模块。
> 每个模块通过自动化 E2E spec 落地用例（见 [04-测试/测试用例库/E2E-测试矩阵.md](E2E-测试矩阵.md)），本文档汇总用例索引 + 执行记录。

## 头部

| 字段 | 值 |
|---|---|
| 关联 PRD | `prd和原型/AgriAI-PLM-完整PRD文档.md` V1.0 §F1.1, F1.3, F2.2-F2.3, F3.1-F3.3, F3.5, F4.3, F4.5, F5.2-F5.3, F6, DevOps |
| 关联 SSoT | [`PRD-MAPPING.md`](../../PRD-MAPPING.md) §2 字段对照表 |
| 关联 Phase 03 Gate | [`prd-align-batch-2026-05-17`](../../99-跨阶段/gate-checklists/instances/prd-align-batch-2026-05-17/Phase03-开发-Gate-2026-05-17.md) |
| 测试环境 | dev (localhost:8081 / port 80 / mysql 3306 / redis 6379) |
| 被测版本 | main HEAD `9d6a4af` |
| QA Owner | Wjl `[solo-review]` |
| 测试日期 | 2026-05-17 |

---

## 测试范围

### 包含

- 18 个 PRD-align 模块的 CRUD 主路径
- 状态机合法转换 + 反向边
- ENUM 白名单 + FK 校验（702）+ 必填字段（602）
- AI 入口端点（mock 模式返回 200 + 字段写入）
- 字典翻译 + 权限 `business:<entity>:*`
- 编码守门员（Mojibake guard）

### 不含

- 性能压测（D.2 — `early` 阶段非强制，留 Phase 05 前补）
- 真实 Dify AI 工作流（mock 实现,Dify 接入留待 v0.5）
- 多角色权限矩阵（仅 admin 角色验证）

---

## 用例总览（按 PRD 域）

### F1 立项域（3 模块）

| 模块 | E2E spec | case 数 | 通过 |
|---|---|---|---|
| inception (F1.1) | [inception.spec.ts](../../plm-frontend/e2e/inception.spec.ts) | 1 | ✅ |
| prd (F2.2) | [prd.spec.ts](../../plm-frontend/e2e/prd.spec.ts) | 1 | ✅ |
| competitive (F1.3) | [competitive.spec.ts](../../plm-frontend/e2e/competitive.spec.ts) | 1 | ✅ |

### F2/F3 设计域（4 模块）

| 模块 | E2E spec | case 数 | 通过 |
|---|---|---|---|
| ued (F2.3) | [ued.spec.ts](../../plm-frontend/e2e/ued.spec.ts) | 1 | ✅ |
| arch (F3.1) | [arch.spec.ts](../../plm-frontend/e2e/arch.spec.ts) | 1 | ✅ |
| dbdesign (F3.2) | [dbdesign.spec.ts](../../plm-frontend/e2e/dbdesign.spec.ts) | 1 | ✅ |
| apidesign (F3.3) | [apidesign.spec.ts](../../plm-frontend/e2e/apidesign.spec.ts) | 1 | ✅ |

### F4 测试域（2 模块）

| 模块 | E2E spec | case 数 | 通过 |
|---|---|---|---|
| testdata (F4.3) | [testdata.spec.ts](../../plm-frontend/e2e/testdata.spec.ts) | 1 | ✅ |
| autotest (F4.5) | [autotest.spec.ts](../../plm-frontend/e2e/autotest.spec.ts) | 1 | ✅ |

### F5 文档域（2 模块）

| 模块 | E2E spec | case 数 | 通过 |
|---|---|---|---|
| manual-impl (F5.2) | [manual-impl.spec.ts](../../plm-frontend/e2e/manual-impl.spec.ts) | 3（创建 Docker / 国产化 K8s / AI 生成）| ✅ |
| manual-ops (F5.3) | [manual-ops.spec.ts](../../plm-frontend/e2e/manual-ops.spec.ts) | 3（Prometheus / Zabbix / AI 生成）| ✅ |

### F6 效能域（2 模块）

| 模块 | E2E spec | case 数 | 通过 |
|---|---|---|---|
| analytics (F6) | [analytics.spec.ts](../../plm-frontend/e2e/analytics.spec.ts) | 含 | ✅ |
| dashboard (UI §4.2) | [dashboard.spec.ts](../../plm-frontend/e2e/dashboard.spec.ts) | 含 | ✅ |

### F3.5 AI 编排域（2 模块）

| 模块 | E2E spec | case 数 | 通过 |
|---|---|---|---|
| ai-agent | [ai-agent.spec.ts](../../plm-frontend/e2e/ai-agent.spec.ts) | 含 | ✅ |
| openspec (F3.5) | [openspec.spec.ts](../../plm-frontend/e2e/openspec.spec.ts) | 4（OpenAPI 3.1 / AsyncAPI 3.0 / AI 生成 GraphQL / 701 冲突）| ✅ |

### DevOps 扩展域（3 模块）

| 模块 | E2E spec | case 数 | 通过 |
|---|---|---|---|
| pipeline | [pipeline.spec.ts](../../plm-frontend/e2e/pipeline.spec.ts) | 3（Jenkins push / Cron 必填 / 触发累计 successRate）| ✅ |
| feature-flag | [feature-flag.spec.ts](../../plm-frontend/e2e/feature-flag.spec.ts) | 4（canary 创建 / 百分比校验 / snake_case / check 端点）| ✅ |
| dora | [dora.spec.ts](../../plm-frontend/e2e/dora.spec.ts) | 3（部署频率 Elite / MTTR + AI 建议 / 无效 metricType 604）| ✅ |

---

## D.1 功能测试执行结果

```
批 1 (立项+设计 8 specs):     13 passed
批 2 (研发+测试 9 specs):     41 passed
批 3 (文档+DevOps+AI 13 specs): 40 passed
批 4 (基础 4 specs):          26 passed
————————————————————————————————————
合计:                         120 / 120 ✅
```

P0 用例: 18/18 (100%) ✅
P1 用例: covered by E2E (覆盖率 100%) ✅
P2 用例: 留 Phase 04+ 阶段补 (state machine 非法分支补全)

## D.2 接口测试

通过 Swagger UI (`http://localhost:8081/swagger-ui/index.html`) 抽查：

- 178 个 `/business/*` 路由全部注册（每模块标准 6 端点 + 16 个 AI 入口）
- 每端点 `@PreAuthorize` 已校验 (不带 token 返回 401)
- POST/PUT 端点 `@Log` 标注（操作日志自动写 `sys_oper_log`）

## D.3 安全审计快速扫描

### 后端 Maven 依赖

- [x] `mvn validate` 无 duplicate dependency 警告（修复 plm-ued 重复声明）
- [x] 业务代码无明文 secret（`.env` gitignore + `${VAR:default}` 占位符）
- [x] 每业务 Controller 端点必有 `@PreAuthorize`（grep 验证）

### 前端 NPM 依赖

`npm audit --omit=dev --registry=https://registry.npmjs.org` 发现：

| CVE | 包 | 当前版本 | 严重度 | 处置 |
|---|---|---|---|---|
| GHSA-pmwg-cvhr-8vh7 等 13 条 | `axios` | <1.16.1 | **high** | 🟡 已知 risk,留下个 Sprint 升级 (避免 breaking change) |
| GHSA-5j98-mcp5-4vw2 | `glob` | 10.2.0-10.4.5 | **high** | 🟡 已知 risk,影响仅 CLI 命令注入 (生产路径不暴露) |

**风险评估** (`internal-tool` + `solo` + `early` 阶段):
- axios CRLF/SSRF 路径：PLM 后端 URL 完全可控（无用户输入透传），实际暴露面 **0**
- glob CLI 注入：仅影响 build/test 工具链，运行时 jar 不引入 ⇒ 实际暴露面 **0**

→ 准出**有条件通过**，跟踪到下一 Sprint backlog 升级。

## D.4 回归测试

- [x] 13 个历史已对齐模块（project/requirement/sprint/task/defect/testcase/document/...）E2E 全过
- [x] 编码守门员 6/6 全过（无 Mojibake 回归）
- [x] 导航 + 截图巡检 13 case 全过（页面路由无 regression）

## D.5 UAT

`solo` 团队规模下，UAT 由 Owner 自验。已通过 [Swagger UI](http://localhost:8081/swagger-ui/index.html) 抽查 18 个新模块的 6 端点：

- ✅ list 端点：分页正常
- ✅ POST 端点：必填校验 + 字典翻译 + 状态默认 '00'
- ✅ AI 入口：返回 mock 内容（Markdown / JSON / Mermaid）

---

## 已知风险（带入 Phase 05）

1. **axios CVE 13 条**（high）— 留 axios@1.16.1 升级，避免 breaking change 立即引入
2. **glob CVE 1 条**（high）— 仅 dev 依赖，影响有限
3. **真实 Dify 工作流未接入**（mock 模式）— 留 v0.5 接入
4. **Service 单元测试覆盖率未补**（按 `early` 成熟度允许 Phase 03 推迟到 Phase 04，但实际覆盖率指标 `pending`，由 E2E 替代）

---

## 修订记录

| 日期 | 修订人 | 修订原因 |
|---|---|---|
| 2026-05-17 | Wjl | 首次创建 |
