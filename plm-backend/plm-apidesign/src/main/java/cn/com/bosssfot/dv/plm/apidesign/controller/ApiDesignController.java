package cn.com.bosssfot.dv.plm.apidesign.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import cn.com.bosssfot.dv.plm.apidesign.domain.ApiDesign;
import cn.com.bosssfot.dv.plm.apidesign.service.IApiDesignService;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;

/**
 * LLD 接口详细设计 Controller — PRD §F3.3
 * /business/apidesign/* (6 标准 + AI 生成入口)
 */
@RestController
@RequestMapping("/business/apidesign")
public class ApiDesignController extends BaseController {

    @Autowired
    private IApiDesignService apidesignService;

    @PreAuthorize("@ss.hasPermi('business:apidesign:list')")
    @GetMapping("/list")
    public TableDataInfo list(ApiDesign apidesign) {
        startPage();
        return getDataTable(apidesignService.selectApiDesignList(apidesign));
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:export')")
    @Log(title = "接口设计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ApiDesign apidesign) {
        List<ApiDesign> list = apidesignService.selectApiDesignList(apidesign);
        new ExcelUtil<ApiDesign>(ApiDesign.class).exportExcel(response, list, "接口设计数据");
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(apidesignService.selectApiDesignById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:add')")
    @Log(title = "接口设计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ApiDesign apidesign) {
        return toAjax(apidesignService.insertApiDesign(apidesign));
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:edit')")
    @Log(title = "接口设计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ApiDesign apidesign) {
        return toAjax(apidesignService.updateApiDesign(apidesign));
    }

    @PreAuthorize("@ss.hasPermi('business:apidesign:remove')")
    @Log(title = "接口设计", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(apidesignService.deleteApiDesignByIds(ids));
    }

    /** PRD §F3.3 AI 生成 OpenAPI YAML — detail-design-flow */
    @PreAuthorize("@ss.hasPermi('business:apidesign:edit')")
    @Log(title = "接口设计-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(apidesignService.aiGenerate(id));
    }
}
