package cn.com.bosssfot.dv.plm.release.controller;

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
import cn.com.bosssfot.dv.plm.release.domain.Release;
import cn.com.bosssfot.dv.plm.release.service.IReleaseService;

@RestController
@RequestMapping("/business/release")
public class ReleaseController extends BaseController {

    @Autowired
    private IReleaseService releaseService;

    @PreAuthorize("@ss.hasPermi('business:release:list')")
    @GetMapping("/list")
    public TableDataInfo list(Release release) {
        startPage();
        return getDataTable(releaseService.selectReleaseList(release));
    }

    @PreAuthorize("@ss.hasPermi('business:release:export')")
    @Log(title = "发布管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Release release) {
        List<Release> list = releaseService.selectReleaseList(release);
        new ExcelUtil<Release>(Release.class).exportExcel(response, list, "发布管理数据");
    }

    @PreAuthorize("@ss.hasPermi('business:release:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(releaseService.selectReleaseById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:release:add')")
    @Log(title = "发布管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Release release) {
        return toAjax(releaseService.insertRelease(release));
    }

    @PreAuthorize("@ss.hasPermi('business:release:edit')")
    @Log(title = "发布管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Release release) {
        return toAjax(releaseService.updateRelease(release));
    }

    @PreAuthorize("@ss.hasPermi('business:release:remove')")
    @Log(title = "发布管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(releaseService.deleteReleaseByIds(ids));
    }
}
