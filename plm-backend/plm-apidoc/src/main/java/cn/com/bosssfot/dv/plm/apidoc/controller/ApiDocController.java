package cn.com.bosssfot.dv.plm.apidoc.controller;

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
import cn.com.bosssfot.dv.plm.apidoc.domain.ApiDoc;
import cn.com.bosssfot.dv.plm.apidoc.service.IApiDocService;

@RestController
@RequestMapping("/business/apidoc")
public class ApiDocController extends BaseController {

    @Autowired
    private IApiDocService apidocService;

    @PreAuthorize("@ss.hasPermi('business:apidoc:list')")
    @GetMapping("/list")
    public TableDataInfo list(ApiDoc apidoc) {
        startPage();
        return getDataTable(apidocService.selectApiDocList(apidoc));
    }

    @PreAuthorize("@ss.hasPermi('business:apidoc:export')")
    @Log(title = "API 文档", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ApiDoc apidoc) {
        List<ApiDoc> list = apidocService.selectApiDocList(apidoc);
        new ExcelUtil<ApiDoc>(ApiDoc.class).exportExcel(response, list, "API 文档数据");
    }

    @PreAuthorize("@ss.hasPermi('business:apidoc:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(apidocService.selectApiDocById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:apidoc:add')")
    @Log(title = "API 文档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ApiDoc apidoc) {
        return toAjax(apidocService.insertApiDoc(apidoc));
    }

    @PreAuthorize("@ss.hasPermi('business:apidoc:edit')")
    @Log(title = "API 文档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ApiDoc apidoc) {
        return toAjax(apidocService.updateApiDoc(apidoc));
    }

    @PreAuthorize("@ss.hasPermi('business:apidoc:remove')")
    @Log(title = "API 文档", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(apidocService.deleteApiDocByIds(ids));
    }
}
