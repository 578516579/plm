package cn.com.bosssfot.dv.plm.pipeline.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;

public interface PipelineMapper {
    List<Pipeline> selectPipelineList(Pipeline pipeline);
    Pipeline selectPipelineById(Long pipelineId);
    int insertPipeline(Pipeline pipeline);
    int updatePipeline(Pipeline pipeline);
    int deletePipelineByIds(Long[] pipelineIds);
    Integer selectMaxSeqOfYear(String prefix);
}
