package cn.com.bosssfot.dv.plm.prd.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.prd.domain.Prd;

public interface PrdMapper {
    List<Prd> selectPrdList(Prd prd);
    Prd selectPrdById(Long prdId);
    int insertPrd(Prd prd);
    int updatePrd(Prd prd);
    int deletePrdByIds(Long[] prdIds);

    /** ADR: 查"以 prefix 开头的 prd_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
