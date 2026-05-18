package cn.com.bosssfot.dv.plm.pipeline.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;

/**
 * CI/CD流水线 Mapper 接口
 */
public interface PipelineMapper
{
    public List<Pipeline> selectPipelineList(Pipeline pipeline);

    public Pipeline selectPipelineById(Long pipelineId);

    public int insertPipeline(Pipeline pipeline);

    public int updatePipeline(Pipeline pipeline);

    public int deletePipelineByIds(Long[] pipelineIds);

    /** 查最大流水号（PIP-YYYY- 前缀） */
    public Integer selectMaxSeqOfYear(String prefix);
}
