package cn.com.bosssfot.dv.plm.submission.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 提测管理对象 tb_submission — PRD §F4.4 + 原型 submit.html
 * AI 质量门禁: 单测覆盖率 ≥60% + 代码扫描通过 + PRD 完整 + API 文档更新
 * 5×5 状态机 (含反向边 04→00): 00 草稿 → 01 已提交 → 02 质量门禁中 → 03 已通过 / 04 已退回
 */
public class Submission extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long submissionId;
    @Excel(name = "提测编号") private String submissionNo;
    @Excel(name = "项目ID") private Long projectId;
    private Long sprintId;
    @Excel(name = "提测标题") private String title;
    private String scope;
    @Excel(name = "环境", dictType = "biz_submission_environment") private String environment;
    @Excel(name = "期望测试周期(天)") private Integer expectedTestDays;
    private String riskNotes;

    /** AI 质量门禁 4 项 */
    @Excel(name = "单测覆盖率%") private BigDecimal unitTestCoverage;
    private String codeScanPassed;
    private String prdCompleted;
    private String apiDocUpdated;
    @Excel(name = "门禁通过") private String qualityGatePassed;

    @Excel(name = "状态") private String status;
    private String rejectReason;
    private Long submitterUserId;
    private Long reviewerUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date submittedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date approvedAt;
    private String delFlag;

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long v) { this.submissionId = v; }
    public String getSubmissionNo() { return submissionNo; }
    public void setSubmissionNo(String v) { this.submissionNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getSprintId() { return sprintId; }
    public void setSprintId(Long v) { this.sprintId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getScope() { return scope; }
    public void setScope(String v) { this.scope = v; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String v) { this.environment = v; }
    public Integer getExpectedTestDays() { return expectedTestDays; }
    public void setExpectedTestDays(Integer v) { this.expectedTestDays = v; }
    public String getRiskNotes() { return riskNotes; }
    public void setRiskNotes(String v) { this.riskNotes = v; }
    public BigDecimal getUnitTestCoverage() { return unitTestCoverage; }
    public void setUnitTestCoverage(BigDecimal v) { this.unitTestCoverage = v; }
    public String getCodeScanPassed() { return codeScanPassed; }
    public void setCodeScanPassed(String v) { this.codeScanPassed = v; }
    public String getPrdCompleted() { return prdCompleted; }
    public void setPrdCompleted(String v) { this.prdCompleted = v; }
    public String getApiDocUpdated() { return apiDocUpdated; }
    public void setApiDocUpdated(String v) { this.apiDocUpdated = v; }
    public String getQualityGatePassed() { return qualityGatePassed; }
    public void setQualityGatePassed(String v) { this.qualityGatePassed = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String v) { this.rejectReason = v; }
    public Long getSubmitterUserId() { return submitterUserId; }
    public void setSubmitterUserId(Long v) { this.submitterUserId = v; }
    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long v) { this.reviewerUserId = v; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date v) { this.submittedAt = v; }
    public Date getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Date v) { this.approvedAt = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("submissionId", submissionId)
            .append("submissionNo", submissionNo)
            .append("projectId", projectId)
            .append("title", title)
            .append("qualityGatePassed", qualityGatePassed)
            .append("status", status)
            .toString();
    }
}
