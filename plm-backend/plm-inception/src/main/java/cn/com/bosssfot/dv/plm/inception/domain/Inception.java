package cn.com.bosssfot.dv.plm.inception.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 项目立项对象 tb_inception — PRD §F1.1 + 原型 inception.html
 * AI 辅助生成立项建议书 + 风险识别
 * 5×5 状态机 (含反向边 04→00):
 *   00 草稿 → 01 已提交 → 02 审批中 → 03 已批准 / 04 已驳回 → 00 (打回重写)
 */
public class Inception extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long inceptionId;
    @Excel(name = "立项编号") private String inceptionNo;
    @Excel(name = "项目名称") private String projectName;
    @Excel(name = "业务线") private String businessLine;
    @Excel(name = "项目类型") private String inceptionType;
    private String background;
    @Excel(name = "预计工期(月)") private Integer estimatedDurationMonths;
    @Excel(name = "团队规模") private String estimatedTeam;
    private String aiGenerated;
    private String aiProposalContent;
    private String aiRisks;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private String rejectReason;
    private Long submitterUserId;
    private Long approverUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date approvedAt;
    private Long projectId;
    private String delFlag;

    public Long getInceptionId() { return inceptionId; }
    public void setInceptionId(Long v) { this.inceptionId = v; }
    public String getInceptionNo() { return inceptionNo; }
    public void setInceptionNo(String v) { this.inceptionNo = v; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String v) { this.projectName = v; }
    public String getBusinessLine() { return businessLine; }
    public void setBusinessLine(String v) { this.businessLine = v; }
    public String getInceptionType() { return inceptionType; }
    public void setInceptionType(String v) { this.inceptionType = v; }
    public String getBackground() { return background; }
    public void setBackground(String v) { this.background = v; }
    public Integer getEstimatedDurationMonths() { return estimatedDurationMonths; }
    public void setEstimatedDurationMonths(Integer v) { this.estimatedDurationMonths = v; }
    public String getEstimatedTeam() { return estimatedTeam; }
    public void setEstimatedTeam(String v) { this.estimatedTeam = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public String getAiProposalContent() { return aiProposalContent; }
    public void setAiProposalContent(String v) { this.aiProposalContent = v; }
    public String getAiRisks() { return aiRisks; }
    public void setAiRisks(String v) { this.aiRisks = v; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date v) { this.aiGeneratedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String v) { this.rejectReason = v; }
    public Long getSubmitterUserId() { return submitterUserId; }
    public void setSubmitterUserId(Long v) { this.submitterUserId = v; }
    public Long getApproverUserId() { return approverUserId; }
    public void setApproverUserId(Long v) { this.approverUserId = v; }
    public Date getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Date v) { this.approvedAt = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("inceptionId", inceptionId)
            .append("inceptionNo", inceptionNo)
            .append("projectName", projectName)
            .append("businessLine", businessLine)
            .append("status", status)
            .toString();
    }
}
