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

/** Feature Flag Controller — DevOps 扩展 */
@RestController
@RequestMapping("/business/feature-flag")
public class FeatureFlagController extends BaseController {

    @Autowired private IFeatureFlagService flagService;

    @PreAuthorize("@ss.hasPermi('business:feature-flag:list')")
    @GetMapping("/list")
    public TableDataInfo list(FeatureFlag flag) {
        startPage();
        return getDataTable(flagService.selectFeatureFlagList(flag));
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:export')")
    @Log(title = "Feature Flag", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FeatureFlag flag) {
        List<FeatureFlag> list = flagService.selectFeatureFlagList(flag);
        new ExcelUtil<FeatureFlag>(FeatureFlag.class).exportExcel(response, list, "Feature Flag 数据");
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(flagService.selectFeatureFlagById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:add')")
    @Log(title = "Feature Flag", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FeatureFlag flag) {
        return toAjax(flagService.insertFeatureFlag(flag));
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:edit')")
    @Log(title = "Feature Flag", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FeatureFlag flag) {
        return toAjax(flagService.updateFeatureFlag(flag));
    }

    @PreAuthorize("@ss.hasPermi('business:feature-flag:remove')")
    @Log(title = "Feature Flag", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(flagService.deleteFeatureFlagByIds(ids));
    }

    /** 业务侧判断 flag 是否对该用户开启 (按 userId 哈希) */
    @PreAuthorize("@ss.hasPermi('business:feature-flag:query')")
    @GetMapping("/check")
    public AjaxResult check(@RequestParam String flagKey,
                            @RequestParam String environment,
                            @RequestParam(required = false) Long userId) {
        boolean enabled = flagService.isEnabled(flagKey, environment, userId);
        return AjaxResult.success("ok", enabled);
    }
}
