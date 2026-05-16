package cn.com.bosssfot.dv.plm.manualproduct.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.manualproduct.domain.ManualProduct;

public interface ManualProductMapper {
    List<ManualProduct> selectManualProductList(ManualProduct manualproduct);
    ManualProduct selectManualProductById(Long manualproductId);
    int insertManualProduct(ManualProduct manualproduct);
    int updateManualProduct(ManualProduct manualproduct);
    int deleteManualProductByIds(Long[] manualproductIds);

    /** ADR: 查"以 prefix 开头的 manualproduct_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
