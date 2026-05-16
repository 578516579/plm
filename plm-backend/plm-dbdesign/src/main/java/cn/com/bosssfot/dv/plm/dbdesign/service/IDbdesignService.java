package cn.com.bosssfot.dv.plm.dbdesign.service;

import cn.com.bosssfot.dv.plm.dbdesign.domain.Dbdesign;
import java.util.List;

public interface IDbdesignService {

    List<Dbdesign> selectDbdesignList(Dbdesign dbdesign);

    Dbdesign selectDbdesignById(Long dbdesignId);

    int insertDbdesign(Dbdesign dbdesign);

    int updateDbdesign(Dbdesign dbdesign);

    int deleteDbdesignByIds(Long[] dbdesignIds);

    Dbdesign aiEr(Long dbdesignId);
}
