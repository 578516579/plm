package cn.com.bosssfot.dv.plm.task.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.com.bosssfot.dv.plm.common.api.ITaskQueryService;
import cn.com.bosssfot.dv.plm.task.mapper.TaskMapper;

/**
 * 跨模块 Task 计数查询服务的实现。
 *
 * <p>本类是 plm-task 对外暴露的"轻接口"实现，专供 plm-sprint / plm-requirement
 * 在删除前置检查 / 健康度统计 场景调用，避免它们直接依赖 plm-task 的 Mapper / Domain。
 *
 * <p>架构意义：打破 plm-sprint ↔ plm-task 编译期循环。
 * 见 03-开发/模块拆分架构.md §2.2 跨模块依赖。
 */
@Service
public class TaskQueryServiceImpl implements ITaskQueryService
{
    @Autowired
    private TaskMapper taskMapper;

    @Override
    public int countBySprintId(Long sprintId)
    {
        return taskMapper.countBySprintId(sprintId);
    }

    @Override
    public int countByStatusAndSprint(Long sprintId, String status)
    {
        return taskMapper.countByStatusAndSprint(sprintId, status);
    }

    @Override
    public int countByRequirementId(Long requirementId)
    {
        return taskMapper.countByRequirementId(requirementId);
    }
}
