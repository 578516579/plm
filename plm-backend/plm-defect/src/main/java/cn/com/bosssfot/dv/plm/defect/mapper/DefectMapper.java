package cn.com.bosssfot.dv.plm.defect.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import cn.com.bosssfot.dv.plm.defect.domain.Defect;

/**
 * 缺陷 Mapper 接口
 */
public interface DefectMapper
{
    public List<Defect> selectDefectList(Defect defect);

    public Defect selectDefectById(Long defectId);

    public int insertDefect(Defect defect);

    public int updateDefect(Defect defect);

    public int deleteDefectByIds(Long[] defectIds);

    /** ADR-0005: 查最大流水号 */
    public Integer selectMaxSeqOfYear(String prefix);

    /**
     * Proposal 0028 P0-3B: DORA MTTR 聚合 — SUM/COUNT(update_time - create_time)
     * WHERE severity IN ('00','01')  -- P0 阻塞 / P1 严重
     *   AND status = '03'            -- 已解决
     *   AND update_time IN window
     *   AND projectId match
     * 返回 Map: {sumMs: Long(可能 null), cnt: Long}。
     */
    Map<String, Object> sumRecoverMsInPeriod(@Param("projectId") Long projectId,
                                             @Param("periodStart") Date periodStart,
                                             @Param("periodEnd") Date periodEnd);
}
