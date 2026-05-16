package cn.com.bosssfot.dv.plm.arch.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.arch.domain.Arch;

public interface IArchService {
    List<Arch> selectArchList(Arch arch);
    Arch selectArchById(Long archId);
    int insertArch(Arch arch);
    int updateArch(Arch arch);
    int deleteArchByIds(Long[] archIds);

    /** AI 生成架构方案 + C4 容器图 (PRD §F3.1 - arch-design-flow);本期 mock 实现 */
    Arch aiGenerate(Long archId);
}
