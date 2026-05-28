package cn.com.bosssfot.dv.plm.testplan.service.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    @Autowired private AiService aiService;

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

    /**
     * AI 生成测试方案 (PRD §F4.1 + test-plan-flow)
     * 本期实现:服务端 mock,按已选测试类型 + 周期生成策略/范围/工具/资源/风险。
     * Phase 后续:通过真厂商 provider 接入,业务输出替换为 result.getText()。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestPlan aiGenerate(Long testplanId) {
        TestPlan t = testplanMapper.selectTestPlanById(testplanId);
        if (t == null) {
            throw new ServiceException("测试方案不存在", 404);
        }
        // P0-1: 真 provider 时 strategy 采用 LLM 输出;mock/失败时下方模板兜底。其余结构化字段保持模板。
        AiChatRequest aiReq = AiChatRequest.builder("")
            .system("你是 PLM 资深测试架构师,擅长制定分层测试策略与资源排布")
            .user("请为测试方案 [" + t.getTitle() + "] 生成测试策略、范围、推荐工具、资源分配与风险评估,测试类型:" + t.getTestTypes())
            .callerTag("testplan#" + testplanId)
            .build();

        String typeLabels = testTypesLabel(t.getTestTypes());
        int cycle = t.getTestCycleDays() == null ? 10 : t.getTestCycleDays();

        t.setStrategy(AiTexts.generate(aiService,aiReq, () -> "分层测试策略(" + typeLabels + "):\n"
            + "1. 功能优先,覆盖核心业务路径与边界条件;\n"
            + "2. 接口测试以契约为准,校验状态码/字段/错误码;\n"
            + "3. 自动化覆盖核心回归用例,纳入 CI 每日跑;\n"
            + "4. 性能压测目标 QPS=500、P95<300ms。"));
        t.setScope("覆盖关联需求全部用户故事与对应接口;含正/负/边界三类路径,排除第三方系统内部逻辑。");
        t.setToolsRecommended("playwright,jmeter,postman,selenium");
        t.setResourcesPlan("人员:测试×2 + AI 辅助×1;周期:" + cycle + " 天;环境:staging cluster + 独立测试库。");
        t.setRiskAssessment("1. 农业场景弱网/离线占比高,需补离线用例;\n"
            + "2. 第三方 OCR/IoT 服务稳定性影响接口用例,需 mock 兜底;\n"
            + "3. 测试数据准备周期长,建议提前用测试数据工厂批量造数。");
        t.setAiGenerated("Y");
        t.setUpdateBy(SecurityUtils.getUsername());
        testplanMapper.updateTestPlan(t);
        return t;
    }

    /** functional/api/performance/automation/security CSV → 中文标签 */
    private static String testTypesLabel(String csv) {
        if (StringUtils.isBlank(csv)) {
            return "功能测试";
        }
        return Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .map(code -> switch (code) {
                case "functional"  -> "功能测试";
                case "api"         -> "接口测试";
                case "performance" -> "性能测试";
                case "automation"  -> "自动化测试";
                case "security"    -> "安全测试";
                default            -> code;
            })
            .collect(Collectors.joining("、"));
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
