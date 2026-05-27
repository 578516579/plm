package cn.com.bosssfot.dv.plm.aiagent.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;
import cn.com.bosssfot.dv.plm.aiagent.mapper.AiAgentMapper;
import cn.com.bosssfot.dv.plm.aiagent.service.IAiAgentService;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;

/**
 * AI Agent Service — PRD §F3.5 + 原型 aiagents.html
 * 6 类 Agent + Dify 工作流; 3 状态 (运行中/已停止/错误)
 */
@Service
public class AiAgentServiceImpl implements IAiAgentService {
    private static final Logger log = LoggerFactory.getLogger(AiAgentServiceImpl.class);

    private static final Set<String> ALLOWED_TYPE = Set.of("requirement","prd","code","test","release","ops");
    private static final Set<String> ALLOWED_PROVIDER = Set.of("mock","dify","openai","anthropic");
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01","02"));
        STATUS_TRANSITIONS.put("01", Set.of("00"));
        STATUS_TRANSITIONS.put("02", Set.of("00","01"));
    }

    /**
     * 各类 Agent 的默认人设(system prompt)。Agent 未配 promptTemplate 时按 agentType 取此默认,
     * 让 6 类 Agent 真正各司其职,而非统一一句"你是 AI Agent"。
     */
    private static final Map<String, String> TYPE_SYSTEM_PROMPT = Map.of(
        "requirement", "你是 AgriPLM 的资深需求分析 Agent,擅长把粗略的农业数字化想法拆解为结构化、可验收的需求条目。"
            + "逐条输出 [标题] [角色/场景] [验收标准] [优先级建议],只输出需求分析结果,不要寒暄。",
        "prd", "你是 AgriPLM 的资深产品经理 Agent,擅长基于需求产出结构化 PRD:"
            + "背景与目标 / 用户故事 / 功能点拆解 / 非功能需求 / 验收标准 / 风险与依赖。",
        "code", "你是 AgriPLM 的资深代码审查 Agent,聚焦缺陷、空指针、并发与事务问题、SQL 注入/越权等安全隐患、性能反模式。"
            + "逐条输出 [问题] [风险等级] [改进建议],并附最小修复示例。",
        "test", "你是 AgriPLM 的资深测试 Agent,擅长基于需求或代码生成覆盖正向 / 反向 / 边界 / 异常的测试用例,"
            + "每条含 [前置条件] [步骤] [预期结果]。",
        "release", "你是 AgriPLM 的发布评审 Agent,核对发布清单、数据库迁移、回滚预案、灰度策略与风险项,"
            + "输出 [可发布 / 阻塞] 结论及理由清单。",
        "ops", "你是 AgriPLM 的运维巡检 Agent,根据系统指标与日志识别异常征兆,"
            + "给出 [现象] [可能根因] [处置建议] [紧急程度]。"
    );

    /** 各类 Agent 的默认任务指令(user message),在调用方未传入业务上下文时使用 */
    private static final Map<String, String> TYPE_TASK = Map.of(
        "requirement", "请对目标需求进行结构化分析。",
        "prd", "请基于需求生成一份结构化 PRD 大纲。",
        "code", "请对目标代码进行审查并列出问题清单。",
        "test", "请为目标需求/代码生成测试用例集。",
        "release", "请对本次发布进行评审并给出结论。",
        "ops", "请执行一次运维巡检并给出报告。"
    );

    @Autowired private AiAgentMapper aiAgentMapper;
    @Autowired private AiService aiService;

    @Override
    public List<AiAgent> selectAiAgentList(AiAgent t) { return aiAgentMapper.selectAiAgentList(t); }

    @Override
    public AiAgent selectAiAgentById(Long id) { return aiAgentMapper.selectAiAgentById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAiAgent(AiAgent t) {
        if (StringUtils.isBlank(t.getAgentName())) throw new ServiceException("Agent 名称不能为空", 602);
        if (StringUtils.isBlank(t.getAgentType())) throw new ServiceException("Agent 类型不能为空", 602);
        if (!ALLOWED_TYPE.contains(t.getAgentType()))
            throw new ServiceException("无效的 Agent 类型: " + t.getAgentType(), 604);
        if (t.getAuthorUserId() == null) throw new ServiceException("创建者不能为空", 602);
        // provider 校验:默认 mock,非法值挡掉
        if (StringUtils.isBlank(t.getProvider())) t.setProvider("mock");
        if (!ALLOWED_PROVIDER.contains(t.getProvider().toLowerCase()))
            throw new ServiceException("无效的 provider: " + t.getProvider() + ",允许 " + ALLOWED_PROVIDER, 604);

        if (t.getTotalCalls() == null)  t.setTotalCalls(0L);
        if (t.getSuccessRate() == null) t.setSuccessRate(BigDecimal.ZERO);
        if (StringUtils.isBlank(t.getStatus())) t.setStatus("00");
        if (StringUtils.isBlank(t.getAgentNo())) t.setAgentNo(generateAgentNo());
        t.setCreateBy(SecurityUtils.getUsername());
        try {
            return aiAgentMapper.insertAiAgent(t);
        } catch (DuplicateKeyException e) {
            log.warn("agent_no 重号,重试: {}", t.getAgentNo());
            t.setAgentNo(generateAgentNo());
            return aiAgentMapper.insertAiAgent(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAiAgent(AiAgent t) {
        AiAgent old = aiAgentMapper.selectAiAgentById(t.getAgentId());
        if (old == null) throw new ServiceException("AI Agent 不存在", 404);
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus()))
                throw new ServiceException("Agent 状态不能从 " + old.getStatus() + " 转到 " + t.getStatus(), 601);
        }
        if (t.getAgentType() != null && !ALLOWED_TYPE.contains(t.getAgentType()))
            throw new ServiceException("无效的 Agent 类型: " + t.getAgentType(), 604);
        if (t.getProvider() != null && !ALLOWED_PROVIDER.contains(t.getProvider().toLowerCase()))
            throw new ServiceException("无效的 provider: " + t.getProvider() + ",允许 " + ALLOWED_PROVIDER, 604);
        t.setUpdateBy(SecurityUtils.getUsername());
        return aiAgentMapper.updateAiAgent(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAiAgentByIds(Long[] ids) { return aiAgentMapper.deleteAiAgentByIds(ids); }

    @Override
    public AiAgent invoke(Long id) {
        return invoke(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiAgent invoke(Long id, String input) {
        AiAgent t = aiAgentMapper.selectAiAgentById(id);
        if (t == null) throw new ServiceException("AI Agent 不存在", 404);
        if (!"00".equals(t.getStatus()))
            throw new ServiceException("Agent 当前状态不可调用 (需运行中)", 601);

        // === 1. 构造统一 AiChatRequest,按 agent.provider 路由 + 按 agentType 注入人设/任务 ===
        AiChatRequest req = buildChatRequestForAgent(t, input);

        AiChatResult result = aiService.chat(req);

        // === 2. 真实成功率统计 (移动平均) ===
        long calls = (t.getTotalCalls() == null ? 0L : t.getTotalCalls()) + 1;
        BigDecimal oldRate = t.getSuccessRate() != null ? t.getSuccessRate() : BigDecimal.ZERO;
        BigDecimal thisCall = result.isSuccess() ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        BigDecimal newRate = oldRate.multiply(BigDecimal.valueOf(calls - 1))
                .add(thisCall)
                .divide(BigDecimal.valueOf(calls), 2, RoundingMode.HALF_UP);
        t.setTotalCalls(calls);
        t.setSuccessRate(newRate);
        t.setLastInvokedAt(new Date());
        t.setUpdateBy("ai-agent");
        aiAgentMapper.updateAiAgent(t);

        // === 3. 失败抛 708 ===
        if (!result.isSuccess()) {
            log.warn("[AiAgent#{}] AI 调用失败 provider={}, error={}",
                    t.getAgentNo(), result.getProvider(), result.getError());
            throw new ServiceException("AI 调用失败: " + result.getError(), 708);
        }
        log.info("[AiAgent#{}] AI 调用成功 provider={}, model={}, tokens={}, elapsed={}ms",
                t.getAgentNo(), result.getProvider(), result.getModel(),
                result.getTotalTokens(), result.getElapsedMs());
        return aiAgentMapper.selectAiAgentById(id);
    }

    private String generateAgentNo() {
        int year = LocalDate.now().getYear();
        String prefix = "AGT-" + year + "-";
        Integer maxSeq = aiAgentMapper.selectMaxSeqOfYear(prefix);
        return String.format("%s%04d", prefix, (maxSeq == null ? 0 : maxSeq) + 1);
    }

    @Override
    public AiChatRequest buildChatRequest(Long agentId) {
        return buildChatRequest(agentId, null);
    }

    @Override
    public AiChatRequest buildChatRequest(Long agentId, String input) {
        AiAgent t = aiAgentMapper.selectAiAgentById(agentId);
        if (t == null) throw new ServiceException("AI Agent 不存在", 404);
        if (!"00".equals(t.getStatus()))
            throw new ServiceException("Agent 当前状态不可调用 (需运行中)", 601);
        return buildChatRequestForAgent(t, input);
    }

    /**
     * invoke() / buildChatRequest() 共用的 request 拼装逻辑。
     *
     * <p>system prompt 取值优先级:agent 自定义 promptTemplate &gt; agentType 默认人设 &gt; description &gt; 通用兜底。
     * user message = 该类型默认任务指令 +(可选)调用方传入的业务上下文 {@code input}
     * (如需求描述 / 代码片段 / 提测摘要)。</p>
     */
    private AiChatRequest buildChatRequestForAgent(AiAgent t, String input) {
        String provider = StringUtils.isBlank(t.getProvider()) ? "mock" : t.getProvider().toLowerCase();
        String type = t.getAgentType() == null ? "" : t.getAgentType();

        // provider=dify 时,model 字段语义为 workflow_id (优先 dify_workflow_id)
        String modelOrWorkflow;
        if ("dify".equals(provider)) {
            modelOrWorkflow = StringUtils.isNotBlank(t.getDifyWorkflowId())
                    ? t.getDifyWorkflowId() : t.getModelName();
        } else {
            modelOrWorkflow = t.getModelName();
        }

        // 系统指令:自定义 promptTemplate > agentType 人设 > description > 通用兜底
        String systemPrompt;
        if (StringUtils.isNotBlank(t.getPromptTemplate())) {
            systemPrompt = t.getPromptTemplate();
        } else if (TYPE_SYSTEM_PROMPT.containsKey(type)) {
            systemPrompt = TYPE_SYSTEM_PROMPT.get(type);
        } else {
            systemPrompt = StringUtils.isBlank(t.getDescription()) ? "你是 PLM 系统中的 AI Agent" : t.getDescription();
        }

        // 用户消息:该类型默认任务指令 +(可选)调用方传入的业务上下文
        String instruction = TYPE_TASK.getOrDefault(type,
                String.format("请执行 [%s] 类型 Agent '%s' 的任务", type, t.getAgentName()));
        String userMsg = StringUtils.isNotBlank(input)
                ? instruction + "\n\n输入内容:\n" + input.trim()
                : instruction;

        return AiChatRequest.builder(provider)
                .model(modelOrWorkflow)
                .system(systemPrompt)
                .user(userMsg)
                .temperature(0.5)
                .maxTokens(2000)
                .callerTag("ai-agent#" + t.getAgentNo())
                .difyInput("agent_no", t.getAgentNo())
                .difyInput("agent_type", t.getAgentType())
                .difyInput("description", t.getDescription() == null ? "" : t.getDescription())
                .difyInput("input", input == null ? "" : input)
                .build();
    }
}
