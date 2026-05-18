package cn.com.bosssfot.dv.plm.analytics.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 效能分析对象 tb_analytics
 *
 * 关联：
 * - PRD §4.6 效能分析字段定义
 * - 编号规则: ANL-YYYY-NNNN
 * - 状态机: 00=草稿 → 01=已生成 (终态)
 */
public class Analytics extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分析 ID */
    private Long analyticsId;

    /** 分析编号（ANL-YYYY-NNNN） */
    @Excel(name = "分析编号")
    private String analyticsNo;

    /** 所属项目 ID */
    @Excel(name = "项目ID")
    private Long projectId;

    /** 分析标题 */
    @Excel(name = "标题")
    private String title;

    /** 分析周期类型（字典 biz_analytics_period: monthly/quarterly/yearly） */
    @Excel(name = "周期类型", dictType = "biz_analytics_period")
    private String period;

    /** 周期值（如 2026-05 或 2026-Q2） */
    @Excel(name = "周期值")
    private String periodValue;

    /** 需求吞吐量 */
    @Excel(name = "需求吞吐量")
    private Integer requirementThroughput;

    /** 迭代准时率(%) */
    @Excel(name = "迭代准时率(%)")
    private BigDecimal iterationOnTimeRate;

    /** 缺陷密度 */
    @Excel(name = "缺陷密度")
    private BigDecimal defectDensity;

    /** AI节省工时 */
    @Excel(name = "AI节省工时")
    private BigDecimal aiTimeSaved;

    /** 项目健康度(0-100) */
    @Excel(name = "项目健康度")
    private BigDecimal projectHealthScore;

    /** AI改进建议 */
    private String aiSuggestions;

    /** 是否AI生成（Y/N） */
    private String aiGenerated;

    /** AI生成时间 */
    private Date aiGeneratedAt;

    /** 状态（字典 biz_analytics_status: 00=草稿,01=已生成） */
    @Excel(name = "状态", dictType = "biz_analytics_status")
    private String status;

    /** 作者用户ID */
    private Long authorUserId;

    /** 删除标志（0=正常 2=删除） */
    private String delFlag;

    public void setAnalyticsId(Long analyticsId) { this.analyticsId = analyticsId; }
    public Long getAnalyticsId() { return analyticsId; }

    public void setAnalyticsNo(String analyticsNo) { this.analyticsNo = analyticsNo; }
    public String getAnalyticsNo() { return analyticsNo; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProjectId() { return projectId; }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    public void setPeriod(String period) { this.period = period; }
    public String getPeriod() { return period; }

    public void setPeriodValue(String periodValue) { this.periodValue = periodValue; }
    public String getPeriodValue() { return periodValue; }

    public void setRequirementThroughput(Integer requirementThroughput) { this.requirementThroughput = requirementThroughput; }
    public Integer getRequirementThroughput() { return requirementThroughput; }

    public void setIterationOnTimeRate(BigDecimal iterationOnTimeRate) { this.iterationOnTimeRate = iterationOnTimeRate; }
    public BigDecimal getIterationOnTimeRate() { return iterationOnTimeRate; }

    public void setDefectDensity(BigDecimal defectDensity) { this.defectDensity = defectDensity; }
    public BigDecimal getDefectDensity() { return defectDensity; }

    public void setAiTimeSaved(BigDecimal aiTimeSaved) { this.aiTimeSaved = aiTimeSaved; }
    public BigDecimal getAiTimeSaved() { return aiTimeSaved; }

    public void setProjectHealthScore(BigDecimal projectHealthScore) { this.projectHealthScore = projectHealthScore; }
    public BigDecimal getProjectHealthScore() { return projectHealthScore; }

    public void setAiSuggestions(String aiSuggestions) { this.aiSuggestions = aiSuggestions; }
    public String getAiSuggestions() { return aiSuggestions; }

    public void setAiGenerated(String aiGenerated) { this.aiGenerated = aiGenerated; }
    public String getAiGenerated() { return aiGenerated; }

    public void setAiGeneratedAt(Date aiGeneratedAt) { this.aiGeneratedAt = aiGeneratedAt; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }
    public Long getAuthorUserId() { return authorUserId; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("analyticsId", getAnalyticsId())
            .append("analyticsNo", getAnalyticsNo())
            .append("projectId", getProjectId())
            .append("title", getTitle())
            .append("period", getPeriod())
            .append("periodValue", getPeriodValue())
            .append("requirementThroughput", getRequirementThroughput())
            .append("iterationOnTimeRate", getIterationOnTimeRate())
            .append("defectDensity", getDefectDensity())
            .append("aiTimeSaved", getAiTimeSaved())
            .append("projectHealthScore", getProjectHealthScore())
            .append("aiGenerated", getAiGenerated())
            .append("aiGeneratedAt", getAiGeneratedAt())
            .append("status", getStatus())
            .append("authorUserId", getAuthorUserId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
