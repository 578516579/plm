package cn.com.bosssfot.dv.plm.mcp.protocol.dto;

import java.util.Map;

/**
 * MCP 协议的 JSON-RPC 2.0 请求。
 *
 * <p>常见 method：
 * <ul>
 *   <li>{@code "tools/list"} — 列出工具，params 可空</li>
 *   <li>{@code "tools/call"} — 调用工具，params = {"name": "...", "arguments": {...}}</li>
 * </ul>
 */
public class JsonRpcRequest {

    private String jsonrpc = "2.0";
    private Object id;
    private String method;
    private Map<String, Object> params;

    public String getJsonrpc() { return jsonrpc; }
    public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }

    public Object getId() { return id; }
    public void setId(Object id) { this.id = id; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
}
