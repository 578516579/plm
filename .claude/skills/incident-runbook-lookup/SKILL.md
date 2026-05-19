---
name: incident-runbook-lookup
description: PLM 事故 Runbook 检索 — 从 05-上线/Runbook.md 找应急步骤 + 执行 + 标 timeline. 用户说"Runbook 检索 / 应急步骤 / 找 Runbook 章节 / 怎么回滚"时调用. 输出: 应用 Runbook 章节 + 记 timeline 行. **incident-commander agent 的子工具**。
---

# incident-runbook-lookup — Runbook 检索 skill v0.1

## 1. 何时调用
- "Runbook 检索 / 应急步骤 / 怎么回滚 / 找 Runbook"
- incident-commander §2.3 触发

## 2. 检索流程

```
事故现象 → grep Runbook §<现象> 标题
       ↓
找到对应章节?
  ├─ 是 → 按章节步骤执行 → timeline 记 "执行 §X.Y"
  └─ 否 → 临时决议 → 事后补 Runbook 章节 (runbook-writer skill)
```

## 3. 常见 Runbook 章节速查

| 现象 | Runbook §? | 应急步骤摘要 |
|---|---|---|
| Backend 不响应 | §3 后端回滚 | systemctl stop → cp 旧 jar → start → curl 验证 |
| DB schema 失配 | §4 DB 回滚 | mysql --default-character-set=utf8mb4 < rollback.sql |
| 前端 404 | §5 前端回滚 | rsync 旧 dist + CDN purge |
| 编码乱码 | §6 编码事故 | -Dfile.encoding=UTF-8 4 标志 + check-encoding.sh |
| Redis 不可达 | §7 缓存恢复 | 检查 IPv6 trap (127.0.0.1 vs localhost) |

## 4. 输出: timeline 行

```markdown
| 17:35 | 5xx 持续 5 min | 查 Runbook §3 后端回滚 → 执行 step 1-3 | 回滚中 |
```

## 5. 衔接
- 上游: incident-triage (定级 → 决议回滚)
- 下游: incident-comms (timeline) + ops Runbook 更新 (事后补章节)

## 6. 历史
| v0.1 | 2026-05-19 | 首版; incident-commander 配套 3/4 |
