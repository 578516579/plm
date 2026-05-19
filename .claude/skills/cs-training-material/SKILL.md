---
name: cs-training-material
description: PLM 培训素材编写 — 新用户上手 (5 min) + 进阶 (30 min) + 管理员 (1h). 用户说"培训 / 培训素材 / onboarding / 上手指南"时调用. 输出: 06-运营/cs-training-<level>.md. **customer-support agent 的子工具**。
---

# cs-training-material — 培训素材 skill v0.1

## 1. 何时调用
- "培训 / onboarding / 上手"
- customer-support §2.3

## 2. 3 层级培训

| 层级 | 时长 | 目标 | 内容 |
|---|---|---|---|
| 新用户上手 | 5 min | 完成首个任务 | 登录 → 看项目列表 → 创建第一个任务 |
| 进阶 | 30 min | 掌握 80% 日常 | 全模块 CRUD + 状态机 + 协作 |
| 管理员 | 1h | 系统管理 | 用户/角色/菜单/字典/参数/通知 |

## 3. 输出模板
```markdown
# PLM 新用户上手 (5 min)

## Step 1 登录 (1 min)
- URL: http://plm.example
- 默认账号: 找运维要 (不要用 admin/admin123, 已禁)

## Step 2 看项目 (1 min)
- 左侧菜单 → 项目
- 顶部搜索框筛选

## Step 3 创建首个任务 (3 min)
- 点 "+ 新建任务"
- 填名称 + 负责人 + 截止日期
- 点 "保存"
- 看任务列表确认
```

## 4. 历史
| v0.1 | 2026-05-19 | 首版; customer-support 配套 3/4 |
