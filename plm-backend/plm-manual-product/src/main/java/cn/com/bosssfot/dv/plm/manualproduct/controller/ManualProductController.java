package cn.com.bosssfot.dv.plm.manualproduct.controller;

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
import cn.com.bosssfot.dv.plm.manualproduct.domain.ManualProduct;
import cn.com.bosssfot.dv.plm.manualproduct.service.IManualProductService;

@RestController
@RequestMapping("/business/manual-product")
public class ManualProductController extends BaseController {

    @Autowired
    private IManualProductService manualproductService;

    @PreAuthorize("@ss.hasPermi('business:manual-product:list')")
    @GetMapping("/list")
    public TableDataInfo list(ManualProduct manualproduct) {
        startPage();
        return getDataTable(manualproductService.selectManualProductList(manualproduct));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-product:export')")
    @Log(title = "产品手册", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ManualProduct manualproduct) {
        List<ManualProduct> list = manualproductService.selectManualProductList(manualproduct);
        new ExcelUtil<ManualProduct>(ManualProduct.class).exportExcel(response, list, "产品手册数据");
    }

    @PreAuthorize("@ss.hasPermi('business:manual-product:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(manualproductService.selectManualProductById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-product:add')")
    @Log(title = "产品手册", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ManualProduct manualproduct) {
        return toAjax(manualproductService.insertManualProduct(manualproduct));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-product:edit')")
    @Log(title = "产品手册", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ManualProduct manualproduct) {
        return toAjax(manualproductService.updateManualProduct(manualproduct));
    }

    @PreAuthorize("@ss.hasPermi('business:manual-product:remove')")
    @Log(title = "产品手册", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(manualproductService.deleteManualProductByIds(ids));
    }
}
