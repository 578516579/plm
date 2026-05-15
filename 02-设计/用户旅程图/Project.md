# Project 用户旅程图

关联 [Project-PRD §2.2 典型场景](../../01-立项/Project-PRD.md)。

## S1: 立项 — 从想法到落库

```mermaid
journey
    title PM 新立一个项目
    section 准备
      想清楚要立啥项目: 5: PM
      跟领导口头同步: 4: PM
    section 进入系统
      登录 PLM: 5: PM
      点击 业务管理→项目管理: 5: PM
      点击「新增」: 5: PM
    section 填写表单
      填项目编号 PRJ-2026-001: 3: PM
      填项目名称: 5: PM
      选类型 rnd: 5: PM
      选负责人 (自己): 5: PM
      填起止日期 / 预算: 4: PM
      点「确定」保存: 5: PM
    section 验收
      列表中看到新项目: 5: PM
      状态 = 未启动: 5: PM
```

**痛点 → 设计响应**

| 痛点 | 响应 |
|---|---|
| 项目编号怕重号 | DB UNIQUE 索引 + 返 602 错误码（[ADR-0001](../../03-开发/ADR/0001-project-no-rule.md)） |
| 不知道选什么类型 | 字典下拉 (`biz_project_type`)，新值由 admin 维护 |
| 怕填错日期 | 起止日期可选；后端校验 start ≤ end |

---

## S2: 推进 — 项目启动到完成

```mermaid
journey
    title PM 推进项目状态
    section 启动
      列表找到项目: 5: PM
      详情查看: 5: PM
      点「编辑」: 5: PM
      改 status 为 进行中: 4: PM
      系统校验 0→1 合法: 5: System
      保存成功: 5: PM
    section 暂停 (中途)
      暂停: 改 status 为 暂停 (1→2): 3: PM
      理由记 remark: 4: PM
    section 恢复
      继续: 改 status 为 进行中 (2→1): 5: PM
    section 完成
      改 status 为 已完成 (1→3): 5: PM
      系统校验 1→3 合法: 5: System
```

**关键交互**：
- 状态机校验在**后端 Service 层强校验**（PRD Q3 决议）；前端可同时校验做 UX 优化
- 非法转换（如 3→1 "已完成回滚到进行中"）返 701 + 中文消息

---

## S3: 看板 — 总监筛选

```mermaid
journey
    title 总监看本月研发项目
    section 进入
      登录: 5: 总监
      项目管理列表: 5: 总监
    section 筛选
      搜索条 选类型=研发: 5: 总监
      选状态=进行中: 5: 总监
      日期范围 = 本月: 4: 总监
      点搜索: 5: 总监
    section 结果
      列表展示符合条件项目: 5: 总监
      点击进入详情查看: 5: 总监
```

**关键交互**：
- 搜索条参数对应 ProjectQuery 字段（projectType / status / params.begin/endStartDate）
- 默认每页 10 条；可调整 pageSize 至 20/50/100

---

## S4: 导出 — 周报准备

```mermaid
journey
    title 运营导出本周项目状态
    section 筛选
      列表→选时间范围: 5: 运营
      或选状态: 5: 运营
    section 导出
      点「导出」: 5: 运营
      浏览器下载 项目数据_xxxx.xlsx: 5: 运营
    section 用
      贴到飞书周报: 5: 运营
      或上传文档库: 4: 运营
```

**关键交互**：
- 导出参数 = 当前搜索条件（不只是当前页）
- 文件名带时间戳，避免覆盖

---

## 旅程图与测试用例的对应

Phase 04 测试用例库应至少覆盖：

- TC-S1-001: 立项完整流程（含字段校验、唯一性、合法状态值）
- TC-S2-001: 状态合法转换（0→1, 1→2, 2→1, 1→3）
- TC-S2-002: 状态非法转换（3→1, 4→任意）
- TC-S3-001: 多条件组合搜索 + 分页
- TC-S4-001: 导出文件内容验证（行数 = 筛选后总数）
