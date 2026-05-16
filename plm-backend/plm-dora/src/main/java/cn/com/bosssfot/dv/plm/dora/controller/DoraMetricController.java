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
import cn.com.bosssfot.dv.plm.dora.domain.DoraMetric;
import cn.com.bosssfot.dv.plm.dora.service.IDoraMetricService;

/** DORA Controller — DevOps 扩展 */
@RestController
@RequestMapping("/business/dora")
public class DoraMetricController extends BaseController {

    @Autowired private IDoraMetricService doraService;

    @PreAuthorize("@ss.hasPermi('business:dora:list')")
    @GetMapping("/list")
    public TableDataInfo list(DoraMetric dora) {
        startPage();
        return getDataTable(doraService.selectDoraList(dora));
    }

    @PreAuthorize("@ss.hasPermi('business:dora:export')")
    @Log(title = "DORA 指标", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DoraMetric dora) {
        List<DoraMetric> list = doraService.selectDoraList(dora);
        new ExcelUtil<DoraMetric>(DoraMetric.class).exportExcel(response, list, "DORA 指标数据");
    }

    @PreAuthorize("@ss.hasPermi('business:dora:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(doraService.selectDoraById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:dora:add')")
    @Log(title = "DORA 指标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DoraMetric dora) {
        return toAjax(doraService.insertDora(dora));
    }

    @PreAuthorize("@ss.hasPermi('business:dora:edit')")
    @Log(title = "DORA 指标", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DoraMetric dora) {
        return toAjax(doraService.updateDora(dora));
    }

    @PreAuthorize("@ss.hasPermi('business:dora:remove')")
    @Log(title = "DORA 指标", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(doraService.deleteDoraByIds(ids));
    }

    /** AI 持续改进建议 */
    @PreAuthorize("@ss.hasPermi('business:dora:edit')")
    @Log(title = "DORA-AI建议", businessType = BusinessType.OTHER)
    @PostMapping("/ai/suggest/{id}")
    public AjaxResult aiSuggest(@PathVariable("id") Long id) {
        return success(doraService.aiSuggest(id));
    }
}
