package cn.com.bosssfot.dv.plm.sprint.service;

import java.util.List;
import java.util.Map;
import cn.com.bosssfot.dv.plm.sprint.domain.Sprint;

/**
 * 迭代 Service 接口
 */
public interface ISprintService
{
    /** 查询迭代列表 */
    public List<Sprint> selectSprintList(Sprint sprint);

    /** 根据 ID 查询迭代 */
    public Sprint selectSprintById(Long sprintId);

    /** 新增迭代 */
    public int insertSprint(Sprint sprint);

    /** 修改迭代（含 4×4 状态机 + actual 日期自动填充 + 703 约束） */
    public int updateSprint(Sprint sprint);

    /** 批量删除迭代（前置检查：是否有关联任务） */
    public int deleteSprintByIds(Long[] sprintIds);

    /** 查项目当前活跃迭代（status='01'），无则返 null */
    public Sprint selectCurrentByProject(Long projectId);

    /** 健康度统计（S-009）— 返回 sprintId + 计划/已完成/进行中/剩余 + 完成率 + onTime + daysOverPlan */
    public Map<String, Object> selectSprintStats(Long sprintId);

    /** 校验迭代是否存在（Task FK 校验调用） */
    public boolean checkExists(Long sprintId);
}
