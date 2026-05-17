package cn.com.bosssfot.dv.plm.mcp.protocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSON;
import cn.com.bosssfot.dv.plm.common.annotation.Anonymous;
import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;
import cn.com.bosssfot.dv.plm.mcp.domain.McpServer;
import cn.com.bosssfot.dv.plm.mcp.domain.McpToolAudit;
import cn.com.bosssfot.dv.plm.mcp.protocol.dto.JsonRpcRequest;
import cn.com.bosssfot.dv.plm.mcp.protocol.dto.JsonRpcResponse;
import cn.com.bosssfot.dv.plm.mcp.protocol.dto.ToolDefinition;
import cn.com.bosssfot.dv.plm.mcp.service.IMcpServerService;
import cn.com.bosssfot.dv.plm.mcp.service.IMcpToolAuditService;
import cn.com.bosssfot.dv.plm.mcp.service.McpToolRegistry;

/**
 * MCP 协议端点（暴露给外部 LLM Agent）。
 *
 * <p>路径 {@code /mcp/*} 不在 JWT 鉴权范围（{@link Anonymous}），自行做 OAuth 长效 token 验证。
 *
 * <p>支持的 method：
 * <ul>
 *   <li>{@code tools/list} — 列出工具（按 server_code 过滤）</li>
 *   <li>{@code tools/call} — 反射调用 @McpTool 方法</li>
 * </ul>
 *
 * <p>所有 tools/call 同步落 {@link McpToolAudit} 审计；落库失败也不影响调用方响应（仅记日志）。
 */
@RestController
@RequestMapping("/mcp")
public class McpProtocolController {

    private static final Logger log = LoggerFactory.getLogger(McpProtocolController.class);

    @Autowired
    private McpToolRegistry registry;

    @Autowired
    private IMcpServerService mcpServerService;

    @Autowired
    private IMcpToolAuditService auditService;

    @Autowired
    private AesGcmCipher cipher;

    @Value("${plm.mcp.tool-timeout-ms:${MCP_TOOL_TIMEOUT_MS:30000}}")
    private int toolTimeoutMs;

    /** POST /mcp/tools/list */
    @Anonymous
    @PostMapping("/tools/list")
    public ResponseEntity<JsonRpcResponse> listTools(
            @RequestBody JsonRpcRequest req,
            HttpServletRequest httpReq) {

        McpServer server = authenticate(httpReq);
        if (server == null) {
            return ResponseEntity.status(401).body(
                JsonRpcResponse.error(req.getId(), 803, "OAuth 验证失败"));
        }

        List<ToolDefinition> tools = registry.listByServer(server.getServerCode());
        List<Map<String, Object>> toolList = tools.stream()
            .map(ToolDefinition::toListEntry)
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("tools", toolList);
        return ResponseEntity.ok(JsonRpcResponse.success(req.getId(), result));
    }

