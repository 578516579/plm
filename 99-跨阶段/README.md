# 99-跨阶段 — Cross-phase Artifacts

> 跨阶段产出物的集中地。Phase 01-06 的卡控 / 提案 / 审计 / 反思 / 工作流 / 风险 / OKR 全部放这里。

## 内容索引

### 规则与流程

| 文件 | 说明 |
|---|---|
| `模块工作流.md` | Phase 01-06 总览 + 准入/准出 / Gate 卡控 / 反模式 |
| `PLM-路线图.md` | 版本规划:v0.1.0 ✅ / v0.2 当前 / v0.5+ 远期 |
| `AgriPLM-模块映射-2026-05-16.md` | PRD → 模块映射基线 |
| `团队 OKR.md` | 季度目标 |
| `风险登记册.md` | 全项目风险跟踪 |

### Gate 卡控

| 路径 | 说明 |
|---|---|
| `gate-checklists/` | 6 个 Phase Gate 模板 + 各模块实例(每实例 = 打勾签字记录) |
| `gate-checklists/README.md` | 卡控总说明 + 分级走 (L1/L2/L3 / P0 hotfix) |
| `gate-checklists/instances/<module>/Phase0X-...-Gate-YYYY-MM-DD.md` | 模块级 Gate 实例 |

### 自进化机制

| 路径 | 说明 |
|---|---|
| `signals/` | 客观数据采集(commit bypass / Gate skip 等) |
| `reflect/` | 周/月/季度反思报告(自动生成,详 §L) |
| `proposals/` | 正式变更提案 - review - 合入 - 跟踪 |
| `audits/` | 跨模块大型审计报告(2026-05-17 已产 2 份 — 12 模块 drift / 31 模块文档完整性) |

### 会议与协作

| 路径 | 说明 |
|---|---|
| `会议纪要/` | 跨模块会议记录 |

## Phase 99 的"全程并行"角色

Phase 99 不像 01-06 是线性阶段,而是**全程并行**:
- 风险登记册随时更新
- OKR 每季度初制定 + 每月 review
- Gate 卡控在每个 Phase 准入/准出节点触发
- 自进化 signals 实时采集 / reflect 按周/月/季度跑

## 相关规则

- [.claude/rules.md §L 自进化机制](../.claude/rules.md)
- [.claude/rules.md §G 评审与卡控](../.claude/rules.md)
- [03-开发/开发规范.md §0 命名总纲](../03-开发/开发规范.md)
