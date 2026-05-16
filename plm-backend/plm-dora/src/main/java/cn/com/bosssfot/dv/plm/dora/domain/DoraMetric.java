package cn.com.bosssfot.dv.plm.dora.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * DORA 效能指标 tb_dora_metric — DevOps 扩展 + 原型 devops.html
 * DORA 4 指标 + 部署热力图 + 前置时间拆解 + AI 持续改进建议
 */
public class DoraMetric extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long doraId;
    @Excel(name = "DORA编号")  private String doraNo;
    @Excel(name = "项目ID")    private Long projectId;
    @Excel(name = "指标名称")  private String metricName;
    @Excel(name = "指标类型")  private String metricType;
    @Excel(name = "指标值")    private BigDecimal metricValue;
    @Excel(name = "单位")      private String metricUnit;
    @Excel(name = "周期")      private String periodType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "记录日期", dateFormat = "yyyy-MM-dd") private Date snapshotDate;
    private String trendChartJson;
    private String heatmapJson;
    private String leadtimeBreakdown;
    private String aiSuggestions;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态")      private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getDoraId() { return doraId; }
    public void setDoraId(Long v) { this.doraId = v; }
    public String getDoraNo() { return doraNo; }
    public void setDoraNo(String v) { this.doraNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String v) { this.metricName = v; }
    public String getMetricType() { return metricType; }
    public void setMetricType(String v) { this.metricType = v; }
    public BigDecimal getMetricValue() { return metricValue; }
    public void setMetricValue(BigDecimal v) { this.metricValue = v; }
    public String getMetricUnit() { return metricUnit; }
    public void setMetricUnit(String v) { this.metricUnit = v; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String v) { this.periodType = v; }
    public Date getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(Date v) { this.snapshotDate = v; }
    public String getTrendChartJson() { return trendChartJson; }
    public void setTrendChartJson(String v) { this.trendChartJson = v; }
    public String getHeatmapJson() { return heatmapJson; }
    public void setHeatmapJson(String v) { this.heatmapJson = v; }
    public String getLeadtimeBreakdown() { return leadtimeBreakdown; }
    public void setLeadtimeBreakdown(String v) { this.leadtimeBreakdown = v; }
    public String getAiSuggestions() { return aiSuggestions; }
    public void setAiSuggestions(String v) { this.aiSuggestions = v; }
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
            .append("doraId", doraId).append("doraNo", doraNo)
            .append("metricType", metricType).append("metricValue", metricValue)
            .append("snapshotDate", snapshotDate).toString();
    }
}
