package cn.com.bosssfot.dv.plm.apidesign.service;

import cn.com.bosssfot.dv.plm.apidesign.domain.Apidesign;
import java.util.List;

public interface IApidesignService {

    List<Apidesign> selectApidesignList(Apidesign apidesign);

    Apidesign selectApidesignById(Long apidesignId);

    int insertApidesign(Apidesign apidesign);

    int updateApidesign(Apidesign apidesign);

    int deleteApidesignByIds(Long[] apidesignIds);

    Apidesign aiOpenapi(Long apidesignId);
}
