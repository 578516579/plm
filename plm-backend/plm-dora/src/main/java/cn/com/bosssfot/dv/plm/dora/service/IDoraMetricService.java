package cn.com.bosssfot.dv.plm.dora.service;

import java.util.Date;
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

    /**
     * Proposal 0028 P0-3B: 按 projectId + 窗口 [periodStart, periodEnd) 计算 4 个 DORA 指标,
     * 4 条记录 upsert 到 tb_dora_metric。指标定义(对照 PRD §F5,适配本仓字典码):
     * <ul>
     *   <li>deploy_freq      = COUNT(pipeline WHERE last_run_status='success' AND last_run_at ∈ window) ÷ periodDays(次/天)</li>
     *   <li>lead_time        = AVG(release.released_at - release.create_time) WHERE status ∈ {02,03}(小时)</li>
     *   <li>mttr             = AVG(defect.update_time - defect.create_time) WHERE severity ∈ {00,01} AND status='03'(小时)</li>
     *   <li>change_fail_rate = COUNT(pipeline.last_run_status='failed') ÷ COUNT(pipeline.last_run_status ∈ {success,failed}) × 100(%)</li>
     * </ul>
     *
     * upsert 规则(同 projectId + metricType + periodStart):
     * - 不存在 → insert,is_computed='Y'
     * - 存在且 is_computed='N'(人工录入) → 跳过,尊重人工值
     * - 存在且 is_computed='Y' → update 覆盖
     *
     * @return 本次产出/跳过 的 4 条 metric 列表(顺序:deploy_freq → lead_time → mttr → change_fail_rate)
     */
    List<DoraMetric> computeMetrics(Long projectId, Date periodStart, Date periodEnd);
}
