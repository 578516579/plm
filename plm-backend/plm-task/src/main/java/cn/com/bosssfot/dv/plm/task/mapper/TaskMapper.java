package cn.com.bosssfot.dv.plm.task.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import cn.com.bosssfot.dv.plm.task.domain.Task;

/**
 * 任务 Mapper 接口
 */
public interface TaskMapper
{
    /** 查询任务列表 */
    public List<Task> selectTaskList(Task task);

    /** 根据 ID 查询任务 */
    public Task selectTaskById(Long taskId);

    /** 新增任务 */
    public int insertTask(Task task);

    /** 修改任务 */
    public int updateTask(Task task);

    /** 批量逻辑删除（del_flag='2'） */
    public int deleteTaskByIds(Long[] taskIds);

    /** ADR-0003：查"以 prefix 开头的 task_no 中"最大的 4 位流水号 */
    public Integer selectMaxSeqOfYear(String prefix);

    /** 统计迭代下未删除的任务数（Sprint 删除前置检查 / 健康度统计） */
    public int countBySprintId(Long sprintId);

    /** 统计迭代下指定状态的任务数（Sprint 健康度统计 S-009） */
    public int countByStatusAndSprint(@Param("sprintId") Long sprintId,
                                      @Param("status") String status);

    /** 统计需求下未删除的任务数（需求删除前可复用） */
    public int countByRequirementId(Long requirementId);

    /** 查迭代下所有未取消任务（看板视图），支持优先级/负责人过滤 */
    public List<Task> selectKanbanTasks(@Param("projectId") Long projectId,
                                        @Param("sprintId") Long sprintId,
                                        @Param("priority") String priority,
                                        @Param("assigneeUserId") Long assigneeUserId);

    /** 查迭代下所有任务（Sprint 聚合详情用） */
    public List<Task> selectBySprintId(Long sprintId);
}
