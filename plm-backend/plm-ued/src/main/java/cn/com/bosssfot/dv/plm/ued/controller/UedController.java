package cn.com.bosssfot.dv.plm.ued.controller;

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
import cn.com.bosssfot.dv.plm.ued.domain.Ued;
import cn.com.bosssfot.dv.plm.ued.service.IUedService;

/**
 * UED 设计协同 Controller — PRD §F2.3
 * 业务路径 /business/ued/* (RuoYi 6 标准端点 + AI 规范检查入口)
 */
@RestController
@RequestMapping("/business/ued")
public class UedController extends BaseController {

    @Autowired
    private IUedService uedService;

    @PreAuthorize("@ss.hasPermi('business:ued:list')")
    @GetMapping("/list")
    public TableDataInfo list(Ued ued) {
        startPage();
        return getDataTable(uedService.selectUedList(ued));
    }

    @PreAuthorize("@ss.hasPermi('business:ued:export')")
    @Log(title = "UED 设计稿", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Ued ued) {
        List<Ued> list = uedService.selectUedList(ued);
        new ExcelUtil<Ued>(Ued.class).exportExcel(response, list, "UED 设计稿数据");
    }

    @PreAuthorize("@ss.hasPermi('business:ued:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(uedService.selectUedById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:ued:add')")
    @Log(title = "UED 设计稿", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Ued ued) {
        return toAjax(uedService.insertUed(ued));
    }

    @PreAuthorize("@ss.hasPermi('business:ued:edit')")
    @Log(title = "UED 设计稿", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Ued ued) {
        return toAjax(uedService.updateUed(ued));
    }

    @PreAuthorize("@ss.hasPermi('business:ued:remove')")
    @Log(title = "UED 设计稿", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(uedService.deleteUedByIds(ids));
    }

    /** PRD §F2.3 AI 设计规范检查 — ued-review-flow */
    @PreAuthorize("@ss.hasPermi('business:ued:edit')")
    @Log(title = "UED-AI规范检查", businessType = BusinessType.OTHER)
    @PostMapping("/ai/review/{id}")
    public AjaxResult aiReview(@PathVariable("id") Long id) {
        return success(uedService.aiReview(id));
    }
}
