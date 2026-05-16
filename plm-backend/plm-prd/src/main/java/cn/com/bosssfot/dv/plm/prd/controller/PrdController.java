package cn.com.bosssfot.dv.plm.prd.controller;

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
import cn.com.bosssfot.dv.plm.prd.domain.Prd;
import cn.com.bosssfot.dv.plm.prd.service.IPrdService;

/**
 * AI PRD 生成器 Controller — PRD §F2.2
 * 业务路径 /business/prd/* (RuoYi 6 标准端点 + AI 生成入口)
 */
@RestController
@RequestMapping("/business/prd")
public class PrdController extends BaseController {

    @Autowired
    private IPrdService prdService;

    @PreAuthorize("@ss.hasPermi('business:prd:list')")
    @GetMapping("/list")
    public TableDataInfo list(Prd prd) {
        startPage();
        return getDataTable(prdService.selectPrdList(prd));
    }

    @PreAuthorize("@ss.hasPermi('business:prd:export')")
    @Log(title = "PRD 文档", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Prd prd) {
        List<Prd> list = prdService.selectPrdList(prd);
        new ExcelUtil<Prd>(Prd.class).exportExcel(response, list, "PRD 文档数据");
    }

    @PreAuthorize("@ss.hasPermi('business:prd:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(prdService.selectPrdById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:prd:add')")
    @Log(title = "PRD 文档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Prd prd) {
        return toAjax(prdService.insertPrd(prd));
    }

    @PreAuthorize("@ss.hasPermi('business:prd:edit')")
    @Log(title = "PRD 文档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Prd prd) {
        return toAjax(prdService.updatePrd(prd));
    }

    @PreAuthorize("@ss.hasPermi('business:prd:remove')")
    @Log(title = "PRD 文档", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(prdService.deletePrdByIds(ids));
    }

    /** PRD §F2.2 AI 生成完整 PRD — prd-generation-flow */
    @PreAuthorize("@ss.hasPermi('business:prd:edit')")
    @Log(title = "PRD-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(prdService.aiGenerate(id));
    }
}
