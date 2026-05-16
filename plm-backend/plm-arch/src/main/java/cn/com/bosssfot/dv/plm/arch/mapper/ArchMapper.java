package cn.com.bosssfot.dv.plm.arch.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.arch.domain.Arch;

public interface ArchMapper {
    List<Arch> selectArchList(Arch arch);
    Arch selectArchById(Long archId);
    int insertArch(Arch arch);
    int updateArch(Arch arch);
    int deleteArchByIds(Long[] archIds);

    /** ADR: 查"以 prefix 开头的 arch_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
