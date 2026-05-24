package cn.com.bosssfot.dv.plm.aiagent.invocationlog.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.aiagent.invocationlog.domain.AiInvocationLog;

/**
 * @author plm
 */
public interface AiInvocationLogMapper {
    int insertAiInvocationLog(AiInvocationLog log);
    List<AiInvocationLog> selectAiInvocationLogList(AiInvocationLog q);
    AiInvocationLog selectAiInvocationLogById(Long logId);
    int deleteAiInvocationLogByIds(Long[] ids);

    /** 汇总 — 按 provider 分组 (count/total_tokens/avg_elapsed_ms/success_rate) */
    List<java.util.Map<String, Object>> selectProviderSummary();
}
