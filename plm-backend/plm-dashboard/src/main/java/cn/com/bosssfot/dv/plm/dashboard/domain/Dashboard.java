package cn.com.bosssfot.dv.plm.dashboard.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 工作台预设 tb_dashboard — UI §4.2 + 原型 dashboard.html
 * 用户自定义工作台 (widget 布局); 聚合查询独立暴露 @ /business/dashboard/aggregate
 */
public class Dashboard extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long dashboardId;
    @Excel(name = "工作台编号") private String dashboardNo;
    @Excel(name = "工作台名称") private String title;
    @Excel(name = "所属用户")   private Long ownerUserId;
    private String layoutJson;
    @Excel(name = "启用Widget") private String widgetTypes;
    @Excel(name = "刷新间隔(s)") private Integer refreshInterval;
    @Excel(name = "默认工作台") private String isDefault;
    @Excel(name = "状态")       private String status;
    private String delFlag;

    public Long getDashboardId() { return dashboardId; }
    public void setDashboardId(Long v) { this.dashboardId = v; }
    public String getDashboardNo() { return dashboardNo; }
    public void setDashboardNo(String v) { this.dashboardNo = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long v) { this.ownerUserId = v; }
    public String getLayoutJson() { return layoutJson; }
    public void setLayoutJson(String v) { this.layoutJson = v; }
    public String getWidgetTypes() { return widgetTypes; }
    public void setWidgetTypes(String v) { this.widgetTypes = v; }
    public Integer getRefreshInterval() { return refreshInterval; }
    public void setRefreshInterval(Integer v) { this.refreshInterval = v; }
    public String getIsDefault() { return isDefault; }
    public void setIsDefault(String v) { this.isDefault = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("dashboardId", dashboardId)
            .append("dashboardNo", dashboardNo)
            .append("title", title)
            .append("ownerUserId", ownerUserId)
            .append("isDefault", isDefault)
            .toString();
    }
}
