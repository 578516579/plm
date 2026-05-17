package cn.com.bosssfot.dv.plm.integration.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.mapper.IntegrationWebhookEventMapper;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;

@Service
public class IntegrationWebhookEventServiceImpl implements IIntegrationWebhookEventService
{
    private static final Logger log = LoggerFactory.getLogger(IntegrationWebhookEventServiceImpl.class);

    @Autowired
    private IntegrationWebhookEventMapper eventMapper;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public List<IntegrationWebhookEvent> selectEventList(IntegrationWebhookEvent event) {
        return eventMapper.selectEventList(event);
    }

    @Override
    public IntegrationWebhookEvent selectEventById(Long id) {
        return eventMapper.selectEventById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IntegrationWebhookEvent receive(IntegrationWebhookEvent event) {
        if (event.getConnectorId() == null || event.getExternalEventId() == null) {
            throw new ServiceException("connectorId 与 externalEventId 不能为空", 601);
        }
        // 幂等去重
        IntegrationWebhookEvent existing = eventMapper.selectByConnectorAndExternalId(
            event.getConnectorId(), event.getExternalEventId());
        if (existing != null) {
            log.info("[plm-integration] 重复 webhook 事件，幂等返回: connector={}, externalEventId={}",
                event.getConnectorId(), event.getExternalEventId());
            return existing;
        }
        if (event.getProcessStatus() == null) {
            event.setProcessStatus("0");
        }
        if (event.getRetryCount() == null) {
            event.setRetryCount(0);
        }
        eventMapper.insertEvent(event);

        // 验签失败的事件不发布业务事件
        if ("1".equals(event.getSignatureVerified())) {
            publisher.publishEvent(new WebhookReceived(event));
        }
        return event;
    }

    @Override
    public int markProcessed(Long id, String processStatus, String error) {
        IntegrationWebhookEvent up = new IntegrationWebhookEvent();
        up.setId(id);
        up.setProcessStatus(processStatus);
        up.setProcessError(error);
        up.setProcessTime(new Date());
        return eventMapper.updateEvent(up);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int retry(Long id) {
        IntegrationWebhookEvent e = eventMapper.selectEventById(id);
        if (e == null) {
            throw new ServiceException("事件不存在", 805);
        }
        if (!"3".equals(e.getProcessStatus())) {
            throw new ServiceException("仅失败(3)状态可重试", 701);
        }
        IntegrationWebhookEvent up = new IntegrationWebhookEvent();
        up.setId(id);
        up.setProcessStatus("1");
        up.setRetryCount((e.getRetryCount() == null ? 0 : e.getRetryCount()) + 1);
        int rows = eventMapper.updateEvent(up);
        publisher.publishEvent(new WebhookReceived(e));
        return rows;
    }

    /** Spring ApplicationEvent —— 让业务模块按 @EventListener 监听 webhook 入站 */
    public static class WebhookReceived {
        private final IntegrationWebhookEvent event;
        public WebhookReceived(IntegrationWebhookEvent event) { this.event = event; }
        public IntegrationWebhookEvent getEvent() { return event; }
    }
}
