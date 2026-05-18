package cn.com.bosssfot.dv.plm.autotest.service.impl;

import java.time.LocalDate;
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
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.autotest.domain.Autotest;
import cn.com.bosssfot.dv.plm.autotest.mapper.AutotestMapper;
import cn.com.bosssfot.dv.plm.autotest.service.IAutotestService;

/**
 * 自动化测试套件 Service — PRD §F4.5 + 原型 autotest.html
 *
 * 落地:
 * - ADR: generateAutotestNo() — ATS-YYYY-NNNN
 * - 5 状态机: 00 草稿 → 01 待执行 ↔ 02 执行中 → 03 已完成 → 04 已归档 (终态)
 * - ENUM 白名单: framework → pytest/junit/playwright/cypress → 604
 * - generate() mock: 生成 pytest 脚本桩,aiGenerated='Y',status='01'
 *   Dify 工作流 autotest-gen-flow Phase 后续接入
 */
@Service
public class AutotestServiceImpl implements IAutotestService
{
    private static final Logger log = LoggerFactory.getLogger(AutotestServiceImpl.class);

    private static final Set<String> ALLOWED_FRAMEWORK =
        Set.of("pytest", "junit", "playwright", "cypress");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("02", "00"));
        STATUS_TRANSITIONS.put("02", Set.of("03"));
        STATUS_TRANSITIONS.put("03", Set.of("04", "01"));
        STATUS_TRANSITIONS.put("04", Set.of());
    }

    @Autowired private AutotestMapper autotestMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<Autotest> selectAutotestList(Autotest t) { return autotestMapper.selectAutotestList(t); }

    @Override
    public Autotest selectAutotestById(Long id) { return autotestMapper.selectAutotestById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAutotest(Autotest t) {
        if (StringUtils.isBlank(t.getSuiteName())) {
            throw new ServiceException("套件名称不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (t.getAuthorUserId() == null) {
            throw new ServiceException("创建人不能为空", 602);
        }
        if (StringUtils.isNotBlank(t.getFramework()) && !ALLOWED_FRAMEWORK.contains(t.getFramework())) {
            throw new ServiceException("框架值非法 (允许: pytest/junit/playwright/cypress)", 604);
        }

        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        if (StringUtils.isBlank(t.getAiGenerated())) t.setAiGenerated("N");
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建自动化测试套件状态必须为「草稿」", 601);
        }

        if (StringUtils.isBlank(t.getAutotestNo())) {
            t.setAutotestNo(generateAutotestNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return autotestMapper.insertAutotest(t);
        } catch (DuplicateKeyException e) {
            log.warn("autotest_no 重号,重试一次: {}", t.getAutotestNo());
            t.setAutotestNo(generateAutotestNo());
            return autotestMapper.insertAutotest(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAutotest(Autotest t) {
        Autotest old = autotestMapper.selectAutotestById(t.getAutotestId());
        if (old == null) {
            throw new ServiceException("自动化测试套件不存在", 404);
        }
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "套件状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }
        }
        if (StringUtils.isNotBlank(t.getFramework()) && !ALLOWED_FRAMEWORK.contains(t.getFramework())) {
            throw new ServiceException("框架值非法 (允许: pytest/junit/playwright/cypress)", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        return autotestMapper.updateAutotest(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAutotestByIds(Long[] ids) {
        return autotestMapper.deleteAutotestByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Autotest generate(Long autotestId) {
        Autotest at = autotestMapper.selectAutotestById(autotestId);
        if (at == null) {
            throw new ServiceException("自动化测试套件不存在", 404);
        }
        String framework = StringUtils.isNotBlank(at.getFramework()) ? at.getFramework() : "pytest";
        String module = StringUtils.isNotBlank(at.getTargetModule()) ? at.getTargetModule() : "target_module";
        String script = "# -*- coding: utf-8 -*-\n"
            + "# AI 生成的 " + framework + " 测试脚本桩 — PRD §F4.5\n"
            + "# 套件: " + at.getSuiteName() + "\n"
            + "# 目标模块: " + module + "\n\n"
            + "import pytest\n\n\n"
            + "class Test" + toPascalCase(module) + ":\n\n"
            + "    def test_create_success(self):\n"
            + "        \"\"\"正常创建 — 期望 HTTP 200\"\"\"\n"
            + "        # TODO: 接入 Dify autotest-gen-flow 后替换为真实实现\n"
            + "        assert True\n\n"
            + "    def test_create_invalid_param(self):\n"
            + "        \"\"\"非法参数 — 期望 HTTP 400\"\"\"\n"
            + "        assert True\n\n"
            + "    def test_list_pagination(self):\n"
            + "        \"\"\"分页列表 — 期望返回 total > 0\"\"\"\n"
            + "        assert True\n";
        at.setScriptContent(script);
        at.setAiGenerated("Y");
        at.setStatus("01");
        at.setUpdateBy(SecurityUtils.getUsername());
        autotestMapper.updateAutotest(at);
        return at;
    }

    private String generateAutotestNo() {
        int year = LocalDate.now().getYear();
        String prefix = "ATS-" + year + "-";
        Integer maxSeq = autotestMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "草稿";
            case "01": return "待执行";
            case "02": return "执行中";
            case "03": return "已完成";
            case "04": return "已归档";
            default:   return "未知(" + status + ")";
        }
    }

    /** 简单 snake_case → PascalCase，用于脚本类名生成 */
    private static String toPascalCase(String s) {
        if (s == null || s.isEmpty()) return "Unknown";
        StringBuilder sb = new StringBuilder();
        for (String part : s.split("[_\\-]")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }
}
