package cn.com.bosssfot.dv.plm.arch.controller;

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
import cn.com.bosssfot.dv.plm.arch.domain.Arch;
import cn.com.bosssfot.dv.plm.arch.service.IArchService;

/**
 * 架构设计 Controller — PRD §F3.1
 */
@RestController
@RequestMapping("/business/arch")
public class ArchController extends BaseController {

    @Autowired
    private IArchService archService;

    @PreAuthorize("@ss.hasPermi('business:arch:list')")
    @GetMapping("/list")
    public TableDataInfo list(Arch arch) {
        startPage();
        return getDataTable(archService.selectArchList(arch));
    }

    @PreAuthorize("@ss.hasPermi('business:arch:export')")
    @Log(title = "架构设计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Arch arch) {
        List<Arch> list = archService.selectArchList(arch);
        new ExcelUtil<Arch>(Arch.class).exportExcel(response, list, "架构设计数据");
    }

    @PreAuthorize("@ss.hasPermi('business:arch:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(archService.selectArchById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:arch:add')")
    @Log(title = "架构设计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Arch arch) {
        return toAjax(archService.insertArch(arch));
    }

    @PreAuthorize("@ss.hasPermi('business:arch:edit')")
    @Log(title = "架构设计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Arch arch) {
        return toAjax(archService.updateArch(arch));
    }

    @PreAuthorize("@ss.hasPermi('business:arch:remove')")
    @Log(title = "架构设计", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(archService.deleteArchByIds(ids));
    }

    /** PRD §F3.1 AI 架构推荐 */
    @PreAuthorize("@ss.hasPermi('business:arch:edit')")
    @Log(title = "架构-AI推荐", businessType = BusinessType.OTHER)
    @PostMapping("/ai/recommend/{id}")
    public AjaxResult aiRecommend(@PathVariable("id") Long id) {
        return success(archService.aiRecommend(id));
    }
}
