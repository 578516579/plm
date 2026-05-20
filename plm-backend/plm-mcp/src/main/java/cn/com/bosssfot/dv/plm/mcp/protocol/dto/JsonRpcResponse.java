package cn.com.bosssfot.dv.plm.mcp.protocol.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP 协议的 JSON-RPC 2.0 响应。
 */
public class JsonRpcResponse {

    private String jsonrpc = "2.0";
    private Object id;
    private Object result;
    private Map<String, Object> error;

    public static JsonRpcResponse success(Object id, Object result) {
        JsonRpcResponse r = new JsonRpcResponse();
        r.id = id;
        r.result = result;
        return r;
    }

    public static JsonRpcResponse error(Object id, int code, String message) {
        JsonRpcResponse r = new JsonRpcResponse();
        r.id = id;
        Map<String, Object> err = new HashMap<>();
        err.put("code", code);
        err.put("message", message);
        r.error = err;
        return r;
    }

    public String getJsonrpc() { return jsonrpc; }
    public Object getId() { return id; }
    public Object getResult() { return result; }
    public Map<String, Object> getError() { return error; }
}
