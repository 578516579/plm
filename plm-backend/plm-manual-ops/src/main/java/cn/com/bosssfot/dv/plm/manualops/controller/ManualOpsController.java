package cn.com.bosssfot.dv.plm.manualops.controller;

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
import cn.com.bosssfot.dv.plm.manualops.domain.ManualOps;
import cn.com.bosssfot.dv.plm.manualops.service.IManualOpsService;

/**
 * 运维手册 Controller — PRD §F5.3
 */
@RestController
@RequestMapping("/business/manual-ops")
public class ManualOpsController extends BaseController {

    @Autowired
    private IManualOpsService manualopsService;

    @PreAuthorize("@ss.hasPermi('business:manual-ops:list')")
    @GetMapping("/list")
    public TableDataInfo list(ManualOps manualops) {
        startPage();
        return getDataTable(manualopsService.selectManualOpsList(manualops));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:export')")
    @Log(title = "运维手册", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ManualOps manualops) {
        List<ManualOps> list = manualopsService.selectManualOpsList(manualops);
        new ExcelUtil<ManualOps>(ManualOps.class).exportExcel(response, list, "运维手册数据");
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(manualopsService.selectManualOpsById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:add')")
    @Log(title = "运维手册", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ManualOps manualops) {
        return toAjax(manualopsService.insertManualOps(manualops));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:edit')")
    @Log(title = "运维手册", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ManualOps manualops) {
        return toAjax(manualopsService.updateManualOps(manualops));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:remove')")
    @Log(title = "运维手册", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(manualopsService.deleteManualOpsByIds(ids));
    }

    /** PRD §F5.3 AI 一键生成运维手册 */
    @PreAuthorize("@ss.hasPermi('business:manual-ops:edit')")
    @Log(title = "运维手册-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(manualopsService.aiGenerate(id));
    }
}
