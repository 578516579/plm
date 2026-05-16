package cn.com.bosssfot.dv.plm.sprint.controller;

import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;
import cn.com.bosssfot.dv.plm.sprint.domain.Sprint;
import cn.com.bosssfot.dv.plm.sprint.service.ISprintService;

/**
 * 迭代 控制器
 *
 * Base path: /business/sprint
 * 关联：02-设计/Sprint-API设计.md §1 端点清单（8 端点）
 */
@RestController
@RequestMapping("/business/sprint")
public class SprintController extends BaseController
{
    @Autowired
    private ISprintService sprintService;

    /** 列表 */
    @PreAuthorize("@ss.hasPermi('business:sprint:list')")
    @GetMapping("/list")
    public TableDataInfo list(Sprint sprint)
    {
        startPage();
        List<Sprint> list = sprintService.selectSprintList(sprint);
        return getDataTable(list);
    }

    /** 导出 */
    @PreAuthorize("@ss.hasPermi('business:sprint:export')")
    @Log(title = "迭代", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Sprint sprint)
    {
        List<Sprint> list = sprintService.selectSprintList(sprint);
        ExcelUtil<Sprint> util = new ExcelUtil<Sprint>(Sprint.class);
        util.exportExcel(response, list, "迭代数据");
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('business:sprint:query')")
    @GetMapping(value = "/{sprintId}")
    public AjaxResult getInfo(@PathVariable("sprintId") Long sprintId)
    {
        return success(sprintService.selectSprintById(sprintId));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('business:sprint:add')")
    @Log(title = "迭代", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Sprint sprint)
    {
        return toAjax(sprintService.insertSprint(sprint));
    }

    /** 修改（含状态机 + 703 单一活跃约束 + actual 日期自动填充） */
    @PreAuthorize("@ss.hasPermi('business:sprint:edit')")
    @Log(title = "迭代", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Sprint sprint)
    {
        return toAjax(sprintService.updateSprint(sprint));
    }

    /** 删除（前置检查关联任务） */
    @PreAuthorize("@ss.hasPermi('business:sprint:remove')")
    @Log(title = "迭代", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sprintIds}")
    public AjaxResult remove(@PathVariable Long[] sprintIds)
    {
        return toAjax(sprintService.deleteSprintByIds(sprintIds));
    }

    /** 查项目当前活跃迭代（S-008，权限复用 query） */
    @PreAuthorize("@ss.hasPermi('business:sprint:query')")
    @GetMapping("/current")
    public AjaxResult current(@RequestParam("projectId") Long projectId)
    {
        return success(sprintService.selectCurrentByProject(projectId));
    }

    /** 健康度统计（S-009） */
    @PreAuthorize("@ss.hasPermi('business:sprint:stats')")
    @GetMapping("/{sprintId}/stats")
    public AjaxResult stats(@PathVariable("sprintId") Long sprintId)
    {
        Map<String, Object> data = sprintService.selectSprintStats(sprintId);
        return success(data);
    }
}
