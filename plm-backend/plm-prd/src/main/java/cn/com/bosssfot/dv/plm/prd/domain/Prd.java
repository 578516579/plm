package cn.com.bosssfot.dv.plm.prd.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * AI PRD 文档对象 tb_prd — PRD §F2.2 + 原型 prd.html
 * AI 基于 AgriKB 知识库自动生成完整 PRD (7 段: 背景/用户故事/功能/非功能/验收/原型/版本)
 * 4 状态机 (含反向边 01→00):
 *   00 草稿 → 01 评审中 → 02 已确认 → 03 已废弃 (终态)
 *   01→00 评审打回 (反向边)
 */
public class Prd extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long prdId;
    @Excel(name = "PRD编号") private String prdNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "功能名称") private String title;
    private String description;
    @Excel(name = "业务场景") private String sceneTemplate;
    @Excel(name = "目标用户") private String targetUser;
    private String content;
    @Excel(name = "完整度%") private BigDecimal completenessScore;
    @Excel(name = "版本") private String version;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private Long reviewerUserId;
    private String delFlag;

    public Long getPrdId() { return prdId; }
    public void setPrdId(Long v) { this.prdId = v; }
    public String getPrdNo() { return prdNo; }
    public void setPrdNo(String v) { this.prdNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getSceneTemplate() { return sceneTemplate; }
    public void setSceneTemplate(String v) { this.sceneTemplate = v; }
    public String getTargetUser() { return targetUser; }
    public void setTargetUser(String v) { this.targetUser = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public BigDecimal getCompletenessScore() { return completenessScore; }
    public void setCompletenessScore(BigDecimal v) { this.completenessScore = v; }
    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date v) { this.aiGeneratedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long v) { this.reviewerUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("prdId", prdId)
            .append("prdNo", prdNo)
            .append("title", title)
            .append("version", version)
            .append("completenessScore", completenessScore)
            .append("status", status)
            .toString();
    }
}
