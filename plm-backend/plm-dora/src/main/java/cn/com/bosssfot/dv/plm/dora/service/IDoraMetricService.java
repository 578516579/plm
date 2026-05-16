package cn.com.bosssfot.dv.plm.dora.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.dora.domain.DoraMetric;

public interface IDoraMetricService {
    List<DoraMetric> selectDoraList(DoraMetric dora);
    DoraMetric selectDoraById(Long doraId);
    int insertDora(DoraMetric dora);
    int updateDora(DoraMetric dora);
    int deleteDoraByIds(Long[] doraIds);
    /** AI 生成持续改进建议 (按 metricType 阈值生成针对性建议) */
    DoraMetric aiSuggest(Long doraId);
}
