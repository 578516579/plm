package cn.com.bosssfot.dv.plm.release.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.release.domain.Release;

public interface IReleaseService {
    List<Release> selectReleaseList(Release release);
    Release selectReleaseById(Long releaseId);
    int insertRelease(Release release);
    int updateRelease(Release release);
    int deleteReleaseByIds(Long[] releaseIds);

    /** P0-1b: AI 发布评审 — 综合 release 元信息 + DORA 指标,产出评分(0-100) + Markdown 评审说明。 */
    Release aiReview(Long releaseId);
}
