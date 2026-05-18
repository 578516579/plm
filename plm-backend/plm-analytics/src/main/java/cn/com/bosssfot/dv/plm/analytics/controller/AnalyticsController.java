package cn.com.bosssfot.dv.plm.analytics.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.com.bosssfot.dv.plm.analytics.domain.Analytics;
import cn.com.bosssfot.dv.plm.analytics.service.IAnalyticsService;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;

/**
 * 效能分析 控制器
 * Base path: /business/analytics
 * PRD §4.6
 */
@RestController
@RequestMapping("/business/analytics")
public class AnalyticsController extends BaseController
{
    @Autowired
    private IAnalyticsService analyticsService;

    @PreAuthorize("@ss.hasPermi('business:analytics:list')")
    @GetMapping("/list")
    public TableDataInfo list(Analytics analytics) {
        startPage();
        return getDataTable(analyticsService.selectAnalyticsList(analytics));
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:export')")
    @Log(title = "效能分析", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Analytics analytics) {
        List<Analytics> list = analyticsService.selectAnalyticsList(analytics);
        ExcelUtil<Analytics> util = new ExcelUtil<Analytics>(Analytics.class);
        util.exportExcel(response, list, "效能分析数据");
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:query')")
    @GetMapping("/{analyticsId}")
    public AjaxResult getInfo(@PathVariable("analyticsId") Long analyticsId) {
        return success(analyticsService.selectAnalyticsById(analyticsId));
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:add')")
    @Log(title = "效能分析", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Analytics analytics) {
        return toAjax(analyticsService.insertAnalytics(analytics));
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:edit')")
    @Log(title = "效能分析", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Analytics analytics) {
        return toAjax(analyticsService.updateAnalytics(analytics));
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:remove')")
    @Log(title = "效能分析", businessType = BusinessType.DELETE)
    @DeleteMapping("/{analyticsIds}")
    public AjaxResult remove(@PathVariable Long[] analyticsIds) {
        return toAjax(analyticsService.deleteAnalyticsByIds(analyticsIds));
    }

    /**
     * AI生成效能分析报告
     * POST /business/analytics/{analyticsId}/ai-generate
     */
    @PreAuthorize("@ss.hasPermi('business:analytics:edit')")
    @Log(title = "效能分析-AI生成", businessType = BusinessType.UPDATE)
    @PostMapping("/{analyticsId}/ai-generate")
    public AjaxResult aiGenerate(@PathVariable("analyticsId") Long analyticsId) {
        return toAjax(analyticsService.aiGenerate(analyticsId));
    }
}
