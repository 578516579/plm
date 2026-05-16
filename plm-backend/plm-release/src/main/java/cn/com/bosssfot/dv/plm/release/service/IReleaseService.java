package cn.com.bosssfot.dv.plm.release.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.release.domain.Release;

public interface IReleaseService {
    List<Release> selectReleaseList(Release release);
    Release selectReleaseById(Long releaseId);
    int insertRelease(Release release);
    int updateRelease(Release release);
    int deleteReleaseByIds(Long[] releaseIds);
}
