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
 * OpenAI Chat Completions 协议 Provider — 一次实现覆盖多个国产/海外服务:
 *
 * <table>
 *   <tr><th>服务</th><th>base-url</th><th>典型 model</th></tr>
 *   <tr><td>OpenAI</td><td>https://api.openai.com/v1</td><td>gpt-4o-mini / gpt-4o</td></tr>
 *   <tr><td>DeepSeek</td><td>https://api.deepseek.com/v1</td><td>deepseek-chat / deepseek-reasoner</td></tr>
 *   <tr><td>通义千问</td><td>https://dashscope.aliyuncs.com/compatible-mode/v1</td><td>qwen-max / qwen-turbo</td></tr>
 *   <tr><td>Moonshot</td><td>https://api.moonshot.cn/v1</td><td>moonshot-v1-8k</td></tr>
 *   <tr><td>SiliconFlow</td><td>https://api.siliconflow.cn/v1</td><td>各厂商在该平台的模型名</td></tr>
 *   <tr><td>智谱 GLM</td><td>https://open.bigmodel.cn/api/paas/v4</td><td>glm-4-plus</td></tr>
 * </table>
 *
 * <p>切换厂商:只需改 {@code plm.ai.openai.base-url} 和 {@code api-key},无需改代码。</p>
 *
 * @author plm
 */
public class OpenAiCompatibleProvider implements AiProvider {
    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleProvider.class);

    private final AiProperties.OpenAi cfg;
    private final RestTemplate rest;

    public OpenAiCompatibleProvider(AiProperties.OpenAi cfg, RestTemplate rest) {
        this.cfg = cfg;
        this.rest = rest;
    }

    @Override
    public String name() { return "openai"; }

    @Override
    public boolean isAvailable() { return cfg.isUsable(); }

    @Override
    @SuppressWarnings("unchecked")
    public AiChatResult chat(AiChatRequest req) {
        long start = System.currentTimeMillis();
        String url = trimRight(cfg.getBaseUrl(), '/') + "/chat/completions";

        // 构造 messages: system 放在最前(OpenAI 协议)
        List<Map<String, Object>> msgs = new ArrayList<>();
        if (req.getSystem() != null && !req.getSystem().isBlank()) {
            msgs.add(Map.of("role", "system", "content", req.getSystem()));
        }
        for (AiChatMessage m : req.getMessages()) {
            msgs.add(Map.of("role", m.getRole(), "content", m.getContent() == null ? "" : m.getContent()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", req.getModel() == null || req.getModel().isBlank() ? cfg.getDefaultModel() : req.getModel());
        body.put("messages", msgs);
        if (req.getTemperature() != null) body.put("temperature", req.getTemperature());
        if (req.getMaxTokens() != null) body.put("max_tokens", req.getMaxTokens());
        body.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(cfg.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), Map.class);
            long elapsed = System.currentTimeMillis() - start;
            if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
                return failWithElapsed("openai", "HTTP " + resp.getStatusCode().value(), elapsed);
            }
            Map<String, Object> root = (Map<String, Object>) resp.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) root.get("choices");
            if (choices == null || choices.isEmpty()) {
                return failWithElapsed("openai", "响应缺少 choices", elapsed);
            }
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> msg = (Map<String, Object>) choice.get("message");
            String text = msg == null ? "" : str(msg.get("content"));
            String finishReason = str(choice.get("finish_reason"));

            AiChatResult r = AiChatResult.ok("openai", str(root.get("model")), text);
            r.setFinishReason(finishReason);
            r.setRequestId(str(root.get("id")));
            Map<String, Object> usage = (Map<String, Object>) root.getOrDefault("usage", Map.of());
            r.setPromptTokens((long) num(usage.get("prompt_tokens")));
            r.setCompletionTokens((long) num(usage.get("completion_tokens")));
            r.setTotalTokens((long) num(usage.get("total_tokens")));
            r.setElapsedMs(elapsed);
            log.info("[Ai-openai] {} ok,model={},tokens={},elapsed={}ms,caller={}",
                    r.getRequestId(), r.getModel(), r.getTotalTokens(), elapsed, req.getCallerTag());
            return r;

        } catch (RestClientResponseException e) {
            long elapsed = System.currentTimeMillis() - start;
            int code = e.getStatusCode().value();
            log.warn("[Ai-openai] HTTP {} {} body={}", code, e.getStatusText(),
                    safeShort(e.getResponseBodyAsString()));
            return failWithElapsed("openai", "HTTP " + code + ": " + e.getStatusText(), elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[Ai-openai] call failed: {}", e.toString());
            return failWithElapsed("openai", e.getClass().getSimpleName() + ": " + e.getMessage(), elapsed);
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
