package cn.com.bosssfot.dv.plm.release.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.release.domain.Release;
import cn.com.bosssfot.dv.plm.release.mapper.ReleaseMapper;
import cn.com.bosssfot.dv.plm.release.service.IReleaseService;

/** 发布管理 Service - 脚手架,需补业务规则 (状态机 / FK 校验 / 编号生成) */
@Service
public class ReleaseServiceImpl implements IReleaseService {

    @Autowired private ReleaseMapper releaseMapper;

    @Override public List<Release> selectReleaseList(Release t) { return releaseMapper.selectReleaseList(t); }
    @Override public Release selectReleaseById(Long id) { return releaseMapper.selectReleaseById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRelease(Release t) {
        // TODO: 加字段校验 + FK 校验 + 编号生成 + 状态约束
        t.setCreateBy(SecurityUtils.getUsername());
        return releaseMapper.insertRelease(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRelease(Release t) {
        // TODO: 加状态机 + FK re-check
        t.setUpdateBy(SecurityUtils.getUsername());
        return releaseMapper.updateRelease(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteReleaseByIds(Long[] ids) {
        return releaseMapper.deleteReleaseByIds(ids);
    }
}
