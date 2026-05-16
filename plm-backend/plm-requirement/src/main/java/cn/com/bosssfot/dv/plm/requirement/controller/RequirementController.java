package cn.com.bosssfot.dv.plm.requirement.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.requirement.service.IRequirementService;

/**
 * 需求 控制器
 *
 * Base path: /business/requirement
 * 关联：02-设计/Requirement-API设计.md §1 端点清单
 */
@RestController
@RequestMapping("/business/requirement")
public class RequirementController extends BaseController
{
    @Autowired
    private IRequirementService requirementService;

    /** 列表（分页 + 搜索） */
    @PreAuthorize("@ss.hasPermi('business:requirement:list')")
    @GetMapping("/list")
    public TableDataInfo list(Requirement requirement)
    {
        startPage();
        List<Requirement> list = requirementService.selectRequirementList(requirement);
        return getDataTable(list);
    }

    /** 导出 Excel */
    @PreAuthorize("@ss.hasPermi('business:requirement:export')")
    @Log(title = "需求", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Requirement requirement)
    {
        List<Requirement> list = requirementService.selectRequirementList(requirement);
        ExcelUtil<Requirement> util = new ExcelUtil<Requirement>(Requirement.class);
        util.exportExcel(response, list, "需求数据");
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('business:requirement:query')")
    @GetMapping(value = "/{requirementId}")
    public AjaxResult getInfo(@PathVariable("requirementId") Long requirementId)
    {
        return success(requirementService.selectRequirementById(requirementId));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('business:requirement:add')")
    @Log(title = "需求", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Requirement requirement)
    {
        return toAjax(requirementService.insertRequirement(requirement));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('business:requirement:edit')")
    @Log(title = "需求", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Requirement requirement)
    {
        return toAjax(requirementService.updateRequirement(requirement));
    }

    /** 删除（逻辑） */
    @PreAuthorize("@ss.hasPermi('business:requirement:remove')")
    @Log(title = "需求", businessType = BusinessType.DELETE)
    @DeleteMapping("/{requirementIds}")
    public AjaxResult remove(@PathVariable Long[] requirementIds)
    {
        return toAjax(requirementService.deleteRequirementByIds(requirementIds));
    }
}
