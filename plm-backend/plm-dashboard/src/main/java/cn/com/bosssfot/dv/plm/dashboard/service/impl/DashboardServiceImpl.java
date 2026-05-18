package cn.com.bosssfot.dv.plm.dashboard.service.impl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.dashboard.domain.Dashboard;
import cn.com.bosssfot.dv.plm.dashboard.mapper.DashboardMapper;
import cn.com.bosssfot.dv.plm.dashboard.service.IDashboardService;

/**
 * 工作台配置 Service 实现
 *
 * 落地:
 * - generateDashboardNo() — DSB-YYYY-NNNN
 * - widgetType 白名单: chart/card/table/link
 * - 状态机: 00→{01}, 01→{00}（可撤回草稿）
 */
@Service
public class DashboardServiceImpl implements IDashboardService
{
    private static final Set<String> ALLOWED_WIDGET_TYPE =
        Set.of("chart", "card", "table", "link");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("00"));
    }

    @Autowired private DashboardMapper dashboardMapper;

    @Override
    public List<Dashboard> selectDashboardList(Dashboard dashboard) {
        return dashboardMapper.selectDashboardList(dashboard);
    }

    @Override
    public Dashboard selectDashboardById(Long dashboardId) {
        return dashboardMapper.selectDashboardById(dashboardId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDashboard(Dashboard dashboard) {
        if (StringUtils.isBlank(dashboard.getWidgetName())) {
            throw new ServiceException("看板名称不能为空", 602);
        }
        if (StringUtils.isNotBlank(dashboard.getWidgetType())
                && !ALLOWED_WIDGET_TYPE.contains(dashboard.getWidgetType())) {
            throw new ServiceException("组件类型仅支持 chart/card/table/link", 604);
        }
        if (StringUtils.isBlank(dashboard.getVisible())) dashboard.setVisible("Y");
        if (StringUtils.isBlank(dashboard.getStatus())) {
            dashboard.setStatus("00");
        } else if (!"00".equals(dashboard.getStatus())) {
            throw new ServiceException("新建工作台配置状态必须为「草稿」", 601);
        }
        if (StringUtils.isBlank(dashboard.getDashboardNo())) {
            dashboard.setDashboardNo(generateDashboardNo());
        }
        dashboard.setCreateBy(SecurityUtils.getUsername());
        return dashboardMapper.insertDashboard(dashboard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDashboard(Dashboard dashboard) {
        Dashboard old = dashboardMapper.selectDashboardById(dashboard.getDashboardId());
        if (old == null) {
            throw new ServiceException("工作台配置不存在", 404);
        }
        if (StringUtils.isNotBlank(dashboard.getStatus()) && !dashboard.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(dashboard.getStatus())) {
                throw new ServiceException(
                    "工作台配置状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(dashboard.getStatus()),
                    601
                );
            }
        }
        if (StringUtils.isNotBlank(dashboard.getWidgetType())
                && !ALLOWED_WIDGET_TYPE.contains(dashboard.getWidgetType())) {
            throw new ServiceException("组件类型仅支持 chart/card/table/link", 604);
        }
        dashboard.setUpdateBy(SecurityUtils.getUsername());
        return dashboardMapper.updateDashboard(dashboard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDashboardByIds(Long[] dashboardIds) {
        return dashboardMapper.deleteDashboardByIds(dashboardIds);
    }

    private String generateDashboardNo() {
        int year = LocalDate.now().getYear();
        String prefix = "DSB-" + year + "-";
        Integer maxSeq = dashboardMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "已发布";
            default:   return "未知(" + status + ")";
        }
    }
}
