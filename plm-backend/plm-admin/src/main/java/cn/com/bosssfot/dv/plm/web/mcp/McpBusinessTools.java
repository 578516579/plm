package cn.com.bosssfot.dv.plm.web.mcp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cn.com.bosssfot.dv.plm.mcp.annotation.McpTool;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.service.IProjectService;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.requirement.service.IRequirementService;
import cn.com.bosssfot.dv.plm.task.domain.Task;
import cn.com.bosssfot.dv.plm.task.service.ITaskService;

/**
 * MCP 业务工具集 —— 把 PLM 核心业务数据（项目 / 需求 / 任务）以只读方式暴露给外部 LLM Agent
 * （Claude Code / Cursor），实现 PRD §2.5 / §3.5「PLM 业务能力 MCP 化」。
 *
 * <p>放在 plm-admin（而非 plm-mcp）的原因:plm-admin 已依赖全部业务模块 + plm-mcp,可直接
 * {@code @Autowired} 业务 Service,避免 plm-mcp 反向耦合业务模块或走反射延迟绑定。
 * {@link cn.com.bosssfot.dv.plm.mcp.service.McpToolRegistry} 在启动期扫全部 Bean,本类被自动注册。
 *
 * <p>仅暴露 <b>只读</b>(list/get)工具,理由:
 * <ul>
 *   <li>安全 — 外部 Agent 不应通过 MCP 直接改库;写操作仍走带权限校验的 REST Controller</li>
 *   <li>无 user 上下文 — MCP 调用是匿名的(走 OAuth token,非 JWT),而 insert/update/selectMyTasks
 *       依赖 {@code SecurityUtils.getUserId()/getUsername()},只读 list/get 不依赖</li>
 * </ul>
 *
 * <p>共用 serverCode {@code plm-core}(与 {@link cn.com.bosssfot.dv.plm.mcp.tools.McpCoreTools}
 * 的 ping/echo 同属一个 MCP Server),外部 Agent 一次接通即可拿到系统自检 + 业务查询全套工具。
 */
@Component
public class McpBusinessTools {

    /** 与 McpCoreTools 同 server,外部 Agent 接通 plm-core 即可见全部工具 */
    private static final String SERVER = "plm-core";
    /** list 类工具默认返回条数 */
    private static final int DEFAULT_LIMIT = 50;
    /** list 类工具返回条数硬上限,防止单次拉爆 */
    private static final int MAX_LIMIT = 200;

    @Autowired private IProjectService projectService;
    @Autowired private IRequirementService requirementService;
    @Autowired private ITaskService taskService;

    // ───────────────────────── 项目 Project ─────────────────────────

    @McpTool(
        name = "project.list",
        description = "查询 PLM 项目列表。可选过滤 status(项目状态码)、projectType(类型码)、projectName(名称模糊)、limit(默认50,上限200)。",
        inputSchema = "{\"type\":\"object\",\"properties\":{"
            + "\"status\":{\"type\":\"string\",\"description\":\"项目状态码,如 1=进行中\"},"
            + "\"projectType\":{\"type\":\"string\"},"
            + "\"projectName\":{\"type\":\"string\",\"description\":\"名称模糊匹配\"},"
            + "\"limit\":{\"type\":\"integer\",\"default\":50}}}",
        serverCode = SERVER,
        tags = {"business", "project", "readonly"}
    )
    public Map<String, Object> projectList(Map<String, Object> args) {
        Project filter = new Project();
        filter.setStatus(asString(args, "status"));
        filter.setProjectType(asString(args, "projectType"));
        filter.setProjectName(asString(args, "projectName"));
        return capped(projectService.selectProjectList(filter), limitOf(args));
    }

