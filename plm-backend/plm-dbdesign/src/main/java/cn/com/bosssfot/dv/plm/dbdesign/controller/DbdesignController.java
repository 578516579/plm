package cn.com.bosssfot.dv.plm.dbdesign.controller;

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
import cn.com.bosssfot.dv.plm.dbdesign.domain.Dbdesign;
import cn.com.bosssfot.dv.plm.dbdesign.service.IDbdesignService;

/**
 * 数据库设计 Controller — PRD §F3.2
 */
@RestController
@RequestMapping("/business/dbdesign")
public class DbdesignController extends BaseController {

    @Autowired
    private IDbdesignService dbdesignService;

    @PreAuthorize("@ss.hasPermi('business:dbdesign:list')")
    @GetMapping("/list")
    public TableDataInfo list(Dbdesign dbdesign) {
        startPage();
        return getDataTable(dbdesignService.selectDbdesignList(dbdesign));
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:export')")
    @Log(title = "数据库设计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Dbdesign dbdesign) {
        List<Dbdesign> list = dbdesignService.selectDbdesignList(dbdesign);
        new ExcelUtil<Dbdesign>(Dbdesign.class).exportExcel(response, list, "数据库设计数据");
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(dbdesignService.selectDbdesignById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:add')")
    @Log(title = "数据库设计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Dbdesign dbdesign) {
        return toAjax(dbdesignService.insertDbdesign(dbdesign));
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:edit')")
    @Log(title = "数据库设计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Dbdesign dbdesign) {
        return toAjax(dbdesignService.updateDbdesign(dbdesign));
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:remove')")
    @Log(title = "数据库设计", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(dbdesignService.deleteDbdesignByIds(ids));
    }

    /** PRD §F3.2 AI ER 图审查 */
    @PreAuthorize("@ss.hasPermi('business:dbdesign:edit')")
    @Log(title = "数据库设计-AI-ER", businessType = BusinessType.OTHER)
    @PostMapping("/ai/er/{id}")
    public AjaxResult aiEr(@PathVariable("id") Long id) {
        return success(dbdesignService.aiEr(id));
    }
}
