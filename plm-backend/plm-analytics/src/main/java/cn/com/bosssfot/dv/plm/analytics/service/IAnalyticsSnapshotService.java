package cn.com.bosssfot.dv.plm.analytics.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.analytics.domain.AnalyticsSnapshot;

public interface IAnalyticsSnapshotService {
    List<AnalyticsSnapshot> selectAnalyticsList(AnalyticsSnapshot snapshot);
    AnalyticsSnapshot selectAnalyticsById(Long snapshotId);
    int insertAnalytics(AnalyticsSnapshot snapshot);
    int updateAnalytics(AnalyticsSnapshot snapshot);
    int deleteAnalyticsByIds(Long[] snapshotIds);
    AnalyticsSnapshot aiRecommend(Long snapshotId);
}
