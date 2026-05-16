package cn.com.bosssfot.dv.plm.autotest.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 自动化测试套件 — PRD §F4.5
 */
public class Autotest extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long autotestId;

    @Excel(name = "套件编号")
    private String autotestNo;

    @Excel(name = "项目ID")
    private Long projectId;

    @Excel(name = "套件名称")
    private String title;

    @Excel(name = "套件类型")
    private String suiteType;

    @Excel(name = "测试框架")
    private String framework;

    private String scriptContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastRunAt;

    @Excel(name = "最近执行结果")
    private String lastRunResult;

    @Excel(name = "通过率(%)")
    private java.math.BigDecimal passRate;

    @Excel(name = "总用例数")
    private Integer totalCases;

    @Excel(name = "失败用例数")
    private Integer failedCases;

    @Excel(name = "执行耗时(ms)")
    private Integer executionTime;

    private String scheduleCron;

    @Excel(name = "AI生成")
    private String aiGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aiGeneratedAt;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "创建者用户ID")
    private Long authorUserId;

    public Long getAutotestId() { return autotestId; }
    public void setAutotestId(Long autotestId) { this.autotestId = autotestId; }

    public String getAutotestNo() { return autotestNo; }
    public void setAutotestNo(String autotestNo) { this.autotestNo = autotestNo; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSuiteType() { return suiteType; }
    public void setSuiteType(String suiteType) { this.suiteType = suiteType; }

    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }

    public String getScriptContent() { return scriptContent; }
    public void setScriptContent(String scriptContent) { this.scriptContent = scriptContent; }

    public Date getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(Date lastRunAt) { this.lastRunAt = lastRunAt; }

    public String getLastRunResult() { return lastRunResult; }
    public void setLastRunResult(String lastRunResult) { this.lastRunResult = lastRunResult; }

    public java.math.BigDecimal getPassRate() { return passRate; }
    public void setPassRate(java.math.BigDecimal passRate) { this.passRate = passRate; }

    public Integer getTotalCases() { return totalCases; }
    public void setTotalCases(Integer totalCases) { this.totalCases = totalCases; }

    public Integer getFailedCases() { return failedCases; }
    public void setFailedCases(Integer failedCases) { this.failedCases = failedCases; }

    public Integer getExecutionTime() { return executionTime; }
    public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }

    public String getScheduleCron() { return scheduleCron; }
    public void setScheduleCron(String scheduleCron) { this.scheduleCron = scheduleCron; }

    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String aiGenerated) { this.aiGenerated = aiGenerated; }

    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date aiGeneratedAt) { this.aiGeneratedAt = aiGeneratedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }
}
