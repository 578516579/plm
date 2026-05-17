package cn.com.bosssfot.dv.plm.task.controller;

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
import cn.com.bosssfot.dv.plm.task.domain.Task;
import cn.com.bosssfot.dv.plm.task.service.ITaskService;

/**
 * 任务 控制器
 *
 * Base path: /business/task
 * 关联：02-设计/Task-API设计.md §1 端点清单（8 端点）
 */
@RestController
@RequestMapping("/business/task")
public class TaskController extends BaseController
{
    @Autowired
    private ITaskService taskService;

    /** 列表 */
    @PreAuthorize("@ss.hasPermi('business:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(Task task)
    {
        startPage();
        List<Task> list = taskService.selectTaskList(task);
        return getDataTable(list);
    }

    /** 导出 */
    @PreAuthorize("@ss.hasPermi('business:task:export')")
    @Log(title = "任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Task task)
    {
        List<Task> list = taskService.selectTaskList(task);
        ExcelUtil<Task> util = new ExcelUtil<Task>(Task.class);
        util.exportExcel(response, list, "任务数据");
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('business:task:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return success(taskService.selectTaskById(taskId));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('business:task:add')")
    @Log(title = "任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Task task)
    {
        return toAjax(taskService.insertTask(task));
    }

    /** 修改（含 6×6 状态机 + 反向边 + 已完成必填 actualHours） */
    @PreAuthorize("@ss.hasPermi('business:task:edit')")
    @Log(title = "任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Task task)
    {
        return toAjax(taskService.updateTask(task));
    }

    /** 删除（逻辑） */
    @PreAuthorize("@ss.hasPermi('business:task:remove')")
    @Log(title = "任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(taskService.deleteTaskByIds(taskIds));
    }

    /** 我的任务（T-007，复用 list 权限） */
    @PreAuthorize("@ss.hasPermi('business:task:list')")
    @GetMapping("/my")
    public TableDataInfo my(Task task)
    {
        startPage();
        List<Task> list = taskService.selectMyTasks(task);
        return getDataTable(list);
    }

    /** 看板视图（T-006）— 支持可选 priority / assigneeUserId 过滤 */
    @PreAuthorize("@ss.hasPermi('business:task:kanban')")
    @GetMapping("/kanban")
    public AjaxResult kanban(@RequestParam("projectId") Long projectId,
                             @RequestParam(value = "sprintId", required = false) Long sprintId,
                             @RequestParam(value = "priority", required = false) String priority,
                             @RequestParam(value = "assigneeUserId", required = false) Long assigneeUserId)
    {
        Map<String, Object> data = taskService.kanban(projectId, sprintId, priority, assigneeUserId);
        return success(data);
    }
}
