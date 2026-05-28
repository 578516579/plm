package cn.com.bosssfot.dv.plm.release.spi;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.bosssfot.dv.plm.common.spi.DoraAggregationSource;
import cn.com.bosssfot.dv.plm.release.mapper.ReleaseMapper;

/**
 * plm-release 暴露给 DORA 聚合的 SPI 实现,bean 名 = "release"。
 *
 * 负责:
 * - 前置时间 SUM(released_at - create_time) WHERE status ∈ {02 已发布, 03 已回滚}
 * - 前置时间样本数(分母)
 *
 * 设计出处:Proposal 0028 P0-3B。
 */
@Component("release")
public class ReleaseDoraAggregationSource implements DoraAggregationSource {

    @Autowired
    private ReleaseMapper releaseMapper;

    @Override
    public String entityType() {
        return "release";
    }

    @Override
    public DoraAggregationData aggregate(Long projectId, Date periodStart, Date periodEnd) {
        DoraAggregationData data = new DoraAggregationData();
        if (projectId == null || periodStart == null || periodEnd == null) {
            return data;
        }
        Map<String, Object> row = releaseMapper.sumLeadTimeMsInPeriod(projectId, periodStart, periodEnd);
        if (row == null) {
            return data;
        }
        Object sum = row.get("sumMs");
        Object cnt = row.get("cnt");
        data.totalLeadTimeMs = toBigDecimal(sum);
        data.leadTimeSampleCnt = cnt == null ? 0L : ((Number) cnt).longValue();
        return data;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        return new BigDecimal(v.toString());
    }
}
