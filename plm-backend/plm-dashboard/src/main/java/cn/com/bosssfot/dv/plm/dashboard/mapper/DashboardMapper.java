package cn.com.bosssfot.dv.plm.dashboard.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.dashboard.domain.Dashboard;

/**
 * 工作台配置 Mapper 接口
 */
public interface DashboardMapper
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
     * 修改工作台配置
     */
    int updateDashboard(Dashboard dashboard);

    /**
     * 软删除工作台配置（设 del_flag='2'）
     */
    int deleteDashboardByIds(Long[] dashboardIds);

    /**
     * 查询指定年份前缀的最大序号（用于编号生成）
     */
    Integer selectMaxSeqOfYear(String prefix);
}
