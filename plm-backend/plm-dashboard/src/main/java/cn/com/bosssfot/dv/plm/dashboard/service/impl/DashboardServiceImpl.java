package cn.com.bosssfot.dv.plm.dashboard.service.impl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.dashboard.domain.Dashboard;
import cn.com.bosssfot.dv.plm.dashboard.mapper.DashboardMapper;
import cn.com.bosssfot.dv.plm.dashboard.service.IDashboardService;

/**
 * 工作台 Service — UI §4.2 + 原型 dashboard.html
 *
 * 落地:
 * - generateDashboardNo() — DASH-YYYY-NNNN
 * - CRUD 用户工作台预设 + is_default 唯一性 (同 user 只允许一个 default)
 * - aggregate() 返回 6 类 widget 聚合数据 (本期 mock,后续接真实跨模块查询)
 */
@Service
public class DashboardServiceImpl implements IDashboardService {
    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired private DashboardMapper dashboardMapper;

    @Override
    public List<Dashboard> selectDashboardList(Dashboard t) {
        return dashboardMapper.selectDashboardList(t);
    }

    @Override
    public Dashboard selectDashboardById(Long id) {
        return dashboardMapper.selectDashboardById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDashboard(Dashboard t) {
        if (StringUtils.isBlank(t.getTitle()))     throw new ServiceException("工作台名称不能为空", 602);
        if (t.getOwnerUserId() == null)            throw new ServiceException("所属用户不能为空", 602);

        if (StringUtils.isBlank(t.getIsDefault()))       t.setIsDefault("N");
        if (StringUtils.isBlank(t.getStatus()))          t.setStatus("00");
        if (t.getRefreshInterval() == null)              t.setRefreshInterval(60);
        if (StringUtils.isBlank(t.getWidgetTypes()))     t.setWidgetTypes("stats,active_projects,my_todos");

        // 设为默认时取消同用户其他默认
        if ("Y".equals(t.getIsDefault())) {
            dashboardMapper.clearDefaultForOwner(t.getOwnerUserId());
        }

        if (StringUtils.isBlank(t.getDashboardNo())) t.setDashboardNo(generateDashboardNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return dashboardMapper.insertDashboard(t);
        } catch (DuplicateKeyException e) {
            log.warn("dashboard_no 重号,重试一次: {}", t.getDashboardNo());
            t.setDashboardNo(generateDashboardNo());
            return dashboardMapper.insertDashboard(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDashboard(Dashboard t) {
        Dashboard old = dashboardMapper.selectDashboardById(t.getDashboardId());
        if (old == null) throw new ServiceException("工作台不存在", 404);

        // 切换为默认时清掉同用户其他默认
        if ("Y".equals(t.getIsDefault()) && !"Y".equals(old.getIsDefault())) {
            dashboardMapper.clearDefaultForOwner(old.getOwnerUserId());
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        return dashboardMapper.updateDashboard(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDashboardByIds(Long[] ids) {
        return dashboardMapper.deleteDashboardByIds(ids);
    }

    @Override
    public Map<String, Object> aggregate(Long ownerUserId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // widget: stats — 4 大顶部卡片 (mock)
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("activeProjects", 7);
        stats.put("aiDocsGenerated", 142);
        stats.put("currentDefects", 18);
        stats.put("autoTestCoverage", 76.0);
        result.put("stats", stats);

        // widget: active_projects (mock)
        List<Map<String, Object>> projects = List.of(
            project("AgriPLM 大屏 v2", 82, "success"),
            project("智慧灌溉系统 v1.3", 65, "primary"),
            project("农资电商小程序", 48, "warning")
        );
        result.put("activeProjects", projects);

        // widget: my_todos (mock)
        List<Map<String, Object>> todos = List.of(
            todo("PRD 评审：农情大屏 v2", "P0", "2026-05-19"),
            todo("提测包验收：智慧灌溉", "P1", "2026-05-20"),
            todo("缺陷复测：DEF-2026-0042", "P1", "2026-05-21")
        );
        result.put("myTodos", todos);

        // widget: quality_snapshot (mock 当迭代)
        Map<String, Object> quality = new LinkedHashMap<>();
        quality.put("defectCount", 12);
        quality.put("testPassRate", 91.5);
        quality.put("codeCoverage", 78.0);
        result.put("qualitySnapshot", quality);

        // widget: ai_metrics (mock)
        Map<String, Object> ai = new LinkedHashMap<>();
        ai.put("hoursSaved", 284);
        ai.put("docsGenerated", 142);
        ai.put("recommendations", List.of(
            "缺陷收敛率本周提升 6%,可复用经验",
            "Sprint 燃尽偏差预警 →AgriPLM 大屏 v2"
        ));
        result.put("aiMetrics", ai);

        // widget: lifecycle (17 阶段静态)
        result.put("lifecycle", List.of(
            "立项","竞品","需求","PRD","UED","概要设计","数据库","接口",
            "编码","测试方案","用例","提测","自动化","报告","手册","实施","运维"
        ));

        result.put("ownerUserId", ownerUserId);
        return result;
    }

    private Map<String, Object> project(String name, int progress, String color) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("progress", progress);
        m.put("color", color);
        return m;
    }

    private Map<String, Object> todo(String title, String priority, String dueDate) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("priority", priority);
        m.put("dueDate", dueDate);
        return m;
    }

    private String generateDashboardNo() {
        int year = LocalDate.now().getYear();
        String prefix = "DASH-" + year + "-";
        Integer maxSeq = dashboardMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }
}
