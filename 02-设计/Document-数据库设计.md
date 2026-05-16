# Document — 数据库设计

## DDL

```sql
DROP TABLE IF EXISTS tb_document;
CREATE TABLE tb_document (
    document_id           BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    document_no           VARCHAR(32)   NOT NULL                 COMMENT '编号 DOC-<TYPE>-YYYY-NNNN ADR-0007',
    project_id            BIGINT(20)    NOT NULL                 COMMENT 'FK→tb_project',
    related_entity_type   VARCHAR(20)   DEFAULT NULL             COMMENT '关联业务实体类型',
    related_entity_id     BIGINT(20)    DEFAULT NULL             COMMENT '关联业务实体 ID',
    doc_type              VARCHAR(20)   NOT NULL                 COMMENT 'biz_doc_type (prd/arch/...)',
    title                 VARCHAR(200)  NOT NULL                 COMMENT '文档标题',
    content               LONGTEXT                               COMMENT 'Markdown 全文',
    version               VARCHAR(20)   NOT NULL DEFAULT 'v1.0'  COMMENT '版本号',
    status                VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_doc_status',
    author_user_id        BIGINT(20)    NOT NULL                 COMMENT '作者',
    reviewer_user_id      BIGINT(20)    DEFAULT NULL             COMMENT '审核人',
    tags                  VARCHAR(200)  DEFAULT NULL             COMMENT 'CSV',
    create_by             VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    create_time           DATETIME      DEFAULT NULL,
    update_by             VARCHAR(64)   DEFAULT '',
    update_time           DATETIME      DEFAULT NULL,
    remark                VARCHAR(500)  DEFAULT '',
    del_flag              CHAR(1)       DEFAULT '0',
    PRIMARY KEY (document_id),
    UNIQUE KEY uk_document_no (document_no),
    KEY idx_document_project (project_id),
    KEY idx_document_type_status (doc_type, status),
    KEY idx_document_related (related_entity_type, related_entity_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='文档（Document）';
```

## 字典（3 + 19）

```sql
-- biz_doc_type (12)
prd / arch / db_design / api_design / proposal / ued / test_plan / test_report / api_doc / manual_product / manual_impl / manual_ops

-- biz_doc_status (4) 含反向边
00 草稿 / 01 待评审 / 02 已发布 / 03 已归档

-- biz_doc_category (3) 用于宽分类
spec(规范) / design(设计) / manual(手册)
```

## 菜单（菜单 ID 2070-2076）

```
2070 文档管理 /document   business:document:list
2071-2075 + 2076 导入       business:document:{query,add,edit,remove,export,import}
```

## 索引策略

- `idx_document_type_status` (doc_type, status) — "查所有已发布 PRD" / "查所有草稿"
- `idx_document_related` — "查 sprint=X 的所有文档"

## 修订记录

| 版本 | 日期 |
|---|---|
| v1.0 | 2026-05-16 |
