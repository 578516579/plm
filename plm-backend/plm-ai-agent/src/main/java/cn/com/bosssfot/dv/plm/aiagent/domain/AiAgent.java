package cn.com.bosssfot.dv.plm.aiagent.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * AI Agent 对象 tb_ai_agent
 */
public class AiAgent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** Agent 编号（AGT-YYYY-NNNN） */
    @Excel(name = "Agent编号")
    private String agentNo;

    /** Agent 名称 */
    @Excel(name = "Agent名称")
    private String agentName;

    /** 适用岗位（字典 biz_agent_role） */
    @Excel(name = "适用岗位", dictType = "biz_agent_role")
    private String agentRole;

    /** Agent 类型（字典 biz_agent_type） */
    @Excel(name = "Agent类型", dictType = "biz_agent_type")
    private String agentType;

    /** AI 模型名称（DeepSeek-V3 / Claude Sonnet 4.6 / DeepSeek-R1） */
    @Excel(name = "AI模型")
    private String modelName;

    /** Dify 工作流 ID */
    private String difyFlowId;

    /** 工具列表（JSON 数组字符串，如 ["agrikb_search","req_template"]） */
    private String toolsJson;

    /** 状态（字典 biz_agent_status：0=运行中 1=待机 2=异常） */
    @Excel(name = "状态", dictType = "biz_agent_status")
    private String status;

    /** 今日调用次数 */
    private Integer callsToday;

    /** 成功率（0.00~100.00） */
    private java.math.BigDecimal successRate;

    /** 平均响应时长（如 1.8s） */
    private String avgLatency;

    /** 描述 */
    private String description;

    /** 删除标志（0=正常, 2=删除） */
    private String delFlag;

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    public void setAgentNo(String agentNo) { this.agentNo = agentNo; }
    public String getAgentNo() { return agentNo; }

    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getAgentName() { return agentName; }

    public void setAgentRole(String agentRole) { this.agentRole = agentRole; }
    public String getAgentRole() { return agentRole; }

    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getAgentType() { return agentType; }

    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getModelName() { return modelName; }

    public void setDifyFlowId(String difyFlowId) { this.difyFlowId = difyFlowId; }
    public String getDifyFlowId() { return difyFlowId; }

    public void setToolsJson(String toolsJson) { this.toolsJson = toolsJson; }
    public String getToolsJson() { return toolsJson; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setCallsToday(Integer callsToday) { this.callsToday = callsToday; }
    public Integer getCallsToday() { return callsToday; }

    public void setSuccessRate(java.math.BigDecimal successRate) { this.successRate = successRate; }
    public java.math.BigDecimal getSuccessRate() { return successRate; }

    public void setAvgLatency(String avgLatency) { this.avgLatency = avgLatency; }
    public String getAvgLatency() { return avgLatency; }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("agentNo", getAgentNo())
            .append("agentName", getAgentName())
            .append("agentRole", getAgentRole())
            .append("agentType", getAgentType())
            .append("modelName", getModelName())
            .append("difyFlowId", getDifyFlowId())
            .append("toolsJson", getToolsJson())
            .append("status", getStatus())
            .append("callsToday", getCallsToday())
            .append("successRate", getSuccessRate())
            .append("avgLatency", getAvgLatency())
            .append("description", getDescription())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
