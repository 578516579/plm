package cn.com.bosssfot.dv.plm.manualproduct.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.manualproduct.domain.ManualProduct;

public interface IManualProductService {
    List<ManualProduct> selectManualProductList(ManualProduct manualproduct);
    ManualProduct selectManualProductById(Long manualproductId);
    int insertManualProduct(ManualProduct manualproduct);
    int updateManualProduct(ManualProduct manualproduct);
    int deleteManualProductByIds(Long[] manualproductIds);

    /** P0-1b: AI 生成产品手册 Markdown 正文(真 provider 落 LLM 文本,否则回退模板)。 */
    ManualProduct aiGenerate(Long manualproductId);
}
