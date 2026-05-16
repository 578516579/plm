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
 * 与 Figma 集成,AI 辅助设计规范检查与标注生成,农业场景 UI 组件库
 * 4 状态机 (含反向边 01→00):
 *   00 草稿 → 01 评审中 → 02 已确认 → 03 已废弃 (终态)
 *   01→00 评审打回 (反向边)
 */
public class Ued extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long uedId;
    @Excel(name = "UED编号") private String uedNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "设计稿名称") private String title;
    @Excel(name = "设计类型") private String designType;
    @Excel(name = "目标平台") private String platform;
    private String figmaFileKey;
    private String figmaUrl;
    @Excel(name = "版本") private String version;
    private String description;
    private String reviewReport;
    @Excel(name = "规范遵从度%") private BigDecimal complianceScore;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    private Long requirementId;
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
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDesignType() { return designType; }
    public void setDesignType(String v) { this.designType = v; }
    public String getPlatform() { return platform; }
    public void setPlatform(String v) { this.platform = v; }
    public String getFigmaFileKey() { return figmaFileKey; }
    public void setFigmaFileKey(String v) { this.figmaFileKey = v; }
    public String getFigmaUrl() { return figmaUrl; }
    public void setFigmaUrl(String v) { this.figmaUrl = v; }
    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getReviewReport() { return reviewReport; }
    public void setReviewReport(String v) { this.reviewReport = v; }
    public BigDecimal getComplianceScore() { return complianceScore; }
    public void setComplianceScore(BigDecimal v) { this.complianceScore = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date v) { this.aiGeneratedAt = v; }
    public Long getRequirementId() { return requirementId; }
    public void setRequirementId(Long v) { this.requirementId = v; }
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
            .append("version", version)
            .append("complianceScore", complianceScore)
            .append("status", status)
            .toString();
    }
}
