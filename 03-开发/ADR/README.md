# ADR — 架构决策记录

Architecture Decision Records：每个**有争议、不可逆、影响范围广**的技术决策一份独立 .md。

## 命名规范

`NNNN-标题.md`，编号递增，例如：
- `0001-选-springboot-4.md`
- `0002-业务模块放-system-子包还是独立-business-模块.md`
- `0003-多数据源是否启用.md`

## 编号台账（防撞号,新增前先查这里）

| 编号 | 主题 | 文件 |
|---|---|---|
| 0001 | project `project_no` 编号规则(PRJ-YYYY-NNNN)| `0001-project-no-rule.md` |
| 0002 | requirement 编号(REQ-YYYY-NNNN)| _(草案,待建文件)_ |
| 0003 | task 编号(TASK-YYYY-NNNN)| _(草案,待建文件)_ |
| 0004 | sprint 编号(SPR-YYYY-NNNN)| _(草案,待建文件)_ |
| 0005 | defect 编号(DEFECT-YYYY-NNNN)| _(草案,待建文件)_ |
| 0006 | testcase 编号(TC-YYYY-NNNN)| _(草案,待建文件)_ |
| 0007 | document 编号(DOC-TYPE-YYYY-NNNN)| _(草案,待建文件)_ |
| **0008** | 进程内领域事件总线(否决 Kafka)| `0008-in-process-domain-event-bus.md` |
| **0009** | 集成层回写旁路业务 Service(裸 JDBC)| `0009-integration-writeback-bypasses-business-service.md` |

> **0001~0007 是各业务模块"编号规则"系列**(多被代码/SQL/测试/Gate 引用,见 `grep -r "ADR-000"`),架构类 ADR **从 0008 起**递增。新增 ADR 前 `grep -rn "ADR-NNNN"` 确认编号未被占用。
>
> ⚠ **已知撞号(待修)**:`0001-git-github-workflow-setup.md` 内部 H1 也写 `ADR-0001`,与本 project_no ADR 撞号。该文件为他人(@578516579)所建、当前另有 session 活跃,**留待协调后**重编为 0010(无其他文件按路径引用它,低 ripple)。

## 模板（建议每条 ADR 都用）

```markdown
# ADR-NNNN: <决策标题>

- 状态：proposed / accepted / deprecated / superseded by ADR-XXXX
- 日期：YYYY-MM-DD
- 决策人：

## 背景
> 为什么要决策？面临什么问题？

## 决策
> 我们选了什么？

## 理由
> 为什么选这个而不是别的？关键权衡。

## 后果
> 这个决策带来的好处、代价、需要后续配合的事。
```

## 何时新增

- 引入 / 替换核心框架、中间件、第三方服务
- 模块拆分 / 合并
- 关键数据模型变更
- 部署架构调整
- 不可逆的命名约定

## 何时不需要

- 写一段业务代码、修一个 bug — 提交信息就够
- 临时方案 / 实验性脚本 — 不上 ADR
