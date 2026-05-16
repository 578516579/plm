package cn.com.bosssfot.dv.plm.openspec.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.openspec.domain.Openspec;

public interface OpenspecMapper {
    List<Openspec> selectOpenspecList(Openspec openspec);
    Openspec selectOpenspecById(Long openspecId);
    int insertOpenspec(Openspec openspec);
    int updateOpenspec(Openspec openspec);
    int deleteOpenspecByIds(Long[] openspecIds);
    Integer selectMaxSeqOfYear(String prefix);
}
