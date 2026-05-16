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
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01","02"));
        STATUS_TRANSITIONS.put("01", Set.of("00"));
        STATUS_TRANSITIONS.put("02", Set.of("00","01"));
    }

    @Autowired private AiAgentMapper aiAgentMapper;

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

        long calls = (t.getTotalCalls() == null ? 0L : t.getTotalCalls()) + 1;
        BigDecimal oldRate = t.getSuccessRate() != null ? t.getSuccessRate() : BigDecimal.ZERO;
        // 模拟: 95% 成功率, 用移动平均更新
        BigDecimal newRate = oldRate.multiply(BigDecimal.valueOf(calls - 1))
                .add(BigDecimal.valueOf(95.0))
                .divide(BigDecimal.valueOf(calls), 2, RoundingMode.HALF_UP);
        t.setTotalCalls(calls);
        t.setSuccessRate(newRate);
        t.setLastInvokedAt(new Date());
        t.setUpdateBy("ai-agent");
        aiAgentMapper.updateAiAgent(t);
        return aiAgentMapper.selectAiAgentById(id);
    }

    private String generateAgentNo() {
        int year = LocalDate.now().getYear();
        String prefix = "AGT-" + year + "-";
        Integer maxSeq = aiAgentMapper.selectMaxSeqOfYear(prefix);
        return String.format("%s%04d", prefix, (maxSeq == null ? 0 : maxSeq) + 1);
    }
}
