# Legacy 镜像 → packages/ 完整迁移 Playbook

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 状态 | 设计 + plm-defect 示范实施 |
| 适用 | 已 active 业务模块 (Project / Requirement / Sprint / Task / Defect / TestCase ...) |

---

## 1. 当前问题

每个 active 业务模块都有 **两套源码**：

| 路径 | 用途 |
|---|---|
| `plm-frontend/packages/plm-<module>/src/{api,types,views,router}/*` | **monorepo 规范源码** (将来唯一) |
| `plm-frontend/src/{api,types,views}/business/<module>/*` | **legacy 镜像** (供 sys_menu 动态路由发现组件) |

每次改动需同步两份,违反 DRY,易引起漂移。

---

## 2. 根因

若依的动态路由从 `sys_menu.component` 字段读路径字符串 (例如 `business/defect/index`),vite 通过 `import.meta.glob('./../views/**/*.vue')` 扫描 `src/views/**/*.vue` 解析组件。

新引入的 packages/ 不在主壳 `src/views/` 下,无法被自动解析。

---

## 3. 迁移方案（3 阶段）

### 3.1 Stage 1: 极薄壳 (本 playbook 落地的策略)

把 `src/views/business/<module>/index.vue` 改成 **极薄壳**,仅做 re-export 不重复内容:

```vue
<!-- src/views/business/defect/index.vue (极薄壳) -->
<template>
  <DefectView />
</template>
<script setup lang="ts">
// 真正的实现在 packages/plm-defect/src/views/index.vue
import DefectView from '../../../../packages/plm-defect/src/views/index.vue'
</script>
```

`src/api/business/defect.ts`:
```ts
// 极薄壳,只 re-export
export * from '../../../packages/plm-defect/src/api/index'
```

`src/types/api/business/defect.ts`:
```ts
export * from '../../../../packages/plm-defect/src/types/index'
```

**优点**:
- 立即生效,不改 router/vite 配置
- 单一真实源 = packages/
- DRY 100% (legacy = 1 行 export)

**缺点**:
- 相对路径长（`../../../../packages/...`）
- TS 编译要 include packages/（已在 tsconfig 完成）

### 3.2 Stage 2: vite-plugin 自动扫描 packages

写一个 vite 插件,启动时把 `packages/plm-*/src/views/*.vue` 软链/虚拟挂载到 `src/views/business/<module>/`,完全删 legacy 文件。

```ts
// vite/plugins/auto-business-modules.ts (待实现)
export default function autoBusinessModules() {
  return {
    name: 'auto-business-modules',
    resolveId(source: string) {
      // src/views/business/<module>/index.vue → packages/plm-<module>/src/views/index.vue
      const m = source.match(/views\/business\/([\w-]+)\/index\.vue$/)
      if (m) {
        return path.resolve(__dirname, `../../packages/plm-${m[1]}/src/views/index.vue`)
      }
    }
  }
}
```

### 3.3 Stage 3: 改 sys_menu component 字段 + 主 router 重写

最终目标:不再用动态 sys_menu 路由,直接在 `src/router/index.ts` 静态聚合:

```ts
import projectRoutes from '@plm/project/router'
import defectRoutes from '@plm/defect/router'
import testcaseRoutes from '@plm/testcase/router'
// ... 30 模块
const routes = [...baseRoutes, ...projectRoutes, ...defectRoutes, ...]
```

`sys_menu` 仅保留权限 + icon + 排序,不再做组件解析。

**优点**: 完全静态,vite 构建期可分析,无运行时反射 + 包按需懒加载。
**缺点**: 改造工作量大,影响权限模型,推 v0.4 stable 转型时做。

---

## 4. Stage 1 实施步骤（本期落地）

每个 active 模块按此 5 步:

```bash
MODULE=defect           # 任意 active 模块
PKG=plm-$MODULE

# 1. 确认 packages/$PKG/src/views/index.vue 是源 (已存在)
[ -f plm-frontend/packages/$PKG/src/views/index.vue ] || echo "❌ 源不存在"

# 2. 改 src/api/business/$MODULE.ts 为极薄壳
cat > plm-frontend/src/api/business/$MODULE.ts <<EOF
/** 极薄壳: 真实现 → packages/$PKG/src/api/index */
export * from '../../../packages/$PKG/src/api/index'
EOF

# 3. 改 src/types/api/business/$MODULE.ts 为极薄壳
cat > plm-frontend/src/types/api/business/$MODULE.ts <<EOF
/** 极薄壳 */
export * from '../../../../packages/$PKG/src/types/index'
EOF

# 4. 改 src/views/business/$MODULE/index.vue 为极薄壳
cat > plm-frontend/src/views/business/$MODULE/index.vue <<EOF
<template>
  <ModuleView />
</template>
<script setup lang="ts">
import ModuleView from '../../../../packages/$PKG/src/views/index.vue'
</script>
EOF

# 5. 启动 vite dev + 浏览器访问 /business/$MODULE 验证 (UI 应与之前一致)
```

如果该模块还有多个 view 文件（如 task 有 index/kanban/my）,需要对每个 view 都建极薄壳。

---

## 5. 已实施

- [x] **plm-defect**: legacy 镜像改极薄壳 (3 文件: api + types + views/index.vue)
- [ ] **plm-project / plm-requirement / plm-sprint / plm-task / plm-testcase**: 保留 legacy 满副本（duplicated,后续按需迁）

---

## 6. 验收

每个迁移完的模块:

- [ ] `npm run dev` 启动正常
- [ ] 浏览器 `/business/<module>` 访问 UI 渲染与之前一致
- [ ] `npm run test:e2e -g "<module>"` 全过 (E2E 不该被迁移影响)
- [ ] `src/views/business/<module>/index.vue` 仅含 1-3 行 import

---

## 7. 验收防回归

新加 active 模块时,**直接走 Stage 1 极薄壳**,不再写满副本:

模板 `src/views/business/<NEW>/index.vue`:
```vue
<template>
  <View />
</template>
<script setup lang="ts">
import View from '../../../../packages/plm-<NEW>/src/views/index.vue'
</script>
```

把这一步加入 [模块拆分指南.md](模块拆分指南.md) §3 新 stub 启动 checklist。

---

## 8. 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建,plm-defect 示范落地 |
