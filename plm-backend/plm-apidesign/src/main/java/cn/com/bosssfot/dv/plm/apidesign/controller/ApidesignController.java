package cn.com.bosssfot.dv.plm.apidesign.controller;

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
import cn.com.bosssfot.dv.plm.apidesign.domain.Apidesign;
import cn.com.bosssfot.dv.plm.apidesign.service.IApidesignService;

/**
 * 接口详细设计 Controller — PRD §F3.3
 */
@RestController
@RequestMapping("/business/apidesign")
public class ApidesignController extends BaseController {

    @Autowired
    private IApidesignService apidesignService;

    @PreAuthorize("@ss.hasPermi('business:apidesign:list')")
    @GetMapping("/list")
    public TableDataInfo list(Apidesign apidesign) {
        startPage();
        return getDataTable(apidesignService.selectApidesignList(apidesign));
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:export')")
    @Log(title = "接口设计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Apidesign apidesign) {
        List<Apidesign> list = apidesignService.selectApidesignList(apidesign);
        new ExcelUtil<Apidesign>(Apidesign.class).exportExcel(response, list, "接口设计数据");
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(apidesignService.selectApidesignById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:add')")
    @Log(title = "接口设计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Apidesign apidesign) {
        return toAjax(apidesignService.insertApidesign(apidesign));
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:edit')")
    @Log(title = "接口设计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Apidesign apidesign) {
        return toAjax(apidesignService.updateApidesign(apidesign));
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:remove')")
    @Log(title = "接口设计", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(apidesignService.deleteApidesignByIds(ids));
    }

    /** PRD §F3.3 AI OpenAPI 规范生成 */
    @PreAuthorize("@ss.hasPermi('business:apidesign:edit')")
    @Log(title = "接口设计-AI-OpenAPI", businessType = BusinessType.OTHER)
    @PostMapping("/ai/openapi/{id}")
    public AjaxResult aiOpenapi(@PathVariable("id") Long id) {
        return success(apidesignService.aiOpenapi(id));
    }
}
