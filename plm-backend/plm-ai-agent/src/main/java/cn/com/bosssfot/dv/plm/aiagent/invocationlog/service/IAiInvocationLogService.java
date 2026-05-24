package cn.com.bosssfot.dv.plm.aiagent.invocationlog.service;

import java.util.List;
import java.util.Map;
import cn.com.bosssfot.dv.plm.aiagent.invocationlog.domain.AiInvocationLog;

public interface IAiInvocationLogService {
    List<AiInvocationLog> selectAiInvocationLogList(AiInvocationLog q);
    AiInvocationLog selectAiInvocationLogById(Long id);
    int deleteAiInvocationLogByIds(Long[] ids);
    List<Map<String, Object>> getProviderSummary();
}
