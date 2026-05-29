package cn.com.bosssfot.dv.plm.release.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import cn.com.bosssfot.dv.plm.release.domain.Release;

public interface ReleaseMapper {
    List<Release> selectReleaseList(Release release);
    Release selectReleaseById(Long releaseId);
    int insertRelease(Release release);
    int updateRelease(Release release);
    int deleteReleaseByIds(Long[] releaseIds);

    /** ADR: 查"以 prefix 开头的 release_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);

    /**
     * Proposal 0028 P0-3B: DORA 前置时间聚合 — SUM/COUNT(released_at - create_time)
     * WHERE status IN ('02','03') AND released_at IN window AND projectId match。
     * 返回 Map: {sumMs: Long(可能 null), cnt: Long}。
     */
    Map<String, Object> sumLeadTimeMsInPeriod(@Param("projectId") Long projectId,
                                              @Param("periodStart") Date periodStart,
                                              @Param("periodEnd") Date periodEnd);
}
