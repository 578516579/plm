package cn.com.bosssfot.dv.plm.dbdesign.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 数据库设计 — PRD §F3.2
 */
public class Dbdesign extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long dbdesignId;

    @Excel(name = "数据库设计编号")
    private String dbdesignNo;

    @Excel(name = "项目ID")
    private Long projectId;

    @Excel(name = "数据库设计标题")
    private String title;

    @Excel(name = "数据库类型")
    private String dbType;

    private String erContent;

    private String dictContent;

    private String ddlContent;

    private String reviewReport;

    @Excel(name = "AI生成")
    private String aiGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aiGeneratedAt;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "设计者用户ID")
    private Long authorUserId;

    private Long reviewerUserId;

    public Long getDbdesignId() { return dbdesignId; }
    public void setDbdesignId(Long dbdesignId) { this.dbdesignId = dbdesignId; }

    public String getDbdesignNo() { return dbdesignNo; }
    public void setDbdesignNo(String dbdesignNo) { this.dbdesignNo = dbdesignNo; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }

    public String getErContent() { return erContent; }
    public void setErContent(String erContent) { this.erContent = erContent; }

    public String getDictContent() { return dictContent; }
    public void setDictContent(String dictContent) { this.dictContent = dictContent; }

    public String getDdlContent() { return ddlContent; }
    public void setDdlContent(String ddlContent) { this.ddlContent = ddlContent; }

    public String getReviewReport() { return reviewReport; }
    public void setReviewReport(String reviewReport) { this.reviewReport = reviewReport; }

    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String aiGenerated) { this.aiGenerated = aiGenerated; }

    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date aiGeneratedAt) { this.aiGeneratedAt = aiGeneratedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }

    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long reviewerUserId) { this.reviewerUserId = reviewerUserId; }
}
