package cn.com.bosssfot.dv.plm.common.ai;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import cn.com.bosssfot.dv.plm.common.ai.impl.AiServiceImpl;
import cn.com.bosssfot.dv.plm.common.ai.impl.AnthropicProvider;
import cn.com.bosssfot.dv.plm.common.ai.impl.DifyAiProvider;
import cn.com.bosssfot.dv.plm.common.ai.impl.MockAiProvider;
import cn.com.bosssfot.dv.plm.common.ai.impl.OpenAiCompatibleProvider;
import cn.com.bosssfot.dv.plm.common.dify.DifyService;

/**
 * AI 集成自动装配 — 同时注册 4 个 provider,可全部装配也可只装配 mock。
 *
 * <p>装配规则:</p>
 * <ul>
 *   <li>{@link MockAiProvider} —— 永远装配</li>
 *   <li>{@link OpenAiCompatibleProvider} —— 永远装配,运行期通过 {@link AiProvider#isAvailable()} 判定</li>
 *   <li>{@link AnthropicProvider} —— 永远装配,运行期判定</li>
 *   <li>{@link DifyAiProvider} —— 依赖 {@link DifyService} bean,后者已在 {@code DifyAutoConfiguration}</li>
 * </ul>
 *
 * <p>"永远装配,运行期判定" 设计的好处:Bean 容器稳定不依赖配置态,
 * AiService 可在请求路由时根据 isAvailable() 即时降级,无需重启。</p>
 *
 * @author plm
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(AiAutoConfiguration.class);

    /** 单独的 RestTemplate (用最大的 timeout 兼容 openai/anthropic) */
    @Bean("aiRestTemplate")
    public RestTemplate aiRestTemplate(AiProperties props) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        int connect = Math.max(props.getOpenai().getConnectTimeoutMs(), props.getAnthropic().getConnectTimeoutMs());
        int read    = Math.max(props.getOpenai().getReadTimeoutMs(),    props.getAnthropic().getReadTimeoutMs());
        f.setConnectTimeout(connect);
        f.setReadTimeout(read);
        return new RestTemplate(f);
    }

    @Bean
    public AiProvider mockAiProvider() { return new MockAiProvider(); }

    @Bean
    public AiProvider openAiProvider(AiProperties props, RestTemplate aiRestTemplate) {
        return new OpenAiCompatibleProvider(props.getOpenai(), aiRestTemplate);
    }

    @Bean
    public AiProvider anthropicProvider(AiProperties props, RestTemplate aiRestTemplate) {
        return new AnthropicProvider(props.getAnthropic(), aiRestTemplate);
    }

    @Bean
    public AiProvider difyAiProvider(DifyService difyService) {
        return new DifyAiProvider(difyService);
    }

    @Bean
    public AiService aiService(List<AiProvider> providers, AiProperties props,
                                ObjectProvider<AiInvocationRecorder> recorderProvider) {
        // 拷贝列表防外部修改
        List<AiProvider> copy = new ArrayList<>(providers);
        log.info("[AiService] init — providers={}, default={}",
                copy.stream().map(AiProvider::name).toList(), props.getDefaultProvider());
        AiServiceImpl svc = new AiServiceImpl(copy, props);
        // 审计 recorder 可选注入 — 若 plm-ai-agent 等模块实现了,自动接上;没有也不会报错
        AiInvocationRecorder recorder = recorderProvider.getIfAvailable();
        if (recorder != null) svc.setRecorder(recorder);
        return svc;
    }
}
