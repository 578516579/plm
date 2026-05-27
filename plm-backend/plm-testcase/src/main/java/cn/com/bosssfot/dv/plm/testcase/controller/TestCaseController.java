package cn.com.bosssfot.dv.plm.testcase.controller;

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
import cn.com.bosssfot.dv.plm.testcase.domain.TestCase;
import cn.com.bosssfot.dv.plm.testcase.service.ITestCaseService;

@RestController
@RequestMapping("/business/testcase")
public class TestCaseController extends BaseController
{
    @Autowired
    private ITestCaseService testcaseService;

    @PreAuthorize("@ss.hasPermi('business:testcase:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestCase testcase) {
        startPage();
        return getDataTable(testcaseService.selectTestCaseList(testcase));
    }

    @PreAuthorize("@ss.hasPermi('business:testcase:export')")
    @Log(title = "测试用例", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TestCase testcase) {
        List<TestCase> list = testcaseService.selectTestCaseList(testcase);
        ExcelUtil<TestCase> util = new ExcelUtil<>(TestCase.class);
        util.exportExcel(response, list, "测试用例数据");
    }

    @PreAuthorize("@ss.hasPermi('business:testcase:query')")
    @GetMapping("/{testcaseId}")
    public AjaxResult getInfo(@PathVariable Long testcaseId) {
        return success(testcaseService.selectTestCaseById(testcaseId));
    }

    @PreAuthorize("@ss.hasPermi('business:testcase:add')")
    @Log(title = "测试用例", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TestCase testcase) {
        return toAjax(testcaseService.insertTestCase(testcase));
    }

    @PreAuthorize("@ss.hasPermi('business:testcase:edit')")
    @Log(title = "测试用例", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TestCase testcase) {
        return toAjax(testcaseService.updateTestCase(testcase));
    }

    @PreAuthorize("@ss.hasPermi('business:testcase:remove')")
    @Log(title = "测试用例", businessType = BusinessType.DELETE)
    @DeleteMapping("/{testcaseIds}")
    public AjaxResult remove(@PathVariable Long[] testcaseIds) {
        return toAjax(testcaseService.deleteTestCaseByIds(testcaseIds));
    }

    /** /execute 独有端点 */
    @PreAuthorize("@ss.hasPermi('business:testcase:execute')")
    @Log(title = "测试用例", businessType = BusinessType.UPDATE)
    @PostMapping("/{testcaseId}/execute")
    public AjaxResult execute(@PathVariable Long testcaseId, @RequestBody ExecuteRequest req) {
        return toAjax(testcaseService.executeTestCase(testcaseId, req.getStatus(), req.getActualResult()));
    }

    /** AI 生成用例要素(前置条件/步骤/预期结果)— PRD §F3.5 */
    @PreAuthorize("@ss.hasPermi('business:testcase:edit')")
    @Log(title = "测试用例-AI生成", businessType = BusinessType.UPDATE)
    @PostMapping("/{testcaseId}/ai-generate")
    public AjaxResult aiGenerate(@PathVariable Long testcaseId) {
        return success(testcaseService.aiGenerate(testcaseId));
    }

    public static class ExecuteRequest {
        private String status;
        private String actualResult;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getActualResult() { return actualResult; }
        public void setActualResult(String actualResult) { this.actualResult = actualResult; }
    }
}
