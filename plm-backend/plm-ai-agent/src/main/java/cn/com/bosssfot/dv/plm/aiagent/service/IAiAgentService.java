package cn.com.bosssfot.dv.plm.aiagent.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;

public interface IAiAgentService {
    List<AiAgent> selectAiAgentList(AiAgent aiAgent);
    AiAgent selectAiAgentById(Long agentId);
    int insertAiAgent(AiAgent aiAgent);
    int updateAiAgent(AiAgent aiAgent);
    int deleteAiAgentByIds(Long[] agentIds);
    /** 调用 Agent(无业务上下文)— 累加 totalCalls + 更新 successRate + lastInvokedAt */
    AiAgent invoke(Long agentId);

    /**
     * 调用 Agent 并传入业务上下文 {@code input}(如需求描述 / 代码片段 / 提测摘要)。
     *
     * <p>system prompt 按 agentType 注入对应人设(需求分析 / PRD / 代码审查 / 测试 / 发布 / 运维),
     * user message = 该类型任务指令 + input,让 6 类 Agent 各司其职而非通用占位。</p>
     */
    AiAgent invoke(Long agentId, String input);

    /**
     * 构造 AiChatRequest — 供流式调用复用 invoke() 的 prompt 拼装逻辑
     *
     * <p>调用方拿到 request 后,可走:</p>
     * <ul>
     *   <li>{@code aiService.chat(req)} — 同步阻塞,即 invoke() 现有方式</li>
     *   <li>{@code aiService.chatStream(req)} — 流式,逐 chunk emit</li>
     * </ul>
     */
    AiChatRequest buildChatRequest(Long agentId);

    /** 构造 AiChatRequest 并注入业务上下文 {@code input}(供流式 invoke-stream 复用) */
    AiChatRequest buildChatRequest(Long agentId, String input);
}
