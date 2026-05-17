package cn.com.bosssfot.dv.plm.mcp.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import cn.com.bosssfot.dv.plm.mcp.annotation.McpTool;
import cn.com.bosssfot.dv.plm.mcp.protocol.dto.ToolDefinition;

/**
 * MCP 工具注册中心。
 *
 * <p>启动期扫描所有 Spring Bean，把带 {@link McpTool @McpTool} 注解的方法构建
 *    {@link ToolDefinition} 索引。运行时供：
 * <ul>
 *   <li>{@code POST /mcp/tools/list} — 列出（按 serverCode 过滤）</li>
 *   <li>{@code POST /mcp/tools/call} — 反射调用</li>
 * </ul>
 *
 * <p>当前实现为静态注册（启动期扫描）。Phase 2 支持运行时增量注册。
 */
@Component
public class McpToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpToolRegistry.class);

    /** name → ToolDefinition */
    private final Map<String, ToolDefinition> byName = new HashMap<>();

    /** serverCode → List<ToolDefinition> */
    private final Map<String, List<ToolDefinition>> byServer = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void scan() {
        int count = 0;
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (Exception ignore) {
                // bean 创建失败的情况（如 FactoryBean 错误），不影响扫描
                continue;
            }
            Class<?> targetClass = org.springframework.aop.support.AopUtils.getTargetClass(bean);
            for (Method method : targetClass.getDeclaredMethods()) {
                McpTool ann = method.getAnnotation(McpTool.class);
                if (ann == null) continue;
                ToolDefinition def = new ToolDefinition();
                def.setName(ann.name());
                def.setDescription(ann.description());
                def.setInputSchema(ann.inputSchema());
                def.setServerCode(ann.serverCode());
                def.setTags(ann.tags());
                def.setBean(bean);
                def.setMethod(method);
                if (byName.containsKey(ann.name())) {
                    log.warn("[plm-mcp] 工具名重复: {}（已存在的来自 {}，忽略本次 {} 的注册）",
                        ann.name(), byName.get(ann.name()).getMethod(), method);
                    continue;
                }
                byName.put(ann.name(), def);
                byServer.computeIfAbsent(ann.serverCode(), k -> new ArrayList<>()).add(def);
                count++;
                log.info("[plm-mcp] 注册 MCP 工具: {} (serverCode={}, bean={}.{})",
                    ann.name(), ann.serverCode(), targetClass.getSimpleName(), method.getName());
            }
        }
        log.info("[plm-mcp] MCP 工具注册完成，共 {} 个工具 / {} 个 server", count, byServer.size());
    }

    public List<ToolDefinition> listAll() {
        return new ArrayList<>(byName.values());
    }

    public List<ToolDefinition> listByServer(String serverCode) {
        return byServer.getOrDefault(serverCode, Collections.emptyList());
    }

    public ToolDefinition find(String name) {
        return byName.get(name);
    }
}
