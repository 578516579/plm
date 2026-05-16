package cn.com.bosssfot.dv.plm.testreport.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 测试报告对象 tb_testreport — PRD §F4.7 + 原型 testreport.html
 * AI 自动生成测试报告 + 上线风险评级 (绿/黄/红)
 * 3 状态机: 00 草稿 → 01 审核中 → 02 已发布
 */
public class TestReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long testreportId;
    @Excel(name = "报告编号") private String testreportNo;
    @Excel(name = "项目ID") private Long projectId;
    private Long sprintId;
    private Long testplanId;
    @Excel(name = "报告标题") private String title;
    @Excel(name = "总用例数") private Integer totalCases;
    @Excel(name = "通过用例") private Integer passedCases;
    @Excel(name = "失败用例") private Integer failedCases;
    @Excel(name = "覆盖率%") private BigDecimal coverageRate;
    private String defectSummary;
    private Integer p0Defects;
    private Integer p1Defects;
    private Integer p2Defects;
    @Excel(name = "上线风险") private String riskLevel;
    private String riskEvaluation;
    private String recommendations;
    private String aiGenerated;
    @Excel(name = "状态") private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date generatedAt;
    private Long reviewerUserId;
    private String delFlag;

    public Long getTestreportId() { return testreportId; }
    public void setTestreportId(Long v) { this.testreportId = v; }
    public String getTestreportNo() { return testreportNo; }
    public void setTestreportNo(String v) { this.testreportNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getSprintId() { return sprintId; }
    public void setSprintId(Long v) { this.sprintId = v; }
    public Long getTestplanId() { return testplanId; }
    public void setTestplanId(Long v) { this.testplanId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public Integer getTotalCases() { return totalCases; }
    public void setTotalCases(Integer v) { this.totalCases = v; }
    public Integer getPassedCases() { return passedCases; }
    public void setPassedCases(Integer v) { this.passedCases = v; }
    public Integer getFailedCases() { return failedCases; }
    public void setFailedCases(Integer v) { this.failedCases = v; }
    public BigDecimal getCoverageRate() { return coverageRate; }
    public void setCoverageRate(BigDecimal v) { this.coverageRate = v; }
    public String getDefectSummary() { return defectSummary; }
    public void setDefectSummary(String v) { this.defectSummary = v; }
    public Integer getP0Defects() { return p0Defects; }
    public void setP0Defects(Integer v) { this.p0Defects = v; }
    public Integer getP1Defects() { return p1Defects; }
    public void setP1Defects(Integer v) { this.p1Defects = v; }
    public Integer getP2Defects() { return p2Defects; }
    public void setP2Defects(Integer v) { this.p2Defects = v; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String v) { this.riskLevel = v; }
    public String getRiskEvaluation() { return riskEvaluation; }
    public void setRiskEvaluation(String v) { this.riskEvaluation = v; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String v) { this.recommendations = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Date getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Date v) { this.generatedAt = v; }
    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long v) { this.reviewerUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("testreportId", testreportId)
            .append("testreportNo", testreportNo)
            .append("title", title)
            .append("riskLevel", riskLevel)
            .append("status", status)
            .toString();
    }
}
