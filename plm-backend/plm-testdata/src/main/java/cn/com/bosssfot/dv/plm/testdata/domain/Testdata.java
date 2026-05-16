package cn.com.bosssfot.dv.plm.testdata.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 测试数据工厂 — PRD §F4.3
 */
public class Testdata extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long testdataId;

    @Excel(name = "数据集编号")
    private String testdataNo;

    @Excel(name = "项目ID")
    private Long projectId;

    @Excel(name = "数据集名称")
    private String title;

    @Excel(name = "目标数据表")
    private String targetTable;

    @Excel(name = "生成数量")
    private Integer generateCount;

    @Excel(name = "输出格式")
    private String outputFormat;

    @Excel(name = "坐标约束")
    private String ruleCoordinate;

    @Excel(name = "时序连续性")
    private String ruleTimeSeries;

    @Excel(name = "传感器范围")
    private String ruleSensorRange;

    @Excel(name = "包含异常值")
    private String ruleIncludeAbnormal;

    private String generatedData;

    @Excel(name = "写入目标环境")
    private String writeTarget;

    @Excel(name = "写入模式")
    private String writeMode;

    @Excel(name = "AI生成")
    private String aiGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aiGeneratedAt;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "创建者用户ID")
    private Long authorUserId;

    public Long getTestdataId() { return testdataId; }
    public void setTestdataId(Long testdataId) { this.testdataId = testdataId; }

    public String getTestdataNo() { return testdataNo; }
    public void setTestdataNo(String testdataNo) { this.testdataNo = testdataNo; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }

    public Integer getGenerateCount() { return generateCount; }
    public void setGenerateCount(Integer generateCount) { this.generateCount = generateCount; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public String getRuleCoordinate() { return ruleCoordinate; }
    public void setRuleCoordinate(String ruleCoordinate) { this.ruleCoordinate = ruleCoordinate; }

    public String getRuleTimeSeries() { return ruleTimeSeries; }
    public void setRuleTimeSeries(String ruleTimeSeries) { this.ruleTimeSeries = ruleTimeSeries; }

    public String getRuleSensorRange() { return ruleSensorRange; }
    public void setRuleSensorRange(String ruleSensorRange) { this.ruleSensorRange = ruleSensorRange; }

    public String getRuleIncludeAbnormal() { return ruleIncludeAbnormal; }
    public void setRuleIncludeAbnormal(String ruleIncludeAbnormal) { this.ruleIncludeAbnormal = ruleIncludeAbnormal; }

    public String getGeneratedData() { return generatedData; }
    public void setGeneratedData(String generatedData) { this.generatedData = generatedData; }

    public String getWriteTarget() { return writeTarget; }
    public void setWriteTarget(String writeTarget) { this.writeTarget = writeTarget; }

    public String getWriteMode() { return writeMode; }
    public void setWriteMode(String writeMode) { this.writeMode = writeMode; }

    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String aiGenerated) { this.aiGenerated = aiGenerated; }

    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date aiGeneratedAt) { this.aiGeneratedAt = aiGeneratedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }
}
