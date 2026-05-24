package cn.com.bosssfot.dv.plm.analytics.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.analytics.domain.AnalyticsSnapshot;

public interface AnalyticsSnapshotMapper {
    List<AnalyticsSnapshot> selectAnalyticsList(AnalyticsSnapshot snapshot);
    AnalyticsSnapshot selectAnalyticsById(Long snapshotId);
    int insertAnalytics(AnalyticsSnapshot snapshot);
    int updateAnalytics(AnalyticsSnapshot snapshot);
    int deleteAnalyticsByIds(Long[] snapshotIds);
    Integer selectMaxSeqOfYear(String prefix);
}
