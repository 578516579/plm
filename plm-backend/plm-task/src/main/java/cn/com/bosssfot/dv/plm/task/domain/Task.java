package cn.com.bosssfot.dv.plm.task.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 任务对象 tb_task
 *
 * 关联：
 * - PRD §3.1 字段定义（01-立项/Task-PRD.md）
 * - ADR-0003：task_no 编号规则 TASK-YYYY-NNNN
 * - PRD §3.3 / API §2.4：6×6 状态机，含 02 ↔ 01 反向边（评审打回）
 */
public class Task extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务 ID */
    private Long taskId;

    /** 任务编号（TASK-YYYY-NNNN） */
    @Excel(name = "任务编号")
    private String taskNo;

    /** 所属项目 ID */
    @Excel(name = "项目ID")
    private Long projectId;

    /** 关联需求 ID（可空） */
    @Excel(name = "需求ID")
    private Long requirementId;

    /** 关联迭代 ID（可空） */
    @Excel(name = "迭代ID")
    private Long sprintId;

    /** 任务标题 */
    @Excel(name = "任务标题")
    private String title;

    /** 详细描述 */
    private String description;

    /** 状态（字典 biz_task_status） */
    @Excel(name = "状态", dictType = "biz_task_status")
    private String status;

    /** 优先级（字典 biz_task_priority） */
    @Excel(name = "优先级", dictType = "biz_task_priority")
    private String priority;

    /** 负责人 ID */
    @Excel(name = "负责人ID")
    private Long assigneeUserId;

    /** 预估工时 */
    @Excel(name = "预估工时")
    private BigDecimal estimatedHours;

    /** 实际工时（完成时填） */
    @Excel(name = "实际工时")
    private BigDecimal actualHours;

    /** 关联 MR/PR 链接 */
    @Excel(name = "MR链接")
    private String mrUrl;

    /** MR 分支名 */
    @Excel(name = "MR分支")
    private String mrBranch;

    /** 删除标志（0=正常, 2=删除） */
    private String delFlag;

    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getTaskId() { return taskId; }

    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public String getTaskNo() { return taskNo; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProjectId() { return projectId; }

    public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }
    public Long getRequirementId() { return requirementId; }

    public void setSprintId(Long sprintId) { this.sprintId = sprintId; }
    public Long getSprintId() { return sprintId; }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setPriority(String priority) { this.priority = priority; }
    public String getPriority() { return priority; }

    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public Long getAssigneeUserId() { return assigneeUserId; }

    public void setEstimatedHours(BigDecimal estimatedHours) { this.estimatedHours = estimatedHours; }
    public BigDecimal getEstimatedHours() { return estimatedHours; }

    public void setActualHours(BigDecimal actualHours) { this.actualHours = actualHours; }
    public BigDecimal getActualHours() { return actualHours; }

    public void setMrUrl(String mrUrl) { this.mrUrl = mrUrl; }
    public String getMrUrl() { return mrUrl; }

    public void setMrBranch(String mrBranch) { this.mrBranch = mrBranch; }
    public String getMrBranch() { return mrBranch; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskNo", getTaskNo())
            .append("projectId", getProjectId())
            .append("requirementId", getRequirementId())
            .append("sprintId", getSprintId())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("priority", getPriority())
            .append("assigneeUserId", getAssigneeUserId())
            .append("estimatedHours", getEstimatedHours())
            .append("actualHours", getActualHours())
            .append("mrUrl", getMrUrl())
            .append("mrBranch", getMrBranch())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
