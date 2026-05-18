package cn.com.bosssfot.dv.plm.manualimpl.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 实施手册对象 tb_manual_impl — PRD §F5.2
 * 5 状态机: 00 草稿 → 01 生成中 → 02 已生成 → 03 已审核 → 04 已发布
 */
public class ManualImpl extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long manualImplId;
    @Excel(name = "手册编号") private String manualImplNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "手册标题") private String title;
    @Excel(name = "部署模式") private String deploymentMode;
    @Excel(name = "操作系统") private String os;
    @Excel(name = "数据库") private String database;
    private String envVars;
    private String content;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private Long reviewerUserId;
    private String delFlag;

    public Long getManualImplId() { return manualImplId; }
    public void setManualImplId(Long v) { this.manualImplId = v; }
    public String getManualImplNo() { return manualImplNo; }
    public void setManualImplNo(String v) { this.manualImplNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDeploymentMode() { return deploymentMode; }
    public void setDeploymentMode(String v) { this.deploymentMode = v; }
    public String getOs() { return os; }
    public void setOs(String v) { this.os = v; }
    public String getDatabase() { return database; }
    public void setDatabase(String v) { this.database = v; }
    public String getEnvVars() { return envVars; }
    public void setEnvVars(String v) { this.envVars = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
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
            .append("manualImplId", manualImplId)
            .append("manualImplNo", manualImplNo)
            .append("title", title)
            .append("deploymentMode", deploymentMode)
            .append("status", status)
            .toString();
    }
}
