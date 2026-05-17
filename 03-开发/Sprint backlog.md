# Sprint Backlog — 待 Sprint 计划吸纳的 code TODO

> 自进化机制 [proposals/](../99-跨阶段/proposals/) 的**降级通道终点**（[proposal 0040](../99-跨阶段/proposals/0040-self-evolution-v2-meta-rules.md) 引入）。
> signals 候选若被判"非规范变更（性能 / 重构 / 纯代码改造）"，降级到本文件等待 Sprint 计划吸纳。
> 与 [Sprint 计划与回顾/](Sprint%20计划与回顾/) 配套：本文件存"待办"，那边存"按 Sprint 实施 + 回顾"。

---

## 待处理

| ID | 来源 | 标题 | 优先级 | 预期 Sprint | 负责人 | 备注 |
|---|---|---|---|---|---|---|
| BL-2026-001 | signals 2026-05 候选 0023 | Sprint 健康度统计 `selectSprintStats` 4 次 SQL 改 1 次 GROUP BY | P2 | TBD | TBD | 性能优化；Sprint 看板加载偶发慢 |
| BL-2026-002 | signals 2026-05 候选 0024 | Task 看板 LIMIT 50 从内存切片改 SQL 分别拉 | P2 | TBD | TBD | 大数据量场景优化；与 BL-2026-001 同源 |
| BL-2026-003 | signals 2026-05 候选 0026 | Task 看板列从字典 `biz_task_status` 读取，避免 hard-code 5 列 | P3 | TBD | TBD | 重构；解耦字典与 UI 列定义 |
| BL-2026-004 | [proposal 0100](../99-跨阶段/proposals/0100-fk-validation-via-service-checkexists.md) §10 Step 4 + W20 audit F-AUDIT-F2 | 审计 8 active 模块 ServiceImpl 的 FK 校验代码 + 迁移现存 `boolean checkExists` 签名到 `void/throw 702`（ISprintService 等）| P1 | TBD | TBD | 规范变更 (0100) 已 merged；scope 扩展含历史签名迁移（见 0100 §1.9 注） |
| BL-2026-005 | [proposal 0100](../99-跨阶段/proposals/0100-fk-validation-via-service-checkexists.md) §10 Step 5 | 更新 `plm-backend/scripts/new-business-module.sh` 业务模块生成器, 默认产 `Service.checkExists()` 调用（void/throw 签名）| P1 | TBD | TBD | 与 BL-2026-004 同源；先做生成器再批量审计可节省工作量 |
| BL-2026-006 | [W20 tracking audit F-AUDIT-F1](../99-跨阶段/reflect/2026-W20-tracking-audit-mid.md) | 24 个 Phase 0X Gate instance 文件补 "溯及 proposal NNNN" 注 (per 0007/0008/0010/0011/0012/0013/0016/0032 §7 Step 4 集体承诺)| P2 | TBD | TBD | 单纯文本回标，无技术风险；建议批量脚本辅助 |
| BL-2026-007 | [proposal 0101](../99-跨阶段/proposals/0101-mr-url-host-whitelist.md) §10 派生 | 实现 `cn.com.bosssfot.dv.plm.common.utils.UrlValidator` (含 checkHost + CSV 多值版本 + Guava InternetDomainName); Task / ManualProduct ServiceImpl add+edit 调用 | P1 | TBD | TBD | per 0041 grep 后确认 2 个字段; 含 unit test |
| BL-2026-008 | [proposal 0101](../99-跨阶段/proposals/0101-mr-url-host-whitelist.md) §10 派生 | 配 application-dev/staging/prod.yml 的 `plm.url.allowed-hosts.task` + `.manualProduct` 白名单 | P1 | TBD | TBD | 与运维同步具体白名单后做 |

## 已完成（归档）

| ID | Sprint | 完成 commit | 完成日期 |
|---|---|---|---|
| | | | |

---

## 自进化降级判据（proposal 0040 引入）

signals 候选满足以下条件之一 → 降级到本 backlog（**不**升格为 proposal）：

- **性能优化**（不涉及规范变更，仅代码层）
- **单个代码模块的重构**（不影响其他模块的约定）
- **文档 typo / 链接修正**
- **字典 / 配置数据维护**

反过来，**升格为 proposal** 的条件：

- 涉及规范文件（`03-开发/开发规范.md` / `.claude/rules.md`）
- 涉及 Gate 模板（`99-跨阶段/gate-checklists/Phase*.md`）
- 涉及多模块约定（命名 / 接口 / 状态机）
- 涉及自进化机制本身（`99-跨阶段/proposals/README.md`）

边界不清时：**先按 proposal 走**（错杀比漏放代价低）。

---

## 字段说明

- **ID**: `BL-YYYY-NNN` 按年累加
- **来源**: 来自 signals 哪份 / 哪条候选；或直接的 PR review / 用户口头要求
- **优先级**: P0（影响当前 release）/ P1（下个 Sprint 必做）/ P2（季度内）/ P3（有空再做，可能 retire）
- **预期 Sprint**: Sprint 编号或 "TBD"
- **负责人**: 个人 / "TBD"

---

## 进入 Sprint 流程

1. Sprint 计划会议时：从本表选优先级 ≥ P1 的项纳入下个 Sprint
2. 选中后：本行的"预期 Sprint" + "负责人" 字段填实
3. 实施完成：本行整体移到 §已完成 区段，附 commit hash
4. 半年未被 Sprint 吸纳 + 优先级仍 P3 → 提议 retire（在评审会拍板）

---

## 维护节奏

- **每月初**: 检查 P0/P1 是否积压（应当近期就被 Sprint 吸纳）
- **季度末**: 清理 P3 中半年以上未动的项

---

## 修订记录

| 日期 | 修改人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首次创建（per [proposal 0040](../99-跨阶段/proposals/0040-self-evolution-v2-meta-rules.md)），backfill 3 行来自 signals 2026-05 候选 0023/0024/0026 |
