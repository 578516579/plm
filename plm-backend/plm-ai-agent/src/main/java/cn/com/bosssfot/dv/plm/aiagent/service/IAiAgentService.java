package cn.com.bosssfot.dv.plm.aiagent.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;

public interface IAiAgentService {
    List<AiAgent> selectAiAgentList(AiAgent aiAgent);
    AiAgent selectAiAgentById(Long agentId);
    int insertAiAgent(AiAgent aiAgent);
    int updateAiAgent(AiAgent aiAgent);
    int deleteAiAgentByIds(Long[] agentIds);
    /** 模拟调用 Agent — 累加 totalCalls + 更新 successRate + lastInvokedAt */
    AiAgent invoke(Long agentId);
}
