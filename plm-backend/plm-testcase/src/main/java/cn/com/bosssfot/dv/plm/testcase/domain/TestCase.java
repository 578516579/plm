package cn.com.bosssfot.dv.plm.testcase.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 测试用例对象 tb_testcase
 * ADR-0006: testcase_no = TC-YYYY-NNNN
 * 5×5 状态机含反向边 03/04 → 01 (重测)
 */
public class TestCase extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long testcaseId;

    @Excel(name = "用例编号")
    private String testcaseNo;

    @Excel(name = "项目ID")
    private Long projectId;

    @Excel(name = "需求ID")
    private Long requirementId;

    @Excel(name = "标题")
    private String title;

    private String description;

    @Excel(name = "分类", dictType = "biz_testcase_category")
    private String category;

    @Excel(name = "优先级", dictType = "biz_testcase_priority")
    private String priority;

    @Excel(name = "状态", dictType = "biz_testcase_status")
    private String status;

    private String preconditions;
    private String steps;
    private String expectedResult;
    private String actualResult;

    @Excel(name = "自动化")
    private String isAutomated;

    @Excel(name = "脚本路径")
    private String automationScriptPath;

    @Excel(name = "执行次数")
    private Integer executionCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近执行", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastExecutedAt;

    @Excel(name = "标签")
    private String tags;

    private String delFlag;

    public void setTestcaseId(Long v) { this.testcaseId = v; }
    public Long getTestcaseId() { return testcaseId; }
    public void setTestcaseNo(String v) { this.testcaseNo = v; }
    public String getTestcaseNo() { return testcaseNo; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getProjectId() { return projectId; }
    public void setRequirementId(Long v) { this.requirementId = v; }
    public Long getRequirementId() { return requirementId; }
    public void setTitle(String v) { this.title = v; }
    public String getTitle() { return title; }
    public void setDescription(String v) { this.description = v; }
    public String getDescription() { return description; }
    public void setCategory(String v) { this.category = v; }
    public String getCategory() { return category; }
    public void setPriority(String v) { this.priority = v; }
    public String getPriority() { return priority; }
    public void setStatus(String v) { this.status = v; }
    public String getStatus() { return status; }
    public void setPreconditions(String v) { this.preconditions = v; }
    public String getPreconditions() { return preconditions; }
    public void setSteps(String v) { this.steps = v; }
    public String getSteps() { return steps; }
    public void setExpectedResult(String v) { this.expectedResult = v; }
    public String getExpectedResult() { return expectedResult; }
    public void setActualResult(String v) { this.actualResult = v; }
    public String getActualResult() { return actualResult; }
    public void setIsAutomated(String v) { this.isAutomated = v; }
    public String getIsAutomated() { return isAutomated; }
    public void setAutomationScriptPath(String v) { this.automationScriptPath = v; }
    public String getAutomationScriptPath() { return automationScriptPath; }
    public void setExecutionCount(Integer v) { this.executionCount = v; }
    public Integer getExecutionCount() { return executionCount; }
    public void setLastExecutedAt(Date v) { this.lastExecutedAt = v; }
    public Date getLastExecutedAt() { return lastExecutedAt; }
    public void setTags(String v) { this.tags = v; }
    public String getTags() { return tags; }
    public void setDelFlag(String v) { this.delFlag = v; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("testcaseId", testcaseId)
            .append("testcaseNo", testcaseNo)
            .append("projectId", projectId)
            .append("requirementId", requirementId)
            .append("title", title)
            .append("category", category)
            .append("priority", priority)
            .append("status", status)
            .append("isAutomated", isAutomated)
            .append("automationScriptPath", automationScriptPath)
            .append("executionCount", executionCount)
            .append("lastExecutedAt", lastExecutedAt)
            .append("tags", tags)
            .toString();
    }
}
