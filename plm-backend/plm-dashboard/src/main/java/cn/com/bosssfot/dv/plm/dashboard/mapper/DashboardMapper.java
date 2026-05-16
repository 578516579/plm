package cn.com.bosssfot.dv.plm.dashboard.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.dashboard.domain.Dashboard;

public interface DashboardMapper {
    List<Dashboard> selectDashboardList(Dashboard dashboard);
    Dashboard selectDashboardById(Long dashboardId);
    int insertDashboard(Dashboard dashboard);
    int updateDashboard(Dashboard dashboard);
    int deleteDashboardByIds(Long[] dashboardIds);
    /** 取消同一用户的其他 default 标记 */
    int clearDefaultForOwner(Long ownerUserId);
    Integer selectMaxSeqOfYear(String prefix);
}
