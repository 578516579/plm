package cn.com.bosssfot.dv.plm.manualops.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.manualops.domain.ManualOps;

public interface ManualOpsMapper {
    List<ManualOps> selectManualOpsList(ManualOps manualOps);
    ManualOps selectManualOpsById(Long manualOpsId);
    int insertManualOps(ManualOps manualOps);
    int updateManualOps(ManualOps manualOps);
    int deleteManualOpsByIds(Long[] manualOpsIds);

    /** ADR: 查"以 prefix 开头的 manual_ops_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
