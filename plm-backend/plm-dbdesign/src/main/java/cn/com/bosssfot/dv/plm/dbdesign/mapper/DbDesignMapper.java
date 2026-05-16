package cn.com.bosssfot.dv.plm.dbdesign.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.dbdesign.domain.DbDesign;

public interface DbDesignMapper {
    List<DbDesign> selectDbDesignList(DbDesign dbdesign);
    DbDesign selectDbDesignById(Long dbdesignId);
    int insertDbDesign(DbDesign dbdesign);
    int updateDbDesign(DbDesign dbdesign);
    int deleteDbDesignByIds(Long[] dbdesignIds);

    /** ADR: 查"以 prefix 开头的 dbdesign_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
