-- ============================================================
-- AI Agent 管理 — DDL + 字典 + 菜单 + 种子数据
-- 模块: plm-ai-agent  PRD-MAPPING §34
-- 执行前提: 数据库 plm 已存在，字符集 utf8mb4
-- 回滚: ai_agent_rollback.sql
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 表结构
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_ai_agent` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `agent_no`     VARCHAR(32)   NOT NULL                COMMENT 'Agent编号（AGT-YYYY-NNNN）',
  `agent_name`   VARCHAR(128)  NOT NULL                COMMENT 'Agent名称',
  `agent_role`   VARCHAR(32)   DEFAULT NULL            COMMENT '适用岗位（字典 biz_agent_role）',
  `agent_type`   VARCHAR(32)   DEFAULT NULL            COMMENT 'Agent类型（字典 biz_agent_type）',
  `model_name`   VARCHAR(128)  DEFAULT NULL            COMMENT 'AI模型（DeepSeek-V3 / Claude Sonnet 4.6 / DeepSeek-R1）',
  `dify_flow_id` VARCHAR(128)  DEFAULT NULL            COMMENT 'Dify工作流ID',
  `tools_json`   TEXT          DEFAULT NULL            COMMENT '工具列表（JSON数组字符串）',
  `status`       CHAR(1)       NOT NULL DEFAULT '1'    COMMENT '状态（0=运行中 1=待机 2=异常）',
  `calls_today`  INT           NOT NULL DEFAULT 0      COMMENT '今日调用次数',
  `success_rate` DECIMAL(5,2)  DEFAULT 100.00          COMMENT '成功率',
  `avg_latency`  VARCHAR(32)   DEFAULT NULL            COMMENT '平均响应时长（如 1.8s）',
  `description`  VARCHAR(500)  DEFAULT NULL            COMMENT '描述',
  `create_by`    VARCHAR(64)   DEFAULT ''              COMMENT '创建人',
  `create_time`  DATETIME      DEFAULT NULL            COMMENT '创建时间',
  `update_by`    VARCHAR(64)   DEFAULT ''              COMMENT '更新人',
  `update_time`  DATETIME      DEFAULT NULL            COMMENT '更新时间',
  `remark`       VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  `del_flag`     CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '删除标志（0=正常 2=删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_no` (`agent_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI Agent 配置表';

-- ------------------------------------------------------------
-- 2. 字典类型
-- ------------------------------------------------------------
INSERT IGNORE INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES
  ('AI Agent 岗位',   'biz_agent_role',   '0', 'admin', sysdate(), 'AI Agent 适用岗位'),
  ('AI Agent 类型',   'biz_agent_type',   '0', 'admin', sysdate(), 'AI Agent 功能类型'),
  ('AI Agent 状态',   'biz_agent_status', '0', 'admin', sysdate(), 'AI Agent 运行状态');

-- ------------------------------------------------------------
-- 3. 字典数据
-- ------------------------------------------------------------

-- biz_agent_role（7 个产研岗位）
INSERT IGNORE INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
  (1,  '产品经理',   'product_manager', 'biz_agent_role', '', 'primary',   'N', '0', 'admin', sysdate(), NULL),
  (2,  'UED设计师',  'ued',             'biz_agent_role', '', 'warning',   'N', '0', 'admin', sysdate(), NULL),
  (3,  '研发工程师', 'dev',             'biz_agent_role', '', 'default',   'N', '0', 'admin', sysdate(), NULL),
  (4,  '测试工程师', 'test',            'biz_agent_role', '', 'info',      'N', '0', 'admin', sysdate(), NULL),
  (5,  '项目经理',   'pm',              'biz_agent_role', '', 'primary',   'N', '0', 'admin', sysdate(), NULL),
  (6,  '实施工程师', 'impl',            'biz_agent_role', '', 'default',   'N', '0', 'admin', sysdate(), NULL),
  (7,  '部门负责人', 'dept_head',       'biz_agent_role', '', 'danger',    'N', '0', 'admin', sysdate(), NULL);

-- biz_agent_type（6 种 Agent 类型，对应研发全链路）
INSERT IGNORE INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
  (1, '需求分析', 'requirement',  'biz_agent_type', '', 'primary', 'N', '0', 'admin', sysdate(), NULL),
  (2, 'PRD生成',  'prd_gen',      'biz_agent_type', '', 'primary', 'N', '0', 'admin', sysdate(), NULL),
  (3, '代码审查', 'code_review',  'biz_agent_type', '', 'default', 'N', '0', 'admin', sysdate(), NULL),
  (4, '测试用例', 'test_case',    'biz_agent_type', '', 'info',    'N', '0', 'admin', sysdate(), NULL),
  (5, '发布评审', 'release',      'biz_agent_type', '', 'warning', 'N', '0', 'admin', sysdate(), NULL),
  (6, '运维巡检', 'ops',          'biz_agent_type', '', 'danger',  'N', '0', 'admin', sysdate(), NULL);

-- biz_agent_status
INSERT IGNORE INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
  (1, '运行中', '0', 'biz_agent_status', '', 'success', 'N', '0', 'admin', sysdate(), NULL),
  (2, '待机',   '1', 'biz_agent_status', '', 'info',    'Y', '0', 'admin', sysdate(), NULL),
  (3, '异常',   '2', 'biz_agent_status', '', 'danger',  'N', '0', 'admin', sysdate(), NULL);

-- ------------------------------------------------------------
-- 4. 菜单（ID 段 2600-2615，AI 能力目录）
-- ------------------------------------------------------------
-- 一级目录
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2600, 'AI 能力', 0, 60, 'ai', NULL, 1, 0, 'M', '0', '0', '', 'robot', 'admin', sysdate(), 'AI 能力一级目录');

-- AI Agent 管理二级菜单
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2610, 'AI Agent 编排', 2600, 1, 'ai-agent', 'business/ai-agent/index', 1, 0, 'C', '0', '0', 'business:ai-agent:list', 'robot', 'admin', sysdate(), 'AI Agent 编排管理');

-- 按钮权限
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
  (2611, '查询', 2610, 1, '', '', 1, 0, 'F', '0', '0', 'business:ai-agent:query',  '#', 'admin', sysdate(), NULL),
  (2612, '新增', 2610, 2, '', '', 1, 0, 'F', '0', '0', 'business:ai-agent:add',    '#', 'admin', sysdate(), NULL),
  (2613, '修改', 2610, 3, '', '', 1, 0, 'F', '0', '0', 'business:ai-agent:edit',   '#', 'admin', sysdate(), NULL),
  (2614, '删除', 2610, 4, '', '', 1, 0, 'F', '0', '0', 'business:ai-agent:remove', '#', 'admin', sysdate(), NULL),
  (2615, '导出', 2610, 5, '', '', 1, 0, 'F', '0', '0', 'business:ai-agent:export', '#', 'admin', sysdate(), NULL);

-- ------------------------------------------------------------
-- 5. 种子数据（5 个 Agent，与原型 agriplm.js state.aiAgents 对齐）
-- ------------------------------------------------------------
INSERT IGNORE INTO tb_ai_agent (agent_no, agent_name, agent_role, agent_type, model_name, dify_flow_id, tools_json, status, calls_today, success_rate, avg_latency, description, create_by, create_time, del_flag)
VALUES
  ('AGT-2026-0001', '需求分析Agent',
   'product_manager', 'requirement', 'DeepSeek-V3',
   'requirements-flow',
   '["agrikb_search","req_template","prd_generator"]',
   '0', 47, 96.00, '1.8s',
   '分析需求文档，引用农业标准，生成结构化PRD',
   'admin', sysdate(), '0'),

  ('AGT-2026-0002', '代码审查Agent',
   'dev', 'code_review', 'Claude Sonnet 4.6',
   'coding-assist-flow',
   '["gitlab_mr","sonarqube","security_scan"]',
   '0', 23, 91.00, '3.2s',
   '审查MR代码质量、安全漏洞、农业业务逻辑',
   'admin', sysdate(), '0'),

  ('AGT-2026-0003', '测试用例Agent',
   'test', 'test_case', 'DeepSeek-V3',
   'testcase-gen-flow',
   '["req_reader","agrikb_search","testcase_writer"]',
   '0', 38, 94.00, '2.1s',
   '基于需求和AgriKB生成覆盖农业场景的测试用例',
   'admin', sysdate(), '0'),

  ('AGT-2026-0004', '发布评审Agent',
   'pm', 'release', 'DeepSeek-R1',
   'test-report-flow',
   '["dora_metrics","risk_evaluator","rollback_advisor"]',
   '0', 8, 100.00, '4.5s',
   '发布前AI评审：DORA指标/变更风险/回滚方案',
   'admin', sysdate(), '0'),

  ('AGT-2026-0005', '运维巡检Agent',
   'impl', 'ops', 'DeepSeek-V3',
   'ops-manual-flow',
   '["prometheus","alert_manager","iot_monitor"]',
   '1', 0, 98.00, '1.2s',
   '定时巡检IoT设备状态、API健康度、告警处置',
   'admin', sysdate(), '0');
