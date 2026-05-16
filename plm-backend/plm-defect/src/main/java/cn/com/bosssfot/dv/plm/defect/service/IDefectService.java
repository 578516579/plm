package cn.com.bosssfot.dv.plm.defect.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.defect.domain.Defect;

/**
 * 缺陷 Service 接口
 */
public interface IDefectService
{
    public List<Defect> selectDefectList(Defect defect);
    public Defect selectDefectById(Long defectId);
    public int insertDefect(Defect defect);
    public int updateDefect(Defect defect);
    public int deleteDefectByIds(Long[] defectIds);
}
