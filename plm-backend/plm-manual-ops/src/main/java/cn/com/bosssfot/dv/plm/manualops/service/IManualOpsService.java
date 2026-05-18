package cn.com.bosssfot.dv.plm.manualops.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.manualops.domain.ManualOps;

public interface IManualOpsService {
    List<ManualOps> selectManualOpsList(ManualOps manualOps);
    ManualOps selectManualOpsById(Long manualOpsId);
    int insertManualOps(ManualOps manualOps);
    int updateManualOps(ManualOps manualOps);
    int deleteManualOpsByIds(Long[] manualOpsIds);

    /** AI 生成运维手册内容 (PRD §F5.3);本期 mock */
    ManualOps aiGenerate(Long manualOpsId);
}
