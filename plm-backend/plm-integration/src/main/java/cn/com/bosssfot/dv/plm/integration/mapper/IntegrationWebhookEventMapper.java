package cn.com.bosssfot.dv.plm.integration.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;

public interface IntegrationWebhookEventMapper
{
    List<IntegrationWebhookEvent> selectEventList(IntegrationWebhookEvent event);

    IntegrationWebhookEvent selectEventById(Long id);

    IntegrationWebhookEvent selectByConnectorAndExternalId(Long connectorId, String externalEventId);

    int insertEvent(IntegrationWebhookEvent event);

    int updateEvent(IntegrationWebhookEvent event);
}
