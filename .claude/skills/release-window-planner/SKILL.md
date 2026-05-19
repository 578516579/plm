---
name: release-window-planner
description: PLM 发布窗口规划 — 低峰时段选 + 团队投入 + 各阶段时间点 + 应急联系. 用户说"发布窗口 / release window / 上线时间 / 低峰窗口 / 发布时段"时调用. 输出: 05-上线/Release-Plan-<release>.md §3. **release-captain agent 的子工具**。
---

# release-window-planner — 发布窗口规划 skill v0.1

## 1. 何时调用
- "发布窗口 / release window / 低峰时段 / 上线时间"
- release-captain §2.3 触发

## 2. 窗口选

| 项目类型 | 推荐时段 | 避开 |
|---|---|---|
| internal-tool | 工作日 17:00-20:00 (下班后) | 月初 / 周一上午 |
| external-product | 周二/三 凌晨 00:00-06:00 | 周末 / 节假日前 |
| critical-business | 凌晨 02:00-04:00 + 业务低峰 | 月末 / 报税期 |

## 3. 团队投入 (per 4D)

| maturity | release-captain | 后端 oncall | 前端 oncall | DBA | security |
|---|---|---|---|---|---|
| early × solo | 1 人兼 | 同 | 同 | 同 | 同 |
| stable × small | 1 主 | 1 备 | 1 备 | 1 备 | (按需) |
| mature × medium+ | 1 主 + 1 副 | 2 oncall | 2 oncall | 1 主 + 1 备 | 1 oncall |

## 4. 输出模板
```markdown
## §3 发布窗口

| 字段 | 值 |
|---|---|
| 窗口 | 2026-05-22 17:00-20:00 (周五下班后) |
| 团队投入 | Wjl (兼 release-captain + 后端/前端/DBA/security oncall) |
| 应急联系 | 电话 1xxxx (Wjl) |
| 业务方对齐 | 提前 2 天通知 (per Phase 05 §D solo + early) |

### 各阶段时间点
| 阶段 | 起 | 止 | duration |
|---|---|---|---|
| 准备 + 内部 | 17:00 | 17:30 | 30 min |
| 5% | 17:30 | 18:00 | 30 min |
| 50% | 18:00 | 18:30 | 30 min |
| 100% | 18:30 | — | 持续 |
| 观察期 | 18:30 | 20:00 | 1.5h |
```

## 5. 衔接
- 上游: gray-release-strategy (4 阶段定义)
- 下游: release-comms (按窗口写公告)

## 6. 历史
| v0.1 | 2026-05-19 | 首版; release-captain 配套 3/4 |
