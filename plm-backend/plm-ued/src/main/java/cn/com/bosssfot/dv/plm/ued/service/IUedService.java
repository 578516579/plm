package cn.com.bosssfot.dv.plm.ued.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.ued.domain.Ued;

/**
 * UED 设计协同 Service 接口 — PRD §F2.3
 */
public interface IUedService
{
    List<Ued> selectUedList(Ued ued);
    Ued selectUedById(Long uedId);
    int insertUed(Ued ued);
    int updateUed(Ued ued);
    int deleteUedByIds(Long[] uedIds);

    /** PRD §F2.3 AI 设计规范检查 — ued-review-flow (本期 mock) */
    Ued aiReview(Long uedId);
}
