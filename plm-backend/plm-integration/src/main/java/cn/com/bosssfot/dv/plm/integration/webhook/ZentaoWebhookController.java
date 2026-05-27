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
import cn.com.bosssfot.dv.plm.integration.adapter.zentao.ZentaoConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;

/**
 * 禅道 webhook 入口(公网可达;X-Zentao-Token 头验签)。
 *
 * <p>端点 {@code POST /integration/webhook/zentao/{connectorId}}。
 *
 * <p>禅道侧后台「通用 → Webhook」配置:
 * <pre>
 * URL:     https://plm.example.com/dev-api/integration/webhook/zentao/{connectorId}
 * Method:  POST
 * 密钥:    填入 connector.webhook_secret,会发到 X-Zentao-Token 头
 * 事件:    勾选 Bug/Story/Task/Case 的 opened/edited/closed
 * </pre>
 */
@RestController
@RequestMapping("/integration/webhook/zentao")
public class ZentaoWebhookController {

    private static final Logger log = LoggerFactory.getLogger(ZentaoWebhookController.class);

    @Autowired
    private IIntegrationConnectorService connectorService;

    @Autowired
    private IIntegrationWebhookEventService eventService;

    @Autowired
    private ZentaoConnectorAdapter zentaoAdapter;

    @Anonymous
    @PostMapping("/{connectorId}")
    public ResponseEntity<?> receive(
            @PathVariable Long connectorId,
            @RequestBody(required = false) String rawBody,
            @RequestHeader(value = "X-Zentao-Token", required = false) String zentaoToken,
            HttpServletRequest httpReq) {

        IntegrationConnector connector = connectorService.selectConnectorById(connectorId);
        if (connector == null || !"zentao".equals(connector.getConnectorType()) || !"0".equals(connector.getStatus())) {
            log.warn("[plm-integration/zentao] connectorId={} 不存在或非 zentao 或停用", connectorId);
            return ResponseEntity.status(404).body(Map.of("errorCode", 805, "msg", "connector 不存在或停用"));
        }

        byte[] rawBytes = rawBody == null ? new byte[0] : rawBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        boolean sigOk = zentaoAdapter.verifyWebhookSignature(connector, zentaoToken, null, rawBytes);

        JSONObject payload = parseLenient(rawBody);
        String action = payload.getString("action");
        String objectType = payload.getString("objectType");
        String objectId = payload.getString("objectID");

        IntegrationWebhookEvent event = new IntegrationWebhookEvent();
        event.setConnectorId(connectorId);
        event.setEventType("zentao." + safe(objectType) + "." + safe(action));
        event.setExternalEventId(buildEventId(objectType, objectId, action, payload));
        event.setPayloadJson(rawBody);
        event.setSignature(zentaoToken == null ? null : "X-Zentao-Token");
        event.setSignatureVerified(sigOk ? "1" : "0");
        event.setProcessStatus(sigOk ? "0" : "4");
        event.setSourceIp(extractClientIp(httpReq));

        eventService.receive(event);

        if (!sigOk) {
            return ResponseEntity.status(401).body(Map.of("errorCode", 815, "msg", "X-Zentao-Token 不匹配"));
        }
        return ResponseEntity.ok(Map.of("msg", "received"));
    }

    private static String buildEventId(String objectType, String objectId, String action, JSONObject payload) {
        if (objectType != null && objectId != null && action != null) {
            // 同一对象的同种 action 在毫秒级窗口可能重发,加 lastEditedDate / openedDate 兜底分辨
            JSONObject data = payload.getJSONObject("data");
            String ts = data == null ? null : (data.getString("lastEditedDate") != null
                ? data.getString("lastEditedDate") : data.getString("openedDate"));
            return "zentao-" + objectType + "-" + objectId + "-" + action + (ts == null ? "" : "-" + ts);
        }
        return "fallback-" + System.nanoTime();
    }

    private static JSONObject parseLenient(String body) {
        if (body == null || body.isEmpty()) return new JSONObject();
        try {
            return JSON.parseObject(body);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static String safe(String s) { return s == null ? "unknown" : s.toLowerCase(); }

    private static String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        return req.getRemoteAddr();
    }
}
