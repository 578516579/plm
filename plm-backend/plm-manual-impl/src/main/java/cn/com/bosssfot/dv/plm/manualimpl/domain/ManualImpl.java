package cn.com.bosssfot.dv.plm.manualimpl.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 实施手册对象 tb_manual_impl — PRD §F5.2 + 原型 implmanual.html
 * AI 一键生成 + 部署模式/OS/DB 维度配置 + 多格式导出
 * 4 状态机: 00 草稿 → 01 生成中 → 02 已生成 → 03 已发布
 */
public class ManualImpl extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long manualimplId;
    @Excel(name = "手册编号") private String manualimplNo;
    @Excel(name = "项目ID")   private Long projectId;
    @Excel(name = "手册标题") private String title;
    @Excel(name = "部署模式") private String deployMode;
    @Excel(name = "操作系统") private String osType;
    @Excel(name = "数据库")   private String dbType;
    private String envConfig;
    private String content;
    @Excel(name = "导出格式") private String outputFormats;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date generatedAt;
    @Excel(name = "状态")    private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getManualimplId() { return manualimplId; }
    public void setManualimplId(Long v) { this.manualimplId = v; }
    public String getManualimplNo() { return manualimplNo; }
    public void setManualimplNo(String v) { this.manualimplNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDeployMode() { return deployMode; }
    public void setDeployMode(String v) { this.deployMode = v; }
    public String getOsType() { return osType; }
    public void setOsType(String v) { this.osType = v; }
    public String getDbType() { return dbType; }
    public void setDbType(String v) { this.dbType = v; }
    public String getEnvConfig() { return envConfig; }
    public void setEnvConfig(String v) { this.envConfig = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public String getOutputFormats() { return outputFormats; }
    public void setOutputFormats(String v) { this.outputFormats = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Date v) { this.generatedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("manualimplId", manualimplId)
            .append("manualimplNo", manualimplNo)
            .append("title", title)
            .append("deployMode", deployMode)
            .append("status", status)
            .toString();
    }
}
