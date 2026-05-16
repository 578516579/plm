package cn.com.bosssfot.dv.plm.release.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 发布管理对象 tb_release — 原型 release.html
 * 策略: 蓝绿 / 金丝雀 / 滚动 + DORA 4 指标 + AI 评审 + 一键回滚
 * 5 状态机: 00 计划中 → 01 发布中 → 02 已发布 → 03 已回滚 / 04 已废弃
 */
public class Release extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long releaseId;
    @Excel(name = "发布编号") private String releaseNo;
    @Excel(name = "版本号") private String version;
    @Excel(name = "项目ID") private Long projectId;
    private Long sprintId;
    @Excel(name = "策略") private String strategy;
    @Excel(name = "环境") private String environment;
    private String releaseNotes;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date plannedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date releasedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date rollbackAt;
    private String rollbackReason;
    @Excel(name = "状态") private String status;

    private BigDecimal aiReviewScore;
    private String aiReviewNotes;

    /** DORA 4 指标 */
    private BigDecimal deploymentFrequency;
    private BigDecimal leadTimeHours;
    private BigDecimal mttrMinutes;
    private BigDecimal changeFailureRate;

    private Long releasedByUserId;
    private String delFlag;

    public Long getReleaseId() { return releaseId; }
    public void setReleaseId(Long v) { this.releaseId = v; }
    public String getReleaseNo() { return releaseNo; }
    public void setReleaseNo(String v) { this.releaseNo = v; }
    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getSprintId() { return sprintId; }
    public void setSprintId(Long v) { this.sprintId = v; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String v) { this.strategy = v; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String v) { this.environment = v; }
    public String getReleaseNotes() { return releaseNotes; }
    public void setReleaseNotes(String v) { this.releaseNotes = v; }
    public Date getPlannedAt() { return plannedAt; }
    public void setPlannedAt(Date v) { this.plannedAt = v; }
    public Date getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Date v) { this.releasedAt = v; }
    public Date getRollbackAt() { return rollbackAt; }
    public void setRollbackAt(Date v) { this.rollbackAt = v; }
    public String getRollbackReason() { return rollbackReason; }
    public void setRollbackReason(String v) { this.rollbackReason = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public BigDecimal getAiReviewScore() { return aiReviewScore; }
    public void setAiReviewScore(BigDecimal v) { this.aiReviewScore = v; }
    public String getAiReviewNotes() { return aiReviewNotes; }
    public void setAiReviewNotes(String v) { this.aiReviewNotes = v; }
    public BigDecimal getDeploymentFrequency() { return deploymentFrequency; }
    public void setDeploymentFrequency(BigDecimal v) { this.deploymentFrequency = v; }
    public BigDecimal getLeadTimeHours() { return leadTimeHours; }
    public void setLeadTimeHours(BigDecimal v) { this.leadTimeHours = v; }
    public BigDecimal getMttrMinutes() { return mttrMinutes; }
    public void setMttrMinutes(BigDecimal v) { this.mttrMinutes = v; }
    public BigDecimal getChangeFailureRate() { return changeFailureRate; }
    public void setChangeFailureRate(BigDecimal v) { this.changeFailureRate = v; }
    public Long getReleasedByUserId() { return releasedByUserId; }
    public void setReleasedByUserId(Long v) { this.releasedByUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("releaseId", releaseId)
            .append("releaseNo", releaseNo)
            .append("version", version)
            .append("strategy", strategy)
            .append("status", status)
            .toString();
    }
}
