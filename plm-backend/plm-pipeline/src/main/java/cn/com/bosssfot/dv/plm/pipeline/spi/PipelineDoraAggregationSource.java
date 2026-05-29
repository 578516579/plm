package cn.com.bosssfot.dv.plm.pipeline.spi;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.bosssfot.dv.plm.common.spi.DoraAggregationSource;
import cn.com.bosssfot.dv.plm.pipeline.mapper.PipelineMapper;

/**
 * plm-pipeline 暴露给 DORA 聚合的 SPI 实现,bean 名 = "pipeline"。
 *
 * 负责:
 * - 部署频率分子 (last_run_status='success')
 * - 变更失败率分子/分母 (failed / failed+success)
 *
 * 设计出处:Proposal 0028 P0-3B,与 P0-2A ProjectScopedLookup 同模式。
 */
@Component("pipeline")
public class PipelineDoraAggregationSource implements DoraAggregationSource {

    @Autowired
    private PipelineMapper pipelineMapper;

    @Override
    public String entityType() {
        return "pipeline";
    }

    @Override
    public DoraAggregationData aggregate(Long projectId, Date periodStart, Date periodEnd) {
        DoraAggregationData data = new DoraAggregationData();
        if (projectId == null || periodStart == null || periodEnd == null) {
            return data;
        }
        data.deployCount = pipelineMapper.countByStatusInPeriod(projectId, "success", periodStart, periodEnd);
        data.failedCount = pipelineMapper.countByStatusInPeriod(projectId, "failed",  periodStart, periodEnd);
        data.totalRunCount = data.deployCount + data.failedCount;
        return data;
    }
}
