package cn.com.bosssfot.dv.plm.aiagent.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.com.bosssfot.dv.plm.aiagent.domain.AiAgent;
import cn.com.bosssfot.dv.plm.aiagent.service.IAiAgentService;
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

    @Autowired private IAiAgentService aiAgentService;
    @Autowired private DifyService difyService;
    @Autowired private DifyProperties difyProperties;

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
}
