package cn.com.bosssfot.dv.plm.ued.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * UED 设计协同对象 tb_ued — PRD §F2.3 + 原型 ued.html
 * Figma MCP 集成 + AI 规范检查 + 双向关联需求
 * 4 状态机 (含反向边 01→00): 00→01→{00,02}→03 已废弃
 */
public class Ued extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long uedId;
    @Excel(name = "UED编号") private String uedNo;
    @Excel(name = "项目ID") private Long projectId;
    private Long requirementId;
    @Excel(name = "设计稿名称") private String title;
    private String figmaUrl;
    private String figmaFileKey;
    @Excel(name = "版本") private String versionLabel;
    private String previewUrl;
    private String annotationContent;
    private String aiReviewReport;
    private BigDecimal aiReviewScore;
    private String complianceCheck;
    private String usabilityIssues;
    @Excel(name = "农业组件") private String agriComponentTags;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private Long designerUserId;
    private Long reviewerUserId;
    private String delFlag;

    public Long getUedId() { return uedId; }
    public void setUedId(Long v) { this.uedId = v; }
    public String getUedNo() { return uedNo; }
    public void setUedNo(String v) { this.uedNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getRequirementId() { return requirementId; }
    public void setRequirementId(Long v) { this.requirementId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getFigmaUrl() { return figmaUrl; }
    public void setFigmaUrl(String v) { this.figmaUrl = v; }
    public String getFigmaFileKey() { return figmaFileKey; }
    public void setFigmaFileKey(String v) { this.figmaFileKey = v; }
    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String v) { this.versionLabel = v; }
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String v) { this.previewUrl = v; }
    public String getAnnotationContent() { return annotationContent; }
    public void setAnnotationContent(String v) { this.annotationContent = v; }
    public String getAiReviewReport() { return aiReviewReport; }
    public void setAiReviewReport(String v) { this.aiReviewReport = v; }
    public BigDecimal getAiReviewScore() { return aiReviewScore; }
    public void setAiReviewScore(BigDecimal v) { this.aiReviewScore = v; }
    public String getComplianceCheck() { return complianceCheck; }
    public void setComplianceCheck(String v) { this.complianceCheck = v; }
    public String getUsabilityIssues() { return usabilityIssues; }
    public void setUsabilityIssues(String v) { this.usabilityIssues = v; }
    public String getAgriComponentTags() { return agriComponentTags; }
    public void setAgriComponentTags(String v) { this.agriComponentTags = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date v) { this.aiGeneratedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getDesignerUserId() { return designerUserId; }
    public void setDesignerUserId(Long v) { this.designerUserId = v; }
    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long v) { this.reviewerUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("uedId", uedId)
            .append("uedNo", uedNo)
            .append("title", title)
            .append("versionLabel", versionLabel)
            .append("aiReviewScore", aiReviewScore)
            .append("status", status)
            .toString();
    }
}
