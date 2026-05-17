package cn.com.bosssfot.dv.plm.aiagent.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;

/**
 * AI Agent Mapper 接口
 */
public interface AiAgentMapper
{
    /** 查询 AI Agent 列表 */
    List<AiAgent> selectAiAgentList(AiAgent aiAgent);

    /** 根据 ID 查询 AI Agent */
    AiAgent selectAiAgentById(Long id);

    /** 新增 AI Agent */
    int insertAiAgent(AiAgent aiAgent);

    /** 修改 AI Agent */
    int updateAiAgent(AiAgent aiAgent);

    /** 逻辑删除（del_flag = '2'） */
    int deleteAiAgentByIds(Long[] ids);

    /** 编号生成辅助：查当年以 prefix 开头的最大流水号 */
    Integer selectMaxSeqOfYear(String prefix);
}
