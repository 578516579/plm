package cn.com.bosssfot.dv.plm.common.dify.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import cn.com.bosssfot.dv.plm.common.dify.DifyProperties;
import cn.com.bosssfot.dv.plm.common.dify.DifyService;
import cn.com.bosssfot.dv.plm.common.dify.dto.DifyWorkflowResult;

/**
 * Dify 真实 HTTP 实现 — 调用 Dify Service API。
 *
 * <p>端点:</p>
 * <ul>
 *   <li>POST {base-url}/workflows/run — 运行 workflow,response_mode=blocking</li>
 *   <li>Header: {@code Authorization: Bearer <api-key>}, {@code Content-Type: application/json}</li>
 * </ul>
 *
 * <p>请求体:</p>
 * <pre>{
 *   "workflow_id": "wf-xxxx",   // 注:Dify Service API 实际通过 app key 绑定 workflow,workflow_id 仅用于多 workflow 应用
 *   "inputs": { ... },
 *   "response_mode": "blocking",
 *   "user": "plm-system"
 * }</pre>
 *
 * <p>响应解析:</p>
 * <pre>{
 *   "workflow_run_id": "...",
 *   "task_id": "...",
 *   "data": {
 *     "status": "succeeded" | "failed",
 *     "outputs": { ... },
 *     "elapsed_time": 12.34,
 *     "total_tokens": 1234,
 *     "error": "..."  // 仅失败时
 *   }
 * }</pre>
 *
 * <p>失败兜底:任何异常(HTTP 4xx/5xx/超时/JSON 解析)都被吃掉,返回 {@code success=false} 的 Result。
 * 业务层根据 Result 决定抛 {@code ServiceException(708)} 还是降级。</p>
 *
 * @author plm
 */
public class DifyServiceHttpImpl implements DifyService {
    private static final Logger log = LoggerFactory.getLogger(DifyServiceHttpImpl.class);

    private final DifyProperties props;
    private final RestTemplate rest;

    public DifyServiceHttpImpl(DifyProperties props, RestTemplate rest) {
        this.props = props;
        this.rest = rest;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DifyWorkflowResult runWorkflow(String workflowId, Map<String, Object> inputs) {
        if (workflowId == null || workflowId.isBlank()) {
            return DifyWorkflowResult.fail("workflowId 不能为空");
        }
        String url = trimRight(props.getBaseUrl(), '/') + "/workflows/run";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(props.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workflow_id", workflowId);
        body.put("inputs", inputs == null ? Map.of() : inputs);
        body.put("response_mode", "blocking");
        body.put("user", props.getDefaultUser() == null ? "plm-system" : props.getDefaultUser());

        try {
            ResponseEntity<Map> resp = rest.exchange(url, org.springframework.http.HttpMethod.POST,
                    new HttpEntity<>(body, headers), Map.class);
            if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
                return DifyWorkflowResult.fail("Dify HTTP " + resp.getStatusCode().value());
            }
            Map<String, Object> root = (Map<String, Object>) resp.getBody();
            String runId  = str(root.get("workflow_run_id"));
            String taskId = str(root.get("task_id"));
            Map<String, Object> data = (Map<String, Object>) root.getOrDefault("data", Map.of());
            String status = str(data.get("status"));
            if (!"succeeded".equalsIgnoreCase(status)) {
                String err = str(data.get("error"));
                log.warn("[Dify] workflow {} failed: status={}, error={}", workflowId, status, err);
                return DifyWorkflowResult.fail("Dify workflow status=" + status + (err.isEmpty() ? "" : ", " + err));
            }
            Map<String, Object> outputs = (Map<String, Object>) data.getOrDefault("outputs", Map.of());
            double elapsed = num(data.get("elapsed_time"));
            long   tokens  = (long) num(data.get("total_tokens"));
            log.info("[Dify] workflow {} ok,runId={},elapsed={}s,tokens={}", workflowId, runId, elapsed, tokens);
            return DifyWorkflowResult.ok(runId, taskId, outputs, elapsed, tokens);

        } catch (RestClientResponseException e) {
            int code = e.getStatusCode().value();
            log.warn("[Dify] HTTP {} {} body={}", code, e.getStatusText(),
                    safeShort(e.getResponseBodyAsString()));
            return DifyWorkflowResult.fail("Dify HTTP " + code + ": " + e.getStatusText());
        } catch (Exception e) {
            log.warn("[Dify] call failed: {}", e.toString());
            return DifyWorkflowResult.fail("Dify 调用异常: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public DifyWorkflowResult runWorkflowByType(String agentType, Map<String, Object> inputs) {
        String wf = props.getWorkflows().get(agentType);
        if (wf == null || wf.isBlank()) {
            return DifyWorkflowResult.fail("agent_type 未在 plm.dify.workflows 中映射: " + agentType);
        }
        return runWorkflow(wf, inputs);
    }

    @Override
    public boolean isLive() { return true; }

    // --- helpers ---
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
