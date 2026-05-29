package cn.com.bosssfot.dv.plm.defect.spi;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.bosssfot.dv.plm.common.spi.DoraAggregationSource;
import cn.com.bosssfot.dv.plm.defect.mapper.DefectMapper;

/**
 * plm-defect 暴露给 DORA 聚合的 SPI 实现,bean 名 = "defect"。
 *
 * 负责:
 * - MTTR SUM(update_time - create_time) WHERE severity ∈ {00 P0, 01 P1} AND status=03
 *
 * 注:tb_defect 没有 resolvedAt 字段;以 status=03(已解决)时的 update_time 近似。
 *
 * 设计出处:Proposal 0028 P0-3B。
 */
@Component("defect")
public class DefectDoraAggregationSource implements DoraAggregationSource {

    @Autowired
    private DefectMapper defectMapper;

    @Override
    public String entityType() {
        return "defect";
    }

    @Override
    public DoraAggregationData aggregate(Long projectId, Date periodStart, Date periodEnd) {
        DoraAggregationData data = new DoraAggregationData();
        if (projectId == null || periodStart == null || periodEnd == null) {
            return data;
        }
        Map<String, Object> row = defectMapper.sumRecoverMsInPeriod(projectId, periodStart, periodEnd);
        if (row == null) {
            return data;
        }
        Object sum = row.get("sumMs");
        Object cnt = row.get("cnt");
        data.totalRecoverMs = toBigDecimal(sum);
        data.recoverSampleCnt = cnt == null ? 0L : ((Number) cnt).longValue();
        return data;
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        return new BigDecimal(v.toString());
    }
}
