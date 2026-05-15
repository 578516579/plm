package cn.com.bosssfot.dv.plm.system.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 项目对象 tb_project
 */
public class Project extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long id;

    /** 项目编号 */
    @Excel(name = "项目编号")
    private String projectNo;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String projectName;

    /** 项目类型（字典 biz_project_type） */
    @Excel(name = "项目类型", dictType = "biz_project_type")
    private String projectType;

    /** 状态（字典 biz_project_status） */
    @Excel(name = "状态", dictType = "biz_project_status")
    private String status;

    /** 负责人用户ID（关联 sys_user.user_id） */
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

    /** 预算 */
    @Excel(name = "预算")
    private BigDecimal budget;

    /** 描述 */
    private String description;

    /** 删除标志（0=正常, 2=删除） */
    private String delFlag;

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    public void setProjectNo(String projectNo) { this.projectNo = projectNo; }
    public String getProjectNo() { return projectNo; }

    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectName() { return projectName; }

    public void setProjectType(String projectType) { this.projectType = projectType; }
    public String getProjectType() { return projectType; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setManagerUserId(Long managerUserId) { this.managerUserId = managerUserId; }
    public Long getManagerUserId() { return managerUserId; }

    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getStartDate() { return startDate; }

    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Date getEndDate() { return endDate; }

    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public BigDecimal getBudget() { return budget; }

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
            .append("projectType", getProjectType())
            .append("status", getStatus())
            .append("managerUserId", getManagerUserId())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("budget", getBudget())
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
