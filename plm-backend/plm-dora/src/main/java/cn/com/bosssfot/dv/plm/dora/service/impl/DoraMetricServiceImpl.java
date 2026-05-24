package cn.com.bosssfot.dv.plm.dora.service.impl;

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
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
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
        aiService.chat(AiChatRequest.builder("")
            .system("你是 PLM 资深 DevOps 顾问,擅长 DORA 4 指标(DF/LT/MTTR/CFR)解读与优化")
            .user("请为 DORA 指标 [" + t.getDoraNo() + "] 生成改进建议(包含 MTTR 与农情专项关联)")
            .callerTag("dora#" + id).build());

        t.setAiSuggestions(buildSuggestions(t));
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
}
