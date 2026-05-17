package cn.com.bosssfot.dv.plm.aiagent.service.impl;

import java.time.LocalDate;
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
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;
import cn.com.bosssfot.dv.plm.aiagent.mapper.AiAgentMapper;
import cn.com.bosssfot.dv.plm.aiagent.service.IAiAgentService;

/**
 * AI Agent Service 实现
 *
 * 状态机（aiagents.html 原型 §PRD-MAPPING §34）：
 *   运行中(0) ⇄ 待机(1)
 *   运行中(0) → 异常(2)  [心跳失败，由系统写入，非人工]
 *   异常(2) → 运行中(0) / 待机(1)
 */
@Service
public class AiAgentServiceImpl implements IAiAgentService
{
    private static final Logger log = LoggerFactory.getLogger(AiAgentServiceImpl.class);

    /** 状态机转换矩阵（人工操作允许的转换） */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("0", Set.of("1"));        // 运行中 → 待机
        STATUS_TRANSITIONS.put("1", Set.of("0"));        // 待机 → 运行中
        STATUS_TRANSITIONS.put("2", Set.of("0", "1"));   // 异常 → 运行中 / 待机
    }

    @Autowired
    private AiAgentMapper aiAgentMapper;

    @Override
    public List<AiAgent> selectAiAgentList(AiAgent aiAgent)
    {
        return aiAgentMapper.selectAiAgentList(aiAgent);
    }

    @Override
    public AiAgent selectAiAgentById(Long id)
    {
        return aiAgentMapper.selectAiAgentById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAiAgent(AiAgent aiAgent)
    {
        if (StringUtils.isBlank(aiAgent.getAgentName())) {
            throw new ServiceException("Agent名称不能为空", 601);
        }
        if (StringUtils.isBlank(aiAgent.getModelName())) {
            throw new ServiceException("AI模型不能为空", 601);
        }

        if (StringUtils.isBlank(aiAgent.getStatus())) {
            aiAgent.setStatus("1");  // 默认待机
        } else if (!"1".equals(aiAgent.getStatus()) && !"0".equals(aiAgent.getStatus())) {
            throw new ServiceException("新建Agent初始状态只能为「运行中」或「待机」", 701);
        }

        if (StringUtils.isBlank(aiAgent.getAgentNo())) {
            aiAgent.setAgentNo(generateAgentNo());
        }

        aiAgent.setCreateBy(SecurityUtils.getUsername());

        try {
            return aiAgentMapper.insertAiAgent(aiAgent);
        } catch (DuplicateKeyException e) {
            log.warn("agent_no 重号，重试一次: {}", aiAgent.getAgentNo());
            aiAgent.setAgentNo(generateAgentNo());
            return aiAgentMapper.insertAiAgent(aiAgent);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAiAgent(AiAgent aiAgent)
    {
        if (StringUtils.isNotBlank(aiAgent.getStatus())) {
            AiAgent old = aiAgentMapper.selectAiAgentById(aiAgent.getId());
            if (old == null) {
                throw new ServiceException("Agent不存在", 404);
            }
            String oldStatus = old.getStatus();
            String newStatus = aiAgent.getStatus();
            if (!oldStatus.equals(newStatus)) {
                Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(oldStatus, Set.of());
                if (!allowed.contains(newStatus)) {
                    throw new ServiceException(
                        "状态「" + statusLabel(oldStatus) + "」不能直接转为「" + statusLabel(newStatus) + "」",
                        701
                    );
                }
            }
        }
        aiAgent.setUpdateBy(SecurityUtils.getUsername());
        return aiAgentMapper.updateAiAgent(aiAgent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAiAgentByIds(Long[] ids)
    {
        return aiAgentMapper.deleteAiAgentByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeAgentStatus(Long id, String newStatus)
    {
        AiAgent old = aiAgentMapper.selectAiAgentById(id);
        if (old == null) {
            throw new ServiceException("Agent不存在", 811);
        }
        String oldStatus = old.getStatus();
        if (oldStatus.equals(newStatus)) {
            return 0;
        }
        Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(oldStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new ServiceException(
                "状态「" + statusLabel(oldStatus) + "」不能转为「" + statusLabel(newStatus) + "」",
                701
            );
        }
        AiAgent upd = new AiAgent();
        upd.setId(id);
        upd.setStatus(newStatus);
        upd.setUpdateBy(SecurityUtils.getUsername());
        return aiAgentMapper.updateAiAgent(upd);
    }

    // ─────────────────────────────────────────────────────────────
    // 私有辅助
    // ─────────────────────────────────────────────────────────────

    /** 编号规则 AGT-YYYY-NNNN */
    private String generateAgentNo() {
        int year = LocalDate.now().getYear();
        String prefix = "AGT-" + year + "-";
        Integer maxSeq = aiAgentMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "0": return "运行中";
            case "1": return "待机";
            case "2": return "异常";
            default:  return "未知(" + status + ")";
        }
    }
}
