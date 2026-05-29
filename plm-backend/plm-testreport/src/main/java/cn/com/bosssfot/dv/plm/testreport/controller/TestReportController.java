package cn.com.bosssfot.dv.plm.testreport.controller;

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
import cn.com.bosssfot.dv.plm.testreport.domain.TestReport;
import cn.com.bosssfot.dv.plm.testreport.service.ITestReportService;

@RestController
@RequestMapping("/business/testreport")
public class TestReportController extends BaseController {

    @Autowired
    private ITestReportService testreportService;

    @PreAuthorize("@ss.hasPermi('business:testreport:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestReport testreport) {
        startPage();
        return getDataTable(testreportService.selectTestReportList(testreport));
    }

    @PreAuthorize("@ss.hasPermi('business:testreport:export')")
    @Log(title = "测试报告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TestReport testreport) {
        List<TestReport> list = testreportService.selectTestReportList(testreport);
        new ExcelUtil<TestReport>(TestReport.class).exportExcel(response, list, "测试报告数据");
    }

    @PreAuthorize("@ss.hasPermi('business:testreport:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(testreportService.selectTestReportById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:testreport:add')")
    @Log(title = "测试报告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TestReport testreport) {
        return toAjax(testreportService.insertTestReport(testreport));
    }

    @PreAuthorize("@ss.hasPermi('business:testreport:edit')")
    @Log(title = "测试报告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TestReport testreport) {
        return toAjax(testreportService.updateTestReport(testreport));
    }

    @PreAuthorize("@ss.hasPermi('business:testreport:remove')")
    @Log(title = "测试报告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(testreportService.deleteTestReportByIds(ids));
    }

    /** Proposal 0028 P0-3A — 按 projectId/testplanId 实时聚合 testcase + defect */
    @PreAuthorize("@ss.hasPermi('business:testreport:edit')")
    @Log(title = "测试报告", businessType = BusinessType.OTHER)
    @PostMapping("/{id}/refresh-aggregate")
    public AjaxResult refreshAggregate(@PathVariable("id") Long id) {
        return AjaxResult.success(testreportService.aggregateFromTestplan(id));
    }
}
