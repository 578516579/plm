package cn.com.bosssfot.dv.plm.pipeline.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;

public interface IPipelineService {
    List<Pipeline> selectPipelineList(Pipeline pipeline);
    Pipeline selectPipelineById(Long pipelineId);
    int insertPipeline(Pipeline pipeline);
    int updatePipeline(Pipeline pipeline);
    int deletePipelineByIds(Long[] pipelineIds);
    /** 模拟触发流水线 — 累加 totalRuns + 更新 lastRunStatus/successRate */
    Pipeline trigger(Long pipelineId);
}
