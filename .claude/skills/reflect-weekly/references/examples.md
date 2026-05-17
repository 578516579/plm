# 已有反思示例 — 调用 skill 时拿来对比格式 / 深度

按场景分类指向。新 reflect 写之前可以快速 Read 1-2 份对比格式。

---

## 1. 周末闭合反思 (典型周报)

- [2026-W20.md](../../../../99-跨阶段/reflect/2026-W20.md) — W20 首份周末闭合
  - 长度 ~150 行 / 5 处 friction (F-W20-01 ~ F-W20-05) / 衍生 7 proposal
  - 量化数据表 11 字段
  - 主诊断 "信号产 4.7× 于处理速率" 这种 1-句话精炼

**模仿要点**:
- 头部 6 字段 + 关联 ad-hoc reflects
- §1.1 量化数据 (来自 git log + Gate + signals)
- §1.3 friction 编号 F-WW-NN 系统
- §3 行动 3 段 (本会话 / 下周 / 月底)

---

## 2. 事件触发 ad-hoc 反思 (dogfood 实战回顾)

- [2026-W20-project-phase01-dogfood.md](../../../../99-跨阶段/reflect/2026-W20-project-phase01-dogfood.md) — Project Phase 01 走完 Gate 后的复盘
- [2026-W20-project-phase03-dogfood.md](../../../../99-跨阶段/reflect/2026-W20-project-phase03-dogfood.md) — Phase 03 同款

**模仿要点**:
- 头部场景 = "Project 模块走完 Phase 01 立项 Gate (dogfood)"
- §1.2 "顺手的部分 ✅" + "不顺手的部分 ⚠️" 二分
- §2 §3 之外加 §4 关注下一步 + §6 一句话总结

---

## 3. 元反思 (反思反思机制本身)

- [2026-W20-self-evolution-process-meta.md](../../../../99-跨阶段/reflect/2026-W20-self-evolution-process-meta.md) — Self-evolution v1→v2 升级触发反思
  - 5 处 F-META-NN 盲区 → bundle 1 个 proposal 0040
  - 元复盘段 §4 "关于本反思自身"

**模仿要点**:
- 不再反思业务模块, 而是反思"反思机制本身"
- 5 Whys 用于挖根因 (业务 reflect 一般不挖那么深)
- §4 元复盘有"递归 1 层" 标识

---

## 4. Tracking 中段审计 (规则验证型反思)

- [2026-W20-tracking-audit-mid.md](../../../../99-跨阶段/reflect/2026-W20-tracking-audit-mid.md) — 21 proposal merged 后中段审计
  - 8 处 F-AUDIT (4 pass + 4 fail/需关注)
  - 用 grep / find 量化验证规则真落地

**模仿要点**:
- 表格化 audit findings (PASS / FAIL 分两组)
- 每条 fail 立即 "本会话同 commit 修复" 或 "派生 BL"
- §6 一句话总结点明 "审计密度" 指标

---

## 通用建议

调 `reflect-weekly` skill 时, **先选 1 份相似场景 reflect Read 一遍**, 再写新 reflect。会大幅减少 "结构走样" 概率。

Decision tree:
```
当周是事件触发?
├─ 是 → 走类型 2 (dogfood) 或类型 3 (meta) 模式
└─ 否, 是周末闭合 → 走类型 1 (closing) 模式
    └─ 当周还涉及 audit 任务 → 走类型 4 (audit) 模式补充
```
