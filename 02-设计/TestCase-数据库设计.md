# TestCase 模块 — 数据库设计

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 关联 PRD | [TestCase-PRD.md](../01-立项/TestCase-PRD.md) |
| 关联 ADR | ADR-0006 (TC-YYYY-NNNN) |
| 菜单 ID 段 | 2060-2067 |

## 1. DDL

```sql
DROP TABLE IF EXISTS tb_testcase;
CREATE TABLE tb_testcase (
    testcase_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    testcase_no              VARCHAR(32)   NOT NULL                 COMMENT '编号 TC-YYYY-NNNN ADR-0006',
    project_id               BIGINT(20)    NOT NULL                 COMMENT '所属项目',
    requirement_id           BIGINT(20)    DEFAULT NULL             COMMENT '关联需求（可空）',
    title                    VARCHAR(200)  NOT NULL                 COMMENT '用例标题',
    description              TEXT                                   COMMENT '概述',
    category                 VARCHAR(2)    NOT NULL DEFAULT '01'    COMMENT 'biz_testcase_category',
    priority                 VARCHAR(2)    NOT NULL DEFAULT '01'    COMMENT 'biz_testcase_priority',
    status                   VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_testcase_status',
    preconditions            TEXT                                   COMMENT '前置条件',
    steps                    TEXT          NOT NULL                 COMMENT '测试步骤',
    expected_result          TEXT          NOT NULL                 COMMENT '期望结果',
    actual_result            TEXT                                   COMMENT '实际结果（最近一次）',
    is_automated             CHAR(1)       NOT NULL DEFAULT 'N'     COMMENT 'Y/N',
    automation_script_path   VARCHAR(500)  DEFAULT NULL             COMMENT '自动化脚本路径',
    execution_count          INT           NOT NULL DEFAULT 0       COMMENT '累计执行次数',
    last_executed_at         DATETIME      DEFAULT NULL             COMMENT '最近一次执行时间',
    tags                     VARCHAR(200)  DEFAULT NULL             COMMENT 'CSV 标签',
    create_by                VARCHAR(64)   DEFAULT ''               COMMENT '',
    create_time              DATETIME      DEFAULT NULL,
    update_by                VARCHAR(64)   DEFAULT '',
    update_time              DATETIME      DEFAULT NULL,
    remark                   VARCHAR(500)  DEFAULT '',
    del_flag                 CHAR(1)       DEFAULT '0',
    PRIMARY KEY (testcase_id),
    UNIQUE KEY uk_testcase_no (testcase_no),
    KEY idx_testcase_project (project_id),
    KEY idx_testcase_req (requirement_id),
    KEY idx_testcase_status_priority (status, priority),
    KEY idx_testcase_automated (is_automated)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='测试用例（TestCase）';
```

## 2. 字典（3 + 15 条）

```sql
INSERT INTO sys_dict_type VALUES ... 'biz_testcase_category', 'biz_testcase_priority', 'biz_testcase_status';

-- category (7)
INSERT INTO sys_dict_data ... ('功能','01'), ('接口','02'), ('性能','03'), ('安全','04'), ('兼容性','05'), ('E2E','06'), ('烟雾','07');

-- priority (3)
INSERT INTO sys_dict_data ... ('P0 关键','00'), ('P1 主要','01' default Y), ('P2 次要','02');

-- status (5)
INSERT INTO sys_dict_data ... ('草稿','00' default Y), ('待执行','01'), ('执行中','02'), ('已通过','03'), ('已失败','04');
```

## 3. 菜单（8 个,2060-2067）

```
2060 测试用例管理  /testcase  business:testcase:list
2061 用例查询      business:testcase:query
2062 用例新增      business:testcase:add
2063 用例修改      business:testcase:edit
2064 用例删除      business:testcase:remove
2065 用例导出      business:testcase:export
2066 用例执行      business:testcase:execute
2067 用例指派      business:testcase:assign (v0.4 备用)
```

## 4. 索引策略

- `idx_testcase_status_priority` (status, priority) — 看板按状态分列 + P0 紧急筛选
- `idx_testcase_automated` (is_automated) — "全自动化用例"快查

## 5. 变更记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建 |
