package cn.com.bosssfot.dv.plm.testreport.service.impl;

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
import cn.com.bosssfot.dv.plm.testreport.domain.TestReport;
import cn.com.bosssfot.dv.plm.testreport.mapper.TestReportMapper;
import cn.com.bosssfot.dv.plm.testreport.service.ITestReportService;

/**
 * 测试报告 Service — PRD §F4.7 + 原型 testreport.html
 *
 * 落地:
 * - ADR: generateTestReportNo() — TR-YYYY-NNNN
 * - PRD §F4.7: 上线风险评级 绿 / 黄 / 红
 * - 3 状态机: 00 草稿 → 01 审核中 → 02 已发布
 *   - 00→{01}, 01→{00,02}, 02→{} (终态)
 * - AI 生成时自动填 generatedAt
 */
@Service
public class TestReportServiceImpl implements ITestReportService
{
    private static final Logger log = LoggerFactory.getLogger(TestReportServiceImpl.class);

    private static final Set<String> ALLOWED_RISK_LEVEL = Set.of("green", "yellow", "red");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01"),         // 草稿 → 审核中
        "01", Set.of("00", "02"),   // 审核中 → 草稿(打回) / 已发布
        "02", Set.of()              // 已发布 (终态)
    );

    @Autowired private TestReportMapper testreportMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<TestReport> selectTestReportList(TestReport t) {
        return testreportMapper.selectTestReportList(t);
    }

    @Override
    public TestReport selectTestReportById(Long id) {
        return testreportMapper.selectTestReportById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTestReport(TestReport t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("报告标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (StringUtils.isNotBlank(t.getRiskLevel()) && !ALLOWED_RISK_LEVEL.contains(t.getRiskLevel())) {
            throw new ServiceException("风险级别仅支持 green/yellow/red", 604);
        }

        // 默认值
        if (StringUtils.isBlank(t.getRiskLevel())) t.setRiskLevel("green");
        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (t.getTotalCases() == null)  t.setTotalCases(0);
        if (t.getPassedCases() == null) t.setPassedCases(0);
        if (t.getFailedCases() == null) t.setFailedCases(0);
        if (t.getP0Defects() == null)   t.setP0Defects(0);
        if (t.getP1Defects() == null)   t.setP1Defects(0);
        if (t.getP2Defects() == null)   t.setP2Defects(0);

        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建测试报告状态必须为「草稿」", 601);
        }
        if ("Y".equalsIgnoreCase(t.getAiGenerated()) && t.getGeneratedAt() == null) {
            t.setGeneratedAt(new Date());
        }

        if (StringUtils.isBlank(t.getTestreportNo())) {
            t.setTestreportNo(generateTestreportNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return testreportMapper.insertTestReport(t);
        } catch (DuplicateKeyException e) {
            log.warn("testreport_no 重号,重试一次: {}", t.getTestreportNo());
            t.setTestreportNo(generateTestreportNo());
            return testreportMapper.insertTestReport(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTestReport(TestReport t) {
        TestReport old = testreportMapper.selectTestReportById(t.getTestreportId());
        if (old == null) {
            throw new ServiceException("测试报告不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "测试报告状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }

        if (StringUtils.isNotBlank(t.getRiskLevel()) && !ALLOWED_RISK_LEVEL.contains(t.getRiskLevel())) {
            throw new ServiceException("风险级别仅支持 green/yellow/red", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return testreportMapper.updateTestReport(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTestReportByIds(Long[] ids) {
        return testreportMapper.deleteTestReportByIds(ids);
    }

    private String generateTestreportNo() {
        int year = LocalDate.now().getYear();
        String prefix = "TR-" + year + "-";
        Integer maxSeq = testreportMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "草稿";
            case "01" -> "审核中";
            case "02" -> "已发布";
            default   -> "未知(" + status + ")";
        };
    }
}
