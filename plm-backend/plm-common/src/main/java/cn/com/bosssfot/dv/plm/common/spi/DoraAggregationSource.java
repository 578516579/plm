package cn.com.bosssfot.dv.plm.common.spi;

import java.util.Date;

/**
 * DORA 真聚合数据源 SPI — Proposal 0028 P0-3B。
 *
 * 设计动机:plm-dora 模块需要从 plm-pipeline / plm-release / plm-defect
 * 拉聚合数据来算 4 个 DORA 指标,但 Maven Reactor 约束下 dora 不能反向
 * 依赖那 3 个业务模块的 Mapper(同 P0-2A 的循环依赖约束)。
 *
 * 实现约定:
 * - 每个上游业务模块旁挂一个 @Component 实现,bean 名按 entityType()
 *   ("pipeline" / "release" / "defect")
 * - DoraMetricServiceImpl 通过 Map&lt;String, DoraAggregationSource&gt; 按 key 取数
 * - 实现内使用本模块自己的 Mapper(避免跨模块 Mapper 注入)
 *
 * 同源 SPI:{@link ProjectScopedLookup}(P0-2A 用于 same-project FK 校验)。
 */
public interface DoraAggregationSource {

    /** 业务模块标识(对应 Spring bean 名),用作 Map key */
    String entityType();

    /**
     * 在 [periodStart, periodEnd) 窗口内,对指定 project 聚合返回 4 个 DORA 中
     * 该 source 负责的子项数据。dora 各指标公式:
     *
     * <pre>
     *   pipeline source:
     *     deployCount       = COUNT(pipeline WHERE last_run_status='success' AND last_run_at IN window)
     *     totalRunCount     = COUNT(pipeline WHERE last_run_status IN ('success','failed') AND last_run_at IN window)
     *     failedCount       = COUNT(pipeline WHERE last_run_status='failed' AND last_run_at IN window)
     *
     *   release source:
     *     totalLeadTimeMs   = SUM(released_at - create_time) WHERE status IN ('02','03') AND released_at IN window
     *     leadTimeSampleCnt = COUNT 同上(用作分母)
     *
     *   defect source:
     *     totalRecoverMs    = SUM(update_time - create_time) WHERE severity IN ('00','01') AND status='03' AND update_time IN window
     *     recoverSampleCnt  = COUNT 同上
     * </pre>
     *
     * 字段缺省含义:不参与对应指标的 source 返回该字段为 0/null。
     */
    DoraAggregationData aggregate(Long projectId, Date periodStart, Date periodEnd);

    /**
     * 聚合返回 DTO — 全部为 0/null 表示窗口内无数据。
     * 加 BigDecimal 以保留毫秒级 SUM 精度,避免 long 溢出。
     */
    final class DoraAggregationData {
        /** pipeline: 部署频率分子(success 次数) */
        public long deployCount;
        /** pipeline: 变更失败率分母(success+failed 次数) */
        public long totalRunCount;
        /** pipeline: 变更失败率分子(failed 次数) */
        public long failedCount;

        /** release: 前置时间总毫秒数(用 BigDecimal 防溢出) */
        public java.math.BigDecimal totalLeadTimeMs = java.math.BigDecimal.ZERO;
        /** release: 前置时间样本数(分母) */
        public long leadTimeSampleCnt;

        /** defect: MTTR 总毫秒数 */
        public java.math.BigDecimal totalRecoverMs = java.math.BigDecimal.ZERO;
        /** defect: MTTR 样本数(分母) */
        public long recoverSampleCnt;
    }
}
