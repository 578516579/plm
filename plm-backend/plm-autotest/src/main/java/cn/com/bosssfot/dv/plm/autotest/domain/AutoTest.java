package cn.com.bosssfot.dv.plm.autotest.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 自动化测试 tb_autotest — PRD §F4.5 + 原型 autotest.html
 * AI 生成测试脚本 + 定时执行 + 智能根因分析
 * 3 状态机: 00 草稿 → 01 已激活 → 02 已禁用
 */
public class AutoTest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long autotestId;
    @Excel(name = "套件编号") private String autotestNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "套件名称") private String title;
    @Excel(name = "套件类型") private String testSuiteType;
    @Excel(name = "测试框架") private String framework;
    private String targetUrl;
    private String scriptContent;
    private String scheduleEnabled;
    private String scheduleCron;
    @Excel(name = "用例总数") private Integer totalCases;
    @Excel(name = "通过数") private Integer passedCases;
    @Excel(name = "失败数") private Integer failedCases;
    @Excel(name = "通过率") private BigDecimal passRate;
    private Integer executionDurationSec;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date lastExecutedAt;
    private String lastRootCauseAnalysis;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getAutotestId() { return autotestId; }
    public void setAutotestId(Long v) { this.autotestId = v; }
    public String getAutotestNo() { return autotestNo; }
    public void setAutotestNo(String v) { this.autotestNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getTestSuiteType() { return testSuiteType; }
    public void setTestSuiteType(String v) { this.testSuiteType = v; }
    public String getFramework() { return framework; }
    public void setFramework(String v) { this.framework = v; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String v) { this.targetUrl = v; }
    public String getScriptContent() { return scriptContent; }
    public void setScriptContent(String v) { this.scriptContent = v; }
    public String getScheduleEnabled() { return scheduleEnabled; }
    public void setScheduleEnabled(String v) { this.scheduleEnabled = v; }
    public String getScheduleCron() { return scheduleCron; }
    public void setScheduleCron(String v) { this.scheduleCron = v; }
    public Integer getTotalCases() { return totalCases; }
    public void setTotalCases(Integer v) { this.totalCases = v; }
    public Integer getPassedCases() { return passedCases; }
    public void setPassedCases(Integer v) { this.passedCases = v; }
    public Integer getFailedCases() { return failedCases; }
    public void setFailedCases(Integer v) { this.failedCases = v; }
    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal v) { this.passRate = v; }
    public Integer getExecutionDurationSec() { return executionDurationSec; }
    public void setExecutionDurationSec(Integer v) { this.executionDurationSec = v; }
    public Date getLastExecutedAt() { return lastExecutedAt; }
    public void setLastExecutedAt(Date v) { this.lastExecutedAt = v; }
    public String getLastRootCauseAnalysis() { return lastRootCauseAnalysis; }
    public void setLastRootCauseAnalysis(String v) { this.lastRootCauseAnalysis = v; }
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
            .append("autotestId", autotestId)
            .append("autotestNo", autotestNo)
            .append("title", title)
            .append("testSuiteType", testSuiteType)
            .append("framework", framework)
            .append("status", status)
            .toString();
    }
}
