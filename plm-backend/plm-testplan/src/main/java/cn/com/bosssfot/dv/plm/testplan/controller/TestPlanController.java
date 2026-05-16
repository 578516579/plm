package cn.com.bosssfot.dv.plm.testplan.controller;

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
import cn.com.bosssfot.dv.plm.testplan.domain.TestPlan;
import cn.com.bosssfot.dv.plm.testplan.service.ITestPlanService;

@RestController
@RequestMapping("/business/testplan")
public class TestPlanController extends BaseController {

    @Autowired
    private ITestPlanService testplanService;

    @PreAuthorize("@ss.hasPermi('business:testplan:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestPlan testplan) {
        startPage();
        return getDataTable(testplanService.selectTestPlanList(testplan));
    }

    @PreAuthorize("@ss.hasPermi('business:testplan:export')")
    @Log(title = "测试方案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TestPlan testplan) {
        List<TestPlan> list = testplanService.selectTestPlanList(testplan);
        new ExcelUtil<TestPlan>(TestPlan.class).exportExcel(response, list, "测试方案数据");
    }

    @PreAuthorize("@ss.hasPermi('business:testplan:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(testplanService.selectTestPlanById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:testplan:add')")
    @Log(title = "测试方案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TestPlan testplan) {
        return toAjax(testplanService.insertTestPlan(testplan));
    }

    @PreAuthorize("@ss.hasPermi('business:testplan:edit')")
    @Log(title = "测试方案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TestPlan testplan) {
        return toAjax(testplanService.updateTestPlan(testplan));
    }

    @PreAuthorize("@ss.hasPermi('business:testplan:remove')")
    @Log(title = "测试方案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(testplanService.deleteTestPlanByIds(ids));
    }
}
