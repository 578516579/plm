package cn.com.bosssfot.dv.plm.defect.mapper;

import java.util.List;
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
}
