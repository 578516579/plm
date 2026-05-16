package cn.com.bosssfot.dv.plm.analytics.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.com.bosssfot.dv.plm.analytics.domain.AnalyticsSnapshot;
import cn.com.bosssfot.dv.plm.analytics.service.IAnalyticsSnapshotService;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;

/**
 * 效能分析 Controller — PRD §F6
 */
@RestController
@RequestMapping("/business/analytics")
public class AnalyticsSnapshotController extends BaseController {

    @Autowired
    private IAnalyticsSnapshotService analyticsService;

    @PreAuthorize("@ss.hasPermi('business:analytics:list')")
    @GetMapping("/list")
    public TableDataInfo list(AnalyticsSnapshot snapshot) {
        startPage();
        return getDataTable(analyticsService.selectAnalyticsList(snapshot));
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:export')")
    @Log(title = "效能分析", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AnalyticsSnapshot snapshot) {
        List<AnalyticsSnapshot> list = analyticsService.selectAnalyticsList(snapshot);
        new ExcelUtil<AnalyticsSnapshot>(AnalyticsSnapshot.class).exportExcel(response, list, "效能分析快照");
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(analyticsService.selectAnalyticsById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:add')")
    @Log(title = "效能分析", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AnalyticsSnapshot snapshot) {
        return toAjax(analyticsService.insertAnalytics(snapshot));
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:edit')")
    @Log(title = "效能分析", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AnalyticsSnapshot snapshot) {
        return toAjax(analyticsService.updateAnalytics(snapshot));
    }

    @PreAuthorize("@ss.hasPermi('business:analytics:remove')")
    @Log(title = "效能分析", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(analyticsService.deleteAnalyticsByIds(ids));
    }

    /** PRD §F6 AI 复盘建议 */
    @PreAuthorize("@ss.hasPermi('business:analytics:edit')")
    @Log(title = "效能分析-AI建议", businessType = BusinessType.OTHER)
    @PostMapping("/ai/recommend/{id}")
    public AjaxResult aiRecommend(@PathVariable("id") Long id) {
        return success(analyticsService.aiRecommend(id));
    }
}
