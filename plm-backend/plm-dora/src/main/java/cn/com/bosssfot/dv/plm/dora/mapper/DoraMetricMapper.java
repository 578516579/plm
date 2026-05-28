package cn.com.bosssfot.dv.plm.dora.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import cn.com.bosssfot.dv.plm.dora.domain.DoraMetric;

public interface DoraMetricMapper {
    List<DoraMetric> selectDoraList(DoraMetric dora);
    DoraMetric selectDoraById(Long doraId);
    int insertDora(DoraMetric dora);
    int updateDora(DoraMetric dora);
    int deleteDoraByIds(Long[] doraIds);
    Integer selectMaxSeqOfYear(String prefix);

    /**
     * Proposal 0028 P0-3B: upsert 查重用,按 (projectId, metricType, periodStart) 定位已有指标。
     * 返回 null 表示未存在,需 insert;非 null 时根据 is_computed='N' 跳过,'Y' 更新覆盖。
     */
    DoraMetric selectByProjectTypePeriod(@Param("projectId") Long projectId,
                                          @Param("metricType") String metricType,
                                          @Param("periodStart") Date periodStart);
}
