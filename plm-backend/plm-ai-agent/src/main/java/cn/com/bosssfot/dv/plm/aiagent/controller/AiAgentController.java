package cn.com.bosssfot.dv.plm.aiagent.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;
import cn.com.bosssfot.dv.plm.aiagent.service.IAiAgentService;
import cn.com.bosssfot.dv.plm.common.ai.AiProperties;
import cn.com.bosssfot.dv.plm.common.ai.AiService;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatChunk;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.dify.DifyProperties;
import cn.com.bosssfot.dv.plm.common.dify.DifyService;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;

/** AI Agent Controller — PRD §F3.5 */
@RestController
@RequestMapping("/business/ai-agent")
public class AiAgentController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(AiAgentController.class);

    @Autowired private IAiAgentService aiAgentService;
    @Autowired private AiService aiService;
    @Autowired private AiProperties aiProperties;
    @Autowired private DifyService difyService;
    @Autowired private DifyProperties difyProperties;
    @Autowired @Qualifier("threadPoolTaskExecutor") private TaskExecutor taskExecutor;

    @PreAuthorize("@ss.hasPermi('business:ai-agent:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiAgent aiAgent) {
        startPage();
        return getDataTable(aiAgentService.selectAiAgentList(aiAgent));
    }

    @PreAuthorize("@ss.hasPermi('business:ai-agent:export')")
    @Log(title = "AI Agent", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AiAgent aiAgent) {
        List<AiAgent> list = aiAgentService.selectAiAgentList(aiAgent);
        new ExcelUtil<AiAgent>(AiAgent.class).exportExcel(response, list, "AI Agent 数据");
    }

    @PreAuthorize("@ss.hasPermi('business:ai-agent:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(aiAgentService.selectAiAgentById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:ai-agent:add')")
    @Log(title = "AI Agent", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AiAgent aiAgent) {
        return toAjax(aiAgentService.insertAiAgent(aiAgent));
    }

    @PreAuthorize("@ss.hasPermi('business:ai-agent:edit')")
    @Log(title = "AI Agent", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AiAgent aiAgent) {
        return toAjax(aiAgentService.updateAiAgent(aiAgent));
    }

    @PreAuthorize("@ss.hasPermi('business:ai-agent:remove')")
    @Log(title = "AI Agent", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(aiAgentService.deleteAiAgentByIds(ids));
    }

    /** PRD §F3.5 调用 Agent — 真调 Dify workflow (enabled=false 时降级 mock) */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:edit')")
    @Log(title = "AI Agent-调用", businessType = BusinessType.OTHER)
    @PostMapping("/invoke/{id}")
    public AjaxResult invoke(@PathVariable("id") Long id) {
        return success(aiAgentService.invoke(id));
    }

    /**
     * 流式调用 Agent (V4 Phase 3) — SSE 推送 token chunk
     *
     * <p>前端用 EventSource 订阅,实时显示 deltaText 累积:</p>
     * <pre>
     * const es = new EventSource('/dev-api/business/ai-agent/invoke-stream/42')
     * es.addEventListener('delta', e => append(JSON.parse(e.data).deltaText))
     * es.addEventListener('done',  e => { es.close(); showFinal(JSON.parse(e.data)) })
     * </pre>
     *
     * <p>实现细节:</p>
     * <ul>
     *   <li>用 Spring MVC 原生 {@link SseEmitter}(不引入 WebFlux)</li>
     *   <li>异步推送走 plm-framework 的 {@code threadPoolTaskExecutor} Bean</li>
     *   <li>失败时 emitter.completeWithError 让前端 onerror 收到</li>
     *   <li>超时默认 60s(够大多数 LLM 调用)</li>
     *   <li>不更新 totalCalls/successRate(避免与同步 invoke 双写,审计仍走 V3 recorder)</li>
     * </ul>
     */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:edit')")
    @GetMapping(value = "/invoke-stream/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter invokeStream(@PathVariable("id") Long id) {
        SseEmitter emitter = new SseEmitter(60_000L);

        // 构造请求(在主线程做,异常立刻 completeWithError)
        final AiChatRequest req;
        try {
            req = aiAgentService.buildChatRequest(id);
        } catch (Exception e) {
            emitter.completeWithError(e);
            return emitter;
        }

        taskExecutor.execute(() -> {
            try {
                Iterator<AiChatChunk> it = aiService.chatStream(req);
                while (it.hasNext()) {
                    AiChatChunk chunk = it.next();
                    String eventName = chunk.isDone()
                            ? (chunk.getError() != null ? "error" : "done")
                            : "delta";
                    emitter.send(SseEmitter.event().name(eventName).data(chunk));
                    // 模拟流式延迟 — Mock 即时分块,加 50ms 让前端能看到逐字渲染
                    if (!chunk.isDone()) {
                        try { Thread.sleep(50); } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt(); break;
                        }
                    }
                }
                emitter.complete();
                log.info("[invoke-stream#{}] 完成", id);
            } catch (IOException ioe) {
                // 客户端中断
                log.info("[invoke-stream#{}] 客户端断开: {}", id, ioe.getMessage());
                emitter.completeWithError(ioe);
            } catch (Exception e) {
                log.warn("[invoke-stream#{}] 失败: {}", id, e.toString());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /** Dify 集成健康状态 — 运维/前端可读,不暴露 api-key */
    @GetMapping("/dify/health")
    public AjaxResult difyHealth() {
        AjaxResult r = AjaxResult.success();
        r.put("enabled",  difyProperties.isEnabled());
        r.put("usable",   difyProperties.isUsable());
        r.put("live",     difyService.isLive());
        r.put("baseUrl",  difyProperties.getBaseUrl());
        r.put("workflowsMapped", difyProperties.getWorkflows().size());
        r.put("mode",     difyService.isLive() ? "http" : "mock");
        return r;
    }

    /** AI 集成总览 — 列出所有 provider 及可用状态(不暴露 api-key) */
    @GetMapping("/ai/health")
    public AjaxResult aiHealth() {
        AjaxResult r = AjaxResult.success();
        r.put("defaultProvider", aiService.defaultProvider());
        r.put("providers",       aiService.providerStatus());   // {"mock":true, "openai":false, "anthropic":false, "dify":false}
        // 配置概览 (不含 key)
        r.put("openaiEnabled",    aiProperties.getOpenai().isEnabled());
        r.put("openaiBaseUrl",    aiProperties.getOpenai().getBaseUrl());
        r.put("openaiModel",      aiProperties.getOpenai().getDefaultModel());
        r.put("anthropicEnabled", aiProperties.getAnthropic().isEnabled());
        r.put("anthropicBaseUrl", aiProperties.getAnthropic().getBaseUrl());
        r.put("anthropicModel",   aiProperties.getAnthropic().getDefaultModel());
        r.put("difyUsable",       difyService.isLive());
        return r;
    }
}
