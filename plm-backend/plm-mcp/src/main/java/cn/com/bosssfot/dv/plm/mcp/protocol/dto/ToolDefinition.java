package cn.com.bosssfot.dv.plm.mcp.protocol.dto;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

/**
 * MCP 工具运行时定义。
 *
 * <p>由 {@link cn.com.bosssfot.dv.plm.mcp.service.McpToolRegistry} 启动期构建索引。
 *
 * <p>tools/list 响应只返回外部可见字段（name/description/inputSchema），
 *    bean/method 仅供 tools/call 内部反射调用使用。
 */
public class ToolDefinition {

    /** 工具名，例 "project.list" */
    private String name;

    /** 工具描述 */
    private String description;

    /** 入参 JSON Schema（原始 JSON 字符串） */
    private String inputSchema;

    /** 所属 MCP Server 编码 */
    private String serverCode;

    /** 标签 */
    private String[] tags;

    // ─── 反射调度专用，不参与 tools/list 响应 ──────────────────────────────────

    private transient Object bean;
    private transient Method method;

    public Map<String, Object> toListEntry() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("description", description);
        if (inputSchema != null && !inputSchema.isEmpty()) {
            m.put("inputSchema", inputSchema);
        }
        return m;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInputSchema() { return inputSchema; }
    public void setInputSchema(String inputSchema) { this.inputSchema = inputSchema; }

    public String getServerCode() { return serverCode; }
    public void setServerCode(String serverCode) { this.serverCode = serverCode; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public Object getBean() { return bean; }
    public void setBean(Object bean) { this.bean = bean; }

    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }
}
