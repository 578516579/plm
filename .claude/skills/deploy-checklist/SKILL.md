---
name: deploy-checklist
description: PLM Phase 05 上线 Checklist 编写 — 代码/构建 + 数据库 + 配置/凭据 + 监控/告警 + 沟通 5 段全打勾。当用户说"上线 Checklist / pre-deploy / 部署前检查 / 上线清单 / Phase 05 §B.1"时调用。输出: 05-上线/Pre-Deploy-Checklist-<release>.md。**ops agent 的子工具** — agent §2.1 Phase 05 主持时触发。
---

# deploy-checklist — 上线前检查清单 skill v0.1

**ops agent 的子工具**, 主走 §2.1 Phase 05 §B.1 上线 Checklist 必产出。

---

## 1. 何时调用

- 用户说 "上线 Checklist / pre-deploy / 部署前检查"
- ops agent §2.1 触发
- 每次 release 前必走
- Phase 05 §B.1 必产出物

---

## 2. 5 段全清单 (per Phase 05-上线-Gate.md §B.1)

### 2.1 代码 & 构建

- [ ] 版本号已分配 (pom.xml `<plm.version>` + frontend `package.json` version)
- [ ] `mvn clean install -DskipTests -T 4` 成功 (38 modules)
- [ ] `cd plm-frontend && npm run build:prod` 成功 (2860+ modules)
- [ ] E2E 全套件 100% 通过 (per quality-gate-audit skill)
- [ ] git tag 已打: `git tag -a vX.Y.Z -m "..."`
- [ ] git tag 已 push: `git push origin vX.Y.Z`
- [ ] Changelog 已加本版本块 (per [05-上线/Changelog.md](../../05-上线/))

### 2.2 数据库

- [ ] DDL 演练成功 (staging 或 dev)
- [ ] 数据备份完成 (`mysqldump -uroot -p plm > backup-${DATE}.sql`)
- [ ] 备份验证: 文件 > 0 字节 + 可读 (head -100)
- [ ] 迁移在低峰窗口 (00:00 ~ 06:00)
- [ ] 回滚 SQL 已写 (`sql/rollback/vX.Y.Z-rollback.sql`)

### 2.3 配置 & 凭据 (per Phase 05 §C 红线)

- [ ] `JWT_SECRET` ≥ 32 字符强随机值 (`openssl rand -base64 48`)
- [ ] `DB_PASSWORD` 非默认值 / 非 `please-change-me`
- [ ] `REDIS_PASSWORD` 已设
- [ ] `DRUID_PASSWORD` 强口令或内网限制
- [ ] `.env` 文件未提交 git (`git log -p | grep -E 'JWT_SECRET|DB_PASSWORD'` 为空)
- [ ] 默认 admin `admin/admin123` 已改密码或禁用
- [ ] 编码标志: 启动脚本含 `-Dfile.encoding=UTF-8 ...` 4 标志 (per rules.md §D)

### 2.4 监控 & 告警

按 maturity 差异化 (per [proposal 0010](../../99-跨阶段/proposals/0010-phase06-substrate-only-metrics.md)):

**stable/mature**:
- [ ] 5 指标看板已配 (业务/性能/错误率/用户行为/容量)
- [ ] 告警接收人在 Runbook 明确
- [ ] 告警阈值已定 + 测试触发 1 次

**early** (PLM 当前):
- [ ] 监控替代方案表已写入 Phase 06 §J (≥ 5 项观察手段)
- [ ] 升级路径: 转 stable 时补正式看板

### 2.5 沟通

- [ ] 业务方提前 2 工作日 (或按 maturity 简化) 通知
- [ ] 用户公告 (如 external-product) 已发
- [ ] 客服 / oncall 已对齐回滚条件
- [ ] 灰度策略 (按级别 + 维度差异化, per Phase 05 §D)

---

## 3. 输出

`05-上线/Pre-Deploy-Checklist-<release>.md` (用上面 5 段, 每项打勾或 N/A)。

可选 N/A 项需注理由 (per Phase 05 §I "异常 / 例外")。

---

## 4. 衔接

| 上游 | deploy-checklist | 下游 |
|---|---|---|
| quality-gate-audit Phase 04 准出 | → checklist 准入 | → ops agent §2.1 §H 签字 |
| runbook-writer 回滚命令 | → 2.2 数据库回滚 SQL 已写 | → 灰度执行 |
| pm-priority-matrix release scope | → 2.1 版本号 | → 2.5 沟通范围 |

---

## 5. 反模式

- ❌ "数据库备份了" 不验证文件可读
- ❌ JWT_SECRET 仍 default (please-change-me)
- ❌ 默认 admin 未禁 / 未改密
- ❌ 编码标志缺失 (rules.md §D MUST)
- ❌ 监控空 (early 也必填 substrate-only 表)
- ❌ 业务方未通知 (突发上线)
- ❌ 5 段任一全 N/A 不解释

---

## 6. 历史

| v0.1 | 2026-05-19 | 首版; ops 配套 4 skill 之二; 5 段 + maturity 差异化 + 凭据红线 |
