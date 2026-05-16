package cn.com.bosssfot.dv.plm.arch.service;

import cn.com.bosssfot.dv.plm.arch.domain.Arch;
import java.util.List;

public interface IArchService {

    List<Arch> selectArchList(Arch arch);

    Arch selectArchById(Long archId);

    int insertArch(Arch arch);

    int updateArch(Arch arch);

    int deleteArchByIds(Long[] archIds);

    Arch aiRecommend(Long archId);
}
