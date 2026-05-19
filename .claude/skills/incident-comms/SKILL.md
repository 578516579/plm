---
name: incident-comms
description: PLM 事故沟通 — timeline 维护 + 用户公告 + 内部通知 (oncall/业务方/管理层). 用户说"事故 timeline / 应急沟通 / 用户公告 / incident 内部通知"时调用. 输出: 06-运营/incident-<id>-<date>.md §3 timeline + 公告草稿. **incident-commander agent 的子工具**。
---

# incident-comms — 事故沟通 skill v0.1

## 1. 何时调用
- "事故 timeline / 应急沟通 / 用户公告 / incident 通知"
- incident-commander §2.2 触发

## 2. Timeline 维护

每 5-15 min 一条, 含:
- 时间 (HH:MM)
- 事件 (发生了什么)
- 决策 (做了什么)
- 状态 (恢复进度)

## 3. 沟通分层

| 受众 | 渠道 | 频次 | 内容 |
|---|---|---|---|
| 用户 (external-product) | 站内信 + 状态页 | 首次 + 30min + 恢复 | "正在处理 / 进度 / 恢复" |
| 业务方 | 邮件 + IM | 首次 + 关键节点 | 含 ETA |
| oncall 团队 | IM 群 | 实时 (5-15 min) | timeline + 行动项 |
| 管理层 | 邮件 | 1h+ 持续 / P0 即时 | 影响范围 + 决策需求 |

## 4. 输出模板
```markdown
## §3 Timeline

| 时间 | 事件 | 决策 | 状态 |
|---|---|---|---|
| 17:30 | 5xx 错误率突增 | 启动应急 | 调查中 |
| 17:35 | 确认 = vX.Y.Z 部署导致 | 决议回滚 | 回滚中 |
| 17:45 | 回滚 vX.Y.Z-1 完成 | 验证 | 验证中 |
| 17:50 | curl healthcheck 全绿 | 恢复 ✅ | 已恢复 |

## §3.1 用户公告 (草稿)
> [PLM] 已于 17:50 恢复正常。期间影响时段 17:30-17:50, 不便致歉。

## §3.2 oncall 实时 IM
[17:30] 报警: 5xx > 5%, 启动 INC-2026-001
[17:35] 决议: 回滚 vX.Y.Z (因新版本 fix(xxx) 引入回归)
...
```

## 5. 衔接
- 上游: incident-triage (定级)
- 下游: incident-postmortem (timeline 入复盘)

## 6. 历史
| v0.1 | 2026-05-19 | 首版; incident-commander 配套 2/4 |
