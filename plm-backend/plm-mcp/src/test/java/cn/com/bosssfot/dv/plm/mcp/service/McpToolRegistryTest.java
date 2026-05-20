package cn.com.bosssfot.dv.plm.mcp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import cn.com.bosssfot.dv.plm.mcp.annotation.McpTool;
import cn.com.bosssfot.dv.plm.mcp.protocol.dto.ToolDefinition;

/**
 * McpToolRegistry 启动期扫描 + lookup 测试。
 *
 * 用 mock ApplicationContext 喂一个含 @McpTool 注解方法的样例 Bean，
 * 验证 registry 能找到该工具并暴露反射元数据。
 */
class McpToolRegistryTest {

    private McpToolRegistry registry;

    @BeforeEach
    void setUp() throws Exception {
        registry = new McpToolRegistry();
        SampleToolBean sample = new SampleToolBean();
        ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.getBeanDefinitionNames()).thenReturn(new String[]{"sampleBean"});
        when(ctx.getBean("sampleBean")).thenReturn(sample);

        Field f = McpToolRegistry.class.getDeclaredField("applicationContext");
        f.setAccessible(true);
        f.set(registry, ctx);

        registry.scan();
    }

    @Test
    @DisplayName("扫描到带 @McpTool 注解的方法")
    void scansAnnotatedMethods() {
        ToolDefinition pingDef = registry.find("test.ping");
        assertThat(pingDef).isNotNull();
        assertThat(pingDef.getDescription()).isEqualTo("test ping");
        assertThat(pingDef.getServerCode()).isEqualTo("test-server");
        assertThat(pingDef.getMethod()).isNotNull();
        assertThat(pingDef.getBean()).isNotNull();
    }

    @Test
    @DisplayName("不存在的工具 → find 返回 null")
    void lookupMissingReturnsNull() {
        assertThat(registry.find("non.existent")).isNull();
    }

    @Test
    @DisplayName("按 serverCode 列出工具")
    void listByServer() {
        assertThat(registry.listByServer("test-server"))
            .extracting(ToolDefinition::getName)
            .containsExactlyInAnyOrder("test.ping", "test.echo");
        assertThat(registry.listByServer("non-existent-server")).isEmpty();
    }

    @Test
    @DisplayName("listAll 返回所有")
    void listAll() {
        assertThat(registry.listAll()).hasSize(2);
    }

    @Test
    @DisplayName("toListEntry 不暴露 bean/method（内部反射元数据）")
    void listEntryHidesReflection() {
        ToolDefinition def = registry.find("test.echo");
        Map<String, Object> entry = def.toListEntry();
        assertThat(entry).containsKeys("name", "description");
        assertThat(entry).doesNotContainKeys("bean", "method");
    }

    /** 测试样例 bean —— 注册 2 个工具 */
    static class SampleToolBean {
        @McpTool(name = "test.ping", description = "test ping", serverCode = "test-server")
        public Map<String, Object> ping() {
            Map<String, Object> m = new HashMap<>();
            m.put("status", "pong");
            return m;
        }

        @McpTool(name = "test.echo", description = "test echo",
                 inputSchema = "{\"type\":\"object\"}", serverCode = "test-server")
        public Map<String, Object> echo(Map<String, Object> args) {
            return args;
        }

        /** 不带注解 —— 应被跳过 */
        public void unrelatedMethod() {}
    }
}
