package cn.com.bosssfot.dv.plm.apidesign.mapper;

import cn.com.bosssfot.dv.plm.apidesign.domain.Apidesign;
import java.util.List;

public interface ApidesignMapper {

    List<Apidesign> selectApidesignList(Apidesign apidesign);

    Apidesign selectApidesignById(Long apidesignId);

    int insertApidesign(Apidesign apidesign);

    int updateApidesign(Apidesign apidesign);

    int deleteApidesignByIds(Long[] apidesignIds);

    Integer selectMaxSeqOfYear(String prefix);
}
