package cn.com.bosssfot.dv.plm.sprint.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import cn.com.bosssfot.dv.plm.sprint.domain.Sprint;

/**
 * 迭代 Mapper 接口
 */
public interface SprintMapper
{
    /** 查询迭代列表 */
    public List<Sprint> selectSprintList(Sprint sprint);

    /** 根据 ID 查询迭代 */
    public Sprint selectSprintById(Long sprintId);

    /** 新增迭代 */
    public int insertSprint(Sprint sprint);

    /** 修改迭代 */
    public int updateSprint(Sprint sprint);

    /** 批量逻辑删除（del_flag='2'） */
    public int deleteSprintByIds(Long[] sprintIds);

    /** ADR-0004：查"以 prefix 开头的 sprint_no 中"最大的 4 位流水号 */
    public Integer selectMaxSeqOfYear(String prefix);

    /**
     * 业务硬规则 703：统计项目下进行中（status='01'）的迭代数量。
     * @param projectId       项目 ID
     * @param excludeSprintId 排除自身（update 场景），新建时传 -1
     */
    public int countActiveByProject(@Param("projectId") Long projectId,
                                    @Param("excludeSprintId") Long excludeSprintId);

    /** 查项目下当前活跃（01 进行中）迭代，无则返 null */
    public Sprint selectCurrentByProject(Long projectId);
}
