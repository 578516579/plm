package cn.com.bosssfot.dv.plm.manualproduct.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.manualproduct.domain.ManualProduct;
import cn.com.bosssfot.dv.plm.manualproduct.mapper.ManualProductMapper;
import cn.com.bosssfot.dv.plm.manualproduct.service.IManualProductService;

/** 产品手册 Service - 脚手架,需补业务规则 (状态机 / FK 校验 / 编号生成) */
@Service
public class ManualProductServiceImpl implements IManualProductService {

    @Autowired private ManualProductMapper manualproductMapper;

    @Override public List<ManualProduct> selectManualProductList(ManualProduct t) { return manualproductMapper.selectManualProductList(t); }
    @Override public ManualProduct selectManualProductById(Long id) { return manualproductMapper.selectManualProductById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertManualProduct(ManualProduct t) {
        // TODO: 加字段校验 + FK 校验 + 编号生成 + 状态约束
        t.setCreateBy(SecurityUtils.getUsername());
        return manualproductMapper.insertManualProduct(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateManualProduct(ManualProduct t) {
        // TODO: 加状态机 + FK re-check
        t.setUpdateBy(SecurityUtils.getUsername());
        return manualproductMapper.updateManualProduct(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteManualProductByIds(Long[] ids) {
        return manualproductMapper.deleteManualProductByIds(ids);
    }
}
