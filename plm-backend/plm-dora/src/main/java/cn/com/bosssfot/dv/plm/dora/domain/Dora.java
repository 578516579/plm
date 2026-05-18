package cn.com.bosssfot.dv.plm.dora.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * DORA效能指标对象 tb_dora
 *
 * 关联：
 * - 编号规则: DOR-YYYY-NNNN
 * - 状态机: 00=草稿 → 01=已生成 (终态)
 * - DORA四大指标: 部署频率/变更前置时间/变更失败率/平均恢复时间
 */
public class Dora extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** DORA ID */
    private Long doraId;

    /** DORA编号（DOR-YYYY-NNNN） */
    @Excel(name = "DORA编号")
    private String doraNo;

    /** 所属项目 ID */
    @Excel(name = "项目ID")
    private Long projectId;

    /** 统计周期（YYYY-MM 或 YYYY-QN） */
    @Excel(name = "统计周期")
    private String period;

    /** 部署频率（次/天） */
    @Excel(name = "部署频率(次/天)")
    private BigDecimal deployFrequency;

    /** 变更前置时间（小时） */
    @Excel(name = "变更前置时间(小时)")
    private BigDecimal leadTimeHours;

    /** 变更失败率(%) */
    @Excel(name = "变更失败率(%)")
    private BigDecimal changeFailureRate;

    /** 平均恢复时间（小时） */
    @Excel(name = "平均恢复时间(小时)")
    private BigDecimal mttrHours;

    /** DORA等级（字典 biz_dora_level: elite/high/medium/low） */
    @Excel(name = "DORA等级", dictType = "biz_dora_level")
    private String doraLevel;

    /** AI改进建议 */
    private String aiSuggestions;

    /** 是否AI生成（Y/N） */
    private String aiGenerated;

    /** AI生成时间 */
    private Date aiGeneratedAt;

    /** 状态（字典 biz_dora_status: 00=草稿,01=已生成） */
    @Excel(name = "状态", dictType = "biz_dora_status")
    private String status;

    /** 作者用户ID */
    private Long authorUserId;

    /** 删除标志（0=正常 2=删除） */
    private String delFlag;

    public void setDoraId(Long doraId) { this.doraId = doraId; }
    public Long getDoraId() { return doraId; }

    public void setDoraNo(String doraNo) { this.doraNo = doraNo; }
    public String getDoraNo() { return doraNo; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProjectId() { return projectId; }

    public void setPeriod(String period) { this.period = period; }
    public String getPeriod() { return period; }

    public void setDeployFrequency(BigDecimal deployFrequency) { this.deployFrequency = deployFrequency; }
    public BigDecimal getDeployFrequency() { return deployFrequency; }

    public void setLeadTimeHours(BigDecimal leadTimeHours) { this.leadTimeHours = leadTimeHours; }
    public BigDecimal getLeadTimeHours() { return leadTimeHours; }

    public void setChangeFailureRate(BigDecimal changeFailureRate) { this.changeFailureRate = changeFailureRate; }
    public BigDecimal getChangeFailureRate() { return changeFailureRate; }

    public void setMttrHours(BigDecimal mttrHours) { this.mttrHours = mttrHours; }
    public BigDecimal getMttrHours() { return mttrHours; }

    public void setDoraLevel(String doraLevel) { this.doraLevel = doraLevel; }
    public String getDoraLevel() { return doraLevel; }

    public void setAiSuggestions(String aiSuggestions) { this.aiSuggestions = aiSuggestions; }
    public String getAiSuggestions() { return aiSuggestions; }

    public void setAiGenerated(String aiGenerated) { this.aiGenerated = aiGenerated; }
    public String getAiGenerated() { return aiGenerated; }

    public void setAiGeneratedAt(Date aiGeneratedAt) { this.aiGeneratedAt = aiGeneratedAt; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }
    public Long getAuthorUserId() { return authorUserId; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("doraId", getDoraId())
            .append("doraNo", getDoraNo())
            .append("projectId", getProjectId())
            .append("period", getPeriod())
            .append("deployFrequency", getDeployFrequency())
            .append("leadTimeHours", getLeadTimeHours())
            .append("changeFailureRate", getChangeFailureRate())
            .append("mttrHours", getMttrHours())
            .append("doraLevel", getDoraLevel())
            .append("aiGenerated", getAiGenerated())
            .append("aiGeneratedAt", getAiGeneratedAt())
            .append("status", getStatus())
            .append("authorUserId", getAuthorUserId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
