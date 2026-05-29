package cn.com.bosssfot.dv.plm.pipeline.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * CI/CD 流水线 tb_pipeline — DevOps 扩展 + 原型 pipeline.html
 * 代码仓库 / 触发方式 / YAML 定义 / 执行统计
 */
public class Pipeline extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long pipelineId;
    @Excel(name = "流水线编号") private String pipelineNo;
    @Excel(name = "项目ID")     private Long projectId;
    /** 关联发布单 ID（可空）— Proposal 0028 P0-1 产品主线 FK,跨模块同 projectId 强约束 */
    @Excel(name = "发布单ID")   private Long releaseId;
    @Excel(name = "流水线名称") private String pipelineName;
    @Excel(name = "代码仓库")   private String repoName;
    @Excel(name = "分支")       private String repoBranch;
    @Excel(name = "CICD工具")   private String cicdTool;
    @Excel(name = "触发方式")   private String triggerType;
    private String cronExpr;
    private String yamlContent;
    @Excel(name = "最近结果")   private String lastRunStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date lastRunAt;
    @Excel(name = "总执行")     private Integer totalRuns;
    @Excel(name = "成功次数")   private Integer successCount;
    @Excel(name = "成功率")     private BigDecimal successRate;
    @Excel(name = "状态")       private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getPipelineId() { return pipelineId; }
    public void setPipelineId(Long v) { this.pipelineId = v; }
    public String getPipelineNo() { return pipelineNo; }
    public void setPipelineNo(String v) { this.pipelineNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getReleaseId() { return releaseId; }
    public void setReleaseId(Long v) { this.releaseId = v; }
    public String getPipelineName() { return pipelineName; }
    public void setPipelineName(String v) { this.pipelineName = v; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String v) { this.repoName = v; }
    public String getRepoBranch() { return repoBranch; }
    public void setRepoBranch(String v) { this.repoBranch = v; }
    public String getCicdTool() { return cicdTool; }
    public void setCicdTool(String v) { this.cicdTool = v; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String v) { this.triggerType = v; }
    public String getCronExpr() { return cronExpr; }
    public void setCronExpr(String v) { this.cronExpr = v; }
    public String getYamlContent() { return yamlContent; }
    public void setYamlContent(String v) { this.yamlContent = v; }
    public String getLastRunStatus() { return lastRunStatus; }
    public void setLastRunStatus(String v) { this.lastRunStatus = v; }
    public Date getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(Date v) { this.lastRunAt = v; }
    public Integer getTotalRuns() { return totalRuns; }
    public void setTotalRuns(Integer v) { this.totalRuns = v; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer v) { this.successCount = v; }
    public BigDecimal getSuccessRate() { return successRate; }
    public void setSuccessRate(BigDecimal v) { this.successRate = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("pipelineId", pipelineId).append("pipelineNo", pipelineNo)
            .append("pipelineName", pipelineName).append("repoName", repoName)
            .append("status", status).toString();
    }
}
