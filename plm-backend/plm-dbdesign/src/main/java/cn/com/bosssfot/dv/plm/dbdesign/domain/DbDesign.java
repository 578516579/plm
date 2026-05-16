package cn.com.bosssfot.dv.plm.dbdesign.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 数据库设计对象 tb_dbdesign — PRD §F3.2 + 原型 dbdesign.html
 * AI 自动生成 ER 图 + 建表 SQL + 数据字典 + 规范检查
 * 4 状态机 (含反向边 01→00): 00 草稿 → 01 评审中 → {00,02} → 03 已废弃
 */
public class DbDesign extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long dbdesignId;
    @Excel(name = "DB设计编号") private String dbdesignNo;
    @Excel(name = "项目ID") private Long projectId;
    private Long archId;
    @Excel(name = "设计标题") private String title;
    @Excel(name = "DB引擎") private String dbEngine;
    private String erDiagramContent;
    private String dataDictionary;
    private String ddlScript;
    private String normalizationCheck;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private Long reviewerUserId;
    private String delFlag;

    public Long getDbdesignId() { return dbdesignId; }
    public void setDbdesignId(Long v) { this.dbdesignId = v; }
    public String getDbdesignNo() { return dbdesignNo; }
    public void setDbdesignNo(String v) { this.dbdesignNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getArchId() { return archId; }
    public void setArchId(Long v) { this.archId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDbEngine() { return dbEngine; }
    public void setDbEngine(String v) { this.dbEngine = v; }
    public String getErDiagramContent() { return erDiagramContent; }
    public void setErDiagramContent(String v) { this.erDiagramContent = v; }
    public String getDataDictionary() { return dataDictionary; }
    public void setDataDictionary(String v) { this.dataDictionary = v; }
    public String getDdlScript() { return ddlScript; }
    public void setDdlScript(String v) { this.ddlScript = v; }
    public String getNormalizationCheck() { return normalizationCheck; }
    public void setNormalizationCheck(String v) { this.normalizationCheck = v; }
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
            .append("dbdesignId", dbdesignId)
            .append("dbdesignNo", dbdesignNo)
            .append("title", title)
            .append("dbEngine", dbEngine)
            .append("status", status)
            .toString();
    }
}
