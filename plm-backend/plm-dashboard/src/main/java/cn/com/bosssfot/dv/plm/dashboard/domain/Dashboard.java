package cn.com.bosssfot.dv.plm.dashboard.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 工作台配置对象 tb_dashboard
 *
 * 状态机: 00=草稿 → 01=已发布, 01 → 00（可撤回）
 * 小组件类型: chart/card/table/link
 */
public class Dashboard extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long dashboardId;
    @Excel(name = "看板编号") private String dashboardNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "看板名称") private String widgetName;
    @Excel(name = "组件类型") private String widgetType;
    @Excel(name = "数据源标识") private String dataSource;
    private String config;
    @Excel(name = "排序") private Integer sortOrder;
    @Excel(name = "是否可见") private String visible;
    private Long userId;
    @Excel(name = "状态") private String status;
    private String delFlag;

    public Long getDashboardId() { return dashboardId; }
    public void setDashboardId(Long v) { this.dashboardId = v; }
    public String getDashboardNo() { return dashboardNo; }
    public void setDashboardNo(String v) { this.dashboardNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getWidgetName() { return widgetName; }
    public void setWidgetName(String v) { this.widgetName = v; }
    public String getWidgetType() { return widgetType; }
    public void setWidgetType(String v) { this.widgetType = v; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String v) { this.dataSource = v; }
    public String getConfig() { return config; }
    public void setConfig(String v) { this.config = v; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer v) { this.sortOrder = v; }
    public String getVisible() { return visible; }
    public void setVisible(String v) { this.visible = v; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("dashboardId", dashboardId)
            .append("dashboardNo", dashboardNo)
            .append("widgetName", widgetName)
            .append("widgetType", widgetType)
            .append("status", status)
            .toString();
    }
}
