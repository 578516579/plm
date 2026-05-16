package cn.com.bosssfot.dv.plm.requirement.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;

/**
 * 需求 Mapper 接口
 */
public interface RequirementMapper
{
    /** 查询需求列表 */
    public List<Requirement> selectRequirementList(Requirement requirement);

    /** 根据 ID 查询需求 */
    public Requirement selectRequirementById(Long requirementId);

    /** 新增需求 */
    public int insertRequirement(Requirement requirement);

    /** 修改需求 */
    public int updateRequirement(Requirement requirement);

    /** 批量逻辑删除（del_flag='2'） */
    public int deleteRequirementByIds(Long[] requirementIds);

    /** ADR-0002：查"以 prefix 开头的 requirement_no 中"最大的 4 位流水号；无则返 null */
    public Integer selectMaxSeqOfYear(String prefix);

    /** 统计项目下未删除的需求数（Project 删除前置检查可复用） */
    public int countByProjectId(Long projectId);
}
