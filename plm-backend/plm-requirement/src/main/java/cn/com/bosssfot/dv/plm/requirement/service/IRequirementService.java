package cn.com.bosssfot.dv.plm.requirement.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;

/**
 * 需求 Service 接口
 */
public interface IRequirementService
{
    /** 查询需求列表 */
    public List<Requirement> selectRequirementList(Requirement requirement);

    /** 根据 ID 查询需求 */
    public Requirement selectRequirementById(Long requirementId);

    /** 新增需求 */
    public int insertRequirement(Requirement requirement);

    /** 修改需求 */
    public int updateRequirement(Requirement requirement);

    /** 批量删除需求 */
    public int deleteRequirementByIds(Long[] requirementIds);

    /** 统计项目下未删除的需求数（Project 删除时反向检查） */
    public int countByProjectId(Long projectId);

    /** AI 优先级初评（PRD §F2.1）：根据需求内容生成 high/medium/low 并落库；本期 mock */
    public Requirement aiEvaluate(Long requirementId);
}
