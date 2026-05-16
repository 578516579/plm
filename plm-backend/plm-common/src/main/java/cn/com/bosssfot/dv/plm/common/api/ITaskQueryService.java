package cn.com.bosssfot.dv.plm.common.api;

/**
 * 跨模块任务查询服务（仅暴露聚合 / 计数类只读操作）。
 *
 * <p>设计目的：打破 plm-sprint ↔ plm-task 的编译期循环依赖。
 * plm-task 模块的 ServiceImpl 实现本接口，plm-sprint 通过 @Autowired 注入接口，
 * 不直接依赖 plm-task 的 Mapper 类。
 *
 * <p>仅返回原始类型 / Map，避免把 Task 业务类拉进 plm-common。
 */
public interface ITaskQueryService
{
    /** 统计 sprint 下未删除的任务数（Sprint 删除前置检查 / 健康度统计） */
    int countBySprintId(Long sprintId);

    /** 统计 sprint 下指定状态的任务数（Sprint 健康度统计 S-009） */
    int countByStatusAndSprint(Long sprintId, String status);

    /** 统计 requirement 下未删除的任务数（Requirement 删除前置检查） */
    int countByRequirementId(Long requirementId);
}
