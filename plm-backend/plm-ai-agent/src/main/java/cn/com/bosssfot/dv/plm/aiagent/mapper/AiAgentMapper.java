package cn.com.bosssfot.dv.plm.aiagent.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;

public interface AiAgentMapper {
    List<AiAgent> selectAiAgentList(AiAgent aiAgent);
    AiAgent selectAiAgentById(Long agentId);
    int insertAiAgent(AiAgent aiAgent);
    int updateAiAgent(AiAgent aiAgent);
    int deleteAiAgentByIds(Long[] agentIds);
    Integer selectMaxSeqOfYear(String prefix);
}
