package cn.com.bosssfot.dv.plm.competitive.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.competitive.domain.Competitive;

public interface CompetitiveMapper {
    List<Competitive> selectCompetitiveList(Competitive competitive);
    Competitive selectCompetitiveById(Long competitiveId);
    int insertCompetitive(Competitive competitive);
    int updateCompetitive(Competitive competitive);
    int deleteCompetitiveByIds(Long[] competitiveIds);

    /** ADR: 查"以 prefix 开头的 competitive_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
