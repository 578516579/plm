package cn.com.bosssfot.dv.plm.analytics.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.analytics.domain.Analytics;

/**
 * 效能分析 Service 接口
 */
public interface IAnalyticsService
{
    public List<Analytics> selectAnalyticsList(Analytics analytics);

    public Analytics selectAnalyticsById(Long analyticsId);

    public int insertAnalytics(Analytics analytics);

    public int updateAnalytics(Analytics analytics);

    public int deleteAnalyticsByIds(Long[] analyticsIds);

    /**
     * AI生成效能分析报告
     * 设置 status='01', aiGenerated='Y', aiGeneratedAt=now，填充 mock 指标
     */
    public int aiGenerate(Long analyticsId);
}
