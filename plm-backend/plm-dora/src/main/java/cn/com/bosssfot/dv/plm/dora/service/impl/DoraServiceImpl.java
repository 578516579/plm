package cn.com.bosssfot.dv.plm.dora.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.dora.domain.Dora;
import cn.com.bosssfot.dv.plm.dora.mapper.DoraMapper;
import cn.com.bosssfot.dv.plm.dora.service.IDoraService;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * DORA效能指标 Service 实现
 *
 * 落地:
 * - 编号规则: DOR-YYYY-NNNN
 * - 状态机: 00=草稿 → 01=已生成 (终态)
 * - period 格式: YYYY-MM 或 YYYY-QN
 * - aiGenerate(): 根据 deployFrequency 计算 doraLevel
 *   deployFreq>1=elite, >0.5=high, >0.1=medium, else=low
 */
@Service
public class DoraServiceImpl implements IDoraService
{
    private static final Logger log = LoggerFactory.getLogger(DoraServiceImpl.class);

    /**
     * 状态机转换矩阵
     * 00=草稿 → 01=已生成 (终态)
     */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of());  // 终态
    }

    /** period 格式: YYYY-MM 或 YYYY-QN (Q1~Q4) */
    private static final Pattern PERIOD_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2]|Q[1-4])$");

    @Autowired private DoraMapper doraMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<Dora> selectDoraList(Dora dora) {
        return doraMapper.selectDoraList(dora);
    }

    @Override
    public Dora selectDoraById(Long doraId) {
        return doraMapper.selectDoraById(doraId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDora(Dora dora)
    {
        if (dora.getProjectId() == null) throw new ServiceException("关联项目不能为空", 602);
        if (StringUtils.isBlank(dora.getPeriod())) throw new ServiceException("统计周期不能为空", 602);
        if (!PERIOD_PATTERN.matcher(dora.getPeriod()).matches()) {
            throw new ServiceException("统计周期格式必须为 YYYY-MM 或 YYYY-QN（如 2026-05 或 2026-Q2）", 601);
        }

        // FK 校验
        if (projectMapper.selectProjectById(dora.getProjectId()) == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        // 默认值
        if (StringUtils.isBlank(dora.getStatus())) dora.setStatus("00");
        else if (!"00".equals(dora.getStatus())) {
            throw new ServiceException("新建DORA指标状态必须为「草稿」", 601);
        }
        if (StringUtils.isBlank(dora.getAiGenerated())) dora.setAiGenerated("N");
        if (dora.getAuthorUserId() == null) dora.setAuthorUserId(SecurityUtils.getUserId());

        if (StringUtils.isBlank(dora.getDoraNo())) dora.setDoraNo(generateDoraNo());
        dora.setCreateBy(SecurityUtils.getUsername());

        try {
            return doraMapper.insertDora(dora);
        } catch (DuplicateKeyException e) {
            log.warn("dora_no 重号,重试: {}", dora.getDoraNo());
            dora.setDoraNo(generateDoraNo());
            return doraMapper.insertDora(dora);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDora(Dora dora)
    {
        Dora old = doraMapper.selectDoraById(dora.getDoraId());
        if (old == null) throw new ServiceException("DORA指标不存在", 404);

        // 终态不可修改
        if ("01".equals(old.getStatus())) {
            throw new ServiceException("已生成的DORA指标不能修改，如需更新请重新创建", 601);
        }

        // 状态机校验
        if (StringUtils.isNotBlank(dora.getStatus())
                && !dora.getStatus().equals(old.getStatus())) {
            String os = old.getStatus(), ns = dora.getStatus();
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(os, Set.of());
            if (!allowed.contains(ns)) {
                throw new ServiceException(
                    "DORA指标状态 " + statusLabel(os) + " 不能转到 " + statusLabel(ns), 601);
            }
        }

        // period 修改时格式校验
        if (StringUtils.isNotBlank(dora.getPeriod())
                && !PERIOD_PATTERN.matcher(dora.getPeriod()).matches()) {
            throw new ServiceException("统计周期格式必须为 YYYY-MM 或 YYYY-QN", 601);
        }

        dora.setUpdateBy(SecurityUtils.getUsername());
        return doraMapper.updateDora(dora);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDoraByIds(Long[] doraIds) {
        return doraMapper.deleteDoraByIds(doraIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int aiGenerate(Long doraId)
    {
        Dora dora = doraMapper.selectDoraById(doraId);
        if (dora == null) throw new ServiceException("DORA指标不存在", 404);
        if ("01".equals(dora.getStatus())) {
            throw new ServiceException("DORA指标已生成，不能重复触发", 601);
        }

        // 根据部署频率计算 DORA 等级
        String level = calculateDoraLevel(dora.getDeployFrequency());
        dora.setDoraLevel(level);
        dora.setAiSuggestions(buildAiSuggestions(level));

        // 状态跃迁
        dora.setStatus("01");
        dora.setAiGenerated("Y");
        dora.setAiGeneratedAt(new Date());
        dora.setUpdateBy(SecurityUtils.getUsername());

        return doraMapper.updateDora(dora);
    }

    // ───────── 私有 ─────────

    private String generateDoraNo() {
        int year = LocalDate.now().getYear();
        String prefix = "DOR-" + year + "-";
        Integer maxSeq = doraMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    /**
     * 根据部署频率计算 DORA 等级
     * deployFreq > 1   → elite
     * deployFreq > 0.5 → high
     * deployFreq > 0.1 → medium
     * else             → low
     */
    private String calculateDoraLevel(BigDecimal deployFrequency) {
        if (deployFrequency == null) return "low";
        double freq = deployFrequency.doubleValue();
        if (freq > 1.0)  return "elite";
        if (freq > 0.5)  return "high";
        if (freq > 0.1)  return "medium";
        return "low";
    }

    private String buildAiSuggestions(String level) {
        switch (level) {
            case "elite":  return "当前部署频率已达精英级别，建议持续保持并关注变更失败率与 MTTR 指标。";
            case "high":   return "部署频率良好，建议推进自动化测试覆盖率提升以保障高频发布质量。";
            case "medium": return "建议通过 CI/CD 流水线优化缩短变更前置时间，逐步提升部署频率。";
            default:       return "建议优先建立基础 CI/CD 流水线，实现自动化构建与部署，提升研发效能。";
        }
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "已生成";
            default:   return "未知(" + status + ")";
        }
    }
}
