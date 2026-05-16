package cn.com.bosssfot.dv.plm.dora.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.dora.domain.DoraMetric;

public interface DoraMetricMapper {
    List<DoraMetric> selectDoraList(DoraMetric dora);
    DoraMetric selectDoraById(Long doraId);
    int insertDora(DoraMetric dora);
    int updateDora(DoraMetric dora);
    int deleteDoraByIds(Long[] doraIds);
    Integer selectMaxSeqOfYear(String prefix);
}
