package cn.com.bosssfot.dv.plm.aiagent.invocationlog.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.aiagent.invocationlog.domain.AiInvocationLog;
import cn.com.bosssfot.dv.plm.aiagent.invocationlog.mapper.AiInvocationLogMapper;
import cn.com.bosssfot.dv.plm.aiagent.invocationlog.service.IAiInvocationLogService;
import cn.com.bosssfot.dv.plm.common.ai.AiInvocationRecorder;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;

/**
 * 实现 {@link AiInvocationRecorder} — AiServiceImpl 通过 ObjectProvider 自动接上。
 *
 * <p>关键设计:</p>
 * <ul>
 *   <li>{@code @Transactional(REQUIRES_NEW)} — 审计写库独立事务,业务事务回滚不影响审计</li>
 *   <li>所有异常吃掉(log.warn) — 审计绝不阻塞主链路</li>
 *   <li>caller_tag/provider/model 非空兜底,避免 DB NOT NULL 报错</li>
 * </ul>
 *
 * @author plm
 */
@Service
public class AiInvocationLogServiceImpl implements IAiInvocationLogService, AiInvocationRecorder {
    private static final Logger log = LoggerFactory.getLogger(AiInvocationLogServiceImpl.class);

    @Autowired private AiInvocationLogMapper mapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void record(AiChatRequest request, AiChatResult result) {
        try {
            AiInvocationLog row = new AiInvocationLog();
            row.setCallerTag(orDefault(request.getCallerTag(), "(unknown)"));
            row.setProvider(orDefault(result.getProvider(), orDefault(request.getProvider(), "mock")));
            row.setModel(result.getModel() == null ? request.getModel() : result.getModel());
            row.setSuccess(result.isSuccess() ? 1 : 0);
            // V4 Phase 4: streaming + firstTokenMs
            row.setStreaming(result.isStreaming() ? 1 : 0);
            row.setFirstTokenMs(result.getFirstTokenMs() > 0 ? result.getFirstTokenMs() : null);
            row.setFinishReason(result.getFinishReason());
            row.setPromptTokens(result.getPromptTokens());
            row.setCompletionTokens(result.getCompletionTokens());
            row.setTotalTokens(result.getTotalTokens());
            row.setElapsedMs(result.getElapsedMs());
            row.setRequestId(result.getRequestId());
            row.setErrorMsg(safeShort(result.getError(), 500));
            row.setInvokedAt(new Date());
            mapper.insertAiInvocationLog(row);
        } catch (Exception e) {
            // 审计绝不抛 — 业务主链路不能被它阻塞
            log.warn("[ai-audit] record failed (吞掉): caller={}, provider={}, err={}",
                    request.getCallerTag(), request.getProvider(), e.toString());
        }
    }

    @Override
    public List<AiInvocationLog> selectAiInvocationLogList(AiInvocationLog q) {
        return mapper.selectAiInvocationLogList(q);
    }

    @Override
    public AiInvocationLog selectAiInvocationLogById(Long id) { return mapper.selectAiInvocationLogById(id); }

    @Override
    public int deleteAiInvocationLogByIds(Long[] ids) { return mapper.deleteAiInvocationLogByIds(ids); }

    @Override
    public List<Map<String, Object>> getProviderSummary() { return mapper.selectProviderSummary(); }

    private static String orDefault(String s, String def) { return s == null || s.isBlank() ? def : s; }
    private static String safeShort(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
