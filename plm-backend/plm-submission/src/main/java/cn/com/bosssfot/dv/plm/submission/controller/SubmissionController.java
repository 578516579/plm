package cn.com.bosssfot.dv.plm.submission.controller;

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
import cn.com.bosssfot.dv.plm.submission.domain.Submission;
import cn.com.bosssfot.dv.plm.submission.service.ISubmissionService;

@RestController
@RequestMapping("/business/submission")
public class SubmissionController extends BaseController {

    @Autowired
    private ISubmissionService submissionService;

    @PreAuthorize("@ss.hasPermi('business:submission:list')")
    @GetMapping("/list")
    public TableDataInfo list(Submission submission) {
        startPage();
        return getDataTable(submissionService.selectSubmissionList(submission));
    }

    @PreAuthorize("@ss.hasPermi('business:submission:export')")
    @Log(title = "提测管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Submission submission) {
        List<Submission> list = submissionService.selectSubmissionList(submission);
        new ExcelUtil<Submission>(Submission.class).exportExcel(response, list, "提测管理数据");
    }

    @PreAuthorize("@ss.hasPermi('business:submission:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(submissionService.selectSubmissionById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:submission:add')")
    @Log(title = "提测管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Submission submission) {
        return toAjax(submissionService.insertSubmission(submission));
    }

    @PreAuthorize("@ss.hasPermi('business:submission:edit')")
    @Log(title = "提测管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Submission submission) {
        return toAjax(submissionService.updateSubmission(submission));
    }

    @PreAuthorize("@ss.hasPermi('business:submission:remove')")
    @Log(title = "提测管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(submissionService.deleteSubmissionByIds(ids));
    }
}
