---
name: security-sql-injection-scan
description: PLM SQL 注入审计 — 扫 Mapper XML `${var}` 拼接 + ServiceImpl String concat SQL + jdbcTemplate.queryForList String concat. 当用户说"SQL 注入审 / Mapper 安全 / ${} 拼接 / 字符串拼 SQL / SAST"时调用. 输出: 04-测试/security-audit-<module>-<date>.md §1 SQL 注入风险表. **security-reviewer agent 的子工具**。
---

# security-sql-injection-scan — SQL 注入审 skill v0.1

**security-reviewer agent 的子工具**, 主走 §2.1 SQL 注入审职责。

PLM 用 MyBatis (mybatis-spring-boot 4.0.1), 99% SQL 在 Mapper XML 中。注入风险点 3 个: `${var}`、字符串拼接、jdbcTemplate.queryForList。

---

## 1. 何时调用

- 用户说 "SQL 注入审 / Mapper 安全 / ${} 拼接 / SAST"
- security-reviewer agent §2.1 触发
- 每次 Mapper XML 改动 + Phase 03 → 04 准入前
- Phase 06 P0 安全缺陷复发

---

## 2. 3 类风险扫描

### 2.1 Mapper XML `${var}` (vs `#{var}`)

```bash
# 扫所有 Mapper, 找含 ${} 但不应该用 ${} 的位置
# ${} 只能用于动态表名 / order by 字段名, 用户输入必须 #{}
grep -rn '\${' plm-backend/*/src/main/resources/mapper/ \
  | grep -v '\(orderBy\|tableName\|ASC\|DESC\)' \
  | tee /tmp/sql-injection-suspects.txt
```

每行结果都需要人工审核: 是合法 `${}` (orderBy / 表名) 还是用户输入 (危险)。

### 2.2 Java 字符串拼接 SQL

```bash
grep -rnE 'sql\s*\+\s*"|"\s*\+\s*[a-zA-Z_]+\s*\+\s*"' plm-backend/*/src/main/java/ \
  | grep -iE 'select|insert|update|delete|where'
```

凡是出现 SQL 字符串拼接 → 必须重构为 MyBatis Mapper #{} 参数化。

### 2.3 jdbcTemplate / NamedParameterJdbcTemplate

```bash
grep -rnE 'jdbcTemplate\.(query|update|execute|queryFor)' plm-backend/*/src/main/java/
```

必须使用 `?` placeholder + `args` 数组, 不能 String concat。

---

## 3. 输出 — §1 SQL 注入风险表

```markdown
## §1 SQL 注入风险

### 1.1 Mapper XML `${var}` 审查

| 文件 | 行号 | 内容 | 风险 | 建议 |
|---|---|---|---|---|
| ProjectMapper.xml | 45 | `ORDER BY ${orderBy}` | 中 (orderBy 来自前端) | 加白名单 SET(orderBy) ∈ {'id','create_time'} |
| TaskMapper.xml | 102 | `WHERE name LIKE '${keyword}%'` | **高** | 立即改为 `#{keyword}` |

### 1.2 Java 字符串拼接 SQL

| 文件 | 行号 | 内容 | 风险 |
|---|---|---|---|
| ReportServiceImpl.java | 67 | `sql = "select * from tb_" + entityType` | 中 | 用 enum 白名单或 MyBatis dynamic SQL |

### 1.3 jdbcTemplate 检查

| 文件 | 行号 | 风险 |
|---|---|---|
| (无) | — | ✅ |

### 风险评级
- 🟢 **0 高** + ≤ 2 中 → ✅ 通过 Phase 04 准入
- 🟡 1 高 → ⚠️ 阻塞, fix 后重审
- 🔴 ≥ 2 高 → ❌ 阻塞, 触发 P0 安全缺陷
```

---

## 4. 衔接

| 上游 | sql-injection-scan | 下游 |
|---|---|---|
| backend-coder Mapper 改动 | → 扫描全表 | → security-audit-<module>.md §1 |
| tech-lead db-design DDL | → 配合检查动态字段 | → backend-coder 修代码 |

---

## 5. 反模式

- ❌ `${userInput}` 直接拼 user 输入字段
- ❌ `String sql = "SELECT * FROM " + tableName` 无白名单
- ❌ jdbcTemplate.query("SELECT * FROM tb WHERE name='" + name + "'", ...) 不用 ? 占位
- ❌ 审过 1 次就永远过 (每次 Mapper 改动应重扫)

---

## 6. 历史

| v0.1 | 2026-05-19 | 首版; security-reviewer 4 配套 之 1 |
