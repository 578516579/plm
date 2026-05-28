package cn.com.bosssfot.dv.plm.analytics.service.impl;

import java.time.LocalDate;
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
import cn.com.bosssfot.dv.plm.analytics.domain.AnalyticsSnapshot;
import cn.com.bosssfot.dv.plm.analytics.mapper.AnalyticsSnapshotMapper;
import cn.com.bosssfot.dv.plm.analytics.service.IAnalyticsSnapshotService;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.AiTexts;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;

/**
 * 效能分析快照 Service — PRD §F6 + 原型 analytics.html / devops.html
 *
 * 落地:
 * - generateSnapshotNo() — AS-YYYY-NNNN
 * - PRD §F6: 周期性快照 + DORA 4 指标 + AI 复盘建议
 * - 3 状态机: 00 草稿 → 01 已发布 → 02 已归档 (终态)
 * - projectId 可空 (NULL=全局快照)
 */
@Service
public class AnalyticsSnapshotServiceImpl implements IAnalyticsSnapshotService {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsSnapshotServiceImpl.class);

    private static final Set<String> ALLOWED_PERIOD = Set.of("month", "quarter", "year");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of());
    }

    @Autowired private AnalyticsSnapshotMapper analyticsMapper;
    @Autowired private AiService aiService;

    @Override
    public List<AnalyticsSnapshot> selectAnalyticsList(AnalyticsSnapshot t) {
        return analyticsMapper.selectAnalyticsList(t);
    }

    @Override
    public AnalyticsSnapshot selectAnalyticsById(Long id) {
        return analyticsMapper.selectAnalyticsById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAnalytics(AnalyticsSnapshot t) {
        if (StringUtils.isBlank(t.getTitle()))        throw new ServiceException("快照标题不能为空", 602);
        if (StringUtils.isBlank(t.getPeriodType()))   throw new ServiceException("快照周期不能为空", 602);
        if (t.getSnapshotDate() == null)              throw new ServiceException("快照日期不能为空", 602);
        if (t.getAuthorUserId() == null)              throw new ServiceException("作者不能为空", 602);
        if (!ALLOWED_PERIOD.contains(t.getPeriodType()))
            throw new ServiceException("无效的周期: " + t.getPeriodType(), 604);

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus()))      t.setStatus("00");

        if (StringUtils.isBlank(t.getSnapshotNo())) t.setSnapshotNo(generateSnapshotNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return analyticsMapper.insertAnalytics(t);
        } catch (DuplicateKeyException e) {
            log.warn("snapshot_no 重号,重试一次: {}", t.getSnapshotNo());
            t.setSnapshotNo(generateSnapshotNo());
            return analyticsMapper.insertAnalytics(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAnalytics(AnalyticsSnapshot t) {
        AnalyticsSnapshot old = analyticsMapper.selectAnalyticsById(t.getSnapshotId());
        if (old == null) throw new ServiceException("效能分析快照不存在", 404);

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "快照状态 " + statusLabel(old.getStatus()) + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601);
            }
        }
        if (t.getPeriodType() != null && !ALLOWED_PERIOD.contains(t.getPeriodType()))
            throw new ServiceException("无效的周期: " + t.getPeriodType(), 604);

        t.setUpdateBy(SecurityUtils.getUsername());
        return analyticsMapper.updateAnalytics(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAnalyticsByIds(Long[] ids) {
        return analyticsMapper.deleteAnalyticsByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalyticsSnapshot aiRecommend(Long id) {
        AnalyticsSnapshot t = analyticsMapper.selectAnalyticsById(id);
        if (t == null) throw new ServiceException("效能分析快照不存在", 404);
        t.setAiRecommendations(AiTexts.generate(aiService,AiChatRequest.builder("")
            .system("你是 PLM 资深效能分析师,擅长 DORA 指标解读与改进建议")
            .user("请基于快照 [" + t.getSnapshotNo() + "] 生成 AI 复盘改进建议")
            .callerTag("analytics#" + id).build(),
            () -> buildAiRecommendations(t)));
        t.setAiGenerated("Y");
        t.setAiGeneratedAt(new Date());
        t.setUpdateBy("ai-agent");
        analyticsMapper.updateAnalytics(t);
        return analyticsMapper.selectAnalyticsById(id);
    }

    private String buildAiRecommendations(AnalyticsSnapshot t) {
        StringBuilder sb = new StringBuilder();
        sb.append("# AI 复盘改进建议\n\n");
        sb.append("> 周期: ").append(t.getPeriodType()).append(" | 快照日期: ").append(t.getSnapshotDate()).append("\n\n");

        sb.append("## 1. 关键指标速览\n");
        sb.append("- 需求吞吐量: **").append(t.getRequirementThroughput()).append("**\n");
        sb.append("- 迭代准时率: **").append(t.getSprintOnTimeRate()).append("%**\n");
        sb.append("- 缺陷密度: **").append(t.getDefectDensity()).append("** 个/KLOC\n");
        sb.append("- 自动化覆盖率: **").append(t.getAutoTestCoverage()).append("%**\n\n");

        sb.append("## 2. DORA 指标评估\n");
        sb.append("- 部署频率: ").append(t.getDeploymentFrequency()).append(" 次/天\n");
        sb.append("- 前置时间: ").append(t.getLeadTimeHours()).append(" 小时\n");
        sb.append("- MTTR: ").append(t.getMttrHours()).append(" 小时\n");
        sb.append("- 变更失败率: ").append(t.getChangeFailureRate()).append("%\n\n");

        sb.append("## 3. 改进建议 (AgriPLM AI Coach)\n");

        if (t.getSprintOnTimeRate() != null && t.getSprintOnTimeRate().doubleValue() < 80.0) {
            sb.append("- ⚠ 迭代准时率偏低 (<80%),建议拆解 Sprint 任务粒度,引入 daily standup blocker 上报机制\n");
        }
        if (t.getDefectDensity() != null && t.getDefectDensity().doubleValue() > 3.0) {
            sb.append("- ⚠ 缺陷密度偏高 (>3/KLOC),建议加强 PRD/接口评审 + 单测覆盖率 80% 卡口\n");
        }
        if (t.getAutoTestCoverage() != null && t.getAutoTestCoverage().doubleValue() < 60.0) {
            sb.append("- ⚠ 自动化覆盖率不足 (<60%),建议引入 Playwright E2E + pytest API 双线推进\n");
        }
        if (t.getChangeFailureRate() != null && t.getChangeFailureRate().doubleValue() > 15.0) {
            sb.append("- ⚠ DORA 变更失败率偏高 (>15%),建议引入 feature-flag 灰度 + 自动回滚\n");
        }
        if (t.getAiHoursSaved() != null && t.getAiHoursSaved().doubleValue() > 200.0) {
            sb.append("- ✅ AI 节省工时 >200h,可在团队复盘会复用 AI 提效经验\n");
        }
        sb.append("\n## 4. 农情专项建议\n");
        sb.append("- 增加 IoT 设备稳定性指标 (心跳率/丢包率) 到下期快照\n");
        sb.append("- 灌溉/施肥旺季前预演容灾切换,降低 MTTR 至 1 小时内\n");

        return sb.toString();
    }

    private String generateSnapshotNo() {
        int year = LocalDate.now().getYear();
        String prefix = "AS-" + year + "-";
        Integer maxSeq = analyticsMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String s) {
        switch (s) {
            case "00": return "草稿";
            case "01": return "已发布";
            case "02": return "已归档";
            default:   return "未知(" + s + ")";
        }
    }
}
