package cn.com.bosssfot.dv.plm.apidoc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.apidoc.domain.ApiDoc;
import cn.com.bosssfot.dv.plm.apidoc.mapper.ApiDocMapper;
import cn.com.bosssfot.dv.plm.apidoc.service.IApiDocService;

/** API 文档 Service - 脚手架,需补业务规则 (状态机 / FK 校验 / 编号生成) */
@Service
public class ApiDocServiceImpl implements IApiDocService {

    @Autowired private ApiDocMapper apidocMapper;

    @Override public List<ApiDoc> selectApiDocList(ApiDoc t) { return apidocMapper.selectApiDocList(t); }
    @Override public ApiDoc selectApiDocById(Long id) { return apidocMapper.selectApiDocById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertApiDoc(ApiDoc t) {
        // TODO: 加字段校验 + FK 校验 + 编号生成 + 状态约束
        t.setCreateBy(SecurityUtils.getUsername());
        return apidocMapper.insertApiDoc(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateApiDoc(ApiDoc t) {
        // TODO: 加状态机 + FK re-check
        t.setUpdateBy(SecurityUtils.getUsername());
        return apidocMapper.updateApiDoc(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteApiDocByIds(Long[] ids) {
        return apidocMapper.deleteApiDocByIds(ids);
    }
}
