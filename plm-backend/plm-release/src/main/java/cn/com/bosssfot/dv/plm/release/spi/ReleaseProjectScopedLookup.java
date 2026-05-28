package cn.com.bosssfot.dv.plm.release.spi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.bosssfot.dv.plm.common.spi.ProjectScopedLookup;
import cn.com.bosssfot.dv.plm.release.domain.Release;
import cn.com.bosssfot.dv.plm.release.mapper.ReleaseMapper;

/**
 * plm-release 对外暴露的 ProjectScopedLookup 实现,bean 名 = "release"。
 * 用于跨模块(如 plm-pipeline)在 Maven Reactor 循环依赖约束下查询
 * 发布单所属 projectId,做 same-project FK 校验。
 *
 * 设计出处:Proposal 0028 P0-2A,解 P0-1 known limitation。
 */
@Component("release")
public class ReleaseProjectScopedLookup implements ProjectScopedLookup {

    @Autowired
    private ReleaseMapper releaseMapper;

    @Override
    public String entityType() {
        return "release";
    }

    @Override
    public Long resolveProjectId(Long entityId) {
        if (entityId == null) {
            return null;
        }
        Release r = releaseMapper.selectReleaseById(entityId);
        return r == null ? null : r.getProjectId();
    }
}