    /** POST /mcp/tools/call */
    @Anonymous
    @PostMapping("/tools/call")
    public ResponseEntity<JsonRpcResponse> callTool(
            @RequestBody JsonRpcRequest req,
            HttpServletRequest httpReq) {

        long startNanos = System.nanoTime();
        McpServer server = authenticate(httpReq);
        if (server == null) {
            return ResponseEntity.status(401).body(
                JsonRpcResponse.error(req.getId(), 803, "OAuth 验证失败"));
        }

        Map<String, Object> params = req.getParams();
        String toolName = params != null ? (String) params.get("name") : null;
        Object arguments = params != null ? params.get("arguments") : null;

        if (toolName == null) {
            return ResponseEntity.badRequest().body(
                JsonRpcResponse.error(req.getId(), 601, "params.name 不能为空"));
        }

        ToolDefinition def = registry.find(toolName);
        if (def == null || !server.getServerCode().equals(def.getServerCode())) {
            return ResponseEntity.status(404).body(
                JsonRpcResponse.error(req.getId(), 802,
                    "工具 [" + toolName + "] 在 [" + server.getServerCode() + "] 中未注册"));
        }

        // 反射调用 —— 当前实现按"单参数 Map"或"无参"两种最简形态分派；
        // 未来需要复杂参数映射时再扩展。
        McpToolAudit audit = new McpToolAudit();
        audit.setServerId(server.getId());
        audit.setToolName(toolName);
        audit.setCallerType("agent");
        audit.setCallerId(server.getOauthClientId());
        audit.setParamsJson(JSON.toJSONString(arguments));
        audit.setCallTime(new Date());

        try {
            Object result = invoke(def, arguments);
            String brief = result == null ? "null" : JSON.toJSONString(result);
            audit.setResultStatus("0");
            audit.setResultBrief(brief);
            audit.setLatencyMs((int) ((System.nanoTime() - startNanos) / 1_000_000));
            safeAudit(audit);

            Map<String, Object> rpcResult = new HashMap<>();
            rpcResult.put("content", List.of(Map.of("type", "json", "data", result)));
            return ResponseEntity.ok(JsonRpcResponse.success(req.getId(), rpcResult));
        } catch (InvocationTargetException e) {
            audit.setResultStatus("1");
            audit.setResultBrief(String.valueOf(e.getTargetException()));
            audit.setLatencyMs((int) ((System.nanoTime() - startNanos) / 1_000_000));
            safeAudit(audit);
            log.warn("[plm-mcp] 工具 {} 调用异常", toolName, e.getTargetException());
            return ResponseEntity.status(502).body(
                JsonRpcResponse.error(req.getId(), 804, "工具调用失败: " + e.getTargetException().getMessage()));
        } catch (Exception e) {
            audit.setResultStatus("1");
            audit.setResultBrief(String.valueOf(e));
            audit.setLatencyMs((int) ((System.nanoTime() - startNanos) / 1_000_000));
            safeAudit(audit);
            log.error("[plm-mcp] 工具 {} 反射失败", toolName, e);
            return ResponseEntity.status(500).body(
                JsonRpcResponse.error(req.getId(), 804, "工具反射失败: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────

    /**
     * 验证 Authorization: Bearer 头。
     *
     * Phase 1：长效 token。token = oauth_client_secret（明文），数据库存的是加密后的；
     * 解出来比对相等即通过。
     *
     * Phase 2：切换为 OAuth 2.0 Authorization Code，验签 JWT。
     */
    private McpServer authenticate(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        String token = auth.substring("Bearer ".length()).trim();
        // 简化：token 格式 = "<serverCode>:<clientSecret>"
        int idx = token.indexOf(':');
        if (idx <= 0) return null;
        String serverCode = token.substring(0, idx);
        String secret = token.substring(idx + 1);

        McpServer server = mcpServerService.selectMcpServerByCode(serverCode);
        if (server == null || !"0".equals(server.getStatus())) {
            return null;
        }
        if (server.getOauthClientSecretEnc() == null || server.getOauthClientSecretEnc().isEmpty()) {
            return null;
        }
        try {
            String stored = cipher.decrypt(server.getOauthClientSecretEnc());
            if (!constantTimeEquals(stored, secret)) {
                return null;
            }
            return server;
        } catch (Exception e) {
            log.warn("[plm-mcp] OAuth 验证：解密失败 serverCode={}", serverCode, e);
            return null;
        }
    }

    /** 反射调度：根据方法签名是 (Map) 还是 () 选择性传参 */
    private Object invoke(ToolDefinition def, Object arguments) throws Exception {
        Method m = def.getMethod();
        Class<?>[] paramTypes = m.getParameterTypes();
        if (paramTypes.length == 0) {
            return m.invoke(def.getBean());
        }
        if (paramTypes.length == 1 && Map.class.isAssignableFrom(paramTypes[0])) {
            return m.invoke(def.getBean(), arguments);
        }
        throw new UnsupportedOperationException(
            "@McpTool 方法当前仅支持 0 个参数或 1 个 Map 参数；其他签名待 Phase 2 支持");
    }

    private void safeAudit(McpToolAudit audit) {
        try {
            auditService.recordAudit(audit);
        } catch (Exception e) {
            log.error("[plm-mcp] 审计落库失败 toolName={}", audit.getToolName(), e);
        }
    }

    /** 常量时间字符串比对（防 timing 攻击） */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
