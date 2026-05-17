# ADR 一致性审计 — 季度独有 6 维对比

> 季度独有动作。每个 accepted ADR (`03-开发/ADR/NNNN-*.md`) 逐一对比 "ADR 写的决策" vs "现存代码 / 部署 / Phase 进度实际状态"。
> 防 ADR 沉睡 / 沉默漂移反模式。

---

## 准备 — 列所有 accepted ADR

```bash
ls 03-开发/ADR/ | grep -E "^[0-9]{4}-" | sort > /tmp/all-adrs.txt

# 各自状态 (假设 ADR 元信息含 status: accepted / superseded / deprecated)
for f in $(cat /tmp/all-adrs.txt); do
    STATUS=$(grep -i "^| status\|^- status\|^status:" "03-开发/ADR/$f" | head -1)
    echo "$f -- $STATUS"
done
```

只对 **accepted** 的 ADR 跑审计。其他状态 (superseded / deprecated / proposed) 不在本审计 scope。

---

## 6 维对比 (每 ADR 都走一遍)

### 维度 1: ADR 决策 vs 代码实现

例: `ADR-0001 项目编号规则 PRJ-YYYY-NNNN`

```bash
# grep 代码是否实现该决策
grep -rE 'PRJ-[0-9]{4}-[0-9]{4}' plm-backend/plm-project/
# 是否有 ServiceImpl.generateProjectNo 方法?
grep -l 'generateProjectNo' plm-backend/plm-project/**/*ServiceImpl.java
```

判定:
- ✅ 完全实现, 现存代码遵循 ADR
- ⚠️ 部分实现 (某模块遵循某模块不)
- ❌ 未实现 (ADR 写了但代码没动)

### 维度 2: ADR 决策 vs 部署 / 配置实际

例: `ADR-0005 ServiceImpl.checkExists 必须 throw 702`

```bash
# 现存 Service 是否真按签名实施
grep -l 'void checkExists' plm-backend/**/*Service.java        # 期望
grep -l 'boolean checkExists' plm-backend/**/*Service.java     # ⚠️ 不符合
```

判定: ADR 决策 vs 实际部署 codebase 差异。

### 维度 3: ADR 决策 vs Phase 模板

例: `ADR-0007 文档类型按 type 分别累加 (DOC-PRD-YYYY-NNNN / DOC-ARCH-YYYY-NNNN)`

```bash
# Phase 02 设计模板是否引用 ADR-0007?
grep -l "ADR-0007\|DOC-PRD-\|DOC-ARCH-" 99-跨阶段/gate-checklists/Phase02-设计-Gate.md
```

判定: ADR 决策是否在 Gate 模板 / 模块工作流中得到反映。

### 维度 4: ADR 决策 vs 后续 proposals

例: ADR-0003 "Redis 单点高可用计划" 后续是否有 proposal 跟进?

```bash
# proposal 提到 ADR-0003 的次数
grep -rl "ADR-0003" 99-跨阶段/proposals/
```

判定:
- ADR 决策已被后续 proposal 推进或落地 → 一致
- ADR 决策被后续 proposal 否决 → 应该走 reverted / superseded

### 维度 5: ADR 决策 vs reflect / dogfood 反馈

例: ADR-0004 决策 "Sprint 编号 SPR-YYYY-NNNN", reflect 是否反馈有冲突?

```bash
grep -rl "ADR-0004\|SPR-[0-9]" 99-跨阶段/reflect/
```

判定: 实操中是否遇到 ADR 决策落地困难。

### 维度 6: ADR 状态本身

ADR 元信息中的 status (accepted / superseded / deprecated) 是否准确?

判定:
- ADR 仍 accepted, 但维度 1-5 显示已被否决 → 应改 superseded / deprecated
- ADR 标 superseded by NNNN, 但 NNNN 不存在 → ADR 元信息错

---

## 判定汇总

| 整体判定 | 条件 | 后续 |
|---|---|---|
| ✅ 一致 | 维度 1-5 全 ✓ | 保留 accepted, 无 action |
| ⚠️ 漂移 | 维度 1-5 有 1-2 处 ❌ / ⚠️ | amend ADR 或派生 proposal 校正 |
| ❌ 失效 | 维度 1-5 有 ≥ 3 处 ❌ | 走 reverted: 标 deprecated, 写"失败学习"段 |

---

## 反模式

- ❌ ADR 只写没维护 (季度审计才发现漂移 6 月)
- ❌ ADR 编号跳跃 (0001 → 0003, 0002 在哪?)
- ❌ ADR 引用其他 ADR 但链接死了
- ❌ ADR 元信息 status 字段缺失或不一致

每季度审计是防御 ADR 沉睡的唯一手段。

---

## 输出格式 — 季报 §A 段

| ADR | 标题 | 维度 1 代码 | 维度 2 部署 | 维度 3 模板 | 维度 4 proposal | 维度 5 reflect | 维度 6 status | 整体 |
|---|---|---|---|---|---|---|---|---|
| ADR-0001 | 项目编号 | ✅ | ✅ | ✅ | N/A | N/A | accepted ✓ | ✅ 一致 |
| ADR-0005 | checkExists 签名 | ⚠️ (Sprint boolean) | ⚠️ | ⚠️ | 0100 amend | F-AUDIT-F2 | accepted | ⚠️ 漂移 → BL-004 |
| ... | | | | | | | | |

**统计**: 一致 N, 漂移 M, 失效 K. {如有失效, 列每条"失败学习"}
