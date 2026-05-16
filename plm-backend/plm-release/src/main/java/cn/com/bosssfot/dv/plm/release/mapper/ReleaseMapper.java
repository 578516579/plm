package cn.com.bosssfot.dv.plm.release.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.release.domain.Release;

public interface ReleaseMapper {
    List<Release> selectReleaseList(Release release);
    Release selectReleaseById(Long releaseId);
    int insertRelease(Release release);
    int updateRelease(Release release);
    int deleteReleaseByIds(Long[] releaseIds);
}
