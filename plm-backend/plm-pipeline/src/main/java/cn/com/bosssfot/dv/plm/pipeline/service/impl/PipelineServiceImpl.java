package cn.com.bosssfot.dv.plm.pipeline.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.spi.ProjectScopedLookup;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;
import cn.com.bosssfot.dv.plm.pipeline.mapper.PipelineMapper;
import cn.com.bosssfot.dv.plm.pipeline.service.IPipelineService;

/**
 * Pipeline Service — DevOps 扩展 + 原型 pipeline.html
 * CI/CD 流水线 CRUD + trigger 模拟
 */
@Service
public class PipelineServiceImpl implements IPipelineService {
    private static final Logger log = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private static final Set<String> ALLOWED_TOOL    = Set.of("jenkins","gitlab","github","gitea");
    private static final Set<String> ALLOWED_TRIGGER = Set.of("manual","push","cron","tag");
    private static final Set<String> ALLOWED_RESULT  = Set.of("success","failed","running","skipped");
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("00", Set.of("01"));
        STATUS_TRANSITIONS.put("01", Set.of("00"));
    }

    @Autowired private PipelineMapper pipelineMapper;

    /**
     * proposal 0028 P0-2A 解 P0-1 known limitation:SPI 模式 ProjectScopedLookup,
     * 按 bean 名("release" / "pipeline" / ...)查跨模块归属项目,避免 Maven Reactor 循环依赖。
     */
    @Autowired(required = false) private Map<String, ProjectScopedLookup> projectScopedLookups;

    @Override
    public List<Pipeline> selectPipelineList(Pipeline t) { return pipelineMapper.selectPipelineList(t); }

    @Override
    public Pipeline selectPipelineById(Long id) { return pipelineMapper.selectPipelineById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPipeline(Pipeline t) {
        if (StringUtils.isBlank(t.getPipelineName())) throw new ServiceException("流水线名称不能为空", 602);
        if (StringUtils.isBlank(t.getRepoName()))     throw new ServiceException("代码仓库不能为空", 602);
        if (t.getAuthorUserId() == null)              throw new ServiceException("创建者不能为空", 602);
        validateEnums(t);
        if ("cron".equals(t.getTriggerType()) && StringUtils.isBlank(t.getCronExpr()))
            throw new ServiceException("定时触发必须填 Cron 表达式", 602);
        // proposal 0028 P0-2A: 跨模块 FK 校验 — release 必须同项目
        validateReleaseFk(t);

        if (StringUtils.isBlank(t.getRepoBranch())) t.setRepoBranch("main");
        if (t.getTotalRuns() == null)               t.setTotalRuns(0);
        if (t.getSuccessCount() == null)            t.setSuccessCount(0);
        if (t.getSuccessRate() == null)             t.setSuccessRate(BigDecimal.ZERO);
        if (StringUtils.isBlank(t.getStatus()))     t.setStatus("00");
        if (StringUtils.isBlank(t.getPipelineNo())) t.setPipelineNo(generatePipelineNo());
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return pipelineMapper.insertPipeline(t);
        } catch (DuplicateKeyException e) {
            log.warn("pipeline_no 重号,重试: {}", t.getPipelineNo());
            t.setPipelineNo(generatePipelineNo());
            return pipelineMapper.insertPipeline(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePipeline(Pipeline t) {
        Pipeline old = pipelineMapper.selectPipelineById(t.getPipelineId());
        if (old == null) throw new ServiceException("流水线不存在", 404);
        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus()))
                throw new ServiceException("状态不能从 " + old.getStatus() + " 转到 " + t.getStatus(), 601);
        }
        validateEnums(t);
        // proposal 0028 P0-2A: 更新 releaseId 时校验 same-project
        if (t.getReleaseId() != null && !t.getReleaseId().equals(old.getReleaseId())) {
            Pipeline merged = new Pipeline();
            merged.setProjectId(t.getProjectId() != null ? t.getProjectId() : old.getProjectId());
            merged.setReleaseId(t.getReleaseId());
            validateReleaseFk(merged);
        }
        t.setUpdateBy(SecurityUtils.getUsername());
        return pipelineMapper.updatePipeline(t);
    }

    /**
     * 校验 pipeline.releaseId 与 pipeline.projectId 跨模块强约束(same-project)。
     * releaseId 为 null 时跳过(允许流水线暂不绑定发布单)。
     *
     * proposal 0028 P0-2A:通过 SPI 模式 ProjectScopedLookup 避免 Maven Reactor 循环依赖。
     */
    private void validateReleaseFk(Pipeline pipeline) {
        if (pipeline.getReleaseId() == null) {
            return;
        }
        ProjectScopedLookup releaseLookup =
            projectScopedLookups == null ? null : projectScopedLookups.get("release");
        if (releaseLookup == null) {
            throw new ServiceException("系统配置缺失:ProjectScopedLookup 未注册 release", 500);
        }
        Long targetProjectId = releaseLookup.resolveProjectId(pipeline.getReleaseId());
        if (targetProjectId == null) {
            throw new ServiceException("关联的发布单不存在", 702);
        }
        if (!targetProjectId.equals(pipeline.getProjectId())) {
            throw new ServiceException("流水线的发布单必须属于同一项目", 702);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePipelineByIds(Long[] ids) { return pipelineMapper.deletePipelineByIds(ids); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pipeline trigger(Long id) {
        Pipeline t = pipelineMapper.selectPipelineById(id);
        if (t == null) throw new ServiceException("流水线不存在", 404);
        if (!"00".equals(t.getStatus()))
            throw new ServiceException("流水线已停用,无法触发", 601);

        // 模拟: 85% 成功率
        boolean success = ThreadLocalRandom.current().nextDouble() < 0.85;
        int totalRuns = (t.getTotalRuns() == null ? 0 : t.getTotalRuns()) + 1;
        int successCount = (t.getSuccessCount() == null ? 0 : t.getSuccessCount()) + (success ? 1 : 0);
        BigDecimal rate = BigDecimal.valueOf(successCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalRuns), 2, RoundingMode.HALF_UP);

        t.setLastRunStatus(success ? "success" : "failed");
        t.setLastRunAt(new Date());
        t.setTotalRuns(totalRuns);
        t.setSuccessCount(successCount);
        t.setSuccessRate(rate);
        t.setUpdateBy("ci-trigger");
        pipelineMapper.updatePipeline(t);
        return pipelineMapper.selectPipelineById(id);
    }

    private void validateEnums(Pipeline t) {
        if (t.getCicdTool() != null && !ALLOWED_TOOL.contains(t.getCicdTool()))
            throw new ServiceException("无效的 CICD 工具: " + t.getCicdTool(), 604);
        if (t.getTriggerType() != null && !ALLOWED_TRIGGER.contains(t.getTriggerType()))
            throw new ServiceException("无效的触发方式: " + t.getTriggerType(), 604);
        if (t.getLastRunStatus() != null && !ALLOWED_RESULT.contains(t.getLastRunStatus()))
            throw new ServiceException("无效的执行结果: " + t.getLastRunStatus(), 604);
    }

    private String generatePipelineNo() {
        int year = LocalDate.now().getYear();
        String prefix = "PIPE-" + year + "-";
        Integer maxSeq = pipelineMapper.selectMaxSeqOfYear(prefix);
        return String.format("%s%04d", prefix, (maxSeq == null ? 0 : maxSeq) + 1);
    }
}
