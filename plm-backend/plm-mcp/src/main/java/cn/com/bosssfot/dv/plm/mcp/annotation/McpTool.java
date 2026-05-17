package cn.com.bosssfot.dv.plm.mcp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 把 Spring Bean 的方法注册为 MCP 工具。
 *
 * <p>启动期 {@link cn.com.bosssfot.dv.plm.mcp.service.McpToolRegistry}
 *    扫描带本注解的方法并构建 ToolDefinition 索引；运行时 /mcp/tools/list
 *    返回索引，/mcp/tools/call 用反射调度。
 *
 * <p>示例：
 * <pre>
 * &#64;McpTool(name = "project.list", description = "列出项目")
 * public List&lt;Project&gt; listProjects(@McpToolParam("status") String status) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpTool {

    /** 工具名（必填），通常形如 "<entity>.<action>"，例：project.list */
    String name();

    /** 工具描述（必填），LLM 用它判断何时该调用 */
    String description();

    /** 输入参数 JSON Schema（选填，留空表示无参） */
    String inputSchema() default "";

    /** 所属 Server 编码，必须与 tb_mcp_server.server_code 匹配 */
    String serverCode() default "plm-core";

    /** 标签（便于按标签过滤工具） */
    String[] tags() default {};
}
