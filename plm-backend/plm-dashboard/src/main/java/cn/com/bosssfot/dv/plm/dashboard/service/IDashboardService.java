package cn.com.bosssfot.dv.plm.dashboard.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.dashboard.domain.Dashboard;

/**
 * 工作台配置 Service 接口
 */
public interface IDashboardService
{
    /**
     * 查询工作台配置列表
     */
    List<Dashboard> selectDashboardList(Dashboard dashboard);

    /**
     * 按主键查询工作台配置
     */
    Dashboard selectDashboardById(Long dashboardId);

    /**
     * 新增工作台配置
     */
    int insertDashboard(Dashboard dashboard);

    /**
     * 修改工作台配置（含状态流转校验）
     */
    int updateDashboard(Dashboard dashboard);

    /**
     * 批量删除工作台配置
     */
    int deleteDashboardByIds(Long[] dashboardIds);
}
