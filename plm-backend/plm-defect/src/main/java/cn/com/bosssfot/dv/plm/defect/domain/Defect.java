package cn.com.bosssfot.dv.plm.defect.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 缺陷对象 tb_defect
 *
 * 关联：
 * - PRD §3.1 字段定义（01-立项/Defect-PRD.md）
 * - ADR-0005：defect_no 编号规则 DEFECT-YYYY-NNNN
 * - PRD §3.4 / API §2：5×5 状态机含反向边 03→01（回归打回）
 */
public class Defect extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 缺陷 ID */
    private Long defectId;

    /** 缺陷编号（DEFECT-YYYY-NNNN） */
    @Excel(name = "缺陷编号")
    private String defectNo;

    /** 所属项目 ID */
    @Excel(name = "项目ID")
    private Long projectId;

    /** 关联迭代 ID（可空） */
    @Excel(name = "迭代ID")
    private Long sprintId;

    /** 关联任务 ID（可空） */
    @Excel(name = "任务ID")
    private Long taskId;

    /** 缺陷标题 */
    @Excel(name = "标题")
    private String title;

    /** 详细描述 */
    private String description;

    /** 严重级别（字典 biz_defect_severity） */
    @Excel(name = "严重级别", dictType = "biz_defect_severity")
    private String severity;

    /** 缺陷分类（字典 biz_defect_category） */
    @Excel(name = "分类", dictType = "biz_defect_category")
    private String category;

    /** 状态（字典 biz_defect_status） */
    @Excel(name = "状态", dictType = "biz_defect_status")
    private String status;

    /** 指派开发 user_id */
    @Excel(name = "指派用户ID")
    private Long assigneeUserId;

    /** 报告人 user_id */
    @Excel(name = "报告人ID")
    private Long reporterUserId;

    /** 重现步骤 */
    private String reproduceSteps;

    /** 期望结果 */
    private String expectedResult;

    /** 实际结果 */
    private String actualResult;

    /** 解决说明（进入 03 时填） */
    @Excel(name = "解决说明")
    private String resolution;

    /** 标签 CSV */
    @Excel(name = "标签")
    private String tags;

    /** 删除标志（0=正常 2=删除） */
    private String delFlag;

    public void setDefectId(Long defectId) { this.defectId = defectId; }
    public Long getDefectId() { return defectId; }

    public void setDefectNo(String defectNo) { this.defectNo = defectNo; }
    public String getDefectNo() { return defectNo; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProjectId() { return projectId; }

    public void setSprintId(Long sprintId) { this.sprintId = sprintId; }
    public Long getSprintId() { return sprintId; }

    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getTaskId() { return taskId; }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }

    public void setSeverity(String severity) { this.severity = severity; }
    public String getSeverity() { return severity; }

    public void setCategory(String category) { this.category = category; }
    public String getCategory() { return category; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public Long getAssigneeUserId() { return assigneeUserId; }

    public void setReporterUserId(Long reporterUserId) { this.reporterUserId = reporterUserId; }
    public Long getReporterUserId() { return reporterUserId; }

    public void setReproduceSteps(String reproduceSteps) { this.reproduceSteps = reproduceSteps; }
    public String getReproduceSteps() { return reproduceSteps; }

    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }
    public String getExpectedResult() { return expectedResult; }

    public void setActualResult(String actualResult) { this.actualResult = actualResult; }
    public String getActualResult() { return actualResult; }

    public void setResolution(String resolution) { this.resolution = resolution; }
    public String getResolution() { return resolution; }

    public void setTags(String tags) { this.tags = tags; }
    public String getTags() { return tags; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("defectId", getDefectId())
            .append("defectNo", getDefectNo())
            .append("projectId", getProjectId())
            .append("sprintId", getSprintId())
            .append("taskId", getTaskId())
            .append("title", getTitle())
            .append("severity", getSeverity())
            .append("category", getCategory())
            .append("status", getStatus())
            .append("assigneeUserId", getAssigneeUserId())
            .append("reporterUserId", getReporterUserId())
            .append("resolution", getResolution())
            .append("tags", getTags())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
