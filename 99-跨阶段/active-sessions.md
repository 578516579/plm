# 活跃 Session 注册表

> 协作规范 §3 任务认领台账 [在途任务.md](在途任务.md) 的辅助文件:**实时锁视图**。
>
> **作用**:让任何 session 开工前能 30 秒看出"现在谁在锁哪些 SSoT / 哪个模块",避免撞 §4 同模块串行红线和 §2 SSoT 串行 merge 槽位。
>
> **跟在途任务.md 的区别**:
> - 在途任务.md 是**全周期台账**(开工→完工→归档),关心"是谁在做什么"。
> - active-sessions.md 是**实时锁视图**(只列活跃 session),关心"哪些资源现在被谁锁住"。
>
> **维护规则**:开工首条 → 加行 / 完工 / 转暂停 → 删行或挪暂停段 / > 24h 未动 → 任意 session 可问"是否还在跑"。

---

## 当前活跃锁

| 锁主 (Session) | 分支 / Worktree | 锁住的 SSoT 文件 | 锁住的业务模块代码 | Gate 实例 Owner | 起始 | 预计释放 |
|---|---|---|---|---|---|---|
| Claude (Wjl 会话, 2026-05-25) — **需求评审过程** | chore/local-start-backend-script (主工作树) | PRD-MAPPING.md §2 Requirement + §2 Prd + §3 状态机 + §5 URL/权限 + §6 字典 | plm-requirement / plm-prd (代码 + SQL) | requirement / prd 模块 Phase 01-04 实例(本期新增,**不动**原 prd-align-batch-2026-05-17) | 2026-05-25 | 本 session 内 |
| Claude (Wjl 会话, 2026-05-25) — **zentao 双向同步钩子**(并存) | 同上 (chore branch 同 worktree) | (无 SSoT;proposal 0014 已 merged) | plm-defect / plm-requirement (事件发布钩子,未提交) / plm-integration (untracked) | (proposal 0014 配套,无新 Gate) | 2026-05-25 起在 working tree | 本 session 内或单独 commit 拆出 |

---

## 🤖 机器可读认领块 (CLAIMS — hook 解析用)

> 上面的表给**人**看;下面的 `CLAIM` 行给 **`.claude/hooks/session-guard.sh`** 看(**勿删 `CLAIMS:START/END` 标记**)。
> 开工认领文件 = 加一行;**提交/完工后删掉你那行**(就像释放锁)。格式:
> `CLAIM | <session 标签> | <分支> | <repo 相对路径;分号分隔多个> | <心跳日期>`
> 命中后 hook 会在你 `git add` 该脏文件、或 `Edit` 该文件时提醒(详 [协作规范 §19](协作规范.md))。
> ⚠ 这是 **nudge**(只提示不硬拦):hook 不知道哪条 CLAIM 是"你"的,所以会把所有认领都提示出来,自己判断是不是自己的。

<!-- CLAIMS:START -->
<!-- 当前无活跃认领。开工时仿下例加真行(去掉本注释包裹),完工/提交后删除你的行:
CLAIM | <session 标签> | <分支> | <repo 相对路径;分号分隔> | <心跳日期>
-->
<!-- CLAIMS:END -->

---

## 锁冲突避让规则(摘自协作规范 §4)

- **同业务模块代码 + SQL** 在任一时刻**只允许一个 session 改**。文档/PRD-MAPPING.md/E2E spec 可并行。
- **同模块同阶段 Gate 实例**禁两个 session 同时签字。需要复签 → 文件名加 `-v2`,两份都保留。
- **SSoT 文件改动**走串行 merge(协作规范 §2)。开工不锁,**合入 main 时**才用槽位。

---

## 历史(保留 ≤ 7 天)

| 锁主 | 锁范围 | 起 | 止 | 备注 |
|---|---|---|---|---|

---

## 链接

- 详细规则:[协作规范.md §2 SSoT + §3 任务认领 + §4 同模块串行 + §13 角色矩阵](协作规范.md)
- 全周期台账:[在途任务.md](在途任务.md)
- 编号防撞:[协作规范.md §5](协作规范.md)
