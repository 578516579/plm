package cn.com.bosssfot.dv.plm.pipeline.controller;

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
import cn.com.bosssfot.dv.plm.pipeline.domain.Pipeline;
import cn.com.bosssfot.dv.plm.pipeline.service.IPipelineService;

/**
 * CI/CD流水线 控制器
 * Base path: /business/pipeline
 */
@RestController
@RequestMapping("/business/pipeline")
public class PipelineController extends BaseController
{
    @Autowired
    private IPipelineService pipelineService;

    @PreAuthorize("@ss.hasPermi('business:pipeline:list')")
    @GetMapping("/list")
    public TableDataInfo list(Pipeline pipeline) {
        startPage();
        return getDataTable(pipelineService.selectPipelineList(pipeline));
    }

    @PreAuthorize("@ss.hasPermi('business:pipeline:export')")
    @Log(title = "CI/CD流水线", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Pipeline pipeline) {
        List<Pipeline> list = pipelineService.selectPipelineList(pipeline);
        ExcelUtil<Pipeline> util = new ExcelUtil<Pipeline>(Pipeline.class);
        util.exportExcel(response, list, "CI/CD流水线数据");
    }

    @PreAuthorize("@ss.hasPermi('business:pipeline:query')")
    @GetMapping("/{pipelineId}")
    public AjaxResult getInfo(@PathVariable("pipelineId") Long pipelineId) {
        return success(pipelineService.selectPipelineById(pipelineId));
    }

    @PreAuthorize("@ss.hasPermi('business:pipeline:add')")
    @Log(title = "CI/CD流水线", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Pipeline pipeline) {
        return toAjax(pipelineService.insertPipeline(pipeline));
    }

    @PreAuthorize("@ss.hasPermi('business:pipeline:edit')")
    @Log(title = "CI/CD流水线", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Pipeline pipeline) {
        return toAjax(pipelineService.updatePipeline(pipeline));
    }

    @PreAuthorize("@ss.hasPermi('business:pipeline:remove')")
    @Log(title = "CI/CD流水线", businessType = BusinessType.DELETE)
    @DeleteMapping("/{pipelineIds}")
    public AjaxResult remove(@PathVariable Long[] pipelineIds) {
        return toAjax(pipelineService.deletePipelineByIds(pipelineIds));
    }
}
