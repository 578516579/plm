package cn.com.bosssfot.dv.plm.inception.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 项目立项对象 tb_inception — PRD §F1.1 + 原型 inception.html (含 JS runInceptionAI line 696-729)
 *
 * 关键漂移修复 (2026-05-17):
 * 1. 拆分 aiProposalContent 为 4 个 AI 输出子字段 (背景/市场/ROI/决策) — 原型 runInceptionAI 生成的就是这 4 块
 * 2. 新增 8 个 ROI 结构化数值字段 (marketSize/devCost/firstYearRevenue/roiMultiple/...) — 原型 AI 报告硬编码这些数值
 * 3. aiRisksJson 字段改 JSON 数组语义 ([{level,title,description}, ...]),对应原型 incRisks innerHTML 多条 risk
 *
 * 5 状态机 (含反向边 04→00):
 *   00 草稿 → 01 已提交 → 02 审批中 → 03 已批准 / 04 已驳回 → 00 (打回重写)
 */
public class Inception extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long inceptionId;
    @Excel(name = "立项编号") private String inceptionNo;
    @Excel(name = "项目名称") private String projectName;
    @Excel(name = "业务线") private String businessLine;
    @Excel(name = "项目类型") private String inceptionType;
    private String background;
    @Excel(name = "预计工期(月)") private Integer estimatedDurationMonths;
    @Excel(name = "团队规模") private String estimatedTeam;

    // ===== AI 立项建议书 4 块结构化输出 (对应原型 runInceptionAI 的 4 个 <h4>) =====
    /** AI 项目背景分析 (原型: 一、项目背景) */
    private String aiBackground;
    /** AI 市场机会分析 (原型: 二、市场机会) */
    private String aiMarketOpportunity;
    /** AI ROI 预估 — 散文段落 (原型: 三、ROI预估 — 详细推算逻辑) */
    private String aiRoiEstimate;
    /** AI 建议决策 (原型: 四、建议决策 — ✅ 建议立项,优先级 Pn,Q3 启动,分 n 期交付) */
    private String aiRecommendDecision;

    // ===== 8 个结构化数值字段 (原型 ROI 计算硬编码的数,业务方应该可改) =====
    /** 市场规模 (亿元,原型: 580 亿) */
    @Excel(name = "市场规模(亿元)") private BigDecimal marketSize;
    /** 数字化渗透率 (%,原型: 8) */
    @Excel(name = "数字化渗透率%") private BigDecimal digitalPenetration;
    /** 开发成本预估 (万元,原型: 180) */
    @Excel(name = "开发成本(万元)") private BigDecimal devCostEstimate;
    /** 首年营收预估 (万元,原型: 3000) */
    @Excel(name = "首年营收(万元)") private BigDecimal firstYearRevenue;
    /** ROI 倍数 (原型: 16.7) */
    @Excel(name = "ROI倍数") private BigDecimal roiMultiple;
    /** 建议优先级 — 字典 biz_inception_priority: P0/P1/P2 */
    @Excel(name = "建议优先级") private String recommendedPriority;
    /** 推荐启动季度 — 例 Q3-2026 */
    @Excel(name = "推荐启动季度") private String recommendedStartQuarter;
    /** 分期交付期数 (原型: 3 期) */
    @Excel(name = "分期交付期数") private Integer deliveryPhases;

    // ===== AI 风险识别 — 改为 JSON 数组语义 =====
    /** AI 风险识别 JSON 数组 `[{level:'warning'|'critical',title,description}, ...]` (原型 incRisks innerHTML) */
    private String aiRisksJson;

    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private String rejectReason;
    private Long submitterUserId;
    private Long approverUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date approvedAt;
    private Long projectId;
    private String delFlag;

    public Long getInceptionId() { return inceptionId; }
    public void setInceptionId(Long v) { this.inceptionId = v; }
    public String getInceptionNo() { return inceptionNo; }
    public void setInceptionNo(String v) { this.inceptionNo = v; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String v) { this.projectName = v; }
    public String getBusinessLine() { return businessLine; }
    public void setBusinessLine(String v) { this.businessLine = v; }
    public String getInceptionType() { return inceptionType; }
    public void setInceptionType(String v) { this.inceptionType = v; }
    public String getBackground() { return background; }
    public void setBackground(String v) { this.background = v; }
    public Integer getEstimatedDurationMonths() { return estimatedDurationMonths; }
    public void setEstimatedDurationMonths(Integer v) { this.estimatedDurationMonths = v; }
    public String getEstimatedTeam() { return estimatedTeam; }
    public void setEstimatedTeam(String v) { this.estimatedTeam = v; }
    public String getAiBackground() { return aiBackground; }
    public void setAiBackground(String v) { this.aiBackground = v; }
    public String getAiMarketOpportunity() { return aiMarketOpportunity; }
    public void setAiMarketOpportunity(String v) { this.aiMarketOpportunity = v; }
    public String getAiRoiEstimate() { return aiRoiEstimate; }
    public void setAiRoiEstimate(String v) { this.aiRoiEstimate = v; }
    public String getAiRecommendDecision() { return aiRecommendDecision; }
    public void setAiRecommendDecision(String v) { this.aiRecommendDecision = v; }
    public BigDecimal getMarketSize() { return marketSize; }
    public void setMarketSize(BigDecimal v) { this.marketSize = v; }
    public BigDecimal getDigitalPenetration() { return digitalPenetration; }
    public void setDigitalPenetration(BigDecimal v) { this.digitalPenetration = v; }
    public BigDecimal getDevCostEstimate() { return devCostEstimate; }
    public void setDevCostEstimate(BigDecimal v) { this.devCostEstimate = v; }
    public BigDecimal getFirstYearRevenue() { return firstYearRevenue; }
    public void setFirstYearRevenue(BigDecimal v) { this.firstYearRevenue = v; }
    public BigDecimal getRoiMultiple() { return roiMultiple; }
    public void setRoiMultiple(BigDecimal v) { this.roiMultiple = v; }
    public String getRecommendedPriority() { return recommendedPriority; }
    public void setRecommendedPriority(String v) { this.recommendedPriority = v; }
    public String getRecommendedStartQuarter() { return recommendedStartQuarter; }
    public void setRecommendedStartQuarter(String v) { this.recommendedStartQuarter = v; }
    public Integer getDeliveryPhases() { return deliveryPhases; }
    public void setDeliveryPhases(Integer v) { this.deliveryPhases = v; }
    public String getAiRisksJson() { return aiRisksJson; }
    public void setAiRisksJson(String v) { this.aiRisksJson = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date v) { this.aiGeneratedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String v) { this.rejectReason = v; }
    public Long getSubmitterUserId() { return submitterUserId; }
    public void setSubmitterUserId(Long v) { this.submitterUserId = v; }
    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long v) { this.approverUserId = v; }
    public Date getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Date v) { this.approvedAt = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("inceptionId", inceptionId)
            .append("inceptionNo", inceptionNo)
            .append("projectName", projectName)
            .append("businessLine", businessLine)
            .append("status", status)
            .toString();
    }
}
