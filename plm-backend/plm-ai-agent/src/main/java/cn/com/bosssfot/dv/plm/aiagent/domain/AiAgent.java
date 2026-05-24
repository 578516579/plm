package cn.com.bosssfot.dv.plm.aiagent.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * AI Agent 对象 tb_ai_agent — PRD §F3.5 + 原型 aiagents.html
 * 6 类 Agent (需求/PRD/代码/测试/发布/运维) + Dify 工作流集成
 */
public class AiAgent extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long agentId;
    @Excel(name = "Agent编号") private String agentNo;
    @Excel(name = "Agent名称") private String agentName;
    @Excel(name = "Agent类型") private String agentType;
    @Excel(name = "描述")      private String description;
    private String promptTemplate;
    @Excel(name = "Dify工作流ID") private String difyWorkflowId;
    @Excel(name = "Provider")     private String provider;      // mock / dify / openai / anthropic
    @Excel(name = "模型名")       private String modelName;
    private String configJson;
    @Excel(name = "总调用")    private Long totalCalls;
    @Excel(name = "成功率")    private BigDecimal successRate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date lastInvokedAt;
    @Excel(name = "状态")      private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long v) { this.agentId = v; }
    public String getAgentNo() { return agentNo; }
    public void setAgentNo(String v) { this.agentNo = v; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String v) { this.agentName = v; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String v) { this.agentType = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getPromptTemplate() { return promptTemplate; }
    public void setPromptTemplate(String v) { this.promptTemplate = v; }
    public String getDifyWorkflowId() { return difyWorkflowId; }
    public void setDifyWorkflowId(String v) { this.difyWorkflowId = v; }
    public String getProvider() { return provider; }
    public void setProvider(String v) { this.provider = v; }
    public String getModelName() { return modelName; }
    public void setModelName(String v) { this.modelName = v; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String v) { this.configJson = v; }
    public Long getTotalCalls() { return totalCalls; }
    public void setTotalCalls(Long v) { this.totalCalls = v; }
    public BigDecimal getSuccessRate() { return successRate; }
    public void setSuccessRate(BigDecimal v) { this.successRate = v; }
    public Date getLastInvokedAt() { return lastInvokedAt; }
    public void setLastInvokedAt(Date v) { this.lastInvokedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("agentId", agentId).append("agentNo", agentNo)
            .append("agentName", agentName).append("agentType", agentType)
            .append("status", status).toString();
    }
}
