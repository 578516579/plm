package cn.com.bosssfot.dv.plm.manualimpl.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.manualimpl.domain.ManualImpl;

public interface ManualImplMapper {
    List<ManualImpl> selectManualImplList(ManualImpl manualImpl);
    ManualImpl selectManualImplById(Long manualImplId);
    int insertManualImpl(ManualImpl manualImpl);
    int updateManualImpl(ManualImpl manualImpl);
    int deleteManualImplByIds(Long[] manualImplIds);

    /** ADR: 查"以 prefix 开头的 manual_impl_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
