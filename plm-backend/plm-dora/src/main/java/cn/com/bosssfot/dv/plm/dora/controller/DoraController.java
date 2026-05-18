package cn.com.bosssfot.dv.plm.dora.controller;

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
import cn.com.bosssfot.dv.plm.dora.domain.Dora;
import cn.com.bosssfot.dv.plm.dora.service.IDoraService;

/**
 * DORA效能指标 控制器
 * Base path: /business/dora
 */
@RestController
@RequestMapping("/business/dora")
public class DoraController extends BaseController
{
    @Autowired
    private IDoraService doraService;

    @PreAuthorize("@ss.hasPermi('business:dora:list')")
    @GetMapping("/list")
    public TableDataInfo list(Dora dora) {
        startPage();
        return getDataTable(doraService.selectDoraList(dora));
    }

    @PreAuthorize("@ss.hasPermi('business:dora:export')")
    @Log(title = "DORA效能指标", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Dora dora) {
        List<Dora> list = doraService.selectDoraList(dora);
        ExcelUtil<Dora> util = new ExcelUtil<Dora>(Dora.class);
        util.exportExcel(response, list, "DORA效能指标数据");
    }

    @PreAuthorize("@ss.hasPermi('business:dora:query')")
    @GetMapping("/{doraId}")
    public AjaxResult getInfo(@PathVariable("doraId") Long doraId) {
        return success(doraService.selectDoraById(doraId));
    }

    @PreAuthorize("@ss.hasPermi('business:dora:add')")
    @Log(title = "DORA效能指标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Dora dora) {
        return toAjax(doraService.insertDora(dora));
    }

    @PreAuthorize("@ss.hasPermi('business:dora:edit')")
    @Log(title = "DORA效能指标", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Dora dora) {
        return toAjax(doraService.updateDora(dora));
    }

    @PreAuthorize("@ss.hasPermi('business:dora:remove')")
    @Log(title = "DORA效能指标", businessType = BusinessType.DELETE)
    @DeleteMapping("/{doraIds}")
    public AjaxResult remove(@PathVariable Long[] doraIds) {
        return toAjax(doraService.deleteDoraByIds(doraIds));
    }

    /**
     * AI生成DORA分析
     * POST /business/dora/{doraId}/ai-generate
     */
    @PreAuthorize("@ss.hasPermi('business:dora:edit')")
    @Log(title = "DORA效能指标-AI生成", businessType = BusinessType.UPDATE)
    @PostMapping("/{doraId}/ai-generate")
    public AjaxResult aiGenerate(@PathVariable("doraId") Long doraId) {
        return toAjax(doraService.aiGenerate(doraId));
    }
}
