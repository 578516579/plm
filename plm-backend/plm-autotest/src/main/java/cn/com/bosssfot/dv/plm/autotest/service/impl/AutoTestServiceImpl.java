package cn.com.bosssfot.dv.plm.autotest.service.impl;

import java.math.BigDecimal;
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
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.autotest.domain.AutoTest;
import cn.com.bosssfot.dv.plm.autotest.mapper.AutoTestMapper;
import cn.com.bosssfot.dv.plm.autotest.service.IAutoTestService;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * 自动化测试 Service — PRD §F4.5 + 原型 autotest.html
 * AI 生成脚本 + 定时执行 + 智能根因分析
 * 3 状态机: 00 草稿 → 01 已激活 → 02 已禁用
 */
@Service
public class AutoTestServiceImpl implements IAutoTestService
{
    private static final Logger log = LoggerFactory.getLogger(AutoTestServiceImpl.class);

    private static final Set<String> VALID_SUITE_TYPES = Set.of("ui", "api", "perf", "regression");
    private static final Set<String> VALID_FRAMEWORKS  = Set.of("playwright", "selenium", "jmeter", "cypress");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));         // 草稿 → 已激活
        STATUS_TRANSITIONS.put("01", Set.of("02"));         // 已激活 → 已禁用
        STATUS_TRANSITIONS.put("02", Set.of("01"));         // 已禁用 → 已激活 (反向边)
    }

    @Autowired private AutoTestMapper autotestMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override public List<AutoTest> selectAutoTestList(AutoTest t) { return autotestMapper.selectAutoTestList(t); }
    @Override public AutoTest selectAutoTestById(Long id) { return autotestMapper.selectAutoTestById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAutoTest(AutoTest t) {
        if (StringUtils.isBlank(t.getTitle())) throw new ServiceException("套件名称不能为空", 602);
        if (t.getProjectId() == null) throw new ServiceException("关联项目不能为空", 602);
        if (StringUtils.isBlank(t.getTestSuiteType())) throw new ServiceException("套件类型不能为空", 602);
        if (StringUtils.isBlank(t.getFramework())) throw new ServiceException("测试框架不能为空", 602);
        if (t.getAuthorUserId() == null) throw new ServiceException("创建人不能为空", 602);

        if (!VALID_SUITE_TYPES.contains(t.getTestSuiteType()))
            throw new ServiceException("套件类型非法: " + t.getTestSuiteType(), 604);
        if (!VALID_FRAMEWORKS.contains(t.getFramework()))
            throw new ServiceException("测试框架非法: " + t.getFramework(), 604);

        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) throw new ServiceException("关联项目不存在", 702);

        if (StringUtils.isBlank(t.getScheduleEnabled())) t.setScheduleEnabled("N");
        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (t.getTotalCases() == null) t.setTotalCases(0);
        if (t.getPassedCases() == null) t.setPassedCases(0);
        if (t.getFailedCases() == null) t.setFailedCases(0);
        if (t.getPassRate() == null) t.setPassRate(BigDecimal.ZERO);
        if (t.getExecutionDurationSec() == null) t.setExecutionDurationSec(0);

        if (StringUtils.isBlank(t.getStatus())) t.setStatus("00");
        else if (!"00".equals(t.getStatus()) && !"01".equals(t.getStatus()))
            throw new ServiceException("新建套件状态必须为「草稿」或「已激活」", 601);

        if (StringUtils.isBlank(t.getAutotestNo())) t.setAutotestNo(generateAutotestNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return autotestMapper.insertAutoTest(t);
        } catch (DuplicateKeyException e) {
            log.warn("autotest_no 重号,重试一次: {}", t.getAutotestNo());
            t.setAutotestNo(generateAutotestNo());
            return autotestMapper.insertAutoTest(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAutoTest(AutoTest t) {
        AutoTest old = autotestMapper.selectAutoTestById(t.getAutotestId());
        if (old == null) throw new ServiceException("自动化套件不存在", 404);

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus()))
                throw new ServiceException("套件状态 " + statusLabel(old.getStatus())
                    + " 不能直接转到 " + statusLabel(t.getStatus()), 601);
        }
        if (StringUtils.isNotBlank(t.getTestSuiteType()) && !VALID_SUITE_TYPES.contains(t.getTestSuiteType()))
            throw new ServiceException("套件类型非法: " + t.getTestSuiteType(), 604);
        if (StringUtils.isNotBlank(t.getFramework()) && !VALID_FRAMEWORKS.contains(t.getFramework()))
            throw new ServiceException("测试框架非法: " + t.getFramework(), 604);

        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) throw new ServiceException("关联项目不存在", 702);
        }

        // 自动算 passRate
        Integer total  = t.getTotalCases()  != null ? t.getTotalCases()  : old.getTotalCases();
        Integer passed = t.getPassedCases() != null ? t.getPassedCases() : old.getPassedCases();
        if (total != null && total > 0 && passed != null) {
            BigDecimal rate = BigDecimal.valueOf(passed).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP);
            t.setPassRate(rate);
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return autotestMapper.updateAutoTest(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAutoTestByIds(Long[] ids) { return autotestMapper.deleteAutoTestByIds(ids); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoTest aiGenerate(Long autotestId) {
        AutoTest t = autotestMapper.selectAutoTestById(autotestId);
        if (t == null) throw new ServiceException("自动化套件不存在", 404);

        // mock: Dify auto-test-flow 占位 — 根据框架生成脚本骨架
        String script = generateMockScript(t.getFramework(), t.getTargetUrl());
        t.setScriptContent(script);
        t.setAiGenerated("Y");
        t.setAiGeneratedAt(new Date());
        t.setUpdateBy(SecurityUtils.getUsername());
        autotestMapper.updateAutoTest(t);
        return t;
    }

    private String generateMockScript(String framework, String url) {
        String target = StringUtils.isNotBlank(url) ? url : "http://localhost";
        switch (framework) {
            case "playwright":
                return "import { test, expect } from '@playwright/test'\n\n"
                     + "test('AI generated smoke test', async ({ page }) => {\n"
                     + "  await page.goto('" + target + "')\n"
                     + "  await expect(page).toHaveTitle(/.+/)\n"
                     + "})\n";
            case "selenium":
                return "from selenium import webdriver\n\n"
                     + "driver = webdriver.Chrome()\n"
                     + "driver.get('" + target + "')\n"
                     + "assert driver.title\n"
                     + "driver.quit()\n";
            case "jmeter":
                return "<!-- JMeter Test Plan (AI generated skeleton) -->\n"
                     + "<TestPlan>\n  <ThreadGroup users='100' rampUp='10' duration='60'>\n"
                     + "    <HTTPSamplerProxy url='" + target + "'/>\n"
                     + "  </ThreadGroup>\n</TestPlan>\n";
            case "cypress":
                return "describe('AI generated suite', () => {\n"
                     + "  it('loads page', () => {\n"
                     + "    cy.visit('" + target + "')\n"
                     + "    cy.get('body').should('be.visible')\n"
                     + "  })\n})\n";
            default:
                return "// AI generated script placeholder for " + framework;
        }
    }

    private String generateAutotestNo() {
        int year = LocalDate.now().getYear();
        String prefix = "AT-" + year + "-";
        Integer maxSeq = autotestMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "已激活";
            case "02": return "已禁用";
            default:   return "未知(" + status + ")";
        }
    }
}
