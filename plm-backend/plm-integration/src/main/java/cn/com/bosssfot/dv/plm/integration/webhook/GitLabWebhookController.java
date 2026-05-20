package cn.com.bosssfot.dv.plm.integration.webhook;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.com.bosssfot.dv.plm.common.annotation.Anonymous;
import cn.com.bosssfot.dv.plm.integration.adapter.gitlab.GitLabConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;

/**
 * GitLab webhook 入口（公网可达；X-Gitlab-Token 头验签）。
 *
 * <p>端点 {@code POST /integration/webhook/gitlab/{connectorId}}。
 */
@RestController
@RequestMapping("/integration/webhook/gitlab")
public class GitLabWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GitLabWebhookController.class);

    @Autowired
    private IIntegrationConnectorService connectorService;

    @Autowired
    private IIntegrationWebhookEventService eventService;

    @Autowired
    private GitLabConnectorAdapter gitlabAdapter;

    @Anonymous
    @PostMapping("/{connectorId}")
    public ResponseEntity<?> receive(
            @PathVariable Long connectorId,
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Gitlab-Token", required = false) String gitlabToken,
            @RequestHeader(value = "X-Gitlab-Event", required = false) String gitlabEvent,
            @RequestHeader(value = "X-Gitlab-Event-UUID", required = false) String gitlabEventUuid,
            HttpServletRequest httpReq) {

        IntegrationConnector connector = connectorService.selectConnectorById(connectorId);
        if (connector == null || !"gitlab".equals(connector.getConnectorType()) || !"0".equals(connector.getStatus())) {
            log.warn("[plm-integration/gitlab] connectorId={} 不存在或非 gitlab 或停用", connectorId);
            return ResponseEntity.status(404).body(Map.of("errorCode", 805, "msg", "connector 不存在或停用"));
        }

        boolean sigOk = gitlabAdapter.verifyWebhookSignature(connector, gitlabToken, null,
            rawBody == null ? new byte[0] : rawBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        IntegrationWebhookEvent event = new IntegrationWebhookEvent();
        event.setConnectorId(connectorId);
        event.setEventType("gitlab." + (gitlabEvent == null ? "unknown" : gitlabEvent.toLowerCase().replace(' ', '_')));
        event.setExternalEventId(gitlabEventUuid != null ? gitlabEventUuid : fallbackEventId(rawBody));
        event.setPayloadJson(rawBody);
        event.setSignature(gitlabToken == null ? null : "X-Gitlab-Token");
        event.setSignatureVerified(sigOk ? "1" : "0");
        event.setProcessStatus(sigOk ? "0" : "4");
        event.setSourceIp(extractClientIp(httpReq));

        eventService.receive(event);

        if (!sigOk) {
            return ResponseEntity.status(401).body(Map.of("errorCode", 807, "msg", "X-Gitlab-Token 不匹配"));
        }
        return ResponseEntity.ok(Map.of("msg", "received"));
    }

    private static String fallbackEventId(String body) {
        if (body == null) return "fallback-" + System.nanoTime();
        try {
            JSONObject json = JSON.parseObject(body);
            Object id = json == null ? null : (json.get("object_attributes") instanceof JSONObject oa ? oa.get("id") : null);
            if (id != null) return "gitlab-oa-" + id;
        } catch (Exception ignore) {}
        return "fallback-" + System.nanoTime();
    }

    private static String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        return req.getRemoteAddr();
    }
}
