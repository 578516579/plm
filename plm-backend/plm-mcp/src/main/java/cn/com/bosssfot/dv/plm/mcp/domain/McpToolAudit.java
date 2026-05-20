package cn.com.bosssfot.dv.plm.mcp.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * MCP 工具调用审计 tb_mcp_tool_audit
 *
 * 落地 PRD §3.4 "调用审计"；不可逻辑删除，符合 SOX/审计合规。
 */
public class McpToolAudit extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** Server FK */
    private Long serverId;

    /** 工具名（project.list / requirement.create / ...） */
    @Excel(name = "工具")
    private String toolName;

    /** 调用方类型（user/agent/system） */
    @Excel(name = "调用方类型")
    private String callerType;

    /** 调用方 ID */
    @Excel(name = "调用方")
    private String callerId;

    /** 调用参数 JSON */
    private String paramsJson;

    /** 结果状态（字典 biz_audit_result：0=成功 1=失败 2=超时） */
    @Excel(name = "结果", dictType = "biz_audit_result")
    private String resultStatus;

    /** 结果摘要（截断到 2KB） */
    private String resultBrief;

    /** 耗时（ms） */
    @Excel(name = "耗时(ms)")
    private Integer latencyMs;

    /** 调用时间（注：审计表不带 update_time，复用 BaseEntity.createTime） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date callTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getCallerType() { return callerType; }
    public void setCallerType(String callerType) { this.callerType = callerType; }

    public String getCallerId() { return callerId; }
    public void setCallerId(String callerId) { this.callerId = callerId; }

    public String getParamsJson() { return paramsJson; }
    public void setParamsJson(String paramsJson) { this.paramsJson = paramsJson; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public String getResultBrief() { return resultBrief; }
    public void setResultBrief(String resultBrief) { this.resultBrief = resultBrief; }

    public Integer getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }

    public Date getCallTime() { return callTime; }
    public void setCallTime(Date callTime) { this.callTime = callTime; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("serverId", getServerId())
            .append("toolName", getToolName())
            .append("callerType", getCallerType())
            .append("callerId", getCallerId())
            .append("resultStatus", getResultStatus())
            .append("latencyMs", getLatencyMs())
            .append("callTime", getCallTime())
            .toString();
    }
}
