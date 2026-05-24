-- =============================================================================
-- AI Agent 编排 (AiAgent) — PRD §F3.5 + 原型 aiagents.html
-- 多 Agent 协作 (需求/PRD/代码/测试/发布/运维 6 类) + Dify 工作流集成
-- =============================================================================
DROP TABLE IF EXISTS tb_ai_agent;
CREATE TABLE tb_ai_agent (
    agent_id            BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    agent_no            VARCHAR(32)   NOT NULL                 COMMENT '编号 AGT-YYYY-NNNN',
    agent_name          VARCHAR(200)  NOT NULL                 COMMENT 'Agent 名称',
    agent_type          VARCHAR(30)   NOT NULL                 COMMENT '字典 biz_aiagent_type: requirement/prd/code/test/release/ops',
    description         VARCHAR(500)                           COMMENT 'Agent 描述',
    prompt_template     LONGTEXT                               COMMENT '提示词模板',
    dify_workflow_id    VARCHAR(64)                            COMMENT 'Dify 工作流 ID (provider=dify 时使用)',
    provider            VARCHAR(20)   NOT NULL DEFAULT 'mock'  COMMENT '字典 biz_ai_provider: mock/dify/openai/anthropic',
    model_name          VARCHAR(120)                           COMMENT '模型名 e.g. gpt-4o-mini / deepseek-chat / claude-sonnet-4-5',
    config_json         LONGTEXT                               COMMENT 'Agent 配置 JSON',
    total_calls         BIGINT(20)    DEFAULT 0                COMMENT '总调用次数',
    success_rate        DECIMAL(5,2)  DEFAULT 0                COMMENT '成功率 %',
    last_invoked_at     DATETIME      DEFAULT NULL             COMMENT '最近调用时间',
    status              VARCHAR(2)    NOT NULL DEFAULT '00'    COMMENT 'biz_aiagent_status: 00 运行中/01 已停止/02 错误',
    author_user_id      BIGINT(20)    NOT NULL                 COMMENT '创建者',
    create_by           VARCHAR(64)   DEFAULT '',
    create_time         DATETIME      DEFAULT NULL,
    update_by           VARCHAR(64)   DEFAULT '',
    update_time         DATETIME      DEFAULT NULL,
    remark              VARCHAR(500)  DEFAULT '',
    del_flag            CHAR(1)       DEFAULT '0',
    PRIMARY KEY (agent_id),
    UNIQUE KEY uk_aiagent_no (agent_no),
    KEY idx_aiagent_type (agent_type),
    KEY idx_aiagent_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent 编排（AiAgent）';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('AI Agent 状态',  'biz_aiagent_status', '0', 'admin', SYSDATE(), '3 状态'),
('AI Agent 类型',  'biz_aiagent_type',   '0', 'admin', SYSDATE(), '6 类 Agent'),
('AI Provider',    'biz_ai_provider',    '0', 'admin', SYSDATE(), '4 种 provider');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '运行中', '00', 'biz_aiagent_status', '', 'success', 'Y', '0', 'admin', SYSDATE(), ''),
(2, '已停止', '01', 'biz_aiagent_status', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(3, '错误',   '02', 'biz_aiagent_status', '', 'danger',  'N', '0', 'admin', SYSDATE(), '需人工介入'),

(1, '需求分析', 'requirement', 'biz_aiagent_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), ''),
(2, 'PRD 生成', 'prd',         'biz_aiagent_type', '', 'success', 'N', '0', 'admin', SYSDATE(), ''),
(3, '代码审查', 'code',        'biz_aiagent_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), ''),
(4, '测试生成', 'test',        'biz_aiagent_type', '', 'info',    'N', '0', 'admin', SYSDATE(), ''),
(5, '发布评审', 'release',     'biz_aiagent_type', '', 'danger',  'N', '0', 'admin', SYSDATE(), ''),
(6, '运维巡检', 'ops',         'biz_aiagent_type', '', 'primary', 'N', '0', 'admin', SYSDATE(), ''),

(1, 'Mock 占位',    'mock',      'biz_ai_provider', '', 'info',    'Y', '0', 'admin', SYSDATE(), '本地/降级'),
(2, 'Dify 编排',    'dify',      'biz_ai_provider', '', 'primary', 'N', '0', 'admin', SYSDATE(), 'workflow 编排'),
(3, 'OpenAI 兼容',  'openai',    'biz_ai_provider', '', 'success', 'N', '0', 'admin', SYSDATE(), 'OpenAI/DeepSeek/通义/Moonshot'),
(4, 'Anthropic',    'anthropic', 'biz_ai_provider', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'Claude Messages API');
