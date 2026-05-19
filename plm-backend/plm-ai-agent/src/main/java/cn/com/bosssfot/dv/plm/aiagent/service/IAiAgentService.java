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
    /** 模拟调用 Agent — 累加 totalCalls + 更新 successRate + lastInvokedAt */
    AiAgent invoke(Long agentId);

    /**
     * 构造 AiChatRequest (V4 Phase 3 新增) — 供流式调用复用 invoke() 的 prompt 拼装逻辑
     *
     * <p>调用方拿到 request 后,可走:</p>
     * <ul>
     *   <li>{@code aiService.chat(req)} — 同步阻塞,即 invoke() 现有方式</li>
     *   <li>{@code aiService.chatStream(req)} — V4 流式,逐 chunk emit</li>
     * </ul>
     */
    AiChatRequest buildChatRequest(Long agentId);
}
