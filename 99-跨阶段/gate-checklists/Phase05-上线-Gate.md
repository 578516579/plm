# Phase 05 — 上线 Gate Checklist

> 复制本模板到 `instances/<模块>/Phase05-上线-Gate-<YYYY-MM-DD>.md`，每项打勾 / 填值后归档。
> **任何级别（L1/L2/L3）的发布都必须走本 Gate**，区别仅在评审颗粒度。

---

## 头部信息（必填）

| 字段 | 值 |
|---|---|
| 模块名 | |
| 分级 | L1 / L2 / L3 |
| **分级理由** | _引用 [README §维度 1](../README.md)_ |
| **项目类型** | `external-product` / `internal-tool` / `framework-upgrade` |
| **团队规模** | `solo` / `small` / `medium` / `large` |
| 版本号 | vX.Y.Z |
| Owner（发布指挥） | |
| 计划上线时间 | YYYY-MM-DD HH:MM |
| 实际上线时间 | YYYY-MM-DD HH:MM |
| 灰度方案 | 内部用户 → 5% → 50% → 100% |
| 是否窗口期 | 是 / 否（影响用户 → 公告时间 __） |

---

## A. 准入条件

- [ ] [Phase 04 Gate](Phase04-测试-Gate.md) 已签字通过（L3 仅需 D.4 回归段）
- [ ] 版本号已分配并更新（pom.xml `<plm.version>` + frontend `package.json` version）
- [ ] 发布计划已写（[05-上线/发布计划.md](../../05-上线/发布计划.md) 实例）
- [ ] 已通知 oncall + 业务方 + 客服

---

## B. 必产出物 — 上线材料

### B.1 上线 Checklist

- [ ] 文件：基于 [05-上线/上线 Checklist.md](../../05-上线/上线%20Checklist.md) 复制实例
- [ ] **代码 & 构建** 全部打勾
- [ ] **数据库** 全部打勾（DDL 已演练 / 备份完成 / 迁移在低峰）
- [ ] **配置 & 凭据** 全部打勾（生产 JWT_SECRET / DB_PASSWORD / REDIS_PASSWORD / DRUID_PASSWORD 已更新；默认弱口令禁用）
- [ ] **监控 & 告警** 全部打勾
- [ ] **沟通** 全部打勾

### B.2 Runbook 增量

- [ ] [05-上线/Runbook.md](../../05-上线/Runbook.md) 已加入本次特殊处置（如有）
- [ ] 回滚命令已具体到行（含 jar tag、SQL 回滚脚本路径）
- [ ] 回滚 Owner 明确，并在发布窗口期内待命

### B.3 Changelog

- [ ] [05-上线/Changelog.md](../../05-上线/Changelog.md) 已加入本版本块
- [ ] 内容按 Keep a Changelog 风格分 Added / Changed / Fixed / Removed
- [ ] 用户可见变化已用通俗语言描述（非技术黑话）

### B.4 tag

- [ ] git tag 已打：`git tag -a vX.Y.Z -m "..."`
- [ ] tag 已 push：`git push origin vX.Y.Z`

---

## C. 凭据与权限红线（强制）

- [ ] 生产 `JWT_SECRET` 已替换为 ≥ 32 字符强随机值（`openssl rand -base64 48`）
- [ ] 生产 DB 密码非默认 `please-change-me` / `password`
- [ ] 生产 Druid 监控台已设强口令或仅内网可达
- [ ] `.env` 文件未提交进 git（再 grep 一次 `git log -p | grep -E "JWT_SECRET|DB_PASSWORD"`）
- [ ] 默认管理员 `admin/admin123` 已修改密码或禁用

---

## D. 灰度方案（L1/L2 强制；L3 可跳）

- [ ] 灰度步骤已细化（哪些用户 → 多久 → 看哪些指标）
- [ ] 每步的"放行 / 回滚"判据已量化
- [ ] 灰度期间的指标看板链接已就绪（[06-运营/数据看板](../../06-运营/数据看板（链接）/)）

---

## E. 发布执行（按时间顺序填）

| 步骤 | 计划时间 | 实际时间 | 状态 | 备注 |
|---|---|---|---|---|
| 备份数据库 | | | ✅ / ❌ | |
| 执行 DDL 迁移 | | | | |
| 部署后端 | | | | |
| 部署前端 | | | | |
| 灰度第 1 步 | | | | |
| 灰度第 2 步 | | | | |
| 全量 | | | | |
| 上线后观察期开始 | | | | |

---

## F. 上线后验证（强制）

- [ ] 关键路径 5 个用例已手动 / 自动跑通
- [ ] 监控看板 30 分钟内全绿（错误率 / P99 / 业务转化）
- [ ] 客服 / 业务方未报告异常
- [ ] 日志中无新增 ERROR 堆栈

---

## G. Definition of Done

- [ ] B / C / D / E / F 全部满足
- [ ] [05-上线/Changelog.md](../../05-上线/Changelog.md) 标记本版本为已发布
- [ ] tag 已打、已 push
- [ ] **本 Checklist 文件已 commit 入库**（`docs(gate): <module> phase 05 passed`）

---

## H. 评审记录与签字（按 团队规模 调整必填角色数）

发布是高风险操作，**所有规模都至少 2 个签字**：

- `solo`=2（发布指挥 + 至少 1 个其他人在场，`[solo-review]` 不适用 — 发布必须双人）
- `small` / `medium` / `large` 按下表全签

| 角色 | 姓名 | 签字日期 |
|---|---|---|
| 发布指挥 | | YYYY-MM-DD |
| 后端 oncall | | |
| 前端 oncall（涉及前端时必填） | | |
| DBA（涉及 DB 迁移时必填） | | |
| 客服代表（external-product 必填） | | |

---

## I. 异常 / 例外（含回滚记录）

| 步骤 | 异常 | 处置 | 结果 |
|---|---|---|---|
| | | 回滚 / 修复 / 接受 | |

如发生回滚：必须填本表 + 在 H 段补 "回滚原因 + 责任复盘"。

---

## J. 进入 Phase 06 的准出确认

- [ ] 上线 30 分钟全绿
- [ ] 灰度已 100% 放量
- [ ] 监控看板已切到本版本

✅ **签字人确认**：

| 角色 | 签字 | 日期 |
|---|---|---|
| 发布指挥 | | |
| 运营 lead（接管观察期） | | |

---

## L2 / L3 简化指引

- **L2**：D 灰度可减为 "10% → 100%" 两步
- **L3**：D 灰度可全量；F 观察期 ≥ 15 分钟即可；G 中 Changelog 用一行描述

---

## 修订记录

| 日期 | 修改人 | 原因 | 决议 |
|---|---|---|---|
| | | | |
