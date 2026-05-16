package cn.com.bosssfot.dv.plm.dbdesign.mapper;

import cn.com.bosssfot.dv.plm.dbdesign.domain.Dbdesign;
import java.util.List;

public interface DbdesignMapper {

    List<Dbdesign> selectDbdesignList(Dbdesign dbdesign);

    Dbdesign selectDbdesignById(Long dbdesignId);

    int insertDbdesign(Dbdesign dbdesign);

    int updateDbdesign(Dbdesign dbdesign);

    int deleteDbdesignByIds(Long[] dbdesignIds);

    Integer selectMaxSeqOfYear(String prefix);
}
