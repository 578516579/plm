package cn.com.bosssfot.dv.plm.ued.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.ued.domain.Ued;

public interface IUedService {
    List<Ued> selectUedList(Ued ued);
    Ued selectUedById(Long uedId);
    int insertUed(Ued ued);
    int updateUed(Ued ued);
    int deleteUedByIds(Long[] uedIds);

    /** AI 设计评审 (PRD §F2.3 - ued-review-flow);本期 mock */
    Ued aiReview(Long uedId);
}
