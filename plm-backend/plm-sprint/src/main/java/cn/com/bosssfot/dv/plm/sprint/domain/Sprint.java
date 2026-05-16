package cn.com.bosssfot.dv.plm.sprint.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 迭代对象 tb_sprint
 *
 * 关联：
 * - PRD §3.1 字段定义（01-立项/Sprint-PRD.md）
 * - ADR-0004：sprint_no 编号规则 SPR-YYYY-NNNN
 * - 业务硬规则 703：项目级单一活跃迭代约束（同一 project_id 下 status='01' 唯一）
 */
public class Sprint extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 迭代 ID */
    private Long sprintId;

    /** 迭代编号（SPR-YYYY-NNNN） */
    @Excel(name = "迭代编号")
    private String sprintNo;

    /** 所属项目 ID */
    @Excel(name = "项目ID")
    private Long projectId;

    /** 迭代名称 */
    @Excel(name = "迭代名称")
    private String name;

    /** 迭代目标（一句话） */
    @Excel(name = "迭代目标")
    private String goal;

    /** 状态（字典 biz_sprint_status） */
    @Excel(name = "状态", dictType = "biz_sprint_status")
    private String status;

    /** 计划开始日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划开始", width = 30, dateFormat = "yyyy-MM-dd")
    private Date plannedStartDate;

    /** 计划结束日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划结束", width = 30, dateFormat = "yyyy-MM-dd")
    private Date plannedEndDate;

    /** 实际开始（00→01 时自动填） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际开始", width = 30, dateFormat = "yyyy-MM-dd")
    private Date actualStartDate;

    /** 实际结束（01→02 时自动填） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际结束", width = 30, dateFormat = "yyyy-MM-dd")
    private Date actualEndDate;

    /** 周期天数（默认 14） */
    @Excel(name = "周期天数")
    private Integer durationDays;

    /** 删除标志（0=正常, 2=删除） */
    private String delFlag;

    public void setSprintId(Long sprintId) { this.sprintId = sprintId; }
    public Long getSprintId() { return sprintId; }

    public void setSprintNo(String sprintNo) { this.sprintNo = sprintNo; }
    public String getSprintNo() { return sprintNo; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProjectId() { return projectId; }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setGoal(String goal) { this.goal = goal; }
    public String getGoal() { return goal; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setPlannedStartDate(Date plannedStartDate) { this.plannedStartDate = plannedStartDate; }
    public Date getPlannedStartDate() { return plannedStartDate; }

    public void setPlannedEndDate(Date plannedEndDate) { this.plannedEndDate = plannedEndDate; }
    public Date getPlannedEndDate() { return plannedEndDate; }

    public void setActualStartDate(Date actualStartDate) { this.actualStartDate = actualStartDate; }
    public Date getActualStartDate() { return actualStartDate; }

    public void setActualEndDate(Date actualEndDate) { this.actualEndDate = actualEndDate; }
    public Date getActualEndDate() { return actualEndDate; }

    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Integer getDurationDays() { return durationDays; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("sprintId", getSprintId())
            .append("sprintNo", getSprintNo())
            .append("projectId", getProjectId())
            .append("name", getName())
            .append("goal", getGoal())
            .append("status", getStatus())
            .append("plannedStartDate", getPlannedStartDate())
            .append("plannedEndDate", getPlannedEndDate())
            .append("actualStartDate", getActualStartDate())
            .append("actualEndDate", getActualEndDate())
            .append("durationDays", getDurationDays())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
