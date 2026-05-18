# FeatureFlag 模块 — API 设计 (骨架)

| 字段 | 值 |
|---|---|
| 版本 | v1.0-skeleton (派生于 commit b158d2f / 2026-05-17) |
| 关联 PRD | [FeatureFlag-PRD.md](../01-立项/FeatureFlag-PRD.md) |
| Base URL | `/business/feature-flag` |
| 权限串前缀 | `business:feature-flag:*` |
| Controller | [plm-backend/plm-feature-flag/.../FeatureFlagController.java](../plm-backend/) |

## 1. 端点清单 (6 个标准 CRUD)

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/business/feature-flag/list` | `business:feature-flag:list` | 列表 (分页+搜索) |
| GET | `/business/feature-flag/{id}` | `business:feature-flag:query` | 详情 |
| POST | `/business/feature-flag` | `business:feature-flag:add` | 新建 |
| PUT | `/business/feature-flag` | `business:feature-flag:edit` | 修改 |
| DELETE | `/business/feature-flag/{ids}` | `business:feature-flag:remove` | 逻辑删除 |
| POST | `/business/feature-flag/export` | `business:feature-flag:export` | 导出 Excel |

## 2. 请求/响应 Schema

请求体字段见 [PRD-MAPPING.md §2 "FeatureFlag"](../PRD-MAPPING.md);响应统一封装 RuoYi 标准 `AjaxResult` / `TableDataInfo`。

## 3. 错误码

见 [PRD-MAPPING.md §4 错误码规范](../PRD-MAPPING.md);本模块常见:601 (状态机) / 602 (必填) / 604 (字段格式) / 702 (FK 不存在)。

## 4. AI 端点 (如有)
见 [PRD-MAPPING.md §6 AI 能力清单](../PRD-MAPPING.md)。

## 5. 特殊端点

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/business/feature-flag/{id}/toggle` | `business:feature-flag:edit` | 切换 Flag 开关 (置 `status='00'↔'01'`,无审批快速生效) |
| GET  | `/business/feature-flag/check?flagKey=&environment=&userId=` | (无需权限,公共端点 / 应用 token 鉴权) | 实时判定 (canary 用 `abs(hashCode(userId)) % 100 < rolloutPercentage`),供应用方调用 |
| PUT  | `/business/feature-flag/{id}/rollout` | `business:feature-flag:edit` | 调整灰度百分比 (校验 strategy-percentage 一致性: canary 必须 1-99,all_on 必须 100,all_off 必须 0,违反抛 604) |
| GET  | `/business/feature-flag/{id}/audit-log` | `business:feature-flag:query` | 取 Flag 变更审计日志 (灰度推进时间线) |
