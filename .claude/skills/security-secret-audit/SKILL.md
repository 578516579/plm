---
name: security-secret-audit
description: PLM 凭据/敏感数据审 — 扫 git log + .env + yml + log 输出, 确保 secret 不入 git/log/health endpoint. 当用户说"凭据审 / API key 审 / .env 审 / secret 泄露 / git log 敏感 / Phase 05 §C 红线"时调用. 输出: Phase 05 §C 红线 checklist 6 项填值. **security-reviewer agent 的子工具**。
---

# security-secret-audit — 凭据/敏感数据审 skill v0.1

**security-reviewer agent 的子工具**, 主走 §2.3 凭据审职责。

PLM 用 `${VAR:default}` 约定 (per [CLAUDE.md "Secrets — environment-variable contract"](../../../CLAUDE.md))。本 skill 验证 6 类 secret 守护。

---

## 1. 何时调用

- 用户说 "凭据审 / API key 审 / .env 审 / secret 泄露 / Phase 05 §C 红线"
- security-reviewer agent §2.3 触发
- Phase 05 上线前必产
- Phase 06 P0 凭据泄露事故复盘

---

## 2. 6 类 secret 守护扫描

### 2.1 JWT_SECRET ≥ 32 字符强随机

```bash
# 检查实际部署的值 (要登录目标机器)
ssh <host> 'echo "${JWT_SECRET}" | wc -c'
# 期望: ≥ 32

# 或扫 .env / yml 中默认值
grep -rE "JWT_SECRET[=:]" plm-backend/ --include='*.yml' --include='*.env*'
# 期望: 全部 `${JWT_SECRET:please-change-me}` (生产不应留 please-change-me)
```

### 2.2 DB_PASSWORD 非默认值

```bash
grep -rE "DB_PASSWORD" plm-backend/ --include='*.yml' --include='*.env*'
# 期望: `${DB_PASSWORD}` 无默认值, 强制生产显式注入
```

### 2.3 REDIS_PASSWORD 已设

```bash
grep -rE "REDIS_PASSWORD|spring.redis.password" plm-backend/
```

### 2.4 DRUID 控制台口令

```bash
grep -rE "DRUID_USERNAME|DRUID_PASSWORD|druid.web-stat-filter" plm-backend/
# 期望: 强口令 + 内网限制 + 路径前缀防爬
```

### 2.5 默认 admin 已禁 / 改密

```bash
# 检查 sys_user 默认 admin/admin123 是否生效
ssh <prod> 'mysql plm -e "SELECT user_name,password FROM sys_user WHERE user_name=\"admin\" LIMIT 1"'
# 期望: hash 不是 default 的 $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
```

### 2.6 .env 文件未提交 git

```bash
git log -p | grep -E 'JWT_SECRET=|DB_PASSWORD=|REDIS_PASSWORD=' | head -20
# 期望: 空 (即仅出现 ${VAR:default} 占位)

# 同时检查 .gitignore
grep -E '^\.env|^plm-backend/\.env' .gitignore
# 期望: .env 模式覆盖
```

---

## 3. 输出 — Phase 05 §C 红线 checklist 6 项

```markdown
## §C 凭据 / 敏感数据 红线 (per Phase 05 §C)

| 红线 | 检查方式 | 实际值 | 状态 |
|---|---|---|---|
| JWT_SECRET ≥ 32 字符强随机 | `openssl rand -base64 48` 生成 | `wc -c` ≥ 32 | ✅/❌ |
| DB_PASSWORD 非默认 / 非 `please-change-me` | grep + mysql 登录测试 | 待运维确认 | ✅/❌ |
| REDIS_PASSWORD 已设 | grep + redis-cli 测试 | 待运维确认 | ✅/❌ |
| DRUID 控制台 强口令 / 内网限制 | yml + Nginx 配置审 | 待运维确认 | ✅/❌ |
| 默认 admin/admin123 已改 / 禁 | DB 查询 sys_user | 待运维改密 | ✅/❌ |
| .env 文件未提交 git | git log -p grep | 0 命中 | ✅/❌ |

### 6 类 secret 守护汇总

- 🟢 6/6 ✅ → Phase 05 §C 通过
- 🟡 1-2/6 待运维确认 → 阻塞, 上线前必须完成
- 🔴 ≥ 1 ❌ → P0, 必须 fix 才能上线
```

---

## 4. 衔接

| 上游 | secret-audit | 下游 |
|---|---|---|
| config-engineer .env / yml | → 扫占位 | → ops Phase 05 §C 红线 |
| db-modeler sys_user 初始密码 | → 检查默认 admin | → ops 改密 |
| ops Runbook 启动命令 | → 检查 -Dfile.encoding 含env注入 | → 上线 |

---

## 5. 反模式

- ❌ JWT_SECRET 留 `please-change-me` 上生产
- ❌ DB_PASSWORD 写死在 yml (`${DB_PASSWORD:Bosssfot@2025}`)
- ❌ .env 文件 commit 进 git
- ❌ 默认 admin/admin123 上生产未改
- ❌ DRUID 控制台公网可达
- ❌ secret 出现在 application log (per Phase 05 §H "敏感词扫")

---

## 6. 历史

| v0.1 | 2026-05-19 | 首版; security-reviewer 4 配套 之 3 |
