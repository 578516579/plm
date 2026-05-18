package cn.com.bosssfot.dv.plm.pipeline.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * CI/CD流水线对象 tb_pipeline
 *
 * 关联：
 * - 编号规则: PIP-YYYY-NNNN
 * - 状态机: 00=空闲 ↔ 01=运行中, 00↔02=已暂停, 01/02→03=已停用(终态)
 */
public class Pipeline extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流水线 ID */
    private Long pipelineId;

    /** 流水线编号（PIP-YYYY-NNNN） */
    @Excel(name = "流水线编号")
    private String pipelineNo;

    /** 所属项目 ID */
    @Excel(name = "项目ID")
    private Long projectId;

    /** 流水线名称 */
    @Excel(name = "流水线名称")
    private String pipelineName;

    /** 仓库（字典 biz_pipeline_repo: backend/frontend/ai_service/infra） */
    @Excel(name = "仓库", dictType = "biz_pipeline_repo")
    private String repository;

    /** 分支 */
    @Excel(name = "分支")
    private String branch;

    /** 触发方式（字典 biz_pipeline_trigger: push/schedule/manual/mr） */
    @Excel(name = "触发方式", dictType = "biz_pipeline_trigger")
    private String triggerType;

    /** 阶段列表（JSON 数组） */
    private String stages;

    /** 最后运行状态（字典 biz_pipeline_run: success/failed/running/cancelled） */
    @Excel(name = "最后运行状态", dictType = "biz_pipeline_run")
    private String lastRunStatus;

    /** 最后运行时间 */
    private Date lastRunAt;

    /** 最后运行时长（如 3m20s） */
    private String lastRunDuration;

    /** 成功次数 */
    @Excel(name = "成功次数")
    private Integer successCount;

    /** 失败次数 */
    @Excel(name = "失败次数")
    private Integer failedCount;

    /** 成功率(0-100) */
    @Excel(name = "成功率(%)")
    private BigDecimal successRate;

    /** 状态（字典 biz_pipeline_status: 00=空闲,01=运行中,02=已暂停,03=已停用） */
    @Excel(name = "状态", dictType = "biz_pipeline_status")
    private String status;

    /** 作者用户ID */
    private Long authorUserId;

    /** 删除标志（0=正常 2=删除） */
    private String delFlag;

    public void setPipelineId(Long pipelineId) { this.pipelineId = pipelineId; }
    public Long getPipelineId() { return pipelineId; }

    public void setPipelineNo(String pipelineNo) { this.pipelineNo = pipelineNo; }
    public String getPipelineNo() { return pipelineNo; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProjectId() { return projectId; }

    public void setPipelineName(String pipelineName) { this.pipelineName = pipelineName; }
    public String getPipelineName() { return pipelineName; }

    public void setRepository(String repository) { this.repository = repository; }
    public String getRepository() { return repository; }

    public void setBranch(String branch) { this.branch = branch; }
    public String getBranch() { return branch; }

    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public String getTriggerType() { return triggerType; }

    public void setStages(String stages) { this.stages = stages; }
    public String getStages() { return stages; }

    public void setLastRunStatus(String lastRunStatus) { this.lastRunStatus = lastRunStatus; }
    public String getLastRunStatus() { return lastRunStatus; }

    public void setLastRunAt(Date lastRunAt) { this.lastRunAt = lastRunAt; }
    public Date getLastRunAt() { return lastRunAt; }

    public void setLastRunDuration(String lastRunDuration) { this.lastRunDuration = lastRunDuration; }
    public String getLastRunDuration() { return lastRunDuration; }

    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public Integer getSuccessCount() { return successCount; }

    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    public Integer getFailedCount() { return failedCount; }

    public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }
    public BigDecimal getSuccessRate() { return successRate; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }
    public Long getAuthorUserId() { return authorUserId; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("pipelineId", getPipelineId())
            .append("pipelineNo", getPipelineNo())
            .append("projectId", getProjectId())
            .append("pipelineName", getPipelineName())
            .append("repository", getRepository())
            .append("branch", getBranch())
            .append("triggerType", getTriggerType())
            .append("lastRunStatus", getLastRunStatus())
            .append("lastRunAt", getLastRunAt())
            .append("successCount", getSuccessCount())
            .append("failedCount", getFailedCount())
            .append("successRate", getSuccessRate())
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
