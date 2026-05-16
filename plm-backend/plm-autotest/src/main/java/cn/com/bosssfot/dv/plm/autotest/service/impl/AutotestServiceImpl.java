package cn.com.bosssfot.dv.plm.autotest.service.impl;

import cn.com.bosssfot.dv.plm.autotest.domain.Autotest;
import cn.com.bosssfot.dv.plm.autotest.mapper.AutotestMapper;
import cn.com.bosssfot.dv.plm.autotest.service.IAutotestService;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.DateUtils;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AutotestServiceImpl implements IAutotestService {

    private static final Set<String> ALLOWED_SUITE_TYPE = Set.of("api", "e2e", "unit", "performance");
    private static final Set<String> ALLOWED_FRAMEWORK = Set.of("playwright", "pytest", "jest", "jmeter");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();

    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("00", "02"));
        STATUS_TRANSITIONS.put("02", Set.of());
    }

    @Autowired
    private AutotestMapper autotestMapper;

    @Override
    public List<Autotest> selectAutotestList(Autotest autotest) {
        return autotestMapper.selectAutotestList(autotest);
    }

    @Override
    public Autotest selectAutotestById(Long autotestId) {
        return autotestMapper.selectAutotestById(autotestId);
    }

    @Override
    public int insertAutotest(Autotest autotest) {
        if (autotest.getTitle() == null || autotest.getTitle().isBlank()) {
            throw new ServiceException("套件名称不能为空", 602);
        }
        if (autotest.getProjectId() == null) {
            throw new ServiceException("项目ID不能为空", 602);
        }
        if (autotest.getAuthorUserId() == null) {
            throw new ServiceException("创建者用户ID不能为空", 602);
        }
        validateEnums(autotest);
        autotest.setAutotestNo(generateAutotestNo());
        if (autotest.getTotalCases() == null) autotest.setTotalCases(0);
        if (autotest.getFailedCases() == null) autotest.setFailedCases(0);
        if (autotest.getExecutionTime() == null) autotest.setExecutionTime(0);
        autotest.setAiGenerated("N");
        autotest.setStatus("00");
        autotest.setCreateBy(SecurityUtils.getUsername());
        autotest.setCreateTime(DateUtils.getNowDate());
        autotest.setUpdateBy(SecurityUtils.getUsername());
        autotest.setUpdateTime(DateUtils.getNowDate());
        try {
            return autotestMapper.insertAutotest(autotest);
        } catch (DuplicateKeyException e) {
            autotest.setAutotestNo(generateAutotestNo());
            return autotestMapper.insertAutotest(autotest);
        }
    }

    @Override
    public int updateAutotest(Autotest autotest) {
        if (autotest.getStatus() != null) {
            Autotest existing = autotestMapper.selectAutotestById(autotest.getAutotestId());
            if (existing == null) throw new ServiceException("自动化测试套件不存在", 404);
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(existing.getStatus(), Set.of());
            if (!allowed.contains(autotest.getStatus())) {
                throw new ServiceException("状态不允许从 " + existing.getStatus() + " 流转到 " + autotest.getStatus(), 601);
            }
        }
        validateEnums(autotest);
        autotest.setUpdateBy(SecurityUtils.getUsername());
        autotest.setUpdateTime(DateUtils.getNowDate());
        return autotestMapper.updateAutotest(autotest);
    }

    @Override
    public int deleteAutotestByIds(Long[] autotestIds) {
        return autotestMapper.deleteAutotestByIds(autotestIds);
    }

    @Override
    public Autotest aiScript(Long autotestId) {
        Autotest autotest = autotestMapper.selectAutotestById(autotestId);
        if (autotest == null) throw new ServiceException("自动化测试套件不存在", 404);

        String script = buildAiScript(autotest);
        autotest.setScriptContent(script);
        autotest.setAiGenerated("Y");
        autotest.setAiGeneratedAt(DateUtils.getNowDate());
        autotest.setUpdateBy("ai-agent");
        autotest.setUpdateTime(DateUtils.getNowDate());
        autotestMapper.updateAutotest(autotest);
        return autotestMapper.selectAutotestById(autotestId);
    }

    private String buildAiScript(Autotest autotest) {
        String framework = autotest.getFramework() != null ? autotest.getFramework() : "playwright";
        String type = autotest.getSuiteType() != null ? autotest.getSuiteType() : "api";
        String title = autotest.getTitle();

        if ("playwright".equals(framework)) {
            return "// AI Generated — AgriPLM Auto-Test Script\n" +
                   "// Suite: " + title + " (" + type + ")\n" +
                   "import { test, expect } from '@playwright/test';\n\n" +
                   "test.describe('" + title + "', () => {\n" +
                   "  test('TC-001 核心功能验证', async ({ page }) => {\n" +
                   "    await page.goto('/dev-api/captchaImage');\n" +
                   "    await expect(page).toHaveTitle(/AgriPLM/);\n" +
                   "  });\n\n" +
                   "  test('TC-002 农情大屏数据接口', async ({ request }) => {\n" +
                   "    const res = await request.get('/business/project/list');\n" +
                   "    expect(res.status()).toBe(200);\n" +
                   "    const body = await res.json();\n" +
                   "    expect(body.code).toBe(200);\n" +
                   "  });\n" +
                   "});\n";
        } else if ("pytest".equals(framework)) {
            return "# AI Generated — AgriPLM Auto-Test Script\n" +
                   "# Suite: " + title + " (" + type + ")\n" +
                   "import pytest, requests\n\n" +
                   "BASE_URL = 'http://localhost:8081'\n\n" +
                   "class Test" + title.replaceAll("[^a-zA-Z0-9]", "") + ":\n" +
                   "    def test_health(self):\n" +
                   "        r = requests.get(f'{BASE_URL}/actuator/health')\n" +
                   "        assert r.status_code == 200\n\n" +
                   "    def test_project_list(self, token):\n" +
                   "        headers = {'Authorization': f'Bearer {token}'}\n" +
                   "        r = requests.get(f'{BASE_URL}/business/project/list', headers=headers)\n" +
                   "        assert r.json()['code'] == 200\n";
        } else {
            return "// AI Generated — " + framework + " script for " + title;
        }
    }

    private void validateEnums(Autotest autotest) {
        if (autotest.getSuiteType() != null && !ALLOWED_SUITE_TYPE.contains(autotest.getSuiteType())) {
            throw new ServiceException("无效的套件类型: " + autotest.getSuiteType(), 604);
        }
        if (autotest.getFramework() != null && !ALLOWED_FRAMEWORK.contains(autotest.getFramework())) {
            throw new ServiceException("无效的测试框架: " + autotest.getFramework(), 604);
        }
    }

    private String generateAutotestNo() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "AT-" + year + "-";
        Integer maxSeq = autotestMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return prefix + String.format("%04d", next);
    }
}
