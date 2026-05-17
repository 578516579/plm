package cn.com.bosssfot.dv.plm.common.ai.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import cn.com.bosssfot.dv.plm.common.ai.AiProperties;
import cn.com.bosssfot.dv.plm.common.ai.AiProvider;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatMessage;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;

/**
 * Anthropic Claude Messages API Provider.
 *
 * <p>端点: POST {base-url}/v1/messages</p>
 *
 * <p>认证 header(注意与 OpenAI 不同):</p>
 * <ul>
 *   <li>{@code x-api-key: <api-key>}</li>
 *   <li>{@code anthropic-version: 2023-06-01}</li>
 * </ul>
 *
 * <p>关键协议差异:</p>
 * <ul>
 *   <li>system 是顶层字段,不是 messages 数组里的一条</li>
 *   <li>max_tokens 是 <b>必填</b>(OpenAI 是可选)</li>
 *   <li>响应 content 是数组,取 [0].text</li>
 *   <li>usage.input_tokens / usage.output_tokens (不是 prompt/completion)</li>
 * </ul>
 *
 * @author plm
 */
public class AnthropicProvider implements AiProvider {
    private static final Logger log = LoggerFactory.getLogger(AnthropicProvider.class);
    private static final int DEFAULT_MAX_TOKENS = 2048;

    private final AiProperties.Anthropic cfg;
    private final RestTemplate rest;

    public AnthropicProvider(AiProperties.Anthropic cfg, RestTemplate rest) {
        this.cfg = cfg;
        this.rest = rest;
    }

    @Override
    public String name() { return "anthropic"; }

    @Override
    public boolean isAvailable() { return cfg.isUsable(); }

    @Override
    @SuppressWarnings("unchecked")
    public AiChatResult chat(AiChatRequest req) {
        long start = System.currentTimeMillis();
        String url = trimRight(cfg.getBaseUrl(), '/') + "/v1/messages";

        // Anthropic messages 不含 system (system 是顶层字段)
        List<Map<String, Object>> msgs = new ArrayList<>();
        for (AiChatMessage m : req.getMessages()) {
            if ("system".equalsIgnoreCase(m.getRole())) continue;
            msgs.add(Map.of("role", m.getRole(), "content", m.getContent() == null ? "" : m.getContent()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", req.getModel() == null || req.getModel().isBlank() ? cfg.getDefaultModel() : req.getModel());
        body.put("max_tokens", req.getMaxTokens() == null ? DEFAULT_MAX_TOKENS : req.getMaxTokens());
        if (req.getSystem() != null && !req.getSystem().isBlank()) body.put("system", req.getSystem());
        if (req.getTemperature() != null) body.put("temperature", req.getTemperature());
        body.put("messages", msgs);
        body.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", cfg.getApiKey());
        headers.set("anthropic-version", cfg.getVersion());
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), Map.class);
            long elapsed = System.currentTimeMillis() - start;
            if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
                return failWithElapsed("anthropic", "HTTP " + resp.getStatusCode().value(), elapsed);
            }
            Map<String, Object> root = (Map<String, Object>) resp.getBody();
            List<Map<String, Object>> content = (List<Map<String, Object>>) root.get("content");
            String text = "";
            if (content != null) {
                StringBuilder sb = new StringBuilder();
                for (Map<String, Object> block : content) {
                    if ("text".equalsIgnoreCase(str(block.get("type")))) {
                        sb.append(str(block.get("text")));
                    }
                }
                text = sb.toString();
            }

            AiChatResult r = AiChatResult.ok("anthropic", str(root.get("model")), text);
            r.setFinishReason(str(root.get("stop_reason")));
            r.setRequestId(str(root.get("id")));
            Map<String, Object> usage = (Map<String, Object>) root.getOrDefault("usage", Map.of());
            long in  = (long) num(usage.get("input_tokens"));
            long out = (long) num(usage.get("output_tokens"));
            r.setPromptTokens(in);
            r.setCompletionTokens(out);
            r.setTotalTokens(in + out);
            r.setElapsedMs(elapsed);
            log.info("[Ai-anthropic] {} ok,model={},tokens(in/out)={}/{},elapsed={}ms,caller={}",
                    r.getRequestId(), r.getModel(), in, out, elapsed, req.getCallerTag());
            return r;

        } catch (RestClientResponseException e) {
            long elapsed = System.currentTimeMillis() - start;
            int code = e.getStatusCode().value();
            log.warn("[Ai-anthropic] HTTP {} {} body={}", code, e.getStatusText(),
                    safeShort(e.getResponseBodyAsString()));
            return failWithElapsed("anthropic", "HTTP " + code + ": " + e.getStatusText(), elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[Ai-anthropic] call failed: {}", e.toString());
            return failWithElapsed("anthropic", e.getClass().getSimpleName() + ": " + e.getMessage(), elapsed);
        }
    }

    private static AiChatResult failWithElapsed(String provider, String err, long elapsed) {
        AiChatResult r = AiChatResult.fail(provider, err);
        r.setElapsedMs(elapsed);
        return r;
    }
    private static String str(Object o) { return o == null ? "" : String.valueOf(o); }
    private static double num(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception ignore) { return 0; }
    }
    private static String safeShort(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
    private static String trimRight(String s, char c) {
        if (s == null) return "";
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == c) end--;
        return s.substring(0, end);
    }
}
