package cn.com.bosssfot.dv.plm.aiagent.invocationlog.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.com.bosssfot.dv.plm.aiagent.invocationlog.domain.AiInvocationLog;
import cn.com.bosssfot.dv.plm.aiagent.invocationlog.service.IAiInvocationLogService;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;

/**
 * AI 调用审计日志 Controller — 提供查询/汇总,不暴露写接口(由 AiServiceImpl 自动写)。
 *
 * @author plm
 */
@RestController
@RequestMapping("/business/ai-invocation-log")
public class AiInvocationLogController extends BaseController {

    @Autowired private IAiInvocationLogService logService;

    @PreAuthorize("@ss.hasPermi('business:ai-agent:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiInvocationLog q) {
        startPage();
        return getDataTable(logService.selectAiInvocationLogList(q));
    }

    @PreAuthorize("@ss.hasPermi('business:ai-agent:list')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(logService.selectAiInvocationLogById(id));
    }

    /** Provider 维度汇总 — 仪表盘可用 */
    @PreAuthorize("@ss.hasPermi('business:ai-agent:list')")
    @GetMapping("/summary")
    public AjaxResult summary() {
        List<Map<String, Object>> rows = logService.getProviderSummary();
        return AjaxResult.success(rows);
    }

    @PreAuthorize("@ss.hasPermi('business:ai-agent:remove')")
    @Log(title = "AI 审计-清除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(logService.deleteAiInvocationLogByIds(ids));
    }
}
