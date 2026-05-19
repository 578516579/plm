---
name: pm-prd-writer
description: PLM 结构化 PRD 编写 — 从需求 / 脑暴纪要 / 原型 HTML 产出 Phase 01 Gate 准入级 PRD 文档. 当用户说"写 PRD / 起草 PRD / 产品需求文档 / spec / feature spec / 用户故事 / 验收标准"时调用. 输出: 01-立项/<模块>-PRD.md, 强绑 PRD-MAPPING.md SSoT (per rules.md §M). **product-manager agent 的子工具** — agent §2.1 PRD 编写时调本 skill。
---

# pm-prd-writer — PRD 编写 skill v0.1

**product-manager agent 的子工具**, 主走 PM agent §2.1 PRD 编写职责。

区别于 `anthropic-skills:product-management:write-spec` (全局):
- 本 skill 绑 PRD-MAPPING.md / 原型 HTML / Phase 01 Gate §B 必产出 / rules.md §M (PRD-driven)
- 必引用原型 HTML 元素 + PRD § 章节
- 字段命名按 rules.md §M.3
- 状态机来源原型徽章 (§M.4)
- 错误码登记到 PRD-MAPPING.md §4 (§M.5)

---

## 1. 何时调用

- 用户说 "写 PRD / 起草 PRD / spec / 用户故事" 等
- pm-brainstorm 收敛后入选方向 → 进 PRD
- product-manager agent §2.1 触发
- 新模块立项前 (Phase 01 Gate §B.1 必产出)

不调:
- PRD 已存在仅微调 → 直接 Edit
- 需求模糊 → 先转 pm-brainstorm

---

## 2. 输入与必读

**必读 (per 0040 §3.1 + 0041 §3.1)**:
1. `PRD-MAPPING.md` — 找模块 § + 字段对照表 + 状态机 + 错误码
2. `prd和原型/AgriPLM-DevOps-原型/agriplm_split/<module>.html` — 原型表单 / 状态徽章
3. `prd和原型/AgriAI-PLM-完整PRD文档.md` — PRD 原文 §章节
4. pm-brainstorm 输出 (如有) — 候选大纲
5. 类似模块的 `01-立项/<其他>-PRD.md` — 抄结构

---

## 3. 7 步工作流

### Step 1: 模块定位

- 在 PRD-MAPPING.md §1 找模块行 (确认状态: 🟢/🟡/🔴)
- 找对应原型 HTML 路径
- 找 PRD §F.NN.M 章节号

### Step 2: 字段映射 (per §M.3)

逐 原型表单元素 → Java 字段 → SQL 列:

| 原型 HTML | Java field | SQL column | 类型 | 必填 |
|---|---|---|---|---|
| `<label>标题 *</label>` | title | title | VARCHAR(200) | Y |
| `<label>期望测试周期(天)</label>` | expectedTestDays | expected_test_days | INT | N |
| `<select>测试环境</select>` | environment | environment | VARCHAR(50) | Y |
| `<button>AI质量门禁</button>` | qualityGatePassed | quality_gate_passed | CHAR(1) | (服务端计算) |

**约束**:
- 不允许凭直觉添加 PRD 未提及字段 (per rules.md §M.1)
- 服务端计算字段 (qualityGatePassed) 在 Mapper update 不接受前端值 (§M.3)

### Step 3: 状态机定义 (per §M.4)

来源:
- PRD §3.2 状态术语
- 原型徽章 CSS 类 (.bg / .bam / .bgr / .bd)

输出 M×M 转换矩阵:

```
        ↓ to
from →  draft  review  approved  rejected  archived
draft     -     ✓        -         ✓         -
review    -     -        ✓         ✓         -
approved  -     -        -         -         ✓
rejected  ✓     -        -         -         -    ← 反向边
archived  -     -        -         -         -
```

**反向边必显式** (per proposal 0019 bundle 入 0016): 在 PRD 标"3→1 复活 / 4→1 重提"。

### Step 4: 错误码登记 (per §M.5)

新增任何错误码必先在 PRD-MAPPING.md §4 登记:

