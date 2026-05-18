package cn.com.bosssfot.dv.plm.analytics.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.analytics.domain.Analytics;
import cn.com.bosssfot.dv.plm.analytics.mapper.AnalyticsMapper;
import cn.com.bosssfot.dv.plm.analytics.service.IAnalyticsService;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * 效能分析 Service 实现
 *
 * 落地:
 * - 编号规则: ANL-YYYY-NNNN
 * - PRD §4.6 状态机: 00=草稿 → 01=已生成 (终态)
 * - period 白名单: monthly/quarterly/yearly
 * - aiGenerate(): 设置 status='01', aiGenerated='Y', 填充 mock 指标
 */
@Service
public class AnalyticsServiceImpl implements IAnalyticsService
{
    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    /**
     * 状态机转换矩阵
     * 00=草稿 → 01=已生成 (终态)
     */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of());  // 终态
    }

    private static final List<String> VALID_PERIODS = Arrays.asList("monthly", "quarterly", "yearly");

    @Autowired private AnalyticsMapper analyticsMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<Analytics> selectAnalyticsList(Analytics analytics) {
        return analyticsMapper.selectAnalyticsList(analytics);
    }

    @Override
    public Analytics selectAnalyticsById(Long analyticsId) {
        return analyticsMapper.selectAnalyticsById(analyticsId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAnalytics(Analytics analytics)
    {
        if (StringUtils.isBlank(analytics.getTitle())) throw new ServiceException("分析标题不能为空", 602);
        if (analytics.getProjectId() == null) throw new ServiceException("关联项目不能为空", 602);
        if (StringUtils.isBlank(analytics.getPeriod())) throw new ServiceException("分析周期不能为空", 602);
        if (!VALID_PERIODS.contains(analytics.getPeriod())) {
            throw new ServiceException("分析周期必须为 monthly/quarterly/yearly", 601);
        }

        // FK 校验
        if (projectMapper.selectProjectById(analytics.getProjectId()) == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        // 新建状态必须为草稿
        if (StringUtils.isBlank(analytics.getStatus())) analytics.setStatus("00");
        else if (!"00".equals(analytics.getStatus())) {
            throw new ServiceException("新建效能分析状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(analytics.getAiGenerated())) analytics.setAiGenerated("N");
        if (analytics.getAuthorUserId() == null) analytics.setAuthorUserId(SecurityUtils.getUserId());

        if (StringUtils.isBlank(analytics.getAnalyticsNo())) analytics.setAnalyticsNo(generateAnalyticsNo());
        analytics.setCreateBy(SecurityUtils.getUsername());

        try {
            return analyticsMapper.insertAnalytics(analytics);
        } catch (DuplicateKeyException e) {
            log.warn("analytics_no 重号,重试: {}", analytics.getAnalyticsNo());
            analytics.setAnalyticsNo(generateAnalyticsNo());
            return analyticsMapper.insertAnalytics(analytics);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAnalytics(Analytics analytics)
    {
        Analytics old = analyticsMapper.selectAnalyticsById(analytics.getAnalyticsId());
        if (old == null) throw new ServiceException("效能分析不存在", 404);

        // 终态不可修改
        if ("01".equals(old.getStatus())) {
            throw new ServiceException("已生成的效能分析不能修改，如需更新请重新创建", 601);
        }

        // 状态机校验
        if (StringUtils.isNotBlank(analytics.getStatus())
                && !analytics.getStatus().equals(old.getStatus())) {
            String os = old.getStatus(), ns = analytics.getStatus();
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(os, Set.of());
            if (!allowed.contains(ns)) {
                throw new ServiceException(
                    "效能分析状态 " + statusLabel(os) + " 不能转到 " + statusLabel(ns), 601);
            }
        }

        // period 修改时也做白名单校验
        if (StringUtils.isNotBlank(analytics.getPeriod())
                && !VALID_PERIODS.contains(analytics.getPeriod())) {
            throw new ServiceException("分析周期必须为 monthly/quarterly/yearly", 601);
        }

        analytics.setUpdateBy(SecurityUtils.getUsername());
        return analyticsMapper.updateAnalytics(analytics);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAnalyticsByIds(Long[] analyticsIds) {
        return analyticsMapper.deleteAnalyticsByIds(analyticsIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int aiGenerate(Long analyticsId)
    {
        Analytics analytics = analyticsMapper.selectAnalyticsById(analyticsId);
        if (analytics == null) throw new ServiceException("效能分析不存在", 404);
        if ("01".equals(analytics.getStatus())) {
            throw new ServiceException("效能分析已生成，不能重复触发", 601);
        }

        // 填充 mock 指标
        analytics.setRequirementThroughput(28);
        analytics.setIterationOnTimeRate(new BigDecimal("87.50"));
        analytics.setDefectDensity(new BigDecimal("0.42"));
        analytics.setAiTimeSaved(new BigDecimal("32.00"));
        analytics.setProjectHealthScore(new BigDecimal("82.00"));
        analytics.setAiSuggestions("建议优化需求评审流程，减少返工率。");

        // 状态跃迁
        analytics.setStatus("01");
        analytics.setAiGenerated("Y");
        analytics.setAiGeneratedAt(new Date());
        analytics.setUpdateBy(SecurityUtils.getUsername());

        return analyticsMapper.updateAnalytics(analytics);
    }

    // ───────── 私有 ─────────

    private String generateAnalyticsNo() {
        int year = LocalDate.now().getYear();
        String prefix = "ANL-" + year + "-";
        Integer maxSeq = analyticsMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "已生成";
            default:   return "未知(" + status + ")";
        }
    }
}
