package cn.com.bosssfot.dv.plm.ued.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.ued.domain.Ued;

public interface IUedService {
    List<Ued> selectUedList(Ued ued);
    Ued selectUedById(Long uedId);
    int insertUed(Ued ued);
    int updateUed(Ued ued);
    int deleteUedByIds(Long[] uedIds);

    /** AI 设计规范检查 (PRD §F2.3 - ued-review-flow Dify);本期占位:写库 + 返回规范报告 */
    Ued aiReview(Long uedId);
}
