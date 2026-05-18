package cn.com.bosssfot.dv.plm.analytics.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.analytics.domain.Analytics;

/**
 * 效能分析 Mapper 接口
 */
public interface AnalyticsMapper
{
    public List<Analytics> selectAnalyticsList(Analytics analytics);

    public Analytics selectAnalyticsById(Long analyticsId);

    public int insertAnalytics(Analytics analytics);

    public int updateAnalytics(Analytics analytics);

    public int deleteAnalyticsByIds(Long[] analyticsIds);

    /** 查最大流水号（ANL-YYYY- 前缀） */
    public Integer selectMaxSeqOfYear(String prefix);
}
