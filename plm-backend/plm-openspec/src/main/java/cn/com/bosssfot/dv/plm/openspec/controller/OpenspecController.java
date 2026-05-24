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

/** OpenSpec Controller — PRD §F3.5 */
@RestController
@RequestMapping("/business/openspec")
public class OpenspecController extends BaseController {

    @Autowired private IOpenspecService openspecService;

    @PreAuthorize("@ss.hasPermi('business:openspec:list')")
    @GetMapping("/list")
    public TableDataInfo list(Openspec openspec) {
        startPage();
        return getDataTable(openspecService.selectOpenspecList(openspec));
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:export')")
    @Log(title = "OpenSpec", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Openspec openspec) {
        List<Openspec> list = openspecService.selectOpenspecList(openspec);
        new ExcelUtil<Openspec>(Openspec.class).exportExcel(response, list, "OpenSpec 数据");
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(openspecService.selectOpenspecById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:add')")
    @Log(title = "OpenSpec", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Openspec openspec) {
        return toAjax(openspecService.insertOpenspec(openspec));
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:edit')")
    @Log(title = "OpenSpec", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Openspec openspec) {
        return toAjax(openspecService.updateOpenspec(openspec));
    }

    @PreAuthorize("@ss.hasPermi('business:openspec:remove')")
    @Log(title = "OpenSpec", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(openspecService.deleteOpenspecByIds(ids));
    }

    /** PRD §F3.5 AI 生成规范骨架 (按 specType) */
    @PreAuthorize("@ss.hasPermi('business:openspec:edit')")
    @Log(title = "OpenSpec-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(openspecService.aiGenerate(id));
    }
}
