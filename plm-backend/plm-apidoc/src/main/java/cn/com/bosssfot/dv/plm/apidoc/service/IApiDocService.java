package cn.com.bosssfot.dv.plm.apidoc.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.apidoc.domain.ApiDoc;

public interface IApiDocService {
    List<ApiDoc> selectApiDocList(ApiDoc apidoc);
    ApiDoc selectApiDocById(Long apidocId);
    int insertApiDoc(ApiDoc apidoc);
    int updateApiDoc(ApiDoc apidoc);
    int deleteApiDocByIds(Long[] apidocIds);
}
