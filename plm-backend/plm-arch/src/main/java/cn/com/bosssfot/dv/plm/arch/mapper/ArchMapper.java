package cn.com.bosssfot.dv.plm.arch.mapper;

import cn.com.bosssfot.dv.plm.arch.domain.Arch;
import java.util.List;

public interface ArchMapper {

    List<Arch> selectArchList(Arch arch);

    Arch selectArchById(Long archId);

    int insertArch(Arch arch);

    int updateArch(Arch arch);

    int deleteArchByIds(Long[] archIds);

    Integer selectMaxSeqOfYear(String prefix);
}