    @McpTool(
        name = "project.get",
        description = "按 id 查询单个 PLM 项目的完整字段。",
        inputSchema = "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"}},\"required\":[\"id\"]}",
        serverCode = SERVER,
        tags = {"business", "project", "readonly"}
    )
    public Object projectGet(Map<String, Object> args) {
        Long id = requireId(args);
        Project p = projectService.selectProjectById(id);
        return p != null ? p : notFound("project", id);
    }

    // ───────────────────────── 需求 Requirement ─────────────────────────

    @McpTool(
        name = "requirement.list",
        description = "查询 PLM 需求列表。可选过滤 projectId(所属项目)、status(需求状态码)、priority(优先级码)、limit(默认50,上限200)。",
        inputSchema = "{\"type\":\"object\",\"properties\":{"
            + "\"projectId\":{\"type\":\"integer\"},"
            + "\"status\":{\"type\":\"string\"},"
            + "\"priority\":{\"type\":\"string\"},"
            + "\"limit\":{\"type\":\"integer\",\"default\":50}}}",
        serverCode = SERVER,
        tags = {"business", "requirement", "readonly"}
    )
    public Map<String, Object> requirementList(Map<String, Object> args) {
        Requirement filter = new Requirement();
        filter.setProjectId(asLong(args, "projectId"));
        filter.setStatus(asString(args, "status"));
        filter.setPriority(asString(args, "priority"));
        return capped(requirementService.selectRequirementList(filter), limitOf(args));
    }

    @McpTool(
        name = "requirement.get",
        description = "按 id 查询单个 PLM 需求的完整字段。",
        inputSchema = "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"}},\"required\":[\"id\"]}",
        serverCode = SERVER,
        tags = {"business", "requirement", "readonly"}
    )
    public Object requirementGet(Map<String, Object> args) {
        Long id = requireId(args);
        Requirement r = requirementService.selectRequirementById(id);
        return r != null ? r : notFound("requirement", id);
    }

    // ───────────────────────── 任务 Task ─────────────────────────

    @McpTool(
        name = "task.list",
        description = "查询 PLM 任务列表。可选过滤 projectId、sprintId(迭代)、status(任务状态码)、assigneeUserId(负责人)、limit(默认50,上限200)。",
        inputSchema = "{\"type\":\"object\",\"properties\":{"
            + "\"projectId\":{\"type\":\"integer\"},"
            + "\"sprintId\":{\"type\":\"integer\"},"
            + "\"status\":{\"type\":\"string\"},"
            + "\"assigneeUserId\":{\"type\":\"integer\"},"
            + "\"limit\":{\"type\":\"integer\",\"default\":50}}}",
        serverCode = SERVER,
        tags = {"business", "task", "readonly"}
    )
    public Map<String, Object> taskList(Map<String, Object> args) {
        Task filter = new Task();
        filter.setProjectId(asLong(args, "projectId"));
        filter.setSprintId(asLong(args, "sprintId"));
        filter.setStatus(asString(args, "status"));
        filter.setAssigneeUserId(asLong(args, "assigneeUserId"));
        return capped(taskService.selectTaskList(filter), limitOf(args));
    }

    @McpTool(
        name = "task.get",
        description = "按 id 查询单个 PLM 任务的完整字段。",
        inputSchema = "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"}},\"required\":[\"id\"]}",
        serverCode = SERVER,
        tags = {"business", "task", "readonly"}
    )
    public Object taskGet(Map<String, Object> args) {
        Long id = requireId(args);
        Task t = taskService.selectTaskById(id);
        return t != null ? t : notFound("task", id);
    }

    // ───────────────────────── helpers ─────────────────────────

    /** 把 list 结果截到 limit 条,并带上 total / returned 元信息,便于 LLM 判断是否还有更多 */
    private static Map<String, Object> capped(List<?> all, int limit) {
        List<?> items = all.size() > limit ? new ArrayList<>(all.subList(0, limit)) : all;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", all.size());
        m.put("returned", items.size());
        m.put("truncated", all.size() > items.size());
        m.put("items", items);
        return m;
    }

    private static Map<String, Object> notFound(String type, Long id) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("found", false);
        m.put("type", type);
        m.put("id", id);
        return m;
    }

    /** id 必填,缺失或非法 → IllegalArgumentException(由协议层捕获转 804) */
    private static Long requireId(Map<String, Object> args) {
        Long id = asLong(args, "id");
        if (id == null) throw new IllegalArgumentException("缺少必填参数 id");
        return id;
    }

    private static int limitOf(Map<String, Object> args) {
        Long l = asLong(args, "limit");
        if (l == null || l <= 0) return DEFAULT_LIMIT;
        return (int) Math.min(l, MAX_LIMIT);
    }

    private static String asString(Map<String, Object> args, String key) {
        if (args == null) return null;
        Object v = args.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    /** 兼容 JSON 反序列化出来的 Integer / Long / BigDecimal / 数字字符串 */
    private static Long asLong(Map<String, Object> args, String key) {
        if (args == null) return null;
        Object v = args.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            String s = String.valueOf(v).trim();
            return s.isEmpty() ? null : Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
