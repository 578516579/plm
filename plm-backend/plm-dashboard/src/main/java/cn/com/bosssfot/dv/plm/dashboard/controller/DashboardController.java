package cn.com.bosssfot.dv.plm.dashboard.controller;

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
import cn.com.bosssfot.dv.plm.dashboard.domain.Dashboard;
import cn.com.bosssfot.dv.plm.dashboard.service.IDashboardService;

/**
 * 工作台配置 Controller
 * /business/dashboard/* (6 标准端点)
 */
@RestController
@RequestMapping("/business/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private IDashboardService dashboardService;

    @PreAuthorize("@ss.hasPermi('business:dashboard:list')")
    @GetMapping("/list")
    public TableDataInfo list(Dashboard dashboard) {
        startPage();
        return getDataTable(dashboardService.selectDashboardList(dashboard));
    }

    @PreAuthorize("@ss.hasPermi('business:dashboard:export')")
    @Log(title = "工作台配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Dashboard dashboard) {
        List<Dashboard> list = dashboardService.selectDashboardList(dashboard);
        new ExcelUtil<Dashboard>(Dashboard.class).exportExcel(response, list, "工作台配置数据");
    }

    @PreAuthorize("@ss.hasPermi('business:dashboard:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(dashboardService.selectDashboardById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:dashboard:add')")
    @Log(title = "工作台配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Dashboard dashboard) {
        return toAjax(dashboardService.insertDashboard(dashboard));
    }

    @PreAuthorize("@ss.hasPermi('business:dashboard:edit')")
    @Log(title = "工作台配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Dashboard dashboard) {
        return toAjax(dashboardService.updateDashboard(dashboard));
    }

    @PreAuthorize("@ss.hasPermi('business:dashboard:remove')")
    @Log(title = "工作台配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(dashboardService.deleteDashboardByIds(ids));
    }
}
