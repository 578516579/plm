package cn.com.bosssfot.dv.plm.pipeline.spi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.bosssfot.dv.plm.common.spi.ProjectScopedLookup;
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;
import cn.com.bosssfot.dv.plm.pipeline.mapper.PipelineMapper;

/**
 * plm-pipeline 对外暴露的 ProjectScopedLookup 实现,bean 名 = "pipeline"。
 * 用于跨模块(如 plm-release)在 Maven Reactor 循环依赖约束下查询
 * 流水线所属 projectId,做 same-project FK 校验。
 *
 * 设计出处:Proposal 0028 P0-2A,解 P0-1 known limitation。
 */
@Component("pipeline")
public class PipelineProjectScopedLookup implements ProjectScopedLookup {

    @Autowired
    private PipelineMapper pipelineMapper;

    @Override
    public String entityType() {
        return "pipeline";
    }

    @Override
    public Long resolveProjectId(Long entityId) {
        if (entityId == null) {
            return null;
        }
        Pipeline p = pipelineMapper.selectPipelineById(entityId);
        return p == null ? null : p.getProjectId();
    }
}
