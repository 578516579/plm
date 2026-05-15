# Proposal 0005: solo 模式下 Sprint 计划 / 回顾文件可合并到 Gate 实例

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0005 |
| 标题 | solo 团队规模下 Sprint 文档不必单独产出 |
| 状态 | **proposed** |
| 类型 | 流程 |
| 提出人 | Wjl + Claude |
| 提出日期 | 2026-05-15 |

---

## 1. 背景

Phase 03 模板要求 C 必产出物 "Sprint 计划 + Sprint 回顾文件"。但在 solo 团队规模下：
- 当前 Sprint 就一个人在做，"计划"等于"我自己决定做什么"
- "回顾"等于"我自己写本期完成情况"
- 这些内容 **已经在 Gate 实例 §G / §H / §I 中体现** —— 单独产出独立 Sprint 文档纯粹是抄写

## 2. 证据

- 关联 reflect: [../reflect/2026-W20-project-phase03-dogfood.md](../reflect/2026-W20-project-phase03-dogfood.md) §2.2
- 关联 Gate 实例: F-P03-02
- 0002 已经做了"签字角色数"按团队规模调整，但**没有动 C 段必产出文档**

## 3. 提案

在 Phase 03 模板 §C "Sprint 计划 / Sprint 回顾文件" 后面追加按团队规模差异化的注脚：

```diff
- [ ] Sprint 计划已写：`03-开发/Sprint 计划与回顾/Sprint-NN-...md`
- [ ] Sprint 回顾已写（哪怕一句话）
+ [ ] Sprint 计划（按团队规模）：
+   - `solo`: 可省略独立文档，由本 Gate 实例 §H/§I 替代
+   - `small+`: 文件 `03-开发/Sprint 计划与回顾/Sprint-NN-...md`
+ [ ] Sprint 回顾（按团队规模）：同上
```

## 4. 改动文件

仅 1 个：`gate-checklists/Phase03-开发-Gate.md` §C

## 5. 影响

- solo 模式下 Phase 03 少产出 2 个文件
- 团队扩张到 small+ 后自动回到旧约束

## 6. 风险

- 团队从 solo 扩张到 small 时，没有 Sprint 历史可读 → 用 Gate 实例已经保留了核心内容，可接受

## 7. 实施

合并到 0004 同次 PR。

## 8. 衡量

- Phase 03 E 段豁免数（与 Sprint 文档相关）: 1 → 0

## 9. 评审

| 评审人 | 立场 | 日期 |
|---|---|---|
| Wjl | _待_ | |

---

## 修订

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-15 | Wjl + Claude | 初版 |
