package cn.com.bosssfot.dv.plm.inception.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.inception.domain.Inception;

public interface IInceptionService {
    List<Inception> selectInceptionList(Inception inception);
    Inception selectInceptionById(Long inceptionId);
    int insertInception(Inception inception);
    int updateInception(Inception inception);
    int deleteInceptionByIds(Long[] inceptionIds);

    /** AI 立项 (PRD §F1.1 - project-inception-flow Dify 工作流);本期占位:写库 + 返回 mock proposal */
    Inception aiGenerate(Long inceptionId);

    /**
     * Proposal 0028 P0-2 — 立项晋升项目
     * 校验 inception.status == '03'(已批准)→ 否则 601;
     * 幂等:若 inception.projectId 已存在且对应 project 仍在 → 直接返回旧 projectId;
     * 业务动作:从 inception 拷贝核心字段建 Project,回填 inception.projectId,返回新 projectId。
     *
     * @param inceptionId 立项 ID
     * @return 新建(或已存在)的项目 ID
     */
    Long promoteToProject(Long inceptionId);
}
