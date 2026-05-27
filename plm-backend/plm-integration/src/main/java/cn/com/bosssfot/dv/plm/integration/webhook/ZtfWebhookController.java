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
import cn.com.bosssfot.dv.plm.integration.adapter.ztf.ZtfConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;

/**
 * ZTF webhook 入口(公网可达;X-ZTF-Token 头验签)。单向 Inbound。
 *
 * <p>端点 {@code POST /integration/webhook/ztf/{connectorId}}。
 *
 * <p>ZTF 侧 webhook 配置:
 * <pre>
 * URL:     https://plm.example.com/dev-api/integration/webhook/ztf/{connectorId}
 * Method:  POST
 * 密钥:    填入 connector.webhook_secret,会发到 X-ZTF-Token 头
 * 事件:    run.completed(payload 须含 taskId + total/pass/fail/duration/finishedAt)
 * </pre>
 */
@RestController
@RequestMapping("/integration/webhook/ztf")
public class ZtfWebhookController {

    private static final Logger log = LoggerFactory.getLogger(ZtfWebhookController.class);

    @Autowired
    private IIntegrationConnectorService connectorService;

    @Autowired
    private IIntegrationWebhookEventService eventService;

    @Autowired
    private ZtfConnectorAdapter ztfAdapter;

    @Anonymous
    @PostMapping("/{connectorId}")
    public ResponseEntity<?> receive(
            @PathVariable Long connectorId,
            @RequestBody(required = false) String rawBody,
            @RequestHeader(value = "X-ZTF-Token", required = false) String ztfToken,
            HttpServletRequest httpReq) {

        IntegrationConnector connector = connectorService.selectConnectorById(connectorId);
        if (connector == null || !"ztf".equals(connector.getConnectorType()) || !"0".equals(connector.getStatus())) {
            log.warn("[plm-integration/ztf] connectorId={} 不存在或非 ztf 或停用", connectorId);
            return ResponseEntity.status(404).body(Map.of("errorCode", 805, "msg", "connector 不存在或停用"));
        }

        byte[] rawBytes = rawBody == null ? new byte[0] : rawBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        boolean sigOk = ztfAdapter.verifyWebhookSignature(connector, ztfToken, null, rawBytes);

        JSONObject payload = parseLenient(rawBody);
        String runEvent = payload.getString("event");

        IntegrationWebhookEvent event = new IntegrationWebhookEvent();
        event.setConnectorId(connectorId);
        // event_type 落库格式 ztf.run.<event>(如 ztf.run.completed)
        event.setEventType("ztf.run." + safe(runEvent));
        event.setExternalEventId(buildEventId(payload));
        event.setPayloadJson(rawBody);
        event.setSignature(ztfToken == null ? null : "X-ZTF-Token");
        event.setSignatureVerified(sigOk ? "1" : "0");
        event.setProcessStatus(sigOk ? "0" : "4");
        event.setSourceIp(extractClientIp(httpReq));

        eventService.receive(event);

        if (!sigOk) {
            return ResponseEntity.status(401).body(Map.of("errorCode", 815, "msg", "X-ZTF-Token 不匹配"));
        }
        return ResponseEntity.ok(Map.of("msg", "received"));
    }

    /** 幂等键:taskId + event + finishedAt 时间戳兜底分辨同一 run 的毫秒级重发 */
    private static String buildEventId(JSONObject payload) {
        String taskId = payload.getString("taskId");
        if (taskId == null) taskId = payload.getString("runId");
        String event = payload.getString("event");
        if (taskId != null) {
            String ts = payload.getString("finishedAt");
            return "ztf-" + taskId + "-" + safe(event) + (ts == null ? "" : "-" + ts);
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
