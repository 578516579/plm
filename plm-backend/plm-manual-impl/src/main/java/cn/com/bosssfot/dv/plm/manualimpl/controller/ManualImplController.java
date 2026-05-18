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

/** 实施手册 Controller — PRD §F5.2 — /business/manual-impl/* */
@RestController
@RequestMapping("/business/manual-impl")
public class ManualImplController extends BaseController {

    @Autowired
    private IManualImplService manualImplService;

    @PreAuthorize("@ss.hasPermi('business:manual-impl:list')")
    @GetMapping("/list")
    public TableDataInfo list(ManualImpl manualImpl) {
        startPage();
        return getDataTable(manualImplService.selectManualImplList(manualImpl));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:export')")
    @Log(title = "实施手册", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ManualImpl manualImpl) {
        List<ManualImpl> list = manualImplService.selectManualImplList(manualImpl);
        new ExcelUtil<ManualImpl>(ManualImpl.class).exportExcel(response, list, "实施手册");
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(manualImplService.selectManualImplById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:add')")
    @Log(title = "实施手册", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ManualImpl manualImpl) {
        return toAjax(manualImplService.insertManualImpl(manualImpl));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:edit')")
    @Log(title = "实施手册", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ManualImpl manualImpl) {
        return toAjax(manualImplService.updateManualImpl(manualImpl));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-impl:remove')")
    @Log(title = "实施手册", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(manualImplService.deleteManualImplByIds(ids));
    }

    /** PRD §F5.2 AI 生成实施手册内容 */
    @PreAuthorize("@ss.hasPermi('business:manual-impl:edit')")
    @Log(title = "实施手册-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai-generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(manualImplService.aiGenerate(id));
    }
}
