package cn.com.bosssfot.dv.plm.integration.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 所有 {@link ConnectorAdapter} 的运行时索引。
 *
 * <p>启动期把所有 Spring Bean 类型为 ConnectorAdapter 的实例按 {@code type()} 收集。
 */
@Component
public class ConnectorAdapterRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConnectorAdapterRegistry.class);

    private final Map<String, ConnectorAdapter> byType = new HashMap<>();

    @Autowired(required = false)
    private List<ConnectorAdapter> adapters;

    @PostConstruct
    public void init() {
        if (adapters == null) return;
        for (ConnectorAdapter a : adapters) {
            String t = a.type();
            if (t == null || t.isEmpty()) {
                log.warn("[plm-integration] 跳过：adapter {} type() 为空", a.getClass());
                continue;
            }
            if (byType.containsKey(t)) {
                log.warn("[plm-integration] 跳过：adapter type={} 重复（已有 {}）", t, byType.get(t).getClass());
                continue;
            }
            byType.put(t, a);
            log.info("[plm-integration] 注册 adapter: type={}, class={}", t, a.getClass().getSimpleName());
        }
    }

    public ConnectorAdapter get(String type) {
        return byType.get(type);
    }

    public boolean supports(String type) {
        return byType.containsKey(type);
    }

    public Set<String> supportedTypes() {
        return byType.keySet();
    }
}
