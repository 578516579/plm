package cn.com.bosssfot.dv.plm.dashboard.service;

import java.util.List;
import java.util.Map;
import cn.com.bosssfot.dv.plm.dashboard.domain.Dashboard;

public interface IDashboardService {
    List<Dashboard> selectDashboardList(Dashboard dashboard);
    Dashboard selectDashboardById(Long dashboardId);
    int insertDashboard(Dashboard dashboard);
    int updateDashboard(Dashboard dashboard);
    int deleteDashboardByIds(Long[] dashboardIds);
    /** UI §4.2: 工作台聚合查询 — 返回 6 类 widget 数据 (mock,后续接真实聚合) */
    Map<String, Object> aggregate(Long ownerUserId);
}
