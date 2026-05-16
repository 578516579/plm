package cn.com.bosssfot.dv.plm.manualops.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.manualops.domain.ManualOps;

public interface IManualOpsService {
    List<ManualOps> selectManualOpsList(ManualOps manualops);
    ManualOps selectManualOpsById(Long manualopsId);
    int insertManualOps(ManualOps manualops);
    int updateManualOps(ManualOps manualops);
    int deleteManualOpsByIds(Long[] manualopsIds);
    ManualOps aiGenerate(Long manualopsId);
}
