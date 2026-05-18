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

/** 运维手册 Controller — PRD §F5.3 — /business/manual-ops/* */
@RestController
@RequestMapping("/business/manual-ops")
public class ManualOpsController extends BaseController {

    @Autowired
    private IManualOpsService manualOpsService;

    @PreAuthorize("@ss.hasPermi('business:manual-ops:list')")
    @GetMapping("/list")
    public TableDataInfo list(ManualOps manualOps) {
        startPage();
        return getDataTable(manualOpsService.selectManualOpsList(manualOps));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:export')")
    @Log(title = "运维手册", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ManualOps manualOps) {
        List<ManualOps> list = manualOpsService.selectManualOpsList(manualOps);
        new ExcelUtil<ManualOps>(ManualOps.class).exportExcel(response, list, "运维手册");
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(manualOpsService.selectManualOpsById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:add')")
    @Log(title = "运维手册", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ManualOps manualOps) {
        return toAjax(manualOpsService.insertManualOps(manualOps));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:edit')")
    @Log(title = "运维手册", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ManualOps manualOps) {
        return toAjax(manualOpsService.updateManualOps(manualOps));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-ops:remove')")
    @Log(title = "运维手册", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(manualOpsService.deleteManualOpsByIds(ids));
    }

    /** PRD §F5.3 AI 生成运维手册内容 */
    @PreAuthorize("@ss.hasPermi('business:manual-ops:edit')")
    @Log(title = "运维手册-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai-generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(manualOpsService.aiGenerate(id));
    }
}
