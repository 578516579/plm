package cn.com.bosssfot.dv.plm.apidesign.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.apidesign.domain.ApiDesign;

public interface ApiDesignMapper {
    List<ApiDesign> selectApiDesignList(ApiDesign apidesign);
    ApiDesign selectApiDesignById(Long apidesignId);
    int insertApiDesign(ApiDesign apidesign);
    int updateApiDesign(ApiDesign apidesign);
    int deleteApiDesignByIds(Long[] apidesignIds);

    /** ADR: 查"以 prefix 开头的 apidesign_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
