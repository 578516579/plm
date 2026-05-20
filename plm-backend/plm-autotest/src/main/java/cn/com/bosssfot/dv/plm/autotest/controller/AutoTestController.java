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
import cn.com.bosssfot.dv.plm.autotest.domain.AutoTest;
import cn.com.bosssfot.dv.plm.autotest.service.IAutoTestService;

/** 自动化测试 Controller — PRD §F4.5 */
@RestController
@RequestMapping("/business/autotest")
public class AutoTestController extends BaseController {

    @Autowired
    private IAutoTestService autotestService;

    @PreAuthorize("@ss.hasPermi('business:autotest:list')")
    @GetMapping("/list")
    public TableDataInfo list(AutoTest autotest) {
        startPage();
        return getDataTable(autotestService.selectAutoTestList(autotest));
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:export')")
    @Log(title = "自动化测试", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AutoTest autotest) {
        List<AutoTest> list = autotestService.selectAutoTestList(autotest);
        new ExcelUtil<AutoTest>(AutoTest.class).exportExcel(response, list, "自动化测试数据");
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(autotestService.selectAutoTestById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:add')")
    @Log(title = "自动化测试", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AutoTest autotest) {
        return toAjax(autotestService.insertAutoTest(autotest));
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:edit')")
    @Log(title = "自动化测试", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AutoTest autotest) {
        return toAjax(autotestService.updateAutoTest(autotest));
    }

    @PreAuthorize("@ss.hasPermi('business:autotest:remove')")
    @Log(title = "自动化测试", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(autotestService.deleteAutoTestByIds(ids));
    }

    /** PRD §F4.5 AI 生成脚本 — auto-test-flow */
    @PreAuthorize("@ss.hasPermi('business:autotest:edit')")
    @Log(title = "自动化测试-AI生成脚本", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(autotestService.aiGenerate(id));
    }

    /** PRD §F4.5 立即执行 — mock 执行 + 失败时 AI 根因分析 */
    @PreAuthorize("@ss.hasPermi('business:autotest:edit')")
    @Log(title = "自动化测试-立即执行", businessType = BusinessType.OTHER)
    @PostMapping("/run/{id}")
    public AjaxResult runNow(@PathVariable("id") Long id) {
        return success(autotestService.runAutoTest(id));
    }
}
