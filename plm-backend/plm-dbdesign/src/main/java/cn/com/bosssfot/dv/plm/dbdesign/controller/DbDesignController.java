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
import cn.com.bosssfot.dv.plm.dbdesign.domain.DbDesign;
import cn.com.bosssfot.dv.plm.dbdesign.service.IDbDesignService;

/**
 * 数据库设计 Controller — PRD §F3.2
 * /business/dbdesign/* (6 标准 + AI 生成入口)
 */
@RestController
@RequestMapping("/business/dbdesign")
public class DbDesignController extends BaseController {

    @Autowired
    private IDbDesignService dbdesignService;

    @PreAuthorize("@ss.hasPermi('business:dbdesign:list')")
    @GetMapping("/list")
    public TableDataInfo list(DbDesign dbdesign) {
        startPage();
        return getDataTable(dbdesignService.selectDbDesignList(dbdesign));
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:export')")
    @Log(title = "数据库设计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DbDesign dbdesign) {
        List<DbDesign> list = dbdesignService.selectDbDesignList(dbdesign);
        new ExcelUtil<DbDesign>(DbDesign.class).exportExcel(response, list, "数据库设计数据");
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(dbdesignService.selectDbDesignById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:add')")
    @Log(title = "数据库设计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DbDesign dbdesign) {
        return toAjax(dbdesignService.insertDbDesign(dbdesign));
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:edit')")
    @Log(title = "数据库设计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DbDesign dbdesign) {
        return toAjax(dbdesignService.updateDbDesign(dbdesign));
    }

    @PreAuthorize("@ss.hasPermi('business:dbdesign:remove')")
    @Log(title = "数据库设计", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(dbdesignService.deleteDbDesignByIds(ids));
    }

    /** PRD §F3.2 AI 生成 ER 图 + DDL — db-design-flow */
    @PreAuthorize("@ss.hasPermi('business:dbdesign:edit')")
    @Log(title = "数据库设计-AI生成", businessType = BusinessType.OTHER)
    @PostMapping("/ai/generate/{id}")
    public AjaxResult aiGenerate(@PathVariable("id") Long id) {
        return success(dbdesignService.aiGenerate(id));
    }
}
