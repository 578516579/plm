package cn.com.bosssfot.dv.plm.competitive.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;
import cn.com.bosssfot.dv.plm.competitive.domain.Competitive;
import cn.com.bosssfot.dv.plm.competitive.service.ICompetitiveService;

/**
 * 竞品情报 Controller — PRD §F1.3
 * /business/competitive/* (6 标准 + AI 分析入口)
 */
@RestController
@RequestMapping("/business/competitive")
public class CompetitiveController extends BaseController {

    @Autowired
    private ICompetitiveService competitiveService;

    @PreAuthorize("@ss.hasPermi('business:competitive:list')")
    @GetMapping("/list")
    public TableDataInfo list(Competitive competitive) {
        startPage();
        return getDataTable(competitiveService.selectCompetitiveList(competitive));
    }

    @PreAuthorize("@ss.hasPermi('business:competitive:export')")
    @Log(title = "竞品情报", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Competitive competitive) {
        List<Competitive> list = competitiveService.selectCompetitiveList(competitive);
        new ExcelUtil<Competitive>(Competitive.class).exportExcel(response, list, "竞品情报数据");
    }

    @PreAuthorize("@ss.hasPermi('business:competitive:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(competitiveService.selectCompetitiveById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:competitive:add')")
    @Log(title = "竞品情报", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Competitive competitive) {
        return toAjax(competitiveService.insertCompetitive(competitive));
    }

    @PreAuthorize("@ss.hasPermi('business:competitive:edit')")
    @Log(title = "竞品情报", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Competitive competitive) {
        return toAjax(competitiveService.updateCompetitive(competitive));
    }

    @PreAuthorize("@ss.hasPermi('business:competitive:remove')")
    @Log(title = "竞品情报", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(competitiveService.deleteCompetitiveByIds(ids));
    }

    /** PRD §F1.3 AI 竞品分析 — competitive-analysis-flow */
    @PreAuthorize("@ss.hasPermi('business:competitive:edit')")
    @Log(title = "竞品情报-AI分析", businessType = BusinessType.OTHER)
    @PostMapping("/ai/analyze/{id}")
    public AjaxResult aiAnalyze(@PathVariable("id") Long id) {
        return success(competitiveService.aiAnalyze(id));
    }
}
