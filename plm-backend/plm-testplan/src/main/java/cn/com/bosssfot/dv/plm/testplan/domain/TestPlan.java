package cn.com.bosssfot.dv.plm.testplan.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 测试方案对象 tb_testplan — PRD §F4.1 + 原型 testplan.html
 * AI 生成测试策略、范围、资源分配计划
 * 4 状态机: 00 草稿 → 01 已确认 → 02 执行中 → 03 已完成
 */
public class TestPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long testplanId;
    @Excel(name = "方案编号") private String testplanNo;
    @Excel(name = "项目ID") private Long projectId;
    private Long sprintId;
    @Excel(name = "方案标题") private String title;
    @Excel(name = "测试类型") private String testTypes;
    @Excel(name = "测试周期(天)") private Integer testCycleDays;
    private String scope;
    private String strategy;
    private String toolsRecommended;
    private String resourcesPlan;
    private String riskAssessment;
    private String aiGenerated;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getTestplanId() { return testplanId; }
    public void setTestplanId(Long v) { this.testplanId = v; }
    public String getTestplanNo() { return testplanNo; }
    public void setTestplanNo(String v) { this.testplanNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getSprintId() { return sprintId; }
    public void setSprintId(Long v) { this.sprintId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getTestTypes() { return testTypes; }
    public void setTestTypes(String v) { this.testTypes = v; }
    public Integer getTestCycleDays() { return testCycleDays; }
    public void setTestCycleDays(Integer v) { this.testCycleDays = v; }
    public String getScope() { return scope; }
    public void setScope(String v) { this.scope = v; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String v) { this.strategy = v; }
    public String getToolsRecommended() { return toolsRecommended; }
    public void setToolsRecommended(String v) { this.toolsRecommended = v; }
    public String getResourcesPlan() { return resourcesPlan; }
    public void setResourcesPlan(String v) { this.resourcesPlan = v; }
    public String getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(String v) { this.riskAssessment = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("testplanId", testplanId)
            .append("testplanNo", testplanNo)
            .append("title", title)
            .append("testTypes", testTypes)
            .append("status", status)
            .toString();
    }
}
