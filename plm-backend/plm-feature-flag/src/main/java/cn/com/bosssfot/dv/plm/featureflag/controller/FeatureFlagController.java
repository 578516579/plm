package cn.com.bosssfot.dv.plm.featureflag.controller;

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
import cn.com.bosssfot.dv.plm.featureflag.domain.FeatureFlag;
import cn.com.bosssfot.dv.plm.featureflag.service.IFeatureFlagService;

/**
 * 功能开关 Controller
 * /business/feature-flag/* (6 标准 + toggle 开关切换)
 */
@RestController
@RequestMapping("/business/feature-flag")
public class FeatureFlagController extends BaseController {

    @Autowired
    private IFeatureFlagService featureFlagService;

    @PreAuthorize("@ss.hasPermi('business:feature-flag:list')")
    @GetMapping("/list")
    public TableDataInfo list(FeatureFlag featureFlag) {
        startPage();
        return getDataTable(featureFlagService.selectFeatureFlagList(featureFlag));
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:export')")
    @Log(title = "功能开关", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FeatureFlag featureFlag) {
        List<FeatureFlag> list = featureFlagService.selectFeatureFlagList(featureFlag);
        new ExcelUtil<FeatureFlag>(FeatureFlag.class).exportExcel(response, list, "功能开关数据");
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(featureFlagService.selectFeatureFlagById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:add')")
    @Log(title = "功能开关", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FeatureFlag featureFlag) {
        return toAjax(featureFlagService.insertFeatureFlag(featureFlag));
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:edit')")
    @Log(title = "功能开关", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FeatureFlag featureFlag) {
        return toAjax(featureFlagService.updateFeatureFlag(featureFlag));
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:remove')")
    @Log(title = "功能开关", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(featureFlagService.deleteFeatureFlagByIds(ids));
    }

    /** 切换开关 enabled Y↔N */
    @PreAuthorize("@ss.hasPermi('business:feature-flag:edit')")
    @Log(title = "功能开关-切换状态", businessType = BusinessType.UPDATE)
    @PutMapping("/toggle/{id}")
    public AjaxResult toggle(@PathVariable("id") Long id) {
        return success(featureFlagService.toggleEnabled(id));
    }
}
