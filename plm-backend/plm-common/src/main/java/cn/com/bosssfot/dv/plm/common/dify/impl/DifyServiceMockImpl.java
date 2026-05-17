package cn.com.bosssfot.dv.plm.common.dify.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.dify.DifyProperties;
import cn.com.bosssfot.dv.plm.common.dify.DifyService;
import cn.com.bosssfot.dv.plm.common.dify.dto.DifyWorkflowResult;

/**
 * Dify 降级实现 — 不发任何网络请求。
 *
 * <p>装配条件:{@code plm.dify.enabled=false} 或 api-key 缺失。<br/>
 * 用途:</p>
 * <ul>
 *   <li>本地零依赖启动(开发机不需要 Dify 实例)</li>
 *   <li>CI / E2E 跑 120+ 测试时,AI 路径返回稳定占位</li>
 *   <li>生产降级(Dify 实例故障时切回 mock,保证 PLM 核心流程可用)</li>
 * </ul>
 *
 * <p>返回固定结构: {@code outputs={"mock":true,"workflow":<id>,"echo":<inputs>}}。</p>
 *
 * @author plm
 */
public class DifyServiceMockImpl implements DifyService {
    private static final Logger log = LoggerFactory.getLogger(DifyServiceMockImpl.class);

    private final DifyProperties props;

    public DifyServiceMockImpl(DifyProperties props) { this.props = props; }

    @Override
    public DifyWorkflowResult runWorkflow(String workflowId, Map<String, Object> inputs) {
        log.debug("[Dify-mock] runWorkflow({}, {})", workflowId, inputs);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mock", true);
        out.put("workflow", workflowId == null ? "(none)" : workflowId);
        out.put("echo", inputs == null ? Map.of() : inputs);
        out.put("message", "Dify 未启用或 api-key 未配置,返回 mock 占位结果。生产请设置 plm.dify.enabled=true 并注入 DIFY_API_KEY。");
        return DifyWorkflowResult.ok("mock-" + UUID.randomUUID(), "mock-task", out, 0.0, 0);
    }

    @Override
    public DifyWorkflowResult runWorkflowByType(String agentType, Map<String, Object> inputs) {
        String wf = props.getWorkflows().getOrDefault(agentType, "(unmapped:" + agentType + ")");
        return runWorkflow(wf, inputs);
    }

    @Override
    public boolean isLive() { return false; }
}
