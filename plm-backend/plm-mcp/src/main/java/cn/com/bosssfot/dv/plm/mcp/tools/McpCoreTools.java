package cn.com.bosssfot.dv.plm.mcp.tools;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import cn.com.bosssfot.dv.plm.mcp.annotation.McpTool;

/**
 * MCP 核心工具集 —— 系统自检 / 元信息查询。
 *
 * <p>这是 plm-core MCP Server 的最小工具集，让 Claude Code / Cursor 等 Agent
 *    在接通连接后立刻能跑通 {@code tools/list} + {@code tools/call} 流程。
 *
 * <p>业务工具（project.list / requirement.create 等）后续在对应业务模块的 Service 上加 @McpTool 即可，
 *    无需改 plm-mcp。
 */
@Component
public class McpCoreTools {

    @McpTool(
        name = "mcp.ping",
        description = "心跳。返回 pong + 当前服务器时间，用于联通性自检。",
        serverCode = "plm-core",
        tags = {"system", "health"}
    )
    public Map<String, Object> ping() {
        Map<String, Object> m = new HashMap<>();
        m.put("status", "pong");
        m.put("serverTime", System.currentTimeMillis());
        return m;
    }

    @McpTool(
        name = "mcp.echo",
        description = "把 arguments.text 原样返回，用于参数序列化 / 反序列化测试。",
        inputSchema = "{\"type\":\"object\",\"properties\":{\"text\":{\"type\":\"string\"}},\"required\":[\"text\"]}",
        serverCode = "plm-core",
        tags = {"system", "debug"}
    )
    public Map<String, Object> echo(Map<String, Object> args) {
        Map<String, Object> m = new HashMap<>();
        m.put("text", args == null ? null : args.get("text"));
        return m;
    }
}
