package cn.com.bosssfot.dv.plm.analytics.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 效能分析快照 tb_analytics_snapshot — PRD §F6 + 原型 analytics.html + devops.html
 * 周期性快照: PLM 吞吐/质量 + DORA 4 指标 + AI 工时节省
 */
public class AnalyticsSnapshot extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long snapshotId;
    @Excel(name = "快照编号")   private String snapshotNo;
    @Excel(name = "项目ID")     private Long projectId;
    @Excel(name = "快照标题")   private String title;
    @Excel(name = "周期")       private String periodType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "快照日期", dateFormat = "yyyy-MM-dd") private Date snapshotDate;

    @Excel(name = "需求吞吐量") private Integer requirementThroughput;
    @Excel(name = "迭代准时率") private BigDecimal sprintOnTimeRate;
    @Excel(name = "缺陷密度")   private BigDecimal defectDensity;
    @Excel(name = "自动化覆盖率") private BigDecimal autoTestCoverage;

    @Excel(name = "部署频率") private BigDecimal deploymentFrequency;
    @Excel(name = "前置时间(h)") private BigDecimal leadTimeHours;
    @Excel(name = "MTTR(h)")  private BigDecimal mttrHours;
    @Excel(name = "变更失败率") private BigDecimal changeFailureRate;

    @Excel(name = "AI节省工时") private BigDecimal aiHoursSaved;
    @Excel(name = "在办项目")   private Integer activeProjects;
    @Excel(name = "风险项目")   private Integer projectsAtRisk;

    private String aiRecommendations;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态")    private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long v) { this.snapshotId = v; }
    public String getSnapshotNo() { return snapshotNo; }
    public void setSnapshotNo(String v) { this.snapshotNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String v) { this.periodType = v; }
    public Date getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(Date v) { this.snapshotDate = v; }
    public Integer getRequirementThroughput() { return requirementThroughput; }
    public void setRequirementThroughput(Integer v) { this.requirementThroughput = v; }
    public BigDecimal getSprintOnTimeRate() { return sprintOnTimeRate; }
    public void setSprintOnTimeRate(BigDecimal v) { this.sprintOnTimeRate = v; }
    public BigDecimal getDefectDensity() { return defectDensity; }
    public void setDefectDensity(BigDecimal v) { this.defectDensity = v; }
    public BigDecimal getAutoTestCoverage() { return autoTestCoverage; }
    public void setAutoTestCoverage(BigDecimal v) { this.autoTestCoverage = v; }
    public BigDecimal getDeploymentFrequency() { return deploymentFrequency; }
    public void setDeploymentFrequency(BigDecimal v) { this.deploymentFrequency = v; }
    public BigDecimal getLeadTimeHours() { return leadTimeHours; }
    public void setLeadTimeHours(BigDecimal v) { this.leadTimeHours = v; }
    public BigDecimal getMttrHours() { return mttrHours; }
    public void setMttrHours(BigDecimal v) { this.mttrHours = v; }
    public BigDecimal getChangeFailureRate() { return changeFailureRate; }
    public void setChangeFailureRate(BigDecimal v) { this.changeFailureRate = v; }
    public BigDecimal getAiHoursSaved() { return aiHoursSaved; }
    public void setAiHoursSaved(BigDecimal v) { this.aiHoursSaved = v; }
    public Integer getActiveProjects() { return activeProjects; }
    public void setActiveProjects(Integer v) { this.activeProjects = v; }
    public Integer getProjectsAtRisk() { return projectsAtRisk; }
    public void setProjectsAtRisk(Integer v) { this.projectsAtRisk = v; }
    public String getAiRecommendations() { return aiRecommendations; }
    public void setAiRecommendations(String v) { this.aiRecommendations = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date v) { this.aiGeneratedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("snapshotId", snapshotId)
            .append("snapshotNo", snapshotNo)
            .append("title", title)
            .append("periodType", periodType)
            .append("status", status)
            .toString();
    }
}
