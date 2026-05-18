package cn.com.bosssfot.dv.plm.autotest.controller;

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
import cn.com.bosssfot.dv.plm.autotest.domain.Autotest;
import cn.com.bosssfot.dv.plm.autotest.service.IAutotestService;

/** 自动化测试套件 Controller — PRD §F4.5 — /business/autotest/* */
@RestController
@RequestMapping("/business/autotest")
public class AutotestController extends BaseController {

    @Autowired
    private IAutotestService autotestService;

    @PreAuthorize("@ss.hasPermi('business:autotest:list')")
    @GetMapping("/list")
    public TableDataInfo list(Autotest autotest) {
        startPage();
        return getDataTable(autotestService.selectAutotestList(autotest));
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:export')")
    @Log(title = "自动化测试套件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Autotest autotest) {
        List<Autotest> list = autotestService.selectAutotestList(autotest);
        new ExcelUtil<Autotest>(Autotest.class).exportExcel(response, list, "自动化测试套件");
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(autotestService.selectAutotestById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:add')")
    @Log(title = "自动化测试套件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Autotest autotest) {
        return toAjax(autotestService.insertAutotest(autotest));
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:edit')")
    @Log(title = "自动化测试套件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Autotest autotest) {
        return toAjax(autotestService.updateAutotest(autotest));
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:remove')")
    @Log(title = "自动化测试套件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(autotestService.deleteAutotestByIds(ids));
    }

    /** PRD §F4.5 AI 生成自动化测试脚本 — autotest-gen-flow */
    @PreAuthorize("@ss.hasPermi('business:autotest:edit')")
    @Log(title = "自动化测试套件-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/generate/{id}")
    public AjaxResult generate(@PathVariable("id") Long id) {
        return success(autotestService.generate(id));
    }
}
