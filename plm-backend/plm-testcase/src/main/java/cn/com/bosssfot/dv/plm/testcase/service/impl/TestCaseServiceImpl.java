package cn.com.bosssfot.dv.plm.testcase.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.core.event.EntityChangedEvent.Action;
import cn.com.bosssfot.dv.plm.common.core.event.TestCaseChangedEvent;
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
    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private AiService aiService;

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

        int rows;
        try {
            rows = testcaseMapper.insertTestCase(t);
        } catch (DuplicateKeyException e) {
            log.warn("testcase_no 重号,重试: {}", t.getTestcaseNo());
            t.setTestcaseNo(generateTestCaseNo());
            rows = testcaseMapper.insertTestCase(t);
        }
        if (rows > 0 && t.getTestcaseId() != null) {
            eventPublisher.publishEvent(new TestCaseChangedEvent(t.getTestcaseId(), Action.INSERT));
        }
        return rows;
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
        int rows = testcaseMapper.updateTestCase(t);
        if (rows > 0) {
            eventPublisher.publishEvent(new TestCaseChangedEvent(t.getTestcaseId(), Action.UPDATE));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTestCaseByIds(Long[] ids) {
        int rows = testcaseMapper.deleteTestCaseByIds(ids);
        if (rows > 0 && ids != null) {
            for (Long id : ids) {
                eventPublisher.publishEvent(new TestCaseChangedEvent(id, Action.DELETE));
            }
        }
        return rows;
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
        int rows = testcaseMapper.updateTestCase(upd);
        if (rows > 0) {
            eventPublisher.publishEvent(new TestCaseChangedEvent(id, Action.UPDATE));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCase aiGenerate(Long id) {
        TestCase t = testcaseMapper.selectTestCaseById(id);
        if (t == null) throw new ServiceException("用例不存在", 404);
        if (StringUtils.isBlank(t.getTitle())) throw new ServiceException("请先填写用例标题再用 AI 生成", 602);

        AiChatResult result = aiService.chat(AiChatRequest.builder("")
            .system("你是 AgriPLM 资深测试工程师,擅长农业 IoT / DevOps 场景的用例设计。"
                + "根据用例标题与描述,产出可执行的测试用例要素。"
                + "只输出一个 JSON 对象,字段: preconditions(前置条件), steps(测试步骤,多步用换行分隔), "
                + "expectedResult(预期结果)。不要解释,不要 Markdown 代码块围栏。")
            .user(buildAiPrompt(t))
            .temperature(0.3)
            .maxTokens(1024)
            .callerTag("testcase#" + id)
            .build());

        // 真 provider 失败 → 708(与 OpenSpec / AiAgent 一致,不静默吞错);Mock 永远 success
        if (!result.isSuccess()) {
            log.warn("[TestCase#{}] AI 用例生成失败 provider={}, error={}",
                    t.getTestcaseNo(), result.getProvider(), result.getError());
            throw new ServiceException("AI 用例生成失败: " + result.getError(), 708);
        }

        // 真 LLM 且能解析出 JSON 字段 → 落库;否则(mock / 解析失败)退回确定性骨架,保证 dev/CI 可用
        boolean applied = false;
        if (!"mock".equalsIgnoreCase(result.getProvider()) && StringUtils.isNotBlank(result.getText())) {
            applied = applyAiJson(t, result.getText());
        }
        if (!applied) {
            applyTemplate(t);
        }

        t.setUpdateBy("ai-agent");
        testcaseMapper.updateTestCase(t);
        log.info("[TestCase#{}] AI 用例生成完成 provider={}, source={}",
                t.getTestcaseNo(), result.getProvider(), applied ? "llm" : "template");
        return testcaseMapper.selectTestCaseById(id);
    }

    private static String buildAiPrompt(TestCase t) {
        StringBuilder sb = new StringBuilder();
        sb.append("用例标题: ").append(t.getTitle()).append("\n");
        if (StringUtils.isNotBlank(t.getDescription())) sb.append("用例描述: ").append(t.getDescription()).append("\n");
        if (StringUtils.isNotBlank(t.getCategory()))    sb.append("用例分类码: ").append(t.getCategory()).append("\n");
        sb.append("请给出该用例的前置条件、可执行步骤、预期结果。");
        return sb.toString();
    }

    /** 解析 LLM 返回的 JSON,填充 preconditions/steps/expectedResult。成功填到 steps 或 expectedResult 则返回 true */
    private boolean applyAiJson(TestCase t, String aiText) {
        try {
            JSONObject obj = JSON.parseObject(stripFence(aiText));
            if (obj == null) return false;
            String pre = obj.getString("preconditions");
            String steps = obj.getString("steps");
            String exp = obj.getString("expectedResult");
            if (StringUtils.isNotBlank(pre))   t.setPreconditions(pre.trim());
            if (StringUtils.isNotBlank(steps)) t.setSteps(steps.trim());
            if (StringUtils.isNotBlank(exp))   t.setExpectedResult(exp.trim());
            return StringUtils.isNotBlank(steps) || StringUtils.isNotBlank(exp);
        } catch (Exception e) {
            log.warn("[TestCase#{}] AI 输出非合法 JSON,退回模板: {}", t.getTestcaseNo(), e.toString());
            return false;
        }
    }

    /** mock / 解析失败时的确定性骨架,保证 AI 生成始终产出可读用例要素 */
    private static void applyTemplate(TestCase t) {
        String title = t.getTitle();
        if (StringUtils.isBlank(t.getPreconditions())) {
            t.setPreconditions("1. 系统已部署并可访问\n2. 测试账号已就绪");
        }
        t.setSteps("1. 进入「" + title + "」相关功能入口\n2. 按正常路径执行主流程\n3. 输入边界值与非法值各验证一次");
        t.setExpectedResult("正常路径成功并给出正确反馈;边界/非法输入被正确校验拦截并提示。");
    }

    /** LLM 偶尔用 ```json ... ``` 包裹,解析前剥掉围栏 */
    private static String stripFence(String text) {
        String s = text.trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl > 0) s = s.substring(nl + 1);
            if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        }
        return s.trim();
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
