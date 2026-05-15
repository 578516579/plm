# .githooks

git 钩子脚本。**首次 clone 仓库后请手动启用**：

```bash
git config core.hooksPath .githooks
```

执行一次即可，配置写入 `.git/config`，对本地仓库永久生效。**不要在 hook 里改 `.git/config` 之外的全局配置**。

## 当前启用的 hook

| 文件 | 触发时机 | 作用 |
|---|---|---|
| [commit-msg](commit-msg) | `git commit` 写完 message 后、记录前 | 校验 message 符合 Conventional Commits 规范 |

## 跨平台说明

- macOS / Linux：脚本带 shebang，直接可执行。如遇 "Permission denied"：`chmod +x .githooks/commit-msg`。
- Windows + Git Bash：开箱即用。
- Windows + 原生 CMD：不会触发 bash 脚本，建议用 Git Bash 或 IDE 内置 Terminal 提交。

## 绕过校验（不推荐）

`git commit --no-verify -m "..."` 可跳过 hook。仅在以下情况允许：

- CI 自动提交（如 release-please）
- 紧急 hotfix 期间约定俗成

每次绕过都应该在 PR 描述里说明原因。

## 加新 hook

1. 在本目录新建 hook 脚本（文件名同 git 钩子名：`pre-commit / pre-push` 等）
2. 加可执行权限：`chmod +x .githooks/<name>`
3. 更新本 README 表格
4. 提醒所有团队成员重新执行 `git config core.hooksPath .githooks`（首次即可，但脚本变化无需重新配）
