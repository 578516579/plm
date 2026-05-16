package cn.com.bosssfot.dv.plm.manualops.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 运维手册对象 tb_manual_ops — PRD §F5.3 + 原型 opsmanual.html
 * AI 一键生成 + 监控方案/告警渠道/IoT 设备类型多选 + 多格式导出
 * 4 状态机: 00 草稿 → 01 生成中 → 02 已生成 → 03 已发布
 */
public class ManualOps extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long manualopsId;
    @Excel(name = "手册编号")    private String manualopsNo;
    @Excel(name = "项目ID")      private Long projectId;
    @Excel(name = "手册标题")    private String title;
    @Excel(name = "监控方案")    private String monitoringPlan;
    @Excel(name = "告警渠道")    private String alertChannels;
    @Excel(name = "IoT设备类型") private String iotDeviceTypes;
    private String content;
    @Excel(name = "导出格式")    private String outputFormats;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date generatedAt;
    @Excel(name = "状态")        private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getManualopsId() { return manualopsId; }
    public void setManualopsId(Long v) { this.manualopsId = v; }
    public String getManualopsNo() { return manualopsNo; }
    public void setManualopsNo(String v) { this.manualopsNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getMonitoringPlan() { return monitoringPlan; }
    public void setMonitoringPlan(String v) { this.monitoringPlan = v; }
    public String getAlertChannels() { return alertChannels; }
    public void setAlertChannels(String v) { this.alertChannels = v; }
    public String getIotDeviceTypes() { return iotDeviceTypes; }
    public void setIotDeviceTypes(String v) { this.iotDeviceTypes = v; }
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
            .append("manualopsId", manualopsId)
            .append("manualopsNo", manualopsNo)
            .append("title", title)
            .append("monitoringPlan", monitoringPlan)
            .append("status", status)
            .toString();
    }
}
