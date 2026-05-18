package cn.com.bosssfot.dv.plm.pipeline.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;

/**
 * CI/CD流水线 Service 接口
 */
public interface IPipelineService
{
    public List<Pipeline> selectPipelineList(Pipeline pipeline);

    public Pipeline selectPipelineById(Long pipelineId);

    public int insertPipeline(Pipeline pipeline);

    public int updatePipeline(Pipeline pipeline);

    public int deletePipelineByIds(Long[] pipelineIds);
}
