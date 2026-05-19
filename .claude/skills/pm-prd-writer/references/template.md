# <模块> - PRD

> 由 [pm-prd-writer skill](../../.claude/skills/pm-prd-writer/SKILL.md) v0.1 产出。
> 必引用: [PRD-MAPPING.md §<module>](../PRD-MAPPING.md) + [原型 HTML](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/<module>.html) + PRD §F.NN.M。
> Phase 01 Gate §B.1 必产出物 (per [proposal 0001/0002](../99-跨阶段/proposals/) 参数化)。

---

## 头部

| 字段 | 值 |
|---|---|
| 模块名 | <module> |
| 模块状态 | 🟢 active / 🟡 stub / 🔴 不存在 (来自 PRD-MAPPING.md §1) |
| PRD §章节 | §F.NN.M |
| 原型 HTML | prd和原型/.../module.html |
| 关联 brainstorm | <如有, 链 01-立项/brainstorm/...> |
| 创建日期 | YYYY-MM-DD |
| 主持 | product-manager agent (via pm-prd-writer skill) |

---

## 1. 背景 / 目标

<1 段话, 含为什么做 + 解决谁的痛点 + 期望结果>

---

## 2. 用户故事 (Given-When-Then)

### US-001 <故事标题>

- **Given**: <前置状态>
- **When**: <用户动作>
- **Then**: <预期结果, 含 API + DB + 编码 HEX 校验>

**故事点**: <Fibonacci 1/2/3/5/8/13>
**业务价值**: <1-10>
**风险**: <Low / Med / High>

### US-002 ...

(至少每个核心功能 1 个故事)

---

## 3. 功能列表 (按原型 HTML 分组)

### F1: <功能组 1>
- F1.1: <功能点>
- F1.2: ...

### F2: <功能组 2>
- ...

---

## 4. 字段映射表 (per rules.md §M.3)

| 原型 HTML 元素 | Java field | SQL column | 类型 | 必填 | 注释 |
|---|---|---|---|---|---|
| `<label>标题 *</label>` | title | title | VARCHAR(200) | Y | |
| `<select>状态</select>` | status | status | VARCHAR(50) | Y | 字典 biz_<module>_status |
| `<button>AI...</button>` | aiResult | ai_result | TEXT | N | **服务端计算, 不接受前端值** |
| ... | | | | | |

**字段约束**:
- 字段命名按 rules.md §M.3 表
- 服务端计算字段不接受前端写入
- ENUM 字段 service 入口校验白名单, 非法抛 604

---

## 5. 状态机 (per rules.md §M.4)

### 5.1 状态列表 (来源原型徽章)

| 状态 | 中文 | 原型徽章 CSS | 描述 |
|---|---|---|---|
| draft | 草稿 | .bgr | 初创 |
| review | 评审中 | .bam | 待审 |
| approved | 已确认 | .bg | 通过 |
| rejected | 不通过 | .bd | 退回 |
| archived | 已归档 | (无 badge) | 终态 |

### 5.2 转换矩阵

```
        ↓ to
from →  draft  review  approved  rejected  archived
draft     -     ✓        -         ✓         -
review    -     -        ✓         ✓         -
approved  -     -        -         -         ✓
rejected  ✓     -        -         -         -    ← 反向边 (PRD 显式)
archived  -     -        -         -         -
```

**反向边显式标注** (per proposal 0019):
- `rejected → draft`: 复活 / 重提

---

## 6. 错误码 (登记到 PRD-MAPPING.md §4)

| 错误码 | 含义 | 触发场景 | 示例 |
|---|---|---|---|
| 601 | 状态机违规 | 非法转换 | draft → archived 跳级 |
| 604 | ENUM 白名单外 | service 入口 | environment="foo" |
| 702 | <Entity> 不存在或已删除 | FK 校验 | xService.checkExists 抛 |
| 705 | 进入态必填字段缺 | resolved 缺 resolution | resolve 抛 |
| 708 | URL host 不在白名单 | UrlValidator | mrUrl 钓鱼站 |

---

## 7. 开放问题 (留 Phase 02 决议)

不在 PRD 做技术决策:

- **Q1**: <技术选型> — tech-lead Phase 02 决, 写 ADR
- **Q2**: <并发选型> — @Version vs 悲观锁
- **Q3**: <编号规则> — 形式 / 单调范围
- **Q4**: ...

---

## 8. 验收标准

每条可量化:

- **AC-1**: 编号唯一 + DB HEX 不含 EFBFBD
- **AC-2**: 状态机 5×5 矩阵 100% E2E case 覆盖 (per tester agent §2.5)
- **AC-3**: API P99 < 500ms (per Phase 04 §B 性能基线)
- **AC-4**: 错误码 100% 与 PRD-MAPPING.md §4 一致
- **AC-5**: UI 文案 100% 与原型 HTML 一致

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| YYYY-MM-DD | product-manager agent (via pm-prd-writer) | 首次创建 |
