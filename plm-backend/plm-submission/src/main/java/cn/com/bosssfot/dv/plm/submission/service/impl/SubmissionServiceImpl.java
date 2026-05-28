package cn.com.bosssfot.dv.plm.submission.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.submission.domain.Submission;
import cn.com.bosssfot.dv.plm.submission.mapper.SubmissionMapper;
import cn.com.bosssfot.dv.plm.submission.service.ISubmissionService;
import cn.com.bosssfot.dv.plm.testplan.domain.TestPlan;
import cn.com.bosssfot.dv.plm.testplan.mapper.TestPlanMapper;

/**
 * 提测管理 Service — PRD §F4.4 + 原型 submit.html
 *
 * 落地:
 * - ADR: generateSubmissionNo() — SUB-YYYY-NNNN
 * - PRD §F4.4: AI 质量门禁 — 4 项全 Y → quality_gate_passed='Y'
 *              (单测覆盖率 ≥60% + 代码扫描通过 + PRD 完整 + API 文档更新)
 * - 5×5 状态机 (含反向边 04→00):
 *   00 草稿 → 01 已提交 → 02 质量门禁中 → 03 已通过 / 04 已退回 → 00 (打回到草稿)
 * - 进入 03 (已通过) 必须 qualityGatePassed='Y'   → 否则 708
 * - 进入 04 (已退回) 必须带 rejectReason          → 否则 602
 * - 00→01 自动填 submittedAt; 02→03 自动填 approvedAt
 */
@Service
public class SubmissionServiceImpl implements ISubmissionService
{
    private static final Logger log = LoggerFactory.getLogger(SubmissionServiceImpl.class);

    /** 单测覆盖率最低要求 60% (PRD §F4.4) */
    private static final BigDecimal MIN_UNIT_TEST_COVERAGE = new BigDecimal("60.00");

