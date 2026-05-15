# Project 模块 — E2E 自动化测试

工具：[Playwright](https://playwright.dev/)（headless Chromium）。
脚本位置：[plm-frontend/e2e/project.spec.ts](../../plm-frontend/e2e/project.spec.ts)

## 运行

```bash
# 一次性安装（已在仓库 commit 时完成）
cd plm-frontend
npm install --save-dev @playwright/test
npx playwright install chromium

# 跑测试
cd plm-frontend
npx playwright test                  # 全部 headless 跑
npx playwright test --headed         # 看着浏览器跑
npx playwright test -g "PRJ-2026"    # 跑某条
npx playwright test --debug          # 单步调试
```

**前置依赖**：
- 后端 8081 running（PID 5352 当前）
- 前端 80 running（Vite dev server）
- MySQL plm 库已含 tb_project 数据（至少 PRJ-2026-0001）
- Redis 6379 running，用于 captcha bypass
- `redis-cli` 在 PATH 或 `D:\Program Files\Redis\redis-cli.exe`

## 设计要点

### 1. Programmatic login 绕过 captcha

若依登录有图形验证码（PRD 接受用户体验，无法 OCR）。Playwright 测试用以下方式绕开：

```
GET /captchaImage → 拿 uuid
redis-cli GET captcha_codes:<uuid> → 拿 code
POST /login (admin/admin123/code/uuid) → 拿 JWT
Cookie Admin-Token = JWT
```

每个 `beforeEach` 都跑一次，确保 token 不过期。

### 2. 测试间隔离

`fullyParallel: false` + `workers: 1` — 因为 captcha 每秒只能拿一个新 uuid（避免 redis key 竞争），且业务表共享。

### 3. 失败追踪

`trace: 'retain-on-failure'` + `screenshot: 'only-on-failure'` — 失败时自动保存
`test-results/<test-name>/trace.zip`，可用 `npx playwright show-trace ...` 回放。

## 用例

| 用例 ID | 标题 | 验证点 | 状态 |
|---|---|---|---|
| TC-Proj-E2E-001 | 首页能加载（登录后跳 /index）| URL + Title 含 "PLM" | ✅ Pass (4.5s) |
| TC-Proj-E2E-002 | 项目管理路由能直接访问 | el-table 可见 | ✅ Pass (4.4s) |
| TC-Proj-E2E-003 | 列表中能看到 PRJ-2026-0001 | 表格文本含 "PRJ-2026-" | ✅ Pass (5.7s) |
| TC-Proj-E2E-004 | 点新增按钮，弹出对话框 | el-dialog 可见 + 含项目名称字段 + 取消能关闭 | ✅ Pass (7.0s) |
| TC-Proj-E2E-005 | 搜索条件能输入 | placeholder 含项目名称的 input 能填值 + 搜索按钮可点 | ✅ Pass (8.0s) |

**5/5 通过 / 37s 总耗时**。

## 与单元测试 / 接口测试的关系

| 层 | 覆盖什么 | 工具 |
|---|---|---|
| 单元测试 | Service 业务逻辑（generateProjectNo / 状态机 / 字段校验） | JUnit + Mockito，16 测试，[`ProjectServiceImplTest.java`](../../plm-backend/plm-system/src/test/java/cn/com/bosssfot/dv/plm/system/business/service/impl/ProjectServiceImplTest.java) |
| 轻集成测试 | Service ↔ Mapper 协作 | Mockito，1 测试，[`ProjectServiceImplLightIntegrationTest.java`](../../plm-backend/plm-system/src/test/java/cn/com/bosssfot/dv/plm/system/business/service/impl/ProjectServiceImplLightIntegrationTest.java) |
| 接口用例（手测）| HTTP API 契约 | curl，5 用例，[Project-api.md](Project-api.md) |
| **E2E 用例** | 浏览器视角完整链路（HTML + JS + cookie + API + DB） | **Playwright，5 用例，本文件** |

总计 **27 个自动化测试**（17 后端单测 + 5 接口手测 + 5 浏览器 E2E）。

## 未来扩展

按 Phase 04 §H 异常段标出的 4 处豁免：

- [ ] 普通用户角色权限拦截（待 v0.2 引入"项目经理"角色）
- [ ] 多数据回归（fixture SQL + 测试间隔离）
- [ ] 完整 CRUD 链路（含真实创建 + 删除，含数据库 cleanup）
- [ ] 视觉回归（screenshot diff）

这些在 v0.2 引入 staging 环境后补。

## CI 集成（v0.2 引入 CI 时）

```yaml
# .github/workflows/e2e.yml (示意)
- run: cd plm-frontend && npm ci
- run: cd plm-frontend && npx playwright install --with-deps chromium
- run: # 启动后端 + 前端的脚本
- run: cd plm-frontend && npx playwright test
- uses: actions/upload-artifact@v3
  if: failure()
  with:
    name: playwright-trace
    path: plm-frontend/test-results/
```
