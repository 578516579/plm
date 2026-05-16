package cn.com.bosssfot.dv.plm.defect.controller;

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
import cn.com.bosssfot.dv.plm.defect.domain.Defect;
import cn.com.bosssfot.dv.plm.defect.service.IDefectService;

/**
 * 缺陷 控制器
 * Base path: /business/defect
 */
@RestController
@RequestMapping("/business/defect")
public class DefectController extends BaseController
{
    @Autowired
    private IDefectService defectService;

    @PreAuthorize("@ss.hasPermi('business:defect:list')")
    @GetMapping("/list")
    public TableDataInfo list(Defect defect) {
        startPage();
        return getDataTable(defectService.selectDefectList(defect));
    }

    @PreAuthorize("@ss.hasPermi('business:defect:export')")
    @Log(title = "缺陷", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Defect defect) {
        List<Defect> list = defectService.selectDefectList(defect);
        ExcelUtil<Defect> util = new ExcelUtil<Defect>(Defect.class);
        util.exportExcel(response, list, "缺陷数据");
    }

    @PreAuthorize("@ss.hasPermi('business:defect:query')")
    @GetMapping("/{defectId}")
    public AjaxResult getInfo(@PathVariable("defectId") Long defectId) {
        return success(defectService.selectDefectById(defectId));
    }

    @PreAuthorize("@ss.hasPermi('business:defect:add')")
    @Log(title = "缺陷", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Defect defect) {
        return toAjax(defectService.insertDefect(defect));
    }

    @PreAuthorize("@ss.hasPermi('business:defect:edit')")
    @Log(title = "缺陷", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Defect defect) {
        return toAjax(defectService.updateDefect(defect));
    }

    @PreAuthorize("@ss.hasPermi('business:defect:remove')")
    @Log(title = "缺陷", businessType = BusinessType.DELETE)
    @DeleteMapping("/{defectIds}")
    public AjaxResult remove(@PathVariable Long[] defectIds) {
        return toAjax(defectService.deleteDefectByIds(defectIds));
    }
}
