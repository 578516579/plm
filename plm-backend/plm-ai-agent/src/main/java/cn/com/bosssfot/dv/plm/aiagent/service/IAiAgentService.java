package cn.com.bosssfot.dv.plm.aiagent.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;

/**
 * AI Agent Service 接口
 */
public interface IAiAgentService
{
    /** 查询列表 */
    List<AiAgent> selectAiAgentList(AiAgent aiAgent);

    /** 根据 ID 查询 */
    AiAgent selectAiAgentById(Long id);

    /** 新增 */
    int insertAiAgent(AiAgent aiAgent);

    /** 修改 */
    int updateAiAgent(AiAgent aiAgent);

    /** 批量逻辑删除 */
    int deleteAiAgentByIds(Long[] ids);

    /**
     * 切换 Agent 状态（启动/暂停）
     *
     * 允许转换：运行中(0)→待机(1)、待机(1)→运行中(0)、异常(2)→运行中(0)/待机(1)
     *
     * @param id       Agent 主键
     * @param newStatus 目标状态
     */
    int changeAgentStatus(Long id, String newStatus);
}
