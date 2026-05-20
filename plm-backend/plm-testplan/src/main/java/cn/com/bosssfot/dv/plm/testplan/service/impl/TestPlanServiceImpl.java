package cn.com.bosssfot.dv.plm.testplan.service.impl;

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
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.testplan.domain.TestPlan;
import cn.com.bosssfot.dv.plm.testplan.mapper.TestPlanMapper;
import cn.com.bosssfot.dv.plm.testplan.service.ITestPlanService;

/**
 * 测试方案 Service — PRD §F4.1 + 原型 testplan.html
 *
 * 落地:
 * - ADR: generateTestPlanNo() — TP-YYYY-NNNN
 * - PRD §F4.1: AI 生成测试策略 + 范围 + 资源 + 风险评估
 * - 4 状态机: 00 草稿 → 01 已确认 → 02 执行中 → 03 已完成
 *   - 00→{01}, 01→{00,02}, 02→{03}, 03→{} (终态)
 */
@Service
public class TestPlanServiceImpl implements ITestPlanService
{
    private static final Logger log = LoggerFactory.getLogger(TestPlanServiceImpl.class);

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01"),
        "01", Set.of("00", "02"),
        "02", Set.of("03"),
        "03", Set.of()
    );

    @Autowired private TestPlanMapper testplanMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<TestPlan> selectTestPlanList(TestPlan t) {
        return testplanMapper.selectTestPlanList(t);
    }

    @Override
    public TestPlan selectTestPlanById(Long id) {
        return testplanMapper.selectTestPlanById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTestPlan(TestPlan t) {
        if (StringUtils.isBlank(t.getTitle())) {
            throw new ServiceException("方案标题不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (StringUtils.isBlank(t.getTestTypes())) {
            throw new ServiceException("测试类型不能为空", 602);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("撰写人不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (t.getTestCycleDays() == null) t.setTestCycleDays(10);
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建测试方案状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getTestplanNo())) {
            t.setTestplanNo(generateTestplanNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return testplanMapper.insertTestPlan(t);
        } catch (DuplicateKeyException e) {
            log.warn("testplan_no 重号,重试一次: {}", t.getTestplanNo());
            t.setTestplanNo(generateTestplanNo());
            return testplanMapper.insertTestPlan(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTestPlan(TestPlan t) {
        TestPlan old = testplanMapper.selectTestPlanById(t.getTestplanId());
        if (old == null) {
            throw new ServiceException("测试方案不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "测试方案状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }

        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return testplanMapper.updateTestPlan(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTestPlanByIds(Long[] ids) {
        return testplanMapper.deleteTestPlanByIds(ids);
    }

    private String generateTestplanNo() {
        int year = LocalDate.now().getYear();
        String prefix = "TP-" + year + "-";
        Integer maxSeq = testplanMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "草稿";
            case "01" -> "已确认";
            case "02" -> "执行中";
            case "03" -> "已完成";
            default   -> "未知(" + status + ")";
        };
    }
}
