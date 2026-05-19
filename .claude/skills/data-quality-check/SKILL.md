---
name: data-quality-check
description: PLM 数据质量校验 — 5 维度 (完整性/一致性/准确性/时效性/唯一性) + 对账规则. 用户说"数据质量 / 数据完整性 / 数据对账 / 唯一性检查 / FK 完整性"时调用. 输出: 04-测试/data-quality-suite-<dashboard>.md. **data-engineer agent 的子工具**。
---

# data-quality-check — 数据质量校验 skill v0.1

**data-engineer agent §2.4 配套**。

## 1. 何时调用
- "数据质量 / 完整性 / 对账 / 时效性"
- data-engineer §2.4 触发, Phase 04 数据验收

## 2. 5 维度

### 2.1 完整性
```sql
-- 非空率
SELECT (SUM(CASE WHEN <key> IS NULL THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS null_pct FROM fact_x;
-- 期望: < 1%
-- FK 完整性
SELECT COUNT(*) FROM fact_x f LEFT JOIN dim_y d ON f.y_id = d.y_id WHERE d.y_id IS NULL;
-- 期望: 0
```

### 2.2 一致性
- 单位 (天 vs 小时 vs 秒)
- 编码 (UTF-8 vs GBK; per rules.md §D)
- 时区 (UTC / Asia/Shanghai)

### 2.3 准确性
```sql
-- 与业务源对账
SELECT COUNT(*) FROM tb_sprint WHERE status='completed';  -- 业务源
SELECT SUM(measure) FROM fact_sprint_complete;            -- ETL 结果
-- 期望: 一致 (差 < 1%)
```

### 2.4 时效性
```sql
-- ETL 延迟
SELECT MAX(etl_load_time) - MAX(source_update_time) FROM fact_x;
-- 期望: < 15 min (报表场景)
```

### 2.5 唯一性
```sql
-- PK 重复
SELECT pk, COUNT(*) FROM fact_x GROUP BY pk HAVING COUNT(*) > 1;
-- 期望: 0 行
```

## 3. 输出模板
```markdown
# Data Quality Suite — <dashboard>

| 维度 | 检测 SQL | 期望阈值 | 当前值 | 状态 |
|---|---|---|---|---|
| 完整性 - null 率 | ... | < 1% | 0.3% | ✅ |
| 完整性 - FK | ... | = 0 | 0 | ✅ |
| 一致性 - 编码 | grep EFBFBD | = 0 | 0 | ✅ |
| 准确性 - 对账 | ... | 差 < 1% | 0.2% | ✅ |
| 时效性 - ETL 延迟 | ... | < 15 min | 8 min | ✅ |
| 唯一性 - PK 重复 | ... | = 0 | 0 | ✅ |
```

## 4. 衔接
- 上游: data-model-design (fact/dim 表)
- 下游: tester quality-gate-audit "数据" 维度
- Phase 06 监控: 数据质量基线作为告警阈值

## 5. 历史
| v0.1 | 2026-05-19 | 首版; data-engineer 配套 4/4 (完结) |
