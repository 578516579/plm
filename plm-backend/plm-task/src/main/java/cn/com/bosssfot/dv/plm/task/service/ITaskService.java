package cn.com.bosssfot.dv.plm.task.service;

import java.util.List;
import java.util.Map;
import cn.com.bosssfot.dv.plm.task.domain.Task;

/**
 * 任务 Service 接口
 */
public interface ITaskService
{
    /** 查询任务列表 */
    public List<Task> selectTaskList(Task task);

    /** 根据 ID 查询任务 */
    public Task selectTaskById(Long taskId);

    /** 新增任务（含 FK 校验 + 新建状态约束） */
    public int insertTask(Task task);

    /** 修改任务（含 6×6 状态机 + 已完成必填 actualHours） */
    public int updateTask(Task task);

    /** 批量删除任务 */
    public int deleteTaskByIds(Long[] taskIds);

    /** "我的任务"：按当前 userId 筛选 */
    public List<Task> selectMyTasks(Task taskFilter);

    /** 看板视图：项目 + 可选迭代下分组按状态的任务 */
    public Map<String, Object> kanban(Long projectId, Long sprintId);
}
