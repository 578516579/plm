package cn.com.bosssfot.dv.plm.common.dify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import cn.com.bosssfot.dv.plm.common.dify.impl.DifyServiceHttpImpl;
import cn.com.bosssfot.dv.plm.common.dify.impl.DifyServiceMockImpl;

/**
 * Dify 自动装配 — 由 {@link DifyProperties#isUsable()} 决定走 HTTP 还是 Mock。
 *
 * <p>装配规则:</p>
 * <ul>
 *   <li>{@code plm.dify.enabled=true} 且 api-key 非空非占位 → {@link DifyServiceHttpImpl}</li>
 *   <li>否则 → {@link DifyServiceMockImpl} (本地启动默认)</li>
 * </ul>
 *
 * <p>RestTemplate 独立 Bean 名 {@code difyRestTemplate},不污染应用其他 RestTemplate。</p>
 *
 * @author plm
 */
@Configuration
@EnableConfigurationProperties(DifyProperties.class)
public class DifyAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(DifyAutoConfiguration.class);

    @Bean("difyRestTemplate")
    public RestTemplate difyRestTemplate(DifyProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getConnectTimeoutMs());
        factory.setReadTimeout(props.getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    @Bean
    public DifyService difyService(DifyProperties props, RestTemplate difyRestTemplate) {
        if (props.isUsable()) {
            log.info("[Dify] HTTP 实现已装配 — baseUrl={}, readTimeout={}ms, workflows={}",
                    props.getBaseUrl(), props.getReadTimeoutMs(), props.getWorkflows().size());
            return new DifyServiceHttpImpl(props, difyRestTemplate);
        }
        log.warn("[Dify] 未启用或 api-key 缺失 → 装配 Mock 实现 (enabled={}, hasApiKey={})",
                props.isEnabled(), props.getApiKey() != null && !props.getApiKey().isBlank());
        return new DifyServiceMockImpl(props);
    }
}
