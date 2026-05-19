---
name: release-comms
description: PLM 发布沟通 — 业务方提前通知 + 用户公告 + oncall 对齐 + 灰度阶段广播. 用户说"发布沟通 / release comms / 用户公告 / oncall 对齐 / 业务方通知"时调用. 输出: 05-上线/Release-Plan-<release>.md §4 + 草稿邮件/公告. **release-captain agent 的子工具**。
---

# release-comms — 发布沟通 skill v0.1

## 1. 何时调用
- "发布沟通 / 用户公告 / oncall 对齐"
- release-captain §2.4 触发

## 2. 4 类沟通

### 2.1 业务方提前通知
- 提前时间: early 1d / stable 2d / mature 3-5d
- 渠道: 邮件 + IM 群
- 内容: 时间窗 + 影响范围 + 回滚预案

### 2.2 用户公告 (external-product)
- 内部 internal-tool: N/A
- 外部产品: 站内信 + 邮件 (上线前 24h) + 状态页

### 2.3 oncall 对齐
- 发布前 30 min 同步 (含回滚条件)
- 发布中保持 IM 通畅
- 发布后 24h 备战

### 2.4 灰度阶段广播
- 每阶段开始 + 结束 各 1 条 IM
- 含: 阶段名 / 当前 metric / 是否进下阶段

## 3. 输出模板

```markdown
## §4 沟通

### 4.1 业务方通知 (2 天前发, 邮件草稿)
> Subject: PLM v0.1.0 上线计划 — 2026-05-22 周五 17:00
> 
> 各业务方好:
> PLM v0.1.0 计划于本周五 17:00 开始灰度上线, 预计 20:00 完成全量。
> ...

### 4.2 用户公告
N/A (internal-tool)

### 4.3 oncall 对齐 (发布前 30min, IM)
> [oncall 对齐] 17:00 开始发布, 回滚条件:
> - canary 错误率 > 1% (5%阶段) → 立即回滚
> - 5xx 持续 > 5 min → 立即回滚
> 操作步骤见 Runbook §3

### 4.4 灰度广播模板
> [灰度 5% 开始] 17:30, 当前错误率 0%, 进 50%
> [灰度 100%] 18:30, 进入观察期
```

## 4. 衔接
- 上游: release-window-planner (时间)
- 下游: 实际发布执行

## 5. 历史
| v0.1 | 2026-05-19 | 首版; release-captain 配套 4/4 (完结) |
