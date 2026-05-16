package cn.com.bosssfot.dv.plm.manualimpl.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.manualimpl.domain.ManualImpl;

public interface ManualImplMapper {
    List<ManualImpl> selectManualImplList(ManualImpl manualimpl);
    ManualImpl selectManualImplById(Long manualimplId);
    int insertManualImpl(ManualImpl manualimpl);
    int updateManualImpl(ManualImpl manualimpl);
    int deleteManualImplByIds(Long[] manualimplIds);
    Integer selectMaxSeqOfYear(String prefix);
}
