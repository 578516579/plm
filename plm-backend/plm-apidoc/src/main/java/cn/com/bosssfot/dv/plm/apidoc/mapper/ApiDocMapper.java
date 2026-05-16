package cn.com.bosssfot.dv.plm.apidoc.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.apidoc.domain.ApiDoc;

public interface ApiDocMapper {
    List<ApiDoc> selectApiDocList(ApiDoc apidoc);
    ApiDoc selectApiDocById(Long apidocId);
    int insertApiDoc(ApiDoc apidoc);
    int updateApiDoc(ApiDoc apidoc);
    int deleteApiDocByIds(Long[] apidocIds);

    /** ADR: 查"以 prefix 开头的 apidoc_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
