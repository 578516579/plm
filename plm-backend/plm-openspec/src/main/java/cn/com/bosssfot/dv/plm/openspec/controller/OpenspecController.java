package cn.com.bosssfot.dv.plm.openspec.controller;

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
import cn.com.bosssfot.dv.plm.openspec.domain.Openspec;
import cn.com.bosssfot.dv.plm.openspec.service.IOpenspecService;

/**
 * AI规范中心 Controller
 * /business/openspec/* (6 标准 + AI 生成入口)
 */
@RestController
@RequestMapping("/business/openspec")
public class OpenspecController extends BaseController {

    @Autowired
    private IOpenspecService openspecService;

    @PreAuthorize("@ss.hasPermi('business:openspec:list')")
    @GetMapping("/list")
    public TableDataInfo list(Openspec openspec) {
        startPage();
        return getDataTable(openspecService.selectOpenspecList(openspec));
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:export')")
    @Log(title = "AI规范中心", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Openspec openspec) {
        List<Openspec> list = openspecService.selectOpenspecList(openspec);
        new ExcelUtil<Openspec>(Openspec.class).exportExcel(response, list, "AI规范数据");
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(openspecService.selectOpenspecById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:add')")
    @Log(title = "AI规范中心", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Openspec openspec) {
        return toAjax(openspecService.insertOpenspec(openspec));
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:edit')")
    @Log(title = "AI规范中心", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Openspec openspec) {
        return toAjax(openspecService.updateOpenspec(openspec));
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:remove')")
    @Log(title = "AI规范中心", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(openspecService.deleteOpenspecByIds(ids));
    }

    /** AI生成 OpenAPI 3.1 YAML + x-agrikb-ref，状态流转为「审核中」 */
    @PreAuthorize("@ss.hasPermi('business:openspec:edit')")
    @Log(title = "AI规范中心-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(openspecService.aiGenerate(id));
    }
}
