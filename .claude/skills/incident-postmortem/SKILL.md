---
name: incident-postmortem
description: PLM 事故复盘 — 5 Whys 根因 + blameless + 改进项 + 风险登记. 用户说"postmortem / 事故复盘 / 5 Whys / blameless / 根因分析 / 改进项"时调用. 输出: 06-运营/incident-<id>-<date>.md §4 + 风险登记册增量. **incident-commander agent 的子工具**。
---

# incident-postmortem — 事故复盘 skill v0.1

## 1. 何时调用
- "postmortem / 复盘 / 5 Whys / 根因 / blameless"
- incident-commander §2.4 触发
- P0 事故恢复后 24h 内必产 (per Phase 06 §F)

## 2. 5 Whys 根因分析

```
现象: 上线后 5xx 错误率突增
  Why 1: 因为 service.checkX() 抛 NPE
  Why 2: 因为 X 字段是 null
  Why 3: 因为 DDL 加了 NOT NULL 但旧数据没回填
  Why 4: 因为 migrate-checklist 没 §"数据回填" 段
  Why 5: 因为模板 outdated, Phase 02 §A 准入未检 (流程问题)
```

至少挖到 Why 5 (流程层面), 不止步 Why 3 (代码层面)。

## 3. Blameless 原则

- ❌ "XX 同学没仔细检查"
- ✅ "DDL migrate 流程缺少 数据回填检查步骤"
- 改进项必须改 流程 / 工具 / 文档, 不改 "人要更仔细"

## 4. 输出模板

```markdown
## §4 Postmortem

### 4.1 影响范围
- 时段: 17:30-17:50 (20 min)
- 影响用户: 50% 流量 (灰度阶段)
- 业务损失: <数据估算>

### 4.2 5 Whys
1. ...
2. ...
3. ...
4. ...
5. ... (流程根因)

### 4.3 改进项 (P 排序, 各分负责人 + 完成时间)
| P | 改进项 | 类型 | 负责人 | 完成时间 |
|---|---|---|---|---|
| P1 | migrate-checklist 加 §数据回填 | 流程 | Wjl | 2026-05-26 |
| P2 | DDL 工具加 NOT NULL 预警 | 工具 | Wjl | 2026-06-01 |

### 4.4 风险登记
- 新增 R-2026-NN: DDL 数据回填缺漏 → 风险登记册 §D.1
- 触发 0028 编码三层防御 类似 proposal? 待评估

### 4.5 timeline 完整 (从 §3 复制)
```

## 5. 衔接
- 上游: incident-comms timeline
- 下游: 风险登记册增量 + Sprint backlog 改进项 BL 增量 + proposal (如规则需改)

## 6. 历史
| v0.1 | 2026-05-19 | 首版; incident-commander 配套 4/4 (完结) |
