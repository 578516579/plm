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
import cn.com.bosssfot.dv.plm.requirement.domain.RequirementReview;
import cn.com.bosssfot.dv.plm.requirement.service.IRequirementReviewService;
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

    @Autowired
    private IRequirementReviewService requirementReviewService;

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

    /** AI 优先级初评（PRD §F2.1 req-priority-flow）— 根据需求内容评估 high/medium/low */
    @PreAuthorize("@ss.hasPermi('business:requirement:edit')")
    @Log(title = "需求-AI优先级初评", businessType = BusinessType.OTHER)
    @PostMapping("/ai/evaluate/{id}")
    public AjaxResult aiEvaluate(@PathVariable("id") Long id)
    {
        return success(requirementService.aiEvaluate(id));
    }

    // ─────────────────────────────────────────────────────────────────────
    // PRD §F2.4 需求评审管理（2026-05-25 新增）
    // ─────────────────────────────────────────────────────────────────────

    /** 评审历史：列出某需求的全部评审记录（倒序） */
    @PreAuthorize("@ss.hasPermi('business:requirement:query')")
    @GetMapping("/{requirementId}/reviews")
    public AjaxResult listReviews(@PathVariable("requirementId") Long requirementId)
    {
        List<RequirementReview> reviews = requirementReviewService.selectByRequirementId(requirementId);
        return success(reviews);
    }

    /** 单条评审详情 */
    @PreAuthorize("@ss.hasPermi('business:requirement:query')")
    @GetMapping("/review/{reviewId}")
    public AjaxResult getReview(@PathVariable("reviewId") Long reviewId)
    {
        return success(requirementReviewService.selectRequirementReviewById(reviewId));
    }

    /** 提交评审（业务核心：状态机 00→01 的前置） */
    @PreAuthorize("@ss.hasPermi('business:requirement:review')")
    @Log(title = "需求评审", businessType = BusinessType.INSERT)
    @PostMapping("/{requirementId}/review")
    public AjaxResult submitReview(
            @PathVariable("requirementId") Long requirementId,
            @RequestBody RequirementReview review)
    {
        return toAjax(requirementReviewService.submitReview(requirementId, review));
    }

    /** 撤回评审（逻辑删除） */
    @PreAuthorize("@ss.hasPermi('business:requirement:review')")
    @Log(title = "需求评审", businessType = BusinessType.DELETE)
    @DeleteMapping("/review/{reviewIds}")
    public AjaxResult removeReviews(@PathVariable Long[] reviewIds)
    {
        return toAjax(requirementReviewService.deleteRequirementReviewByIds(reviewIds));
    }
}
