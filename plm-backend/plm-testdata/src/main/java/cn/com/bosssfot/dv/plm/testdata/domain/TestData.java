package cn.com.bosssfot.dv.plm.testdata.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 测试数据工厂对象 tb_testdata — PRD §F4.3 + 原型 testdata.html
 * 基于字段语义 + AgriKB 生成农业场景真实感测试数据
 * 3 状态机: 00 草稿 → 01 已生成 → 02 已归档 (终态)
 */
public class TestData extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long testdataId;
    @Excel(name = "数据集编号") private String testdataNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "任务标题") private String title;
    @Excel(name = "目标表") private String targetTable;
    @Excel(name = "生成数量") private Integer generateCount;
    @Excel(name = "输出格式") private String outputFormat;
    private String fieldSemantics;
    private String ruleChinaCoord;
    private String ruleTimeContinuity;
    private String ruleSensorRange;
    private String ruleIncludeOutliers;
    private String generatedContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date generatedAt;
    private String aiGenerated;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getTestdataId() { return testdataId; }
    public void setTestdataId(Long v) { this.testdataId = v; }
    public String getTestdataNo() { return testdataNo; }
    public void setTestdataNo(String v) { this.testdataNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String v) { this.targetTable = v; }
    public Integer getGenerateCount() { return generateCount; }
    public void setGenerateCount(Integer v) { this.generateCount = v; }
    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String v) { this.outputFormat = v; }
    public String getFieldSemantics() { return fieldSemantics; }
    public void setFieldSemantics(String v) { this.fieldSemantics = v; }
    public String getRuleChinaCoord() { return ruleChinaCoord; }
    public void setRuleChinaCoord(String v) { this.ruleChinaCoord = v; }
    public String getRuleTimeContinuity() { return ruleTimeContinuity; }
    public void setRuleTimeContinuity(String v) { this.ruleTimeContinuity = v; }
    public String getRuleSensorRange() { return ruleSensorRange; }
    public void setRuleSensorRange(String v) { this.ruleSensorRange = v; }
    public String getRuleIncludeOutliers() { return ruleIncludeOutliers; }
    public void setRuleIncludeOutliers(String v) { this.ruleIncludeOutliers = v; }
    public String getGeneratedContent() { return generatedContent; }
    public void setGeneratedContent(String v) { this.generatedContent = v; }
    public Date getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Date v) { this.generatedAt = v; }
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
            .append("testdataId", testdataId)
            .append("testdataNo", testdataNo)
            .append("targetTable", targetTable)
            .append("generateCount", generateCount)
            .append("status", status)
            .toString();
    }
}
