package cn.com.bosssfot.dv.plm.testdata.controller;

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
import cn.com.bosssfot.dv.plm.testdata.domain.Testdata;
import cn.com.bosssfot.dv.plm.testdata.service.ITestdataService;

/**
 * 测试数据集 Controller — PRD §F4.3
 */
@RestController
@RequestMapping("/business/testdata")
public class TestdataController extends BaseController {

    @Autowired
    private ITestdataService testdataService;

    @PreAuthorize("@ss.hasPermi('business:testdata:list')")
    @GetMapping("/list")
    public TableDataInfo list(Testdata testdata) {
        startPage();
        return getDataTable(testdataService.selectTestdataList(testdata));
    }

    @PreAuthorize("@ss.hasPermi('business:testdata:export')")
    @Log(title = "测试数据集", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Testdata testdata) {
        List<Testdata> list = testdataService.selectTestdataList(testdata);
        new ExcelUtil<Testdata>(Testdata.class).exportExcel(response, list, "测试数据集数据");
    }

    @PreAuthorize("@ss.hasPermi('business:testdata:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(testdataService.selectTestdataById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:testdata:add')")
    @Log(title = "测试数据集", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Testdata testdata) {
        return toAjax(testdataService.insertTestdata(testdata));
    }

    @PreAuthorize("@ss.hasPermi('business:testdata:edit')")
    @Log(title = "测试数据集", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Testdata testdata) {
        return toAjax(testdataService.updateTestdata(testdata));
    }

    @PreAuthorize("@ss.hasPermi('business:testdata:remove')")
    @Log(title = "测试数据集", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(testdataService.deleteTestdataByIds(ids));
    }

    /** PRD §F4.3 AI 数据生成 */
    @PreAuthorize("@ss.hasPermi('business:testdata:edit')")
    @Log(title = "测试数据-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(testdataService.aiGenerate(id));
    }
}
