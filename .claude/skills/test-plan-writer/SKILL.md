---
name: test-plan-writer
description: PLM 测试计划编写 — 从 PRD + 原型 + 状态机 + 错误码 产 Phase 04 测试 Gate §B 必产出物。当用户说"写测试计划 / test plan / 测试方案 / 测试范围 / 测试策略 / Phase 04 测试计划"时调用。输出: 04-测试/<模块>-测试计划.md。**tester agent 的子工具** — agent §2.1 触发。
---

# test-plan-writer — 测试计划编写 skill v0.1

**tester agent 的子工具**, 主走 §2.1 测试计划编写职责。

区别于 `engineering:testing-strategy` (全局通用):
- 绑 PLM PRD-MAPPING.md / 原型 HTML / Phase 04 Gate 模板
- 输出格式与 Phase 04 §B 必产出物对齐
- 覆盖矩阵强制 PRD 验收标准 1-N 映射 (per tester agent §1 信念 2)

---

## 1. 何时调用

- 用户说 "写测试计划 / test plan / 测试方案"
- tester agent §2.1 触发
- 新模块进 Phase 04 前 (准入条件)
- 现有模块大改前重写

---

## 2. 必读

1. `01-立项/<模块>-PRD.md` — 找验收标准 (AC-NNN)
2. `PRD-MAPPING.md §<模块>` — 字段 / 状态机 / 错误码
3. `prd和原型/.../<module>.html` — 原型 UI
4. `04-测试/E2E-测试矩阵.md` — 现有覆盖
5. `04-测试/<其他>-测试计划.md` (相似模块作模板)

---

## 3. 6 步工作流

### Step 1: 测试范围 (in scope / out of scope)

| 范围 | in scope | out of scope |
|---|---|---|
| 功能 | CRUD + 状态机 + FK + 编码 | Phase 06 监控告警 |
| 性能 | API P99 < 500ms (per Phase 04 §B) | 压测高并发 |
| 安全 | 权限串 + URL 白名单 | 渗透测试 (转 security-reviewer) |
| 兼容性 | Chrome/Firefox 最新 | IE / 旧浏览器 |

### Step 2: 测试方法选择

| 方法 | 适用 | 工具 |
|---|---|---|
| 单元测试 | Service 业务规则 | JUnit + Mockito (test-engineer 实现) |
| 集成测试 | Mapper + DB | MockRestServiceServer |
| E2E 测试 | 关键路径 + 状态机 | Playwright (e2e-validator 执行) |
| 手测 | UI 微调 / 探索 | 手工 + 截图 |
| 性能 | API P99 | Apache Bench / k6 |
| 编码 | DB HEX 抽样 | check-encoding-runtime.sh |

### Step 3: 覆盖矩阵 — PRD 验收标准 × 测试用例

| PRD AC | 描述 | 用例 | 方法 | 优先级 |
|---|---|---|---|---|
| AC-1 | 编号唯一 + DB HEX 不含 EFBFBD | TC-001 / TC-002 | E2E + 编码 | P0 |
| AC-2 | 状态机 5×5 100% 覆盖 | TC-003 ~ TC-027 | E2E | P0 |
| AC-3 | API P99 < 500ms | TC-028 | 性能 | P1 |
| AC-N | ... | ... | ... | ... |

**约束**: 每条 PRD AC 必映射 ≥ 1 用例 (per tester agent §1 信念 2)。1-N 关系, 不允许 N=0。

### Step 4: 风险测试场景

按 rules.md §M / proposal 历史教训:
- 状态机非法转换 (601)
- FK 失效 (702)
- 进入态必填缺 (705)
- 字符编码 (EFBFBD)
- 并发 (@Version / 悲观锁, per tech-lead Phase 02 决议)
- URL 钓鱼 (708, per proposal 0101)
- 边界值 (空 / 单字符 / 超长)
- 字典 ENUM 越界 (604)

### Step 5: 退出准则 (Phase 04 DoD, per proposal 0004 staged)

- [ ] 测试用例 100% 执行
- [ ] 单测 Service 覆盖率 ≥ 70%
- [ ] E2E 套件通过率 = 100% (任何 fail 阻断, per rules.md §G.4)
- [ ] flake 率 ≤ 5%
- [ ] P0/P1 缺陷 0 open
- [ ] API P99 < 500ms (性能基线)
- [ ] 安全: 0 高危漏洞
- [ ] 回归: 全套件无新增 fail

### Step 6: 自检

- [ ] §3 覆盖矩阵每条 AC 至少 1 用例
- [ ] §4 风险场景 ≥ 5 类 (per tester agent §2.1 必含 6 类)
- [ ] §5 DoD 含 8 条
- [ ] 不包含具体测试代码 (那是 test-engineer 的活)
- [ ] 引用 Phase 04 Gate §B 必产出物 6 条

---

## 4. 输出

`04-测试/<模块>-测试计划.md`, 6 段:
1. 测试范围 (in/out of scope)
2. 测试方法选择
3. 覆盖矩阵 (AC × 用例)
4. 风险测试场景
5. 退出准则 (Phase 04 DoD)
6. 关联文件 (PRD / 原型 / 测试用例库 / E2E 矩阵)

---

## 5. 衔接

| 上游 | test-plan-writer | 下游 |
|---|---|---|
| pm-prd-writer PRD 验收标准 | → 覆盖矩阵 | → test-case-designer (设计具体用例) |
| tech-lead 状态机 / 错误码 | → 风险场景 | → e2e-validator (执行) |
| 现有 E2E 矩阵 | → 增量覆盖 | → quality-gate-audit (Phase 04 准出审) |

---

## 6. 反模式

- ❌ AC → 用例无映射 (违反 tester agent §1 信念 2)
- ❌ 风险场景遗漏字符编码 (rules.md §D MUST)
- ❌ DoD 写 95% 通过率 (per rules.md §G.4 必 100%)
- ❌ 计划里写具体测试代码 (越界, 那是 test-engineer)

---

## 7. 历史

| v0.1 | 2026-05-19 | 首版; tester 配套 4 skill 之一 |
