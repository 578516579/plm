package cn.com.bosssfot.dv.plm.autotest.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 自动化测试套件对象 tb_autotest — PRD §F4.5 + 原型 autotest.html
 * 5 状态机: 00 草稿 → 01 待执行 → 02 执行中 → 03 已完成 → 04 已归档 (终态)
 */
public class Autotest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long autotestId;
    @Excel(name = "编号")       private String autotestNo;
    @Excel(name = "项目ID")     private Long projectId;
    @Excel(name = "套件名称")   private String suiteName;
    private String description;
    @Excel(name = "框架")       private String framework;
    @Excel(name = "目标模块")   private String targetModule;
    private String scriptContent;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date lastRunAt;
    @Excel(name = "通过率%")    private BigDecimal lastRunPassRate;
    private String lastRunDuration;
    private Integer failedCaseCount;
    @Excel(name = "状态")       private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getAutotestId() { return autotestId; }
    public void setAutotestId(Long v) { this.autotestId = v; }
    public String getAutotestNo() { return autotestNo; }
    public void setAutotestNo(String v) { this.autotestNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getSuiteName() { return suiteName; }
    public void setSuiteName(String v) { this.suiteName = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getFramework() { return framework; }
    public void setFramework(String v) { this.framework = v; }
    public String getTargetModule() { return targetModule; }
    public void setTargetModule(String v) { this.targetModule = v; }
    public String getScriptContent() { return scriptContent; }
    public void setScriptContent(String v) { this.scriptContent = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(Date v) { this.lastRunAt = v; }
    public BigDecimal getLastRunPassRate() { return lastRunPassRate; }
    public void setLastRunPassRate(BigDecimal v) { this.lastRunPassRate = v; }
    public String getLastRunDuration() { return lastRunDuration; }
    public void setLastRunDuration(String v) { this.lastRunDuration = v; }
    public Integer getFailedCaseCount() { return failedCaseCount; }
    public void setFailedCaseCount(Integer v) { this.failedCaseCount = v; }
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
            .append("suiteName", suiteName)
            .append("framework", framework)
            .append("status", status)
            .toString();
    }
}
