package cn.com.bosssfot.dv.plm.manualimpl.controller;

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
import cn.com.bosssfot.dv.plm.manualimpl.domain.ManualImpl;
import cn.com.bosssfot.dv.plm.manualimpl.service.IManualImplService;

/**
 * 实施手册 Controller — PRD §F5.2
 */
@RestController
@RequestMapping("/business/manual-impl")
public class ManualImplController extends BaseController {

    @Autowired
    private IManualImplService manualimplService;

    @PreAuthorize("@ss.hasPermi('business:manual-impl:list')")
    @GetMapping("/list")
    public TableDataInfo list(ManualImpl manualimpl) {
        startPage();
        return getDataTable(manualimplService.selectManualImplList(manualimpl));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:export')")
    @Log(title = "实施手册", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ManualImpl manualimpl) {
        List<ManualImpl> list = manualimplService.selectManualImplList(manualimpl);
        new ExcelUtil<ManualImpl>(ManualImpl.class).exportExcel(response, list, "实施手册数据");
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(manualimplService.selectManualImplById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:add')")
    @Log(title = "实施手册", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ManualImpl manualimpl) {
        return toAjax(manualimplService.insertManualImpl(manualimpl));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:edit')")
    @Log(title = "实施手册", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ManualImpl manualimpl) {
        return toAjax(manualimplService.updateManualImpl(manualimpl));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:remove')")
    @Log(title = "实施手册", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(manualimplService.deleteManualImplByIds(ids));
    }

    /** PRD §F5.2 AI 一键生成实施手册 */
    @PreAuthorize("@ss.hasPermi('business:manual-impl:edit')")
    @Log(title = "实施手册-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(manualimplService.aiGenerate(id));
    }
}
