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
}
