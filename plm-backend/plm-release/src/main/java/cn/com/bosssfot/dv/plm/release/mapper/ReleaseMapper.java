package cn.com.bosssfot.dv.plm.release.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.release.domain.Release;

public interface ReleaseMapper {
    List<Release> selectReleaseList(Release release);
    Release selectReleaseById(Long releaseId);
    int insertRelease(Release release);
    int updateRelease(Release release);
    int deleteReleaseByIds(Long[] releaseIds);

    /** ADR: 查"以 prefix 开头的 release_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
