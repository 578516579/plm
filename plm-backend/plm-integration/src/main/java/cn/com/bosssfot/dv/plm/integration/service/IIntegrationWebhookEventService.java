package cn.com.bosssfot.dv.plm.integration.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;

public interface IIntegrationWebhookEventService
{
    List<IntegrationWebhookEvent> selectEventList(IntegrationWebhookEvent event);

    IntegrationWebhookEvent selectEventById(Long id);

    /**
     * 接收入站 webhook：
     *
     * <ol>
     *   <li>幂等去重（同 connector_id + external_event_id 已存在 → 返回原行）</li>
     *   <li>落库 process_status=0 待处理</li>
     *   <li>发布 ApplicationEvent 让业务模块异步处理</li>
     * </ol>
     *
     * @return 已存在则返回旧行 (process_status 不变)；新事件返回新建行
     */
    IntegrationWebhookEvent receive(IntegrationWebhookEvent event);

    /** 标记事件已处理（成功/失败） */
    int markProcessed(Long id, String processStatus, String error);

    /** 重试（仅允许从失败 → 处理中） */
    int retry(Long id);
}