    /** 5×5 状态机转换矩阵 — 含反向边 04→00 */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01"),         // 草稿 → 已提交
        "01", Set.of("02", "04"),   // 已提交 → 质量门禁中 / 已退回
        "02", Set.of("03", "04"),   // 质量门禁中 → 已通过 / 已退回
        "03", Set.of(),             // 已通过 (终态)
        "04", Set.of("00")          // 已退回 → 草稿 (反向边)
    );

    @Autowired private SubmissionMapper submissionMapper;
    @Autowired private ProjectMapper projectMapper;
    @Autowired private TestPlanMapper testPlanMapper;

    @Override
    public List<Submission> selectSubmissionList(Submission t) {
        return submissionMapper.selectSubmissionList(t);
    }

    @Override
    public Submission selectSubmissionById(Long id) {
        return submissionMapper.selectSubmissionById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSubmission(Submission t) {
        // 字段必填校验
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("提测标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        // FK 校验
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        // Proposal 0028 P0-1: 跨模块 FK testplanId — 同 projectId 强约束
        validateTestplanFk(t);

        // 默认值
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建提测单状态必须为「草稿」", 601);
        }

        // 自动算质量门禁
        t.setQualityGatePassed(computeQualityGate(t));

        // 编号
        if (StringUtils.isBlank(t.getSubmissionNo())) {
            t.setSubmissionNo(generateSubmissionNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return submissionMapper.insertSubmission(t);
        } catch (DuplicateKeyException e) {
            log.warn("submission_no 重号,重试一次: {}", t.getSubmissionNo());
            t.setSubmissionNo(generateSubmissionNo());
            return submissionMapper.insertSubmission(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSubmission(Submission t) {
        Submission old = submissionMapper.selectSubmissionById(t.getSubmissionId());
        if (old == null) {
            throw new ServiceException("提测单不存在", 404);
        }

        // 状态机校验
        boolean isStatusChange = StringUtils.isNotBlank(t.getStatus())
                && !t.getStatus().equals(old.getStatus());
        if (isStatusChange) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "提测单状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }

            // 进入 03 (已通过) — 必须门禁通过 → 708
            if ("03".equals(t.getStatus())) {
                String gate = StringUtils.isNotBlank(t.getQualityGatePassed())
                    ? t.getQualityGatePassed() : old.getQualityGatePassed();
                if (!"Y".equalsIgnoreCase(gate)) {
                    throw new ServiceException("AI 质量门禁未通过,不能标记「已通过」", 708);
                }
                t.setApprovedAt(new Date());
            }

            // 进入 04 (已退回) — 必须有 rejectReason → 602
            if ("04".equals(t.getStatus())) {
                String reason = StringUtils.isNotBlank(t.getRejectReason())
                    ? t.getRejectReason() : old.getRejectReason();
                if (StringUtils.isBlank(reason)) {
                    throw new ServiceException("退回必须填写退回原因", 602);
                }
            }

            // 00→01 自动填 submittedAt
            if ("00".equals(old.getStatus()) && "01".equals(t.getStatus())
                    && old.getSubmittedAt() == null && t.getSubmittedAt() == null) {
                t.setSubmittedAt(new Date());
            }
        }

        // FK 复查
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        // Proposal 0028 P0-1: 跨模块 FK testplanId
        if (t.getTestplanId() != null && !t.getTestplanId().equals(old.getTestplanId())) {
            // 重新校验 — 用更新单的 projectId(若有)否则用 old 的 projectId
            Submission forCheck = new Submission();
            forCheck.setTestplanId(t.getTestplanId());
            forCheck.setProjectId(t.getProjectId() != null ? t.getProjectId() : old.getProjectId());
            validateTestplanFk(forCheck);
        }

        // 任意一项门禁字段变更 → 重算 qualityGatePassed
        boolean gateChanged =
            t.getUnitTestCoverage() != null
            || StringUtils.isNotBlank(t.getCodeScanPassed())
            || StringUtils.isNotBlank(t.getPrdCompleted())
            || StringUtils.isNotBlank(t.getApiDocUpdated());
        if (gateChanged) {
            Submission merged = mergeForGateCalc(old, t);
            t.setQualityGatePassed(computeQualityGate(merged));
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return submissionMapper.updateSubmission(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSubmissionByIds(Long[] ids) {
        return submissionMapper.deleteSubmissionByIds(ids);
    }

    /**
     * Proposal 0028 P0-2 — 提测拉起测试方案
     * 取现有 submission 校 404,设置 testplanId,走 updateSubmission 复用 P0-1 已有的
     * 跨模块 FK 校验({@link #validateTestplanFk})。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void attachTestplan(Long submissionId, Long testplanId) {
        Submission old = submissionMapper.selectSubmissionById(submissionId);
        if (old == null) {
            throw new ServiceException("提测单不存在", 404);
        }
        Submission upd = new Submission();
        upd.setSubmissionId(submissionId);
        upd.setTestplanId(testplanId);
        updateSubmission(upd);  // 复用 P0-1 validateTestplanFk(同 projectId 强约束 → 702)
    }

    // ─────────────────────────────────────────────────────────────────
    // 私有辅助
    // ─────────────────────────────────────────────────────────────────

    /**
     * Proposal 0028 P0-1 — 提测 ↔ 测试方案 同 projectId 强约束。
     * testplanId 可空(null 直接放行);非空时:
     *   1) 目标 TestPlan 必须存在 → 否则 702
     *   2) 目标 TestPlan.projectId 必须等于 Submission.projectId → 否则 702
     */
    private void validateTestplanFk(Submission s) {
        if (s.getTestplanId() == null) return;
        TestPlan tp = testPlanMapper.selectTestPlanById(s.getTestplanId());
        if (tp == null) {
            throw new ServiceException("关联的测试方案不存在", 702);
        }
        if (s.getProjectId() != null && !s.getProjectId().equals(tp.getProjectId())) {
            throw new ServiceException("提测的测试方案必须属于同一项目", 702);
        }
    }

    /** PRD §F4.4 — 4 项全 Y 才通过门禁 */
    private static String computeQualityGate(Submission s) {
        BigDecimal cov = s.getUnitTestCoverage();
        boolean covOk = cov != null && cov.compareTo(MIN_UNIT_TEST_COVERAGE) >= 0;
        boolean scanOk = "Y".equalsIgnoreCase(s.getCodeScanPassed());
        boolean prdOk  = "Y".equalsIgnoreCase(s.getPrdCompleted());
        boolean apiOk  = "Y".equalsIgnoreCase(s.getApiDocUpdated());
        return (covOk && scanOk && prdOk && apiOk) ? "Y" : "N";
    }

    /** 用 old 作为基线,把更新单中已填的门禁字段覆盖上去,作为重算输入 */
    private static Submission mergeForGateCalc(Submission old, Submission update) {
        Submission m = new Submission();
        m.setUnitTestCoverage(update.getUnitTestCoverage() != null
            ? update.getUnitTestCoverage() : old.getUnitTestCoverage());
        m.setCodeScanPassed(StringUtils.isNotBlank(update.getCodeScanPassed())
            ? update.getCodeScanPassed() : old.getCodeScanPassed());
        m.setPrdCompleted(StringUtils.isNotBlank(update.getPrdCompleted())
            ? update.getPrdCompleted() : old.getPrdCompleted());
        m.setApiDocUpdated(StringUtils.isNotBlank(update.getApiDocUpdated())
            ? update.getApiDocUpdated() : old.getApiDocUpdated());
        return m;
    }

    /** SUB-YYYY-NNNN */
    private String generateSubmissionNo() {
        int year = LocalDate.now().getYear();
        String prefix = "SUB-" + year + "-";
        Integer maxSeq = submissionMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "草稿";
            case "01" -> "已提交";
            case "02" -> "质量门禁中";
            case "03" -> "已通过";
            case "04" -> "已退回";
            default   -> "未知(" + status + ")";
        };
    }
}
