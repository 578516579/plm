package cn.com.bosssfot.dv.plm.apidesign.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.apidesign.domain.ApiDesign;

public interface IApiDesignService {
    List<ApiDesign> selectApiDesignList(ApiDesign apidesign);
    ApiDesign selectApiDesignById(Long apidesignId);
    int insertApiDesign(ApiDesign apidesign);
    int updateApiDesign(ApiDesign apidesign);
    int deleteApiDesignByIds(Long[] apidesignIds);

    /** AI 生成 OpenAPI YAML + Mock 响应 (PRD §F3.3 - detail-design-flow);本期 mock */
    ApiDesign aiGenerate(Long apidesignId);
}
