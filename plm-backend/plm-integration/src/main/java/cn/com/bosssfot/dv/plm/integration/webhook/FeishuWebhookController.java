package cn.com.bosssfot.dv.plm.integration.webhook;

import java.util.HashMap;
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
import cn.com.bosssfot.dv.plm.integration.adapter.feishu.FeishuConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;

/**
 * 飞书事件订阅入口（公网可达；自身做验签）。
 *
 * <p>端点 {@code POST /integration/webhook/feishu/{connectorId}}。
 *
 * <p>支持飞书的两种模式：
 * <ul>
 *   <li>明文模式 - body 中带 token 字段，验 verification_token</li>
 *   <li>加密模式 - body.encrypt 字段是 AES 密文，本期只解 URL verification (challenge)，
 *     业务事件解密留 Phase 2。</li>
 * </ul>
 *
 * <p>挑战阶段返回 {@code {"challenge": "<value>"}}（飞书要求原样回写）。
 */
@RestController
@RequestMapping("/integration/webhook/feishu")
public class FeishuWebhookController {

    private static final Logger log = LoggerFactory.getLogger(FeishuWebhookController.class);

    @Autowired
    private IIntegrationConnectorService connectorService;

    @Autowired
    private IIntegrationWebhookEventService eventService;

    @Autowired
    private FeishuConnectorAdapter feishuAdapter;

    @Anonymous
    @PostMapping("/{connectorId}")
    public ResponseEntity<?> receive(
            @PathVariable Long connectorId,
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Lark-Signature", required = false) String signature,
            @RequestHeader(value = "X-Lark-Request-Timestamp", required = false) String timestamp,
            HttpServletRequest httpReq) {

        IntegrationConnector connector = connectorService.selectConnectorById(connectorId);
        if (connector == null || !"feishu".equals(connector.getConnectorType()) || !"0".equals(connector.getStatus())) {
            log.warn("[plm-integration/feishu] connectorId={} 不存在或非飞书或停用", connectorId);
            return ResponseEntity.status(404).body(Map.of("errorCode", 805, "msg", "connector 不存在或停用"));
        }

        JSONObject body = parseSafely(rawBody);
        if (body == null) {
            return ResponseEntity.badRequest().body(Map.of("errorCode", 601, "msg", "非法 JSON"));
        }

        // 1. URL verification challenge —— 飞书初始化时会发送 type=url_verification + challenge 字段
        if ("url_verification".equals(body.getString("type"))) {
            String tokenInBody = body.getString("token");
            if (!feishuAdapter.verifyToken(connector, tokenInBody)) {
                log.warn("[plm-integration/feishu] connectorId={} challenge 验 token 失败", connectorId);
                return ResponseEntity.status(401).body(Map.of("errorCode", 807, "msg", "verification_token 不匹配"));
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("challenge", body.getString("challenge"));
            return ResponseEntity.ok(resp);
        }

        // 2. 业务事件
        byte[] rawBytes = rawBody == null ? new byte[0] : rawBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        boolean sigOk;
        if (signature != null && timestamp != null) {
            sigOk = feishuAdapter.verifyWebhookSignature(connector, signature, timestamp, rawBytes);
        } else {
            // 明文模式：从 body.token 校验
            sigOk = feishuAdapter.verifyToken(connector, body.getString("token"));
        }

        IntegrationWebhookEvent event = new IntegrationWebhookEvent();
        event.setConnectorId(connectorId);
        event.setEventType(extractEventType(body));
        event.setExternalEventId(extractEventId(body));
        event.setPayloadJson(rawBody);
        event.setSignature(signature);
        event.setSignatureVerified(sigOk ? "1" : "0");
        event.setProcessStatus(sigOk ? "0" : "4");   // 验签失败直接置为已忽略
        event.setSourceIp(extractClientIp(httpReq));

        eventService.receive(event);

        if (!sigOk) {
            return ResponseEntity.status(401).body(Map.of("errorCode", 807, "msg", "签名验证失败"));
        }
        return ResponseEntity.ok(Map.of("msg", "received"));
    }

    private static JSONObject parseSafely(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return JSON.parseObject(s); } catch (Exception e) { return null; }
    }

    private static String extractEventType(JSONObject body) {
        // 飞书新版 v2 事件结构: header.event_type
        JSONObject header = body.getJSONObject("header");
        if (header != null && header.getString("event_type") != null) {
            return "feishu." + header.getString("event_type");
        }
        // 老版 v1: event.type
        JSONObject event = body.getJSONObject("event");
        if (event != null && event.getString("type") != null) {
            return "feishu." + event.getString("type");
        }
        return "feishu.unknown";
    }

    private static String extractEventId(JSONObject body) {
        JSONObject header = body.getJSONObject("header");
        if (header != null && header.getString("event_id") != null) {
            return header.getString("event_id");
        }
        // 兜底：用 uuid + ts 拼接（不理想，但保证唯一）
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