| 错误码 | 含义 | 触发 | 示例 |
|---|---|---|---|
| 601 | 状态机违规 | 非法转换 | draft → archived 跳级 |
| 702 | 实体不存在或已删除 | FK 校验 | projectService.checkExists 抛 |
| 705 | 进入态必填字段缺 | resolved 缺 resolution | testcaseService.resolve 抛 |

号段 (per tech-lead agent 待 ADR 化):
- 600-699 状态机 / 业务规则
- 700-799 数据完整性 (FK / 必填)
- 800-899 安全 / 权限

### Step 5: 用户故事 (Given-When-Then)

每个核心功能 ≥ 1 故事:

```
**US-001 创建测试用例**:
  Given 用户登录, 项目 P-2026-0001 存在
  When POST /business/testcase body={title:"...", projectId:1}
  Then 响应 code=200, 新行 in tb_testcase, status=draft, testcase_no 形如 TC-2026-0001

**验收标准 AC-001**: testcase_no 自增唯一; 同年内不重号; DB HEX("...") 不含 EFBFBD
```

### Step 6: 开放问题 (留 Phase 02 决议)

PRD 不做技术决策, 留 tech-lead Phase 02:

```
## 开放问题
- Q1: 并发选型 — @Version 乐观锁 vs 悲观锁? (tech-lead 决, 写 ADR)
- Q2: 状态机反向边 04→01 是否需 reviewer 重审?
- Q3: testcase_no 编号规则 — TC-YYYY-NNNN 单年单调 vs 模块级单调?
```

### Step 7: 验收准入

PRD 自检:
- [ ] §字段映射 表与原型 HTML 100% 对应
- [ ] §状态机 含反向边显式
- [ ] §错误码 全登记 PRD-MAPPING.md §4
- [ ] §用户故事 Given-When-Then 含 ≥ 1 故事 / 核心功能
- [ ] §验收标准 含量化条件 (含编码 HEX 校验)
- [ ] §开放问题 列 ≥ 1 (Phase 02 决议)
- [ ] **PRD-MAPPING.md §2 同步增量** (字段表 commit 必须先于 PRD commit, per §M.2)

任一 fail → 不算完成。

---

## 4. 输出文件

`01-立项/<模块>-PRD.md`, 8 段结构:

1. 背景 / 目标 (1 段)
2. 用户故事 (Given-When-Then × ≥ 1 / 核心功能)
3. 功能列表 (按原型 HTML 分组)
4. **字段映射表** (per §M.3)
5. **状态机** (M×M 矩阵 + 反向边)
6. **错误码** (登记 PRD-MAPPING.md §4 增量)
7. 开放问题 (留 Phase 02)
8. 验收标准 (含 HEX / 性能 / 安全)

模板见 [references/template.md](references/template.md)。

---

## 5. 与其他 skill / agent 衔接

| 上游 | pm-prd-writer | 下游 |
|---|---|---|
| pm-brainstorm 收敛大纲 | → 结构化 PRD | → tech-lead agent (Phase 02 设计) |
| product-manager agent §2.1 触发 | → PRD 草稿 | → pm-priority-matrix (本模块 vs 其他模块排序) |
| 原型 HTML | → 字段表 | → ruoyi-bootstrap skill (Phase 7 生成代码骨架) |
| PRD-MAPPING.md §1 模块状态 | → 增量 §2 字段表 | → tester (基于状态机 / 错误码出测试用例) |

---

## 6. 反模式

- ❌ 写 PRD 不读原型 HTML (违反 rules.md §M MUST)
- ❌ 字段表凭记忆写 (违反 0040 §3.1 + 0041 §3.1)
- ❌ 状态机不引用原型徽章 (违反 §M.4)
- ❌ 错误码裸用数字不登记 (违反 §M.5)
- ❌ PRD 里做技术决策 (e.g. "用 @Version") — 留 Phase 02 ADR
- ❌ 用户故事不写 Given-When-Then (无验收依据)
- ❌ 验收标准只写"功能正确" 无量化 (无法测)
- ❌ 字段表 commit 后于 PRD commit (违反 §M.2 先字段表后代码)

---

## 7. 历史

| 版本 | 日期 | 改了什么 |
|---|---|---|
| v0.1 | 2026-05-19 | 首版; PM agent 配套 4 skill 之二; 7 步工作流; 强绑 rules.md §M PRD/原型驱动; 8 段输出 |
