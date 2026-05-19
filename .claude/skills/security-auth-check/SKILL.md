---
name: security-auth-check
description: PLM 鉴权/权限审 — 扫 Controller @PreAuthorize + 前端 v-hasPermi + sys_role_menu 一致性. 当用户说"鉴权审 / 权限串审 / @PreAuthorize / v-hasPermi / 越权 / sys_role_menu"时调用. 输出: 04-测试/security-audit-<module>-<date>.md §4 鉴权矩阵. **security-reviewer agent 的子工具**。
---

# security-auth-check — 鉴权/权限审 skill v0.1

**security-reviewer agent 的子工具**, §2.4。

PLM 鉴权三层: 后端 @PreAuthorize → 前端 v-hasPermi → DB sys_role_menu。必须三层一致, 缺一层即越权风险。

## 1. 何时调用

- "鉴权审 / 权限串审 / @PreAuthorize / v-hasPermi / 越权"
- security-reviewer §2.4 触发
- Phase 03 → 04 准入

## 2. 3 类扫描

### 2.1 Controller @PreAuthorize 覆盖率

```bash
# 列所有 @RequestMapping / @GetMapping 等端点 + 是否有 @PreAuthorize
grep -rn -B 5 '@\(Get\|Post\|Put\|Delete\)Mapping' plm-backend/*/src/main/java/ \
  | grep -v 'PreAuthorize' \
  | tee /tmp/auth-uncovered.txt
# 期望: 0 (除 login / captcha 等公开端点)
```

### 2.2 前端 v-hasPermi vs 后端 @PreAuthorize 一致性

```bash
# 前端权限串
grep -rnE 'v-hasPermi="\[' plm-frontend/src/views/ | sed -E 's/.*v-hasPermi="\[([^]]+)\]".*/\1/' | sort -u

# 后端权限串
grep -rnE '@ss\.hasPermi\(' plm-backend/*/src/main/java/ | sed -E "s/.*@ss\.hasPermi\('([^']+)'\).*/\1/" | sort -u

# 比较: 前端有 / 后端无 = 越权风险 (前端禁用按钮但后端可调)
comm -23 frontend-perms.txt backend-perms.txt
```

### 2.3 sys_role_menu 数据完整性

```sql
-- 任何 menu 都应至少 1 个 role 关联
SELECT m.menu_id, m.menu_name FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
WHERE rm.menu_id IS NULL AND m.menu_type IN ('C', 'F');
```

## 3. 输出 — §4 鉴权矩阵表

```markdown
## §4 鉴权 / 权限审

### 4.1 Controller @PreAuthorize 覆盖

| 端点 | 是否 @PreAuthorize | 权限串 | 风险 |
|---|---|---|---|
| GET /system/user/list | ✅ | system:user:list | 🟢 |
| POST /public/captcha | ⚠ 故意公开 | (无) | 🟢 |
| POST /sprint/checkExists | ❌ 缺 | — | **高 (越权风险)** |

### 4.2 前后端权限串一致性

| 串 | 前端 | 后端 | 风险 |
|---|---|---|---|
| business:sprint:list | ✅ | ✅ | 🟢 |
| business:sprint:export | ✅ | ❌ | **中 (前端有按钮, 后端无校验)** |

### 4.3 sys_role_menu 完整性
- 孤儿 menu (无 role 关联): N 个 → 列出

### 风险评级
- 🟢 0 高 → ✅
- 🟡 1 高 → ⚠️ fix 后重审
- 🔴 ≥ 2 高 → ❌ P0
```

## 4. 衔接

| 上游 | auth-check | 下游 |
|---|---|---|
| backend-coder Controller | → §4.1 | → §4 矩阵 |
| frontend-coder v-hasPermi | → §4.2 | → 前后端协作 |
| db-modeler sys_role_menu seed | → §4.3 | → ops 上线 |

## 5. 反模式

- ❌ Controller 缺 @PreAuthorize "因为前端隐藏了菜单" (前端不可信)
- ❌ 前端 v-hasPermi 但后端忘加 (假权限)
- ❌ 通配权限 `business:*:*` 给非 admin role

## 6. 历史

| v0.1 | 2026-05-19 | 首版; security-reviewer 4 配套 之 4 (完结) |
