package cn.com.bosssfot.dv.plm.arch.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 系统概要设计对象 tb_arch — PRD §F3.1 + 原型 archdesign.html
 * AI 根据 PRD 推荐技术架构 + 生成 C4 模型容器图 + 映射非功能需求
 * 4 状态机 (含反向边 01→00):
 *   00 草稿 → 01 评审中 → {00 (打回) / 02 已确认} → 03 已废弃 (终态)
 */
public class Arch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long archId;
    @Excel(name = "架构编号") private String archNo;
    @Excel(name = "项目ID") private Long projectId;
    private Long prdId;
    @Excel(name = "架构标题") private String title;
    @Excel(name = "架构模式") private String archMode;
    @Excel(name = "技术栈") private String primaryStack;
    @Excel(name = "数据库") private String databaseChoice;
    @Excel(name = "AI编排") private String aiOrchestration;
    @Excel(name = "部署方式") private String deploymentType;
    @Excel(name = "IoT协议") private String iotProtocol;
    private String designContent;
    private String c4DiagramContent;
    private String nfrMapping;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private Long reviewerUserId;
    private String delFlag;

    public Long getArchId() { return archId; }
    public void setArchId(Long v) { this.archId = v; }
    public String getArchNo() { return archNo; }
    public void setArchNo(String v) { this.archNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getPrdId() { return prdId; }
    public void setPrdId(Long v) { this.prdId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getArchMode() { return archMode; }
    public void setArchMode(String v) { this.archMode = v; }
    public String getPrimaryStack() { return primaryStack; }
    public void setPrimaryStack(String v) { this.primaryStack = v; }
    public String getDatabaseChoice() { return databaseChoice; }
    public void setDatabaseChoice(String v) { this.databaseChoice = v; }
    public String getAiOrchestration() { return aiOrchestration; }
    public void setAiOrchestration(String v) { this.aiOrchestration = v; }
    public String getDeploymentType() { return deploymentType; }
    public void setDeploymentType(String v) { this.deploymentType = v; }
    public String getIotProtocol() { return iotProtocol; }
    public void setIotProtocol(String v) { this.iotProtocol = v; }
    public String getDesignContent() { return designContent; }
    public void setDesignContent(String v) { this.designContent = v; }
    public String getC4DiagramContent() { return c4DiagramContent; }
    public void setC4DiagramContent(String v) { this.c4DiagramContent = v; }
    public String getNfrMapping() { return nfrMapping; }
    public void setNfrMapping(String v) { this.nfrMapping = v; }
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
            .append("archId", archId)
            .append("archNo", archNo)
            .append("title", title)
            .append("archMode", archMode)
            .append("primaryStack", primaryStack)
            .append("status", status)
            .toString();
    }
}
