package cn.com.bosssfot.dv.plm.pipeline.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;

public interface PipelineMapper {
    List<Pipeline> selectPipelineList(Pipeline pipeline);
    Pipeline selectPipelineById(Long pipelineId);
    int insertPipeline(Pipeline pipeline);
    int updatePipeline(Pipeline pipeline);
    int deletePipelineByIds(Long[] pipelineIds);
    Integer selectMaxSeqOfYear(String prefix);

    /**
     * Proposal 0028 P0-3B: DORA 聚合用 — 按 last_run_status 在窗口期统计次数。
     * @param projectId 项目 id
     * @param status 'success' / 'failed'
     * @param periodStart 含
     * @param periodEnd 不含
     */
    long countByStatusInPeriod(@Param("projectId") Long projectId,
                                @Param("status") String status,
                                @Param("periodStart") Date periodStart,
                                @Param("periodEnd") Date periodEnd);
}
