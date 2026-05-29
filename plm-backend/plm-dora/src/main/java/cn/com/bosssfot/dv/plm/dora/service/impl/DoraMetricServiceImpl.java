package cn.com.bosssfot.dv.plm.dora.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.AiTexts;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.spi.DoraAggregationSource;
import cn.com.bosssfot.dv.plm.common.spi.DoraAggregationSource.DoraAggregationData;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.dora.domain.DoraMetric;
import cn.com.bosssfot.dv.plm.dora.mapper.DoraMetricMapper;
import cn.com.bosssfot.dv.plm.dora.service.IDoraMetricService;

/**
 * DORA Service — DevOps 扩展 + 原型 devops.html
 * DORA 4 指标 + 热力图/前置时间拆解 + AI 持续改进建议
 */
@Service
public class DoraMetricServiceImpl implements IDoraMetricService {
    private static final Logger log = LoggerFactory.getLogger(DoraMetricServiceImpl.class);

    private static final Set<String> ALLOWED_TYPE   = Set.of("deploy_freq","lead_time","mttr","change_fail_rate");
    private static final Set<String> ALLOWED_PERIOD = Set.of("month","quarter");
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02"));
        STATUS_TRANSITIONS.put("02", Set.of());
    }

    @Autowired private DoraMetricMapper doraMapper;
    @Autowired private AiService aiService;

    /**
     * Proposal 0028 P0-3B: 通过 SPI 模式拉跨模块聚合数据,避免 Maven Reactor 循环依赖。
     * Map key = bean 名 ("pipeline" / "release" / "defect"),值由各业务模块旁挂 @Component 注册。
     */
    @Autowired(required = false) private Map<String, DoraAggregationSource> aggregationSources;

    @Override
    public List<DoraMetric> selectDoraList(DoraMetric t) { return doraMapper.selectDoraList(t); }

    @Override
    public DoraMetric selectDoraById(Long id) { return doraMapper.selectDoraById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDora(DoraMetric t) {
        if (StringUtils.isBlank(t.getMetricName())) throw new ServiceException("指标名称不能为空", 602);
        if (StringUtils.isBlank(t.getMetricType())) throw new ServiceException("指标类型不能为空", 602);
        if (!ALLOWED_TYPE.contains(t.getMetricType()))
            throw new ServiceException("无效的指标类型: " + t.getMetricType(), 604);
        if (t.getMetricValue() == null)             throw new ServiceException("指标值不能为空", 602);
        if (StringUtils.isBlank(t.getPeriodType())) throw new ServiceException("周期不能为空", 602);
        if (!ALLOWED_PERIOD.contains(t.getPeriodType()))
            throw new ServiceException("无效的周期: " + t.getPeriodType(), 604);
        if (t.getSnapshotDate() == null)            throw new ServiceException("记录日期不能为空", 602);
        if (t.getAuthorUserId() == null)            throw new ServiceException("创建者不能为空", 602);

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus()))      t.setStatus("00");
        if (StringUtils.isBlank(t.getDoraNo()))      t.setDoraNo(generateDoraNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return doraMapper.insertDora(t);
        } catch (DuplicateKeyException e) {
            log.warn("dora_no 重号,重试: {}", t.getDoraNo());
            t.setDoraNo(generateDoraNo());
            return doraMapper.insertDora(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDora(DoraMetric t) {
        DoraMetric old = doraMapper.selectDoraById(t.getDoraId());
        if (old == null) throw new ServiceException("DORA 指标不存在", 404);
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus()))
                throw new ServiceException("状态不能从 " + old.getStatus() + " 转到 " + t.getStatus(), 601);
        }
        if (t.getMetricType() != null && !ALLOWED_TYPE.contains(t.getMetricType()))
            throw new ServiceException("无效的指标类型: " + t.getMetricType(), 604);
        if (t.getPeriodType() != null && !ALLOWED_PERIOD.contains(t.getPeriodType()))
            throw new ServiceException("无效的周期: " + t.getPeriodType(), 604);
        t.setUpdateBy(SecurityUtils.getUsername());
        return doraMapper.updateDora(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDoraByIds(Long[] ids) { return doraMapper.deleteDoraByIds(ids); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DoraMetric aiSuggest(Long id) {
        DoraMetric t = doraMapper.selectDoraById(id);
        if (t == null) throw new ServiceException("DORA 指标不存在", 404);
        t.setAiSuggestions(AiTexts.generate(aiService,AiChatRequest.builder("")
            .system("你是 PLM 资深 DevOps 顾问,擅长 DORA 4 指标(DF/LT/MTTR/CFR)解读与优化")
            .user("请为 DORA 指标 [" + t.getDoraNo() + "] 生成改进建议(包含 MTTR 与农情专项关联)")
            .callerTag("dora#" + id).build(),
            () -> buildSuggestions(t)));
        t.setAiGenerated("Y");
        t.setAiGeneratedAt(new Date());
        t.setUpdateBy("ai-agent");
        doraMapper.updateDora(t);
        return doraMapper.selectDoraById(id);
    }

    private String buildSuggestions(DoraMetric t) {
        StringBuilder sb = new StringBuilder();
        sb.append("# DORA 持续改进建议\n\n");
        sb.append("> 指标: ").append(t.getMetricName())
          .append(" (").append(t.getMetricType()).append(")\n")
          .append("> 当前值: ").append(t.getMetricValue()).append(" ")
          .append(t.getMetricUnit() != null ? t.getMetricUnit() : "").append("\n")
          .append("> 周期: ").append(t.getPeriodType()).append(" | 日期: ").append(t.getSnapshotDate()).append("\n\n");

        double val = t.getMetricValue().doubleValue();
        switch (t.getMetricType()) {
            case "deploy_freq":
                sb.append("## DORA 等级评估\n");
                if (val >= 1.0)       sb.append("- ✅ Elite 等级 (≥1 次/天)\n");
                else if (val >= 0.14) sb.append("- 🟢 High 等级 (周/月级)\n");
                else if (val >= 0.03) sb.append("- 🟡 Medium 等级 (月/季度)\n");
                else                  sb.append("- 🔴 Low 等级 — 建议引入自动化部署 + feature-flag 灰度\n");
                sb.append("\n## 优化方向\n- 拆分 monolith → 多服务以降低部署阻塞\n- IoT 农情服务可独立部署,不受平台版本影响\n");
                break;
            case "lead_time":
                sb.append("## 前置时间拆解参考\n");
                sb.append("- 代码 → 评审: 目标 < 4h\n- 评审 → 合并: 目标 < 2h\n- 合并 → 部署: 目标 < 1h\n\n");
                if (val > 168) sb.append("⚠ Lead Time > 1 周,建议引入 trunk-based + 小 PR\n");
                break;
            case "mttr":
                if (val > 4)      sb.append("⚠ MTTR > 4 小时,建议引入告警自动 runbook + 演练\n");
                else if (val < 1) sb.append("✅ MTTR < 1 小时,达 Elite 标准\n");
                sb.append("\n## 农情专项\n- 灌溉/施肥旺季前预演容灾切换,降低 MTTR\n");
                break;
            case "change_fail_rate":
                if (val > 15) sb.append("⚠ 变更失败率 > 15%,建议引入 canary feature-flag + 自动回滚\n");
                else          sb.append("✅ 变更失败率 ≤ 15%,处于健康区间\n");
                break;
        }
        return sb.toString();
    }

    private String generateDoraNo() {
        int year = LocalDate.now().getYear();
        String prefix = "DORA-" + year + "-";
        Integer maxSeq = doraMapper.selectMaxSeqOfYear(prefix);
        return String.format("%s%04d", prefix, (maxSeq == null ? 0 : maxSeq) + 1);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Proposal 0028 P0-3B: 真聚合 4 个 DORA 指标
    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DoraMetric> computeMetrics(Long projectId, Date periodStart, Date periodEnd) {
        if (projectId == null) throw new ServiceException("projectId 不能为空", 602);
        if (periodStart == null || periodEnd == null)
            throw new ServiceException("聚合窗口不能为空", 602);
        if (!periodEnd.after(periodStart))
            throw new ServiceException("periodEnd 必须晚于 periodStart", 604);

        int periodDays = (int) Math.max(1L,
            TimeUnit.MILLISECONDS.toDays(periodEnd.getTime() - periodStart.getTime()));

        // 1. 聚合数据(SPI 缺失时各 source 返回零值,指标仍能算出 0)
        DoraAggregationData pipe   = invokeSource("pipeline", projectId, periodStart, periodEnd);
        DoraAggregationData rel    = invokeSource("release",  projectId, periodStart, periodEnd);
        DoraAggregationData defect = invokeSource("defect",   projectId, periodStart, periodEnd);

        // 2. 计算 4 个值
        // 部署频率 = success 次数 / 天数(次/天)
        BigDecimal deployFreq = BigDecimal.valueOf(pipe.deployCount)
                .divide(BigDecimal.valueOf(periodDays), 2, RoundingMode.HALF_UP);

        // 前置时间 = avg(ms) / 3600000 (小时);0 样本时为 0
        BigDecimal leadTimeHours = rel.leadTimeSampleCnt == 0
                ? BigDecimal.ZERO
                : rel.totalLeadTimeMs
                    .divide(BigDecimal.valueOf(rel.leadTimeSampleCnt), 4, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(3_600_000L), 2, RoundingMode.HALF_UP);

        // MTTR = avg(ms) / 3600000 (小时);0 样本时为 0
        BigDecimal mttrHours = defect.recoverSampleCnt == 0
                ? BigDecimal.ZERO
                : defect.totalRecoverMs
                    .divide(BigDecimal.valueOf(defect.recoverSampleCnt), 4, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(3_600_000L), 2, RoundingMode.HALF_UP);

        // 变更失败率 = failed / (success+failed) * 100 (%);0 样本时为 0
        BigDecimal changeFailRate = pipe.totalRunCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(pipe.failedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(pipe.totalRunCount), 2, RoundingMode.HALF_UP);

        // 3. upsert 4 条记录
        List<DoraMetric> result = new ArrayList<>(4);
        result.add(upsertMetric(projectId, "deploy_freq",      "部署频率",     deployFreq,     "次/天", periodStart, periodEnd, periodDays));
        result.add(upsertMetric(projectId, "lead_time",        "前置时间",     leadTimeHours,  "小时",  periodStart, periodEnd, periodDays));
        result.add(upsertMetric(projectId, "mttr",             "平均恢复时间", mttrHours,      "小时",  periodStart, periodEnd, periodDays));
        result.add(upsertMetric(projectId, "change_fail_rate", "变更失败率",   changeFailRate, "%",     periodStart, periodEnd, periodDays));
        return result;
    }

    private DoraAggregationData invokeSource(String key, Long projectId, Date start, Date end) {
        if (aggregationSources == null) return new DoraAggregationData();
        DoraAggregationSource s = aggregationSources.get(key);
        if (s == null) {
            log.warn("DoraAggregationSource '{}' 未注册,该 source 视为 0 值", key);
            return new DoraAggregationData();
        }
        return s.aggregate(projectId, start, end);
    }

    /**
     * upsert 一条 metric。规则:
     * - 不存在 → insert,is_computed='Y' / computedAt=now
     * - 存在且 is_computed='N'(人工录入)→ 跳过(尊重人工)
     * - 存在且 is_computed='Y' → update 覆盖
     *
     * upsert 主键:projectId + metricType + periodStart。
     * periodType 推断:days≤31 → month;否则 quarter。
     */
    private DoraMetric upsertMetric(Long projectId, String metricType, String metricName,
                                     BigDecimal value, String unit,
                                     Date periodStart, Date periodEnd, int periodDays) {
        DoraMetric existing = doraMapper.selectByProjectTypePeriod(projectId, metricType, periodStart);

        if (existing != null && "N".equalsIgnoreCase(existing.getIsComputed())) {
            log.info("项目 {} metricType={} periodStart={} 是人工录入(is_computed=N),跳过覆盖",
                projectId, metricType, periodStart);
            return existing;
        }

        Date now = new Date();
        if (existing == null) {
            DoraMetric m = new DoraMetric();
            m.setProjectId(projectId);
            m.setMetricName(metricName);
            m.setMetricType(metricType);
            m.setMetricValue(value);
            m.setMetricUnit(unit);
            m.setPeriodType(periodDays <= 31 ? "month" : "quarter");
            m.setSnapshotDate(periodEnd);
            m.setPeriodStart(periodStart);
            m.setPeriodEnd(periodEnd);
            m.setPeriodDays(periodDays);
            m.setIsComputed("Y");
            m.setComputedAt(now);
            m.setStatus("00");
            m.setAiGenerated("N");
            m.setAuthorUserId(0L);   // 系统聚合,无具体作者;0 = 系统
            m.setDoraNo(generateDoraNo());
            m.setCreateBy("dora-compute");
            try {
                doraMapper.insertDora(m);
            } catch (DuplicateKeyException e) {
                m.setDoraNo(generateDoraNo());
                doraMapper.insertDora(m);
            }
            return m;
        }

        // 已有且自动算出 → 覆盖更新
        existing.setMetricName(metricName);
        existing.setMetricValue(value);
        existing.setMetricUnit(unit);
        existing.setPeriodEnd(periodEnd);
        existing.setPeriodDays(periodDays);
        existing.setSnapshotDate(periodEnd);
        existing.setIsComputed("Y");
        existing.setComputedAt(now);
        existing.setUpdateBy("dora-compute");
        doraMapper.updateDora(existing);
        return existing;
    }
}
