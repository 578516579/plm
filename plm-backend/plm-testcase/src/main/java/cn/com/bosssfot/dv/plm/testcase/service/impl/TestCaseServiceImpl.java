package cn.com.bosssfot.dv.plm.testcase.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementMapper;
import cn.com.bosssfot.dv.plm.testcase.domain.TestCase;
import cn.com.bosssfot.dv.plm.testcase.mapper.TestCaseMapper;
import cn.com.bosssfot.dv.plm.testcase.service.ITestCaseService;

/**
 * 测试用例 Service 实现
 *
 * 落地:
 * - ADR-0006: generateTestCaseNo() TC-YYYY-NNNN
 * - PRD §2.3: 5×5 状态机含反向边 03/04 → 01 (重测)
 * - is_automated='Y' 必填 automation_script_path → 706
 * - /execute 端点专属逻辑: status='02' → 03|04 + execution_count+1 + last_executed_at
 */
@Service
public class TestCaseServiceImpl implements ITestCaseService
{
    private static final Logger log = LoggerFactory.getLogger(TestCaseServiceImpl.class);

    /**
     * 5×5 状态机 (PRD §2.3)
     *            00 草稿  01 待执行  02 执行中  03 已通过  04 已失败
     * 00 草稿      —       ✅        ❌        ❌        ❌
     * 01 待执行   ✅        —        ✅        ❌        ❌
     * 02 执行中   ❌       ✅        —         ✅        ✅
     * 03 已通过   ❌       ✅ 反向   ❌        —         ❌
     * 04 已失败   ❌       ✅ 反向   ❌        ❌        —
     */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01"),
        "01", Set.of("00", "02"),
        "02", Set.of("01", "03", "04"),
        "03", Set.of("01"),   // 反向边 重测
        "04", Set.of("01")    // 反向边 重测
    );

    @Autowired private TestCaseMapper testcaseMapper;
    @Autowired private ProjectMapper projectMapper;
    @Autowired private RequirementMapper requirementMapper;

    @Override public List<TestCase> selectTestCaseList(TestCase t) { return testcaseMapper.selectTestCaseList(t); }
    @Override public TestCase selectTestCaseById(Long id) { return testcaseMapper.selectTestCaseById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTestCase(TestCase t) {
        if (StringUtils.isBlank(t.getTitle())) throw new ServiceException("用例标题不能为空", 602);
        if (StringUtils.isBlank(t.getSteps())) throw new ServiceException("测试步骤不能为空", 602);
        if (StringUtils.isBlank(t.getExpectedResult())) throw new ServiceException("期望结果不能为空", 602);
        if (t.getProjectId() == null) throw new ServiceException("关联项目不能为空", 602);
        if (projectMapper.selectProjectById(t.getProjectId()) == null) throw new ServiceException("关联项目不存在", 702);
        if (t.getRequirementId() != null && requirementMapper.selectRequirementById(t.getRequirementId()) == null) {
            throw new ServiceException("关联需求不存在", 702);
        }

        // 默认值
        if (StringUtils.isBlank(t.getCategory())) t.setCategory("01");
        if (StringUtils.isBlank(t.getPriority())) t.setPriority("01");
        if (StringUtils.isBlank(t.getIsAutomated())) t.setIsAutomated("N");
        if (t.getExecutionCount() == null) t.setExecutionCount(0);

        // is_automated='Y' 必填 script_path
        if ("Y".equalsIgnoreCase(t.getIsAutomated()) && StringUtils.isBlank(t.getAutomationScriptPath())) {
            throw new ServiceException("自动化用例必须填写脚本路径", 706);
        }

        // 新建状态必须 00
        if (StringUtils.isBlank(t.getStatus())) t.setStatus("00");
        else if (!"00".equals(t.getStatus())) throw new ServiceException("新建用例状态必须为「草稿」", 601);

        if (StringUtils.isBlank(t.getTestcaseNo())) t.setTestcaseNo(generateTestCaseNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return testcaseMapper.insertTestCase(t);
        } catch (DuplicateKeyException e) {
            log.warn("testcase_no 重号,重试: {}", t.getTestcaseNo());
            t.setTestcaseNo(generateTestCaseNo());
            return testcaseMapper.insertTestCase(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTestCase(TestCase t) {
        TestCase old = testcaseMapper.selectTestCaseById(t.getTestcaseId());
        if (old == null) throw new ServiceException("用例不存在", 404);

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "用例状态 " + statusLabel(old.getStatus()) + " 不能转到 " + statusLabel(t.getStatus()), 601);
            }
        }

        // FK 校验
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())
                && projectMapper.selectProjectById(t.getProjectId()) == null) {
            throw new ServiceException("关联项目不存在", 702);
        }
        if (t.getRequirementId() != null && !t.getRequirementId().equals(old.getRequirementId())
                && requirementMapper.selectRequirementById(t.getRequirementId()) == null) {
            throw new ServiceException("关联需求不存在", 702);
        }

        // is_automated='Y' 时必填 script_path
        String aut = t.getIsAutomated() != null ? t.getIsAutomated() : old.getIsAutomated();
        String path = t.getAutomationScriptPath() != null ? t.getAutomationScriptPath() : old.getAutomationScriptPath();
        if ("Y".equalsIgnoreCase(aut) && StringUtils.isBlank(path)) {
            throw new ServiceException("自动化用例必须填写脚本路径", 706);
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return testcaseMapper.updateTestCase(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTestCaseByIds(Long[] ids) {
        return testcaseMapper.deleteTestCaseByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeTestCase(Long id, String newStatus, String actualResult) {
        TestCase old = testcaseMapper.selectTestCaseById(id);
        if (old == null) throw new ServiceException("用例不存在", 404);
        if (!"02".equals(old.getStatus())) {
            throw new ServiceException("用例必须先推到「执行中」才能 execute,当前状态: " + statusLabel(old.getStatus()), 601);
        }
        if (!"03".equals(newStatus) && !"04".equals(newStatus)) {
            throw new ServiceException("execute 端点 status 只能传 03(通过) 或 04(失败)", 604);
        }
        TestCase upd = new TestCase();
        upd.setTestcaseId(id);
        upd.setStatus(newStatus);
        upd.setActualResult(actualResult);
        upd.setExecutionCount((old.getExecutionCount() == null ? 0 : old.getExecutionCount()) + 1);
        upd.setLastExecutedAt(new Date());
        upd.setUpdateBy(SecurityUtils.getUsername());
        return testcaseMapper.updateTestCase(upd);
    }

    private String generateTestCaseNo() {
        int year = LocalDate.now().getYear();
        String prefix = "TC-" + year + "-";
        Integer maxSeq = testcaseMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String s) {
        return switch (s) {
            case "00" -> "草稿";
            case "01" -> "待执行";
            case "02" -> "执行中";
            case "03" -> "已通过";
            case "04" -> "已失败";
            default   -> "未知(" + s + ")";
        };
    }
}
