---
name: rollback-planner
description: PLM 三层回滚计划 — 代码 / DB / 前端 + 灰度回退步骤 + 触发阈值。当用户说"回滚计划 / rollback plan / 灰度回退 / 回退阈值 / 紧急回滚 / 应急预案"时调用。输出: 05-上线/Rollback-Plan-<release>.md。**ops agent 的子工具** — agent §2.6 触发 (上线后观察 + 退役决策)。
---

# rollback-planner — 回滚计划 skill v0.1

**ops agent 的子工具**, 主走 §2.6 上线后观察 + 退役决策职责。

核心信念 (per ops §1.2): **回滚优先于推进**。出问题第一动作是回滚, 不是修。

---

## 1. 何时调用

- 用户说 "回滚计划 / 灰度回退 / 应急预案"
- ops agent §2.6 触发
- 每次 release 前必产 (含 Runbook 引用)
- 灰度阶段触发回滚条件时

---

## 2. 三层回滚 (代码 / DB / 前端)

### 2.1 代码层回滚

```bash
# 后端 jar
ssh <prod-host> "
  systemctl stop plm-backend && \
  cp /opt/plm/backup/plm-admin-${OLD_VERSION}.jar /opt/plm/plm-admin.jar && \
  JAVA_OPTS='-Dfile.encoding=UTF-8 ...' systemctl start plm-backend
"
# 验证
curl -s http://<prod-host>:8081/captchaImage | grep -q '"code":200'
```

预期耗时: < 2 min。

### 2.2 DB 层回滚 (如有 DDL 迁移)

```bash
# 先停服, 后回滚 DB, 再启服 (按顺序!)
ssh <prod-host> "systemctl stop plm-backend"
mysql -uroot -p${DB_PASSWORD} --default-character-set=utf8mb4 plm \
  < sql/rollback/${VERSION}-rollback.sql
# 验证: 表结构与上版本一致
mysql -uroot -p plm -e "DESC tb_<entity>;" | diff - expected-${OLD_VERSION}.txt
ssh <prod-host> "systemctl start plm-backend"
```

预期耗时: 5-15 min (含数据校验)。

**注意**: DDL 回滚 ≠ DROP COLUMN, 应保留新字段 (避免数据丢失), 仅恢复约束 / 索引 / 默认值。

### 2.3 前端层回滚

```bash
# 静态资源回滚 (rsync 备份)
rsync -av <bastion>:/opt/plm-frontend-backup/${OLD_VERSION}/dist/ /var/www/plm/
# 清 CDN (如有)
curl -X POST https://cdn.example.com/api/purge -d "path=/*"
# 验证
curl -sI http://<prod-host>/ | grep -i x-frontend-version
```

预期耗时: < 5 min。

---

## 3. 触发阈值 (按灰度阶段)

| 灰度阶段 | 触发回滚条件 | 决策时间 |
|---|---|---|
| 内部用户 / 5% | P0 缺陷 (功能阻断 / 数据损坏 / 安全漏洞) ≥ 1 | 即时 |
| 5% → 50% | 错误率 > 1% (或 > 上版本 2×) | 5 min |
| 50% → 100% | 错误率 > 0.5% / API P99 > 1s (上版本 2×) | 15 min |
| 100% 后观察期 | 业务核心指标下降 > 20% (按 maturity) | 30 min |

per Phase 05 §F: 观察期长度 (early ≥ 30 min / stable ≥ 2h / mature ≥ 24h)。

---

## 4. 输出 — `05-上线/Rollback-Plan-<release>.md`

```markdown
# 回滚计划 — vX.Y.Z

| 字段 | 值 |
|---|---|
| 版本 | vX.Y.Z (回滚至 vX.Y.Z-1) |
| 回滚 Owner | <名字> |
| 联系方式 | <电话> |
| 预期总耗时 | < 20 min (3 层串行) |

## 1. 代码层回滚步骤
(具体命令)

## 2. DB 层回滚步骤
(具体命令 + 数据校验)

## 3. 前端层回滚步骤
(具体命令 + CDN purge)

## 4. 触发阈值
(灰度阶段 × 条件)

## 5. 回滚后清理
- 用户公告
- 内部通知 oncall
- Phase 05 §I 异常段填实际值
- 24h 内复盘会议

## 6. 风险 / 不可逆步骤
- DDL 已 add column 但回滚仅恢复约束: 新字段保留 (不 drop, 避免数据丢失)
- 用户在新版本提交的数据: <处置, e.g. "保留 + 在下次 release 兼容查询">
```

---

## 5. 衔接

| 上游 | rollback-planner | 下游 |
|---|---|---|
| tech-lead ADR 部署架构 | → 三层回滚步骤 | → runbook-writer (回滚命令具体到 Runbook) |
| db-design DDL 迁移 | → DB 层步骤 + 不可逆评估 | → ops agent §2.6 退役决策 |
| quality-gate-audit 性能基线 | → 触发阈值 | → 灰度执行 |

---

## 6. 反模式

- ❌ "出问题再想回滚" (没预案 = 没法回滚)
- ❌ DDL 回滚直接 DROP COLUMN (数据丢失)
- ❌ 回滚步骤未与 Runbook 一致 (两套口径)
- ❌ 阈值过宽 (错误率 > 5% 才回滚 = 已经爆炸)
- ❌ 阈值过严 (错误率 > 0.01% 频回滚 = 灰度无意义)
- ❌ 缺前端 CDN purge (用户继续看旧版)
- ❌ 24h 内不开复盘会 (per Phase 06 §F P0/P1 必走根因分析)

---

## 7. 历史

| v0.1 | 2026-05-19 | 首版; ops 配套 4 skill 之三; 三层回滚 + 阈值 × 灰度阶段 |
