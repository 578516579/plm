---
name: runbook-writer
description: PLM Runbook 编写 / 增量维护 — 部署 SOP + 回滚命令具体到行 (含 jar tag / SQL 回滚脚本 / 前端 rsync)。当用户说"写 Runbook / Runbook 增量 / 部署 SOP / 回滚命令 / 上线手册"时调用。输出: 05-上线/Runbook.md 增量。**ops agent 的子工具** — agent §2.3 触发。
---

# runbook-writer — Runbook 编写 skill v0.1

**ops agent 的子工具**, 主走 §2.3 Runbook 编写 / 维护职责。

核心: **回滚命令具体到行**, 不接受"步骤问运维"——半夜 oncall 没人接电话, 必须看 Runbook 直接抄命令。

---

## 1. 何时调用

- 用户说 "写 Runbook / 部署 SOP / 回滚命令"
- ops agent §2.3 触发
- 每次新业务模块上线前必增量
- ADR 改部署架构后必同步

---

## 2. Runbook 结构 (per Phase 05-上线-Gate.md §B.2)

每模块在 `05-上线/Runbook.md` 加章节, 含:

### 2.1 部署步骤

```bash
# 1. 备份当前 jar (回滚用)
ssh <prod-host> "cp /opt/plm/plm-admin-${OLD_VERSION}.jar /opt/plm/backup/"

# 2. 部署新 jar
scp plm-admin/target/plm-admin-${NEW_VERSION}.jar <prod-host>:/opt/plm/

# 3. 滚动重启 (preserve 编码标志)
ssh <prod-host> "
  systemctl stop plm-backend && \
  JAVA_OPTS='-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8' \
  systemctl start plm-backend
"

# 4. 健康检查
curl -s http://<prod-host>:8081/captchaImage | head -c 200
# 期望: {"msg":"操作成功","code":200,...}
```

### 2.2 回滚命令 (具体到行)

```bash
# 回滚后端
ssh <prod-host> "
  systemctl stop plm-backend && \
  cp /opt/plm/backup/plm-admin-${OLD_VERSION}.jar /opt/plm/plm-admin.jar && \
  systemctl start plm-backend
"

# 回滚数据库 (如有 DDL 迁移)
mysql -uroot -p${DB_PASSWORD} --default-character-set=utf8mb4 plm < sql/rollback/${VERSION}-rollback.sql

# 回滚前端
rsync -av <bastion>:/opt/plm-frontend-backup/${OLD_VERSION}/ /var/www/plm/

# 验证
curl -s http://<prod-host>:8081/captchaImage  # 后端
curl -s http://<prod-host>/  # 前端
```

### 2.3 监控 / 告警

- 实时日志: `ssh <prod-host> "journalctl -fu plm-backend"`
- 健康检查 endpoint: `/captchaImage` (返回 200 + 含 captcha JSON)
- 编码自检 (per [proposal 0028](../../99-跨阶段/proposals/0028-encoding-runtime-hardrules.md)): `bash plm-backend/scripts/check-encoding-runtime.sh`
- 告警接收人: <列表>

### 2.4 oncall 联系人

| 角色 | 姓名 | 联系方式 | SLA |
|---|---|---|---|
| 后端 oncall | TBD | TBD | 30 min 响应 |
| 前端 oncall | TBD | TBD | 1h |
| DBA | TBD | TBD | 工作日内 |

### 2.5 特殊处置 (本版本)

如有本版本特殊步骤 (e.g. 配置变更 / 数据迁移):

```bash
# v0.2.0 引入字段 quality_gate_passed
mysql -uroot -p${DB_PASSWORD} plm -e "
  ALTER TABLE tb_testcase ADD COLUMN quality_gate_passed CHAR(1) NULL COMMENT 'AI 计算';
"
```

---

## 3. 4 步工作流

```
[Step 1] Read 现有 Runbook.md, 找 module 段
[Step 2] 增量加 §2.1-§2.5 (回滚命令必具体)
[Step 3] AskUserQuestion 验证回滚 owner / SLA
[Step 4] commit `docs(runbook): <module> <version>`
```

---

## 4. 衔接

| 上游 | runbook-writer | 下游 |
|---|---|---|
| tech-lead ADR 部署架构 | → 部署 SOP | → ops agent §2.1 Phase 05 主持 |
| db-design DDL 迁移 | → SQL 回滚脚本路径 | → backend-coder (执行回滚) |
| 现有 Runbook | → 增量 | → Phase 05 §B.2 必产出 |

---

## 5. 反模式

- ❌ "回滚步骤问运维" (oncall 没人接 = P0)
- ❌ 回滚命令含变量未替换 (e.g. `${OLD_VERSION}` 用户自查)
- ❌ 编码标志缺失 (per rules.md §D #5)
- ❌ 健康检查无具体 endpoint (无法验证回滚成功)
- ❌ oncall 联系人空 (per Phase 05 §B.2 必填)
- ❌ 回滚命令未 commit 入 Runbook (放群里口耳相传)

---

## 6. 历史

| v0.1 | 2026-05-19 | 首版; ops 配套 4 skill 之一 |
