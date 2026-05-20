package cn.com.bosssfot.dv.plm.release.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.project.domain.Project;
import cn.com.bosssfot.dv.plm.project.mapper.ProjectMapper;
import cn.com.bosssfot.dv.plm.release.domain.Release;
import cn.com.bosssfot.dv.plm.release.mapper.ReleaseMapper;
import cn.com.bosssfot.dv.plm.release.service.IReleaseService;

/**
 * 发布管理 Service — 原型 release.html
 *
 * 落地:
 * - ADR: generateReleaseNo() — REL-YYYY-NNNN
 * - 蓝绿 / 金丝雀 / 滚动 三种策略 + DORA 4 指标
 * - 5 状态机: 00 计划中 → 01 发布中 → 02 已发布 → 03 已回滚 / 04 已废弃
 *   - 00→{01,04}
 *   - 01→{02,03}
 *   - 02→{03,04}
 *   - 03→{04}
 *   - 04→{}  终态
 */
@Service
public class ReleaseServiceImpl implements IReleaseService
{
    private static final Logger log = LoggerFactory.getLogger(ReleaseServiceImpl.class);

    private static final Set<String> ALLOWED_STRATEGY = Set.of("blue_green", "canary", "rolling");

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
        "00", Set.of("01", "04"),
        "01", Set.of("02", "03"),
        "02", Set.of("03", "04"),
        "03", Set.of("04"),
        "04", Set.of()
    );

    @Autowired private ReleaseMapper releaseMapper;
    @Autowired private ProjectMapper projectMapper;

    @Override
    public List<Release> selectReleaseList(Release t) {
        return releaseMapper.selectReleaseList(t);
    }

    @Override
    public Release selectReleaseById(Long id) {
        return releaseMapper.selectReleaseById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRelease(Release t) {
        if (StringUtils.isBlank(t.getVersion())) {
            throw new ServiceException("发布版本号不能为空", 602);
        }
        if (t.getProjectId() == null) {
            throw new ServiceException("关联项目不能为空", 602);
        }
        if (t.getReleasedByUserId() == null) {
            throw new ServiceException("发布人不能为空", 602);
        }
        Project project = projectMapper.selectProjectById(t.getProjectId());
        if (project == null) {
            throw new ServiceException("关联项目不存在", 702);
        }

        // 默认 strategy = rolling
        if (StringUtils.isBlank(t.getStrategy())) {
            t.setStrategy("rolling");
        } else if (!ALLOWED_STRATEGY.contains(t.getStrategy())) {
            throw new ServiceException("策略仅支持 blue_green/canary/rolling", 604);
        }
        if (StringUtils.isBlank(t.getEnvironment())) {
            t.setEnvironment("prod");
        }
        if (StringUtils.isBlank(t.getStatus())) {
            t.setStatus("00");
        } else if (!"00".equals(t.getStatus())) {
            throw new ServiceException("新建发布单状态必须为「计划中」", 601);
        }

        if (StringUtils.isBlank(t.getReleaseNo())) {
            t.setReleaseNo(generateReleaseNo());
        }
        t.setCreateBy(SecurityUtils.getUsername());

        try {
            return releaseMapper.insertRelease(t);
        } catch (DuplicateKeyException e) {
            log.warn("release_no 重号,重试一次: {}", t.getReleaseNo());
            t.setReleaseNo(generateReleaseNo());
            return releaseMapper.insertRelease(t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRelease(Release t) {
        Release old = releaseMapper.selectReleaseById(t.getReleaseId());
        if (old == null) {
            throw new ServiceException("发布单不存在", 404);
        }

        if (StringUtils.isNotBlank(t.getStatus()) && !t.getStatus().equals(old.getStatus())) {
            Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(old.getStatus(), Set.of());
            if (!allowed.contains(t.getStatus())) {
                throw new ServiceException(
                    "发布单状态 " + statusLabel(old.getStatus())
                        + " 不能直接转到 " + statusLabel(t.getStatus()),
                    601
                );
            }

            // 回滚必须有 reason
            if ("03".equals(t.getStatus())) {
                String reason = StringUtils.isNotBlank(t.getRollbackReason())
                    ? t.getRollbackReason() : old.getRollbackReason();
                if (StringUtils.isBlank(reason)) {
                    throw new ServiceException("回滚必须填写回滚原因", 602);
                }
            }
        }

        if (StringUtils.isNotBlank(t.getStrategy()) && !ALLOWED_STRATEGY.contains(t.getStrategy())) {
            throw new ServiceException("策略仅支持 blue_green/canary/rolling", 604);
        }
        if (t.getProjectId() != null && !t.getProjectId().equals(old.getProjectId())) {
            Project project = projectMapper.selectProjectById(t.getProjectId());
            if (project == null) {
                throw new ServiceException("关联项目不存在", 702);
            }
        }

        t.setUpdateBy(SecurityUtils.getUsername());
        return releaseMapper.updateRelease(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteReleaseByIds(Long[] ids) {
        return releaseMapper.deleteReleaseByIds(ids);
    }

    private String generateReleaseNo() {
        int year = LocalDate.now().getYear();
        String prefix = "REL-" + year + "-";
        Integer maxSeq = releaseMapper.selectMaxSeqOfYear(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return String.format("%s%04d", prefix, next);
    }

    private static String statusLabel(String status) {
        return switch (status) {
            case "00" -> "计划中";
            case "01" -> "发布中";
            case "02" -> "已发布";
            case "03" -> "已回滚";
            case "04" -> "已废弃";
            default   -> "未知(" + status + ")";
        };
    }
}
