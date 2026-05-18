package cn.com.bosssfot.dv.plm.pipeline.service.impl;

import java.time.LocalDate;
import java.util.Arrays;
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
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;
import cn.com.bosssfot.dv.plm.pipeline.mapper.PipelineMapper;
import cn.com.bosssfot.dv.plm.pipeline.service.IPipelineService;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;

/**
 * CI/CD流水线 Service 实现
 *
 * 落地:
 * - 编号规则: PIP-YYYY-NNNN
 * - 状态机: 00=空闲 ↔ {01=运行中,02=已暂停}, 01→{00,03}, 02→{00,03}, 03=已停用(终态)
 * - repository 白名单: backend/frontend/ai_service/infra
 * - triggerType 白名单: push/schedule/manual/mr
 * - 默认 status='00', branch='main'
 */
@Service
public class PipelineServiceImpl implements IPipelineService
{
    private static final Logger log = LoggerFactory.getLogger(PipelineServiceImpl.class);

    /**
     * 状态机转换矩阵
     * 00=空闲, 01=运行中, 02=已暂停, 03=已停用
     */
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01", "02"));   // 空闲 → 运行中 / 已暂停
        STATUS_TRANSITIONS.put("01", Set.of("00", "03"));   // 运行中 → 空闲 / 已停用
        STATUS_TRANSITIONS.put("02", Set.of("00", "03"));   // 已暂停 → 空闲 / 已停用
        STATUS_TRANSITIONS.put("03", Set.of());              // 已停用 (终态)
    }

    private static final List<String> VALID_REPOS = Arrays.asList("backend", "frontend", "ai_service", "infra");
    private static final List<String> VALID_TRIGGERS = Arrays.asList("push", "schedule", "manual", "mr");

    @Autowired private PipelineMapper pipelineMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<Pipeline> selectPipelineList(Pipeline pipeline) {
        return pipelineMapper.selectPipelineList(pipeline);
    }

    @Override
    public Pipeline selectPipelineById(Long pipelineId) {
        return pipelineMapper.selectPipelineById(pipelineId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPipeline(Pipeline pipeline)
    {
        if (StringUtils.isBlank(pipeline.getPipelineName())) throw new ServiceException("流水线名称不能为空", 602);
        if (pipeline.getProjectId() == null) throw new ServiceException("关联项目不能为空", 602);

        // repository 白名单
        if (StringUtils.isNotBlank(pipeline.getRepository())
                && !VALID_REPOS.contains(pipeline.getRepository())) {
            throw new ServiceException("仓库类型必须为 backend/frontend/ai_service/infra", 601);
        }

        // triggerType 白名单
        if (StringUtils.isNotBlank(pipeline.getTriggerType())
                && !VALID_TRIGGERS.contains(pipeline.getTriggerType())) {
            throw new ServiceException("触发方式必须为 push/schedule/manual/mr", 601);
        }

        // FK 校验
        if (projectMapper.selectProjectById(pipeline.getProjectId()) == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        // 默认值
        if (StringUtils.isBlank(pipeline.getStatus())) pipeline.setStatus("00");
        else if (!"00".equals(pipeline.getStatus())) {
            throw new ServiceException("新建流水线状态必须为「空闲」", 601);
        }
        if (StringUtils.isBlank(pipeline.getBranch())) pipeline.setBranch("main");
        if (pipeline.getSuccessCount() == null) pipeline.setSuccessCount(0);
        if (pipeline.getFailedCount() == null) pipeline.setFailedCount(0);
        if (pipeline.getAuthorUserId() == null) pipeline.setAuthorUserId(SecurityUtils.getUserId());

        if (StringUtils.isBlank(pipeline.getPipelineNo())) pipeline.setPipelineNo(generatePipelineNo());
        pipeline.setCreateBy(SecurityUtils.getUsername());

        try {
            return pipelineMapper.insertPipeline(pipeline);
        } catch (DuplicateKeyException e) {
            log.warn("pipeline_no 重号,重试: {}", pipeline.getPipelineNo());
            pipeline.setPipelineNo(generatePipelineNo());
            return pipelineMapper.insertPipeline(pipeline);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePipeline(Pipeline pipeline)
    {
        Pipeline old = pipelineMapper.selectPipelineById(pipeline.getPipelineId());
        if (old == null) throw new ServiceException("流水线不存在", 404);

        // 终态不可修改非状态字段
        if ("03".equals(old.getStatus())
                && (pipeline.getStatus() == null || pipeline.getStatus().equals(old.getStatus()))) {
            throw new ServiceException("已停用的流水线不能修改", 601);
        }

        // 状态机校验
        if (StringUtils.isNotBlank(pipeline.getStatus())
                && !pipeline.getStatus().equals(old.getStatus())) {
            String os = old.getStatus(), ns = pipeline.getStatus();
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(os, Set.of());
            if (!allowed.contains(ns)) {
                throw new ServiceException(
                    "流水线状态 " + statusLabel(os) + " 不能转到 " + statusLabel(ns), 601);
            }
        }

        // repository/triggerType 修改时白名单校验
        if (StringUtils.isNotBlank(pipeline.getRepository())
                && !VALID_REPOS.contains(pipeline.getRepository())) {
            throw new ServiceException("仓库类型必须为 backend/frontend/ai_service/infra", 601);
        }
        if (StringUtils.isNotBlank(pipeline.getTriggerType())
                && !VALID_TRIGGERS.contains(pipeline.getTriggerType())) {
            throw new ServiceException("触发方式必须为 push/schedule/manual/mr", 601);
        }

        pipeline.setUpdateBy(SecurityUtils.getUsername());
        return pipelineMapper.updatePipeline(pipeline);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePipelineByIds(Long[] pipelineIds) {
        return pipelineMapper.deletePipelineByIds(pipelineIds);
    }

    // ───────── 私有 ─────────

    private String generatePipelineNo() {
        int year = LocalDate.now().getYear();
        String prefix = "PIP-" + year + "-";
        Integer maxSeq = pipelineMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        switch (status) {
            case "00": return "空闲";
            case "01": return "运行中";
            case "02": return "已暂停";
            case "03": return "已停用";
            default:   return "未知(" + status + ")";
        }
    }
}
