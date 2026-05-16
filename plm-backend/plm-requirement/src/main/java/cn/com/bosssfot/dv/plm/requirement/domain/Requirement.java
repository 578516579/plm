package cn.com.bosssfot.dv.plm.requirement.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 需求对象 tb_requirement
 *
 * 关联：
 * - PRD §3.1 字段定义（01-立项/Requirement-PRD.md）
 * - ADR-0002：requirement_no 编号规则 REQ-YYYY-NNNN
 */
public class Requirement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 需求 ID */
    private Long requirementId;

    /** 需求编号（REQ-YYYY-NNNN） */
    @Excel(name = "需求编号")
    private String requirementNo;

    /** 所属项目 ID（FK→tb_project.id） */
    @Excel(name = "项目ID")
    private Long projectId;

    /** 需求标题 */
    @Excel(name = "需求标题")
    private String title;

    /** 详细描述（Markdown） */
    private String description;

    /** 需求来源（字典 biz_req_source） */
    @Excel(name = "需求来源", dictType = "biz_req_source")
    private String source;

    /** 优先级（字典 biz_req_priority） */
    @Excel(name = "优先级", dictType = "biz_req_priority")
    private String priority;

    /** 状态（字典 biz_req_status） */
    @Excel(name = "状态", dictType = "biz_req_status")
    private String status;

    /** 指派给的用户 ID（FK→sys_user.user_id） */
    @Excel(name = "指派用户ID")
    private Long assigneeUserId;

    /** 评审简要纪要（状态推进时填） */
    private String reviewNote;

    /** 删除标志（0=正常, 2=删除） */
    private String delFlag;

    public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }
    public Long getRequirementId() { return requirementId; }

    public void setRequirementNo(String requirementNo) { this.requirementNo = requirementNo; }
    public String getRequirementNo() { return requirementNo; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProjectId() { return projectId; }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }

    public void setSource(String source) { this.source = source; }
    public String getSource() { return source; }

    public void setPriority(String priority) { this.priority = priority; }
    public String getPriority() { return priority; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public Long getAssigneeUserId() { return assigneeUserId; }

    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public String getReviewNote() { return reviewNote; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("requirementId", getRequirementId())
            .append("requirementNo", getRequirementNo())
            .append("projectId", getProjectId())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("source", getSource())
            .append("priority", getPriority())
            .append("status", getStatus())
            .append("assigneeUserId", getAssigneeUserId())
            .append("reviewNote", getReviewNote())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
