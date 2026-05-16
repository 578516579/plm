package cn.com.bosssfot.dv.plm.manualproduct.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.manualproduct.domain.ManualProduct;

public interface IManualProductService {
    List<ManualProduct> selectManualProductList(ManualProduct manualproduct);
    ManualProduct selectManualProductById(Long manualproductId);
    int insertManualProduct(ManualProduct manualproduct);
    int updateManualProduct(ManualProduct manualproduct);
    int deleteManualProductByIds(Long[] manualproductIds);
}
