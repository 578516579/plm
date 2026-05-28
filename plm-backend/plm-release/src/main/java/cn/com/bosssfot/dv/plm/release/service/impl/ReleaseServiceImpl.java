package cn.com.bosssfot.dv.plm.release.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import cn.com.bosssfot.dv.plm.common.ai.AiTexts;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.spi.ProjectScopedLookup;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.release.domain.Release;
import cn.com.bosssfot.dv.plm.release.mapper.ReleaseMapper;
import cn.com.bosssfot.dv.plm.release.service.IReleaseService;

/**
 * 发布管理 Service — 原型 release.html
 *
 * 落地:
 * - ADR: generateReleaseNo() — REL-YYYY-NNNN
 * - 蓝绿 / 金丝雀 / 滚动 三种策略 + DORA 4 指标
 * - 5 状态机: 00 计划中 → 01 发布中 → 02 已发布 → 03 已回滚 / 04 已废弃
 *   - 00→{01,04}
 *   - 01→{02,03}
 *   - 02→{03,04}
 *   - 03→{04}
 *   - 04→{}  终态
 */
@Service
public class ReleaseServiceImpl implements IReleaseService
{
    private static final Logger log = LoggerFactory.getLogger(ReleaseServiceImpl.class);

    private static final Set<String> ALLOWED_STRATEGY = Set.of("blue_green", "canary", "rolling");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01", "04"),
        "01", Set.of("02", "03"),
        "02", Set.of("03", "04"),
        "03", Set.of("04"),
        "04", Set.of()
    );

    @Autowired private ReleaseMapper releaseMapper;
    @Autowired private ProjectMapper projectMapper;
    @Autowired private AiService aiService;

    /**
     * proposal 0028 P0-2A 解 P0-1 known limitation:SPI 模式 ProjectScopedLookup,
     * 按 bean 名("pipeline" / "release" / ...)查跨模块归属项目,避免 Maven Reactor 循环依赖。
     */
    @Autowired(required = false) private Map<String, ProjectScopedLookup> projectScopedLookups;

    @Override
    public List<Release> selectReleaseList(Release t) {
        return releaseMapper.selectReleaseList(t);
    }

    @Override
    public Release selectReleaseById(Long id) {
        return releaseMapper.selectReleaseById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRelease(Release t) {
        if (StringUtils.isBlank(t.getVersion())) {
            throw new ServiceException("发布版本号不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (t.getReleasedByUserId() == null) {
            throw new ServiceException("发布人不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        // proposal 0028 P0-2A: 跨模块 FK 校验 — pipeline 必须同项目
        validatePipelineFk(t);

        // 默认 strategy = rolling
        if (StringUtils.isBlank(t.getStrategy())) {
            t.setStrategy("rolling");
        } else if (!ALLOWED_STRATEGY.contains(t.getStrategy())) {
            throw new ServiceException("策略仅支持 blue_green/canary/rolling", 604);
        }
        if (StringUtils.isBlank(t.getEnvironment())) {
            t.setEnvironment("prod");
        }
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建发布单状态必须为「计划中」", 601);
        }

        if (StringUtils.isBlank(t.getReleaseNo())) {
            t.setReleaseNo(generateReleaseNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return releaseMapper.insertRelease(t);
        } catch (DuplicateKeyException e) {
            log.warn("release_no 重号,重试一次: {}", t.getReleaseNo());
            t.setReleaseNo(generateReleaseNo());
            return releaseMapper.insertRelease(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRelease(Release t) {
        Release old = releaseMapper.selectReleaseById(t.getReleaseId());
        if (old == null) {
            throw new ServiceException("发布单不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "发布单状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }

            // 回滚必须有 reason
            if ("03".equals(t.getStatus())) {
                String reason = StringUtils.isNotBlank(t.getRollbackReason())
                    ? t.getRollbackReason() : old.getRollbackReason();
                if (StringUtils.isBlank(reason)) {
                    throw new ServiceException("回滚必须填写回滚原因", 602);
                }
            }
        }

        if (StringUtils.isNotBlank(t.getStrategy()) && !ALLOWED_STRATEGY.contains(t.getStrategy())) {
            throw new ServiceException("策略仅支持 blue_green/canary/rolling", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }
        // proposal 0028 P0-2A: 更新 pipelineId 时也要校验 same-project
        if (t.getPipelineId() != null && !t.getPipelineId().equals(old.getPipelineId())) {
            Release merged = new Release();
            merged.setProjectId(t.getProjectId() != null ? t.getProjectId() : old.getProjectId());
            merged.setPipelineId(t.getPipelineId());
            validatePipelineFk(merged);
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return releaseMapper.updateRelease(t);
    }

    /**
     * 校验 release.pipelineId 与 release.projectId 跨模块强约束(same-project)。
     * pipelineId 为 null 时跳过(允许发布单暂不挂流水线)。
     *
     * proposal 0028 P0-2A:通过 SPI 模式 ProjectScopedLookup 避免 Maven Reactor 循环依赖。
     */
    private void validatePipelineFk(Release release) {
        if (release.getPipelineId() == null) {
            return;
        }
        ProjectScopedLookup pipelineLookup =
            projectScopedLookups == null ? null : projectScopedLookups.get("pipeline");
        if (pipelineLookup == null) {
            throw new ServiceException("系统配置缺失:ProjectScopedLookup 未注册 pipeline", 500);
        }
        Long targetProjectId = pipelineLookup.resolveProjectId(release.getPipelineId());
        if (targetProjectId == null) {
            throw new ServiceException("关联的流水线不存在", 702);
        }
        if (!targetProjectId.equals(release.getProjectId())) {
            throw new ServiceException("发布单的流水线必须属于同一项目", 702);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteReleaseByIds(Long[] ids) {
        return releaseMapper.deleteReleaseByIds(ids);
    }

    /**
     * P0-1b: AI 发布评审。说明文本走 LLM(真 provider 非空采用,否则回退结构化模板);
     * 评分由 DORA 4 指标确定性计算(不交给 LLM,避免幻觉)。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Release aiReview(Long releaseId) {
        Release r = releaseMapper.selectReleaseById(releaseId);
        if (r == null) {
            throw new ServiceException("发布单不存在", 404);
        }
        String doraSummary = "策略=" + r.getStrategy() + ", 环境=" + r.getEnvironment()
            + ", 部署频率=" + nz(r.getDeploymentFrequency())
            + ", 前置时间(h)=" + nz(r.getLeadTimeHours())
            + ", MTTR(min)=" + nz(r.getMttrMinutes())
            + ", 变更失败率(%)=" + nz(r.getChangeFailureRate());
        AiChatRequest req = AiChatRequest.builder("")
            .system("你是 PLM 资深 SRE / 发布评审专家,精通 DORA 4 指标的解读。"
                + "输出格式:Markdown,含\"## 评审摘要 / ## DORA 解读 / ## 风险与建议\"3 段,"
                + "300-500 字,不要给出数字评分(评分由系统计算)。")
            .user("评审发布单:版本 " + r.getVersion() + " / " + doraSummary
                + ". 发布说明: " + (r.getReleaseNotes() == null ? "" : r.getReleaseNotes()))
            .callerTag("release#" + releaseId)
            .temperature(0.5)
            .maxTokens(1500)
            .build();
        String notes = AiTexts.generate(aiService, req, () -> buildReleaseReviewTemplate(r, doraSummary));
        r.setAiReviewNotes(notes);
        r.setAiReviewScore(computeReviewScore(r));
        r.setUpdateBy(SecurityUtils.getUsername());
        releaseMapper.updateRelease(r);
        return r;
    }

    /** DORA 确定性评分: 基线 85, 失败率/MTTR 扣分, clamp [0,100]。 */
    private static BigDecimal computeReviewScore(Release r) {
        BigDecimal score = BigDecimal.valueOf(85);
        if (r.getChangeFailureRate() != null) {
            // 每 1% 失败率扣 1 分,>15% 时再额外扣 10
            score = score.subtract(r.getChangeFailureRate());
            if (r.getChangeFailureRate().compareTo(BigDecimal.valueOf(15)) > 0) {
                score = score.subtract(BigDecimal.TEN);
            }
        }
        if (r.getMttrMinutes() != null && r.getMttrMinutes().compareTo(BigDecimal.valueOf(60)) > 0) {
            score = score.subtract(BigDecimal.TEN);
        }
        if (r.getDeploymentFrequency() != null
                && r.getDeploymentFrequency().compareTo(BigDecimal.ONE) >= 0) {
            score = score.add(BigDecimal.valueOf(5));
        }
        if (score.compareTo(BigDecimal.ZERO) < 0)   score = BigDecimal.ZERO;
        if (score.compareTo(BigDecimal.valueOf(100)) > 0) score = BigDecimal.valueOf(100);
        return score;
    }

    private static String buildReleaseReviewTemplate(Release r, String doraSummary) {
        return "## 评审摘要\n版本 " + r.getVersion() + " 发布评审(策略 " + r.getStrategy()
            + ",环境 " + r.getEnvironment() + ")。\n\n"
            + "## DORA 解读\n" + doraSummary + "\n\n"
            + "## 风险与建议\n- 部署频率与变更失败率请持续监控\n"
            + "- MTTR 偏高时优先治理告警链路\n"
            + "- 蓝绿/金丝雀策略下,关注切流后的 5 分钟黄金窗口\n";
    }

    private static String nz(BigDecimal v) {
        return v == null ? "N/A" : v.toPlainString();
    }

    private String generateReleaseNo() {
        int year = LocalDate.now().getYear();
        String prefix = "REL-" + year + "-";
        Integer maxSeq = releaseMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "计划中";
            case "01" -> "发布中";
            case "02" -> "已发布";
            case "03" -> "已回滚";
            case "04" -> "已废弃";
            default   -> "未知(" + status + ")";
        };
    }
}
