package cn.com.bosssfot.dv.plm.ued.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.ued.domain.Ued;

public interface UedMapper {
    List<Ued> selectUedList(Ued ued);
    Ued selectUedById(Long uedId);
    int insertUed(Ued ued);
    int updateUed(Ued ued);
    int deleteUedByIds(Long[] uedIds);

    /** ADR: 查"以 prefix 开头的 ued_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
