package cn.com.bosssfot.dv.plm.common.ai.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.ai.AiProvider;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.dify.DifyService;
import cn.com.bosssfot.dv.plm.common.dify.dto.DifyWorkflowResult;

/**
 * Dify Provider — 委托现有 {@link DifyService} 走 workflow 编排模式。
 *
 * <p>路由策略:</p>
 * <ul>
 *   <li>{@code request.model} 视作 Dify workflow_id (业务侧 tb_ai_agent.dify_workflow_id 写过来)</li>
 *   <li>{@code request.difyInputs} 直接作为 Dify workflow 的 inputs</li>
 *   <li>{@code request.system / messages} 也合并进 inputs(key 为 "system" / "query")</li>
 *   <li>输出取 {@code outputs.text} 或 {@code outputs.output};找不到则把整个 outputs JSON 当 text</li>
 * </ul>
 *
 * <p>注意:Dify Provider 适合 <b>workflow 编排</b>(多节点、有自定义 inputs/outputs 的复杂流程);
 * 如果业务只要单轮 chat,推荐直接用 openai / anthropic provider。</p>
 *
 * @author plm
 */
public class DifyAiProvider implements AiProvider {
    private static final Logger log = LoggerFactory.getLogger(DifyAiProvider.class);

    private final DifyService difyService;

    public DifyAiProvider(DifyService difyService) { this.difyService = difyService; }

    @Override
    public String name() { return "dify"; }

    @Override
    public boolean isAvailable() { return difyService != null && difyService.isLive(); }

    @Override
    public AiChatResult chat(AiChatRequest req) {
        long start = System.currentTimeMillis();
        String workflowId = req.getModel();  // 业务方把 dify_workflow_id 放在 model 字段
        if (workflowId == null || workflowId.isBlank()) {
            return AiChatResult.fail("dify", "dify provider 需要在 model 字段传入 workflow_id");
        }
        // 合并 inputs
        Map<String, Object> inputs = new LinkedHashMap<>(req.getDifyInputs());
        if (req.getSystem() != null && !req.getSystem().isBlank()) inputs.putIfAbsent("system", req.getSystem());
        String userMsg = req.firstUserContent();
        if (!userMsg.isEmpty()) inputs.putIfAbsent("query", userMsg);

        DifyWorkflowResult wr = difyService.runWorkflow(workflowId, inputs);
        long elapsed = System.currentTimeMillis() - start;

        if (!wr.isSuccess()) {
            AiChatResult r = AiChatResult.fail("dify", wr.getErrorMessage());
            r.setElapsedMs(elapsed);
            return r;
        }
        String text = extractText(wr.getOutputs());
        AiChatResult r = AiChatResult.ok("dify", workflowId, text);
        r.setFinishReason("stop");
        r.setRequestId(wr.getWorkflowRunId());
        r.setTotalTokens(wr.getTotalTokens());
        r.setElapsedMs(elapsed);
        log.info("[Ai-dify] workflow {} ok,runId={},tokens={},elapsed={}ms,caller={}",
                workflowId, wr.getWorkflowRunId(), wr.getTotalTokens(), elapsed, req.getCallerTag());
        return r;
    }

    /** 输出归一化:依次取 text / output / 整个 outputs JSON-ish */
    private static String extractText(Map<String, Object> outputs) {
        if (outputs == null || outputs.isEmpty()) return "";
        Object o = outputs.get("text");
        if (o != null) return String.valueOf(o);
        o = outputs.get("output");
        if (o != null) return String.valueOf(o);
        // 兜底:把整个 map 序列化为 key=value 形式
        StringBuilder sb = new StringBuilder();
        outputs.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
        return sb.toString();
    }
}
