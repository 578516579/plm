package cn.com.bosssfot.dv.plm.project.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 项目对象 tb_project
 *
 * 字段对照表:PRD-MAPPING.md §2 "Project (F1.2)"
 * v2 (2026-05-17) PRD-align:加 businessLine/priority/lifecyclePhase/progress/health,删 budget
 */
public class Project extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long id;

    /** 项目编号 PRJ-YYYY-NNNN (ADR-0001) */
    @Excel(name = "项目编号")
    private String projectNo;

    /** 项目名称 (原型 np-name) */
    @Excel(name = "项目名称")
    private String projectName;

    /** 业务线 (字典 biz_project_business_line,PRD §F1.2 必填) */
    @Excel(name = "业务线", dictType = "biz_project_business_line")
    private String businessLine;

    /** 项目类型 (字典 biz_project_type,PRD §F1.2 "类型") */
    @Excel(name = "项目类型", dictType = "biz_project_type")
    private String projectType;

    /** 优先级 (字典 biz_project_priority,PRD §F1.2 "优先级") */
    @Excel(name = "优先级", dictType = "biz_project_priority")
    private String priority;

    /** 交付阶段 (字典 biz_project_phase,原型列"阶段") */
    @Excel(name = "阶段", dictType = "biz_project_phase")
    private String lifecyclePhase;

    /** 总状态 (字典 biz_project_status) */
    @Excel(name = "状态", dictType = "biz_project_status")
    private String status;

    /** 进度 0-100 (原型列"进度") */
    @Excel(name = "进度")
    private Integer progress;

    /** 健康度 (字典 biz_project_health,PRD §F1.2 三色预警) */
    @Excel(name = "健康度", dictType = "biz_project_health")
    private String health;

    /** 负责人 user_id (关联 sys_user.user_id,PRD-MAPPING §2 D1) */
    @Excel(name = "负责人ID")
    private Long managerUserId;

    /** 起始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "起始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 项目描述 (扩展字段,PRD-MAPPING §2 D3) */
    private String description;

    /** 删除标志(0=正常, 2=删除) */
    private String delFlag;

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    public void setProjectNo(String projectNo) { this.projectNo = projectNo; }
    public String getProjectNo() { return projectNo; }

    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectName() { return projectName; }

    public void setBusinessLine(String businessLine) { this.businessLine = businessLine; }
    public String getBusinessLine() { return businessLine; }

    public void setProjectType(String projectType) { this.projectType = projectType; }
    public String getProjectType() { return projectType; }

    public void setPriority(String priority) { this.priority = priority; }
    public String getPriority() { return priority; }

    public void setLifecyclePhase(String lifecyclePhase) { this.lifecyclePhase = lifecyclePhase; }
    public String getLifecyclePhase() { return lifecyclePhase; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setProgress(Integer progress) { this.progress = progress; }
    public Integer getProgress() { return progress; }

    public void setHealth(String health) { this.health = health; }
    public String getHealth() { return health; }

    public void setManagerUserId(Long managerUserId) { this.managerUserId = managerUserId; }
    public Long getManagerUserId() { return managerUserId; }

    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getStartDate() { return startDate; }

    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Date getEndDate() { return endDate; }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("projectNo", getProjectNo())
            .append("projectName", getProjectName())
            .append("businessLine", getBusinessLine())
            .append("projectType", getProjectType())
            .append("priority", getPriority())
            .append("lifecyclePhase", getLifecyclePhase())
            .append("status", getStatus())
            .append("progress", getProgress())
            .append("health", getHealth())
            .append("managerUserId", getManagerUserId())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("description", getDescription())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
