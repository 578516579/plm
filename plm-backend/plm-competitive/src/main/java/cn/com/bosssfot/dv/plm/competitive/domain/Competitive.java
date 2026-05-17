package cn.com.bosssfot.dv.plm.competitive.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 竞品情报对象 tb_competitive — PRD §F1.3 + 原型 competitive.html
 * AI 自动爬取竞品官网/App Store + SWOT 分析 + 订阅推送
 * 3 状态机:
 *   00 草稿 → 01 已发布 → 02 已归档 (终态)
 */
public class Competitive extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long competitiveId;
    @Excel(name = "竞品编号") private String competitiveNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "竞品名称") private String competitorName;
    @Excel(name = "厂商") private String vendor;
    private String website;
    private String pricingModel;
    @Excel(name = "价格档") private String pricingTier;
    private String featureMatrix;
    private String strengths;
    private String weaknesses;
    private String opportunities;
    private String threats;
    private String aiAnalysisReport;
    // 2026-05-17 drift 修复: 跟原型 competitive.html renderCompetitive 1:1 对齐 — 3 个项目级 JSON 字段
    /** 竞品对比矩阵 JSON `{dimensions:[{name,order}], vendors:[{name,isOurProduct}], scores:[[0|0.5|1, ...]]}` */
    private String matrixJson;
    /** 竞品动态监控 JSON `[{vendor,news,threatLevel:'low'|'mid'|'high',date}, ...]` */
    private String monitorsJson;
    /** 本品 SWOT JSON `{strengths:[...],weaknesses:[...],opportunities:[...],threats:[...]}` */
    private String ourSwotJson;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    private String monitorEnabled;
    private String monitorKeywords;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date lastMonitoredAt;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getCompetitiveId() { return competitiveId; }
    public void setCompetitiveId(Long v) { this.competitiveId = v; }
    public String getCompetitiveNo() { return competitiveNo; }
    public void setCompetitiveNo(String v) { this.competitiveNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getCompetitorName() { return competitorName; }
    public void setCompetitorName(String v) { this.competitorName = v; }
    public String getVendor() { return vendor; }
    public void setVendor(String v) { this.vendor = v; }
    public String getWebsite() { return website; }
    public void setWebsite(String v) { this.website = v; }
    public String getPricingModel() { return pricingModel; }
    public void setPricingModel(String v) { this.pricingModel = v; }
    public String getPricingTier() { return pricingTier; }
    public void setPricingTier(String v) { this.pricingTier = v; }
    public String getFeatureMatrix() { return featureMatrix; }
    public void setFeatureMatrix(String v) { this.featureMatrix = v; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String v) { this.strengths = v; }
    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String v) { this.weaknesses = v; }
    public String getOpportunities() { return opportunities; }
    public void setOpportunities(String v) { this.opportunities = v; }
    public String getThreats() { return threats; }
    public void setThreats(String v) { this.threats = v; }
    public String getAiAnalysisReport() { return aiAnalysisReport; }
    public void setAiAnalysisReport(String v) { this.aiAnalysisReport = v; }
    public String getMatrixJson() { return matrixJson; }
    public void setMatrixJson(String v) { this.matrixJson = v; }
    public String getMonitorsJson() { return monitorsJson; }
    public void setMonitorsJson(String v) { this.monitorsJson = v; }
    public String getOurSwotJson() { return ourSwotJson; }
    public void setOurSwotJson(String v) { this.ourSwotJson = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date v) { this.aiGeneratedAt = v; }
    public String getMonitorEnabled() { return monitorEnabled; }
    public void setMonitorEnabled(String v) { this.monitorEnabled = v; }
    public String getMonitorKeywords() { return monitorKeywords; }
    public void setMonitorKeywords(String v) { this.monitorKeywords = v; }
    public Date getLastMonitoredAt() { return lastMonitoredAt; }
    public void setLastMonitoredAt(Date v) { this.lastMonitoredAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("competitiveId", competitiveId)
            .append("competitiveNo", competitiveNo)
            .append("competitorName", competitorName)
            .append("pricingTier", pricingTier)
            .append("status", status)
            .toString();
    }
}
