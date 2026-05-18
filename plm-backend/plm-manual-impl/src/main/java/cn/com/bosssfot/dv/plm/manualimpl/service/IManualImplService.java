package cn.com.bosssfot.dv.plm.manualimpl.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.manualimpl.domain.ManualImpl;

public interface IManualImplService {
    List<ManualImpl> selectManualImplList(ManualImpl manualImpl);
    ManualImpl selectManualImplById(Long manualImplId);
    int insertManualImpl(ManualImpl manualImpl);
    int updateManualImpl(ManualImpl manualImpl);
    int deleteManualImplByIds(Long[] manualImplIds);

    /** AI 生成实施手册内容 (PRD §F5.2);本期 mock */
    ManualImpl aiGenerate(Long manualImplId);
}
