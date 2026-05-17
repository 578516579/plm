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
    @Transactional(rollbackFor = Exception.class)
    public AiAgent invoke(Long id) {
        AiAgent t = aiAgentMapper.selectAiAgentById(id);
        if (t == null) throw new ServiceException("AI Agent 不存在", 404);
        if (!"00".equals(t.getStatus()))
            throw new ServiceException("Agent 当前状态不可调用 (需运行中)", 601);

        // === 1. 构造统一 AiChatRequest,按 agent.provider 路由 ===
        String provider = StringUtils.isBlank(t.getProvider()) ? "mock" : t.getProvider().toLowerCase();

        // provider=dify 时,model 字段语义为 workflow_id (优先 dify_workflow_id);
        // 其他 provider 时,model 字段直接是模型名 (gpt-4o-mini / deepseek-chat / claude-sonnet-4-5)
        String modelOrWorkflow;
        if ("dify".equals(provider)) {
            modelOrWorkflow = StringUtils.isNotBlank(t.getDifyWorkflowId())
                    ? t.getDifyWorkflowId() : t.getModelName();
        } else {
            modelOrWorkflow = t.getModelName();
        }

        // 系统指令优先用 prompt_template,否则用 description
        String systemPrompt = StringUtils.isNotBlank(t.getPromptTemplate())
                ? t.getPromptTemplate()
                : (StringUtils.isBlank(t.getDescription()) ? "你是 PLM 系统中的 AI Agent" : t.getDescription());

        // 简单的"用户问题":让 Agent 自检/巡检,真实接入由各业务模块拼装
        String userMsg = String.format("请执行 [%s] 类型 Agent '%s' 的任务", t.getAgentType(), t.getAgentName());

        AiChatRequest req = AiChatRequest.builder(provider)
                .model(modelOrWorkflow)
                .system(systemPrompt)
                .user(userMsg)
                .temperature(0.5)
                .maxTokens(2000)
                .callerTag("ai-agent#" + t.getAgentNo())
                // 给 dify 多传一份元信息作为 inputs 兜底
                .difyInput("agent_no", t.getAgentNo())
                .difyInput("agent_type", t.getAgentType())
                .difyInput("description", t.getDescription() == null ? "" : t.getDescription())
                .build();

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
}
