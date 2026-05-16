package cn.com.bosssfot.dv.plm.ued.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * UED 设计稿版本对象 tb_ued — PRD §F2.3 + 原型 ued.html
 * 4 状态机 (含反向边 01→00):
 *   00 草稿 → 01 评审中 → 02 已确认 → 03 已废弃 (终态)
 *   01→00 评审打回
 */
public class Ued extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long uedId;
    @Excel(name = "UED编号")              private String uedNo;
    @Excel(name = "项目ID")               private Long projectId;
    @Excel(name = "设计稿名称")           private String title;
    @Excel(name = "版本号")               private String version;
    private String figmaUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date figmaSyncedAt;
    private String aiReviewResult;
    @Excel(name = "AI评审分")             private BigDecimal aiReviewScore;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiReviewedAt;
    private String aiGenerated;
    @Excel(name = "状态",
           dictType = "biz_ued_status")   private String status;
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
    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }
    public String getFigmaUrl() { return figmaUrl; }
    public void setFigmaUrl(String v) { this.figmaUrl = v; }
    public Date getFigmaSyncedAt() { return figmaSyncedAt; }
    public void setFigmaSyncedAt(Date v) { this.figmaSyncedAt = v; }
    public String getAiReviewResult() { return aiReviewResult; }
    public void setAiReviewResult(String v) { this.aiReviewResult = v; }
    public BigDecimal getAiReviewScore() { return aiReviewScore; }
    public void setAiReviewScore(BigDecimal v) { this.aiReviewScore = v; }
    public Date getAiReviewedAt() { return aiReviewedAt; }
    public void setAiReviewedAt(Date v) { this.aiReviewedAt = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
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
            .append("status", status)
            .append("aiReviewScore", aiReviewScore)
            .toString();
    }
}
