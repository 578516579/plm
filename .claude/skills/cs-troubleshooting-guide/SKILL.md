---
name: cs-troubleshooting-guide
description: PLM 排错指南 — 现象 → 排查步骤 → 升级路径. 用户说"排错 / troubleshooting / 排查指南 / 升级路径"时调用. 输出: 06-运营/cs-troubleshooting.md. **customer-support agent 的子工具**。
---

# cs-troubleshooting-guide — 排错指南 skill v0.1

## 1. 何时调用
- "排错 / troubleshooting / 排查"
- customer-support §2.2

## 2. 结构

每条:
- 现象 (用户描述)
- 排查步骤 (1-5 步, 简单到复杂)
- 升级路径 (排查仍无解 → 找谁)

## 3. 输出模板
```markdown
# PLM 排错指南

## 现象: 看不到 "项目" 菜单

### 排查步骤
1. 检查是否登录 (左上角是否显示用户名)
2. 检查角色 (我的 → 个人信息 → 角色应含 "项目管理员")
3. 刷新浏览器 (Ctrl+F5)
4. 清缓存 + 重登

### 升级
排查 4 步仍无解 → 联系运维 (Wjl@example)
```

## 4. 历史
| v0.1 | 2026-05-19 | 首版; customer-support 配套 2/4 |
