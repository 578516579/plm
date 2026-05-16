package cn.com.bosssfot.dv.plm.inception.controller;

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
import cn.com.bosssfot.dv.plm.inception.domain.Inception;
import cn.com.bosssfot.dv.plm.inception.service.IInceptionService;

/**
 * 项目立项 Controller — PRD §F1.1
 * 业务路径 /business/inception/* (RuoYi 标准 6 端点 + AI 生成入口)
 */
@RestController
@RequestMapping("/business/inception")
public class InceptionController extends BaseController {

    @Autowired
    private IInceptionService inceptionService;

    @PreAuthorize("@ss.hasPermi('business:inception:list')")
    @GetMapping("/list")
    public TableDataInfo list(Inception inception) {
        startPage();
        return getDataTable(inceptionService.selectInceptionList(inception));
    }

    @PreAuthorize("@ss.hasPermi('business:inception:export')")
    @Log(title = "项目立项", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Inception inception) {
        List<Inception> list = inceptionService.selectInceptionList(inception);
        new ExcelUtil<Inception>(Inception.class).exportExcel(response, list, "项目立项数据");
    }

    @PreAuthorize("@ss.hasPermi('business:inception:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(inceptionService.selectInceptionById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:inception:add')")
    @Log(title = "项目立项", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Inception inception) {
        return toAjax(inceptionService.insertInception(inception));
    }

    @PreAuthorize("@ss.hasPermi('business:inception:edit')")
    @Log(title = "项目立项", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Inception inception) {
        return toAjax(inceptionService.updateInception(inception));
    }

    @PreAuthorize("@ss.hasPermi('business:inception:remove')")
    @Log(title = "项目立项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(inceptionService.deleteInceptionByIds(ids));
    }

    /** PRD §F1.1 AI 立项 — project-inception-flow */
    @PreAuthorize("@ss.hasPermi('business:inception:edit')")
    @Log(title = "项目立项-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(inceptionService.aiGenerate(id));
    }
}
